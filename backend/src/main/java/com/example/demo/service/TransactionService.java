package com.example.demo.service;

import com.example.demo.entity.Transaction;
import com.example.demo.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${astra.db.rest.endpoint}")
    private String astraDbEndpoint;

    @Value("${astra.db.rest.keyspace}")
    private String keyspace;

    @Value("${astra.db.rest.token}")
    private String authToken;

    /**
     * Get all transactions with pagination and sorting
     */
    public List<Transaction> getAllTransactions(int page, int size, String sortBy, String sortDir) {
        try {
            logger.debug("Fetching transactions with page={}, size={}, sortBy={}, sortDir={}", 
                page, size, sortBy, sortDir);

            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // For now, return all transactions (implement pagination when repository is ready)
            List<Transaction> transactions = transactionRepository.findAll();
            
            // Apply sorting manually
            if ("amount".equals(sortBy)) {
                transactions.sort((t1, t2) -> {
                    BigDecimal amount1 = t1.getAmount() != null ? t1.getAmount() : BigDecimal.ZERO;
                    BigDecimal amount2 = t2.getAmount() != null ? t2.getAmount() : BigDecimal.ZERO;
                    return sortDir.equalsIgnoreCase("desc") ? 
                        amount2.compareTo(amount1) : amount1.compareTo(amount2);
                });
            } else if ("createdAt".equals(sortBy)) {
                transactions.sort((t1, t2) -> {
                    LocalDateTime date1 = t1.getCreatedAt() != null ? t1.getCreatedAt() : LocalDateTime.MIN;
                    LocalDateTime date2 = t2.getCreatedAt() != null ? t2.getCreatedAt() : LocalDateTime.MIN;
                    return sortDir.equalsIgnoreCase("desc") ? 
                        date2.compareTo(date1) : date1.compareTo(date2);
                });
            }

            // Apply pagination manually
            int start = page * size;
            int end = Math.min(start + size, transactions.size());
            if (start < transactions.size()) {
                transactions = transactions.subList(start, end);
            } else {
                transactions = new ArrayList<>();
            }

            logger.info("Successfully retrieved {} transactions", transactions.size());
            return transactions;

        } catch (Exception e) {
            logger.error("Error fetching transactions: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transactions", e);
        }
    }

    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(String id) {
        try {
            logger.debug("Fetching transaction with ID: {}", id);
            
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Transaction ID cannot be null or empty");
            }

            Optional<Transaction> transaction = transactionRepository.findById(id);
            
            if (transaction.isPresent()) {
                logger.info("Successfully retrieved transaction with ID: {}", id);
                return transaction.get();
            } else {
                logger.warn("Transaction not found with ID: {}", id);
                return null;
            }

        } catch (Exception e) {
            logger.error("Error fetching transaction with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transaction", e);
        }
    }

    /**
     * Create a new transaction
     */
    public Transaction createTransaction(Transaction transaction) {
        try {
            logger.debug("Creating new transaction: {}", transaction);

            // Validate transaction data
            validateTransaction(transaction);

            // Set default values
            if (transaction.getId() == null) {
                transaction.setId(generateTransactionId());
            }
            if (transaction.getCreatedAt() == null) {
                transaction.setCreatedAt(LocalDateTime.now());
            }
            if (transaction.getStatus() == null) {
                transaction.setStatus("PENDING");
            }

            // Save to database
            Transaction savedTransaction = transactionRepository.save(transaction);

            // Process blockchain transaction asynchronously if it's a crypto transaction
            if ("CRYPTO".equalsIgnoreCase(transaction.getPaymentMethod())) {
                CompletableFuture.runAsync(() -> processBlockchainTransaction(savedTransaction));
            }

            logger.info("Successfully created transaction with ID: {}", savedTransaction.getId());
            return savedTransaction;

        } catch (Exception e) {
            logger.error("Error creating transaction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create transaction", e);
        }
    }

    /**
     * Update an existing transaction
     */
    public Transaction updateTransaction(String id, Transaction transaction) {
        try {
            logger.debug("Updating transaction with ID: {}", id);

            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Transaction ID cannot be null or empty");
            }

            Optional<Transaction> existingTransaction = transactionRepository.findById(id);
            
            if (existingTransaction.isEmpty()) {
                logger.warn("Transaction not found for update with ID: {}", id);
                return null;
            }

            Transaction existing = existingTransaction.get();
            
            // Update fields
            if (transaction.getAmount() != null) {
                existing.setAmount(transaction.getAmount());
            }
            if (transaction.getStatus() != null) {
                existing.setStatus(transaction.getStatus());
            }
            if (transaction.getMessage() != null) {
                existing.setMessage(transaction.getMessage());
            }
            if (transaction.getTransactionHash() != null) {
                existing.setTransactionHash(transaction.getTransactionHash());
            }
            
            existing.setUpdatedAt(LocalDateTime.now());

            Transaction updatedTransaction = transactionRepository.save(existing);
            
            logger.info("Successfully updated transaction with ID: {}", id);
            return updatedTransaction;

        } catch (Exception e) {
            logger.error("Error updating transaction with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to update transaction", e);
        }
    }

    /**
     * Delete a transaction
     */
    public boolean deleteTransaction(String id) {
        try {
            logger.debug("Deleting transaction with ID: {}", id);

            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("Transaction ID cannot be null or empty");
            }

            Optional<Transaction> transaction = transactionRepository.findById(id);
            
            if (transaction.isEmpty()) {
                logger.warn("Transaction not found for deletion with ID: {}", id);
                return false;
            }

            transactionRepository.deleteById(id);
            
            logger.info("Successfully deleted transaction with ID: {}", id);
            return true;

        } catch (Exception e) {
            logger.error("Error deleting transaction with ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete transaction", e);
        }
    }

    /**
     * Get transactions by charity ID
     */
    public List<Transaction> getTransactionsByCharity(String charityId, int page, int size) {
        try {
            logger.debug("Fetching transactions for charity ID: {}", charityId);

            if (charityId == null || charityId.trim().isEmpty()) {
                throw new IllegalArgumentException("Charity ID cannot be null or empty");
            }

            List<Transaction> allTransactions = transactionRepository.findAll();
            
            // Filter by charity ID
            List<Transaction> charityTransactions = allTransactions.stream()
                .filter(t -> charityId.equals(t.getCharityId()))
                .collect(Collectors.toList());

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, charityTransactions.size());
            if (start < charityTransactions.size()) {
                charityTransactions = charityTransactions.subList(start, end);
            } else {
                charityTransactions = new ArrayList<>();
            }

            logger.info("Successfully retrieved {} transactions for charity ID: {}", 
                charityTransactions.size(), charityId);
            return charityTransactions;

        } catch (Exception e) {
            logger.error("Error fetching transactions for charity ID {}: {}", charityId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transactions for charity", e);
        }
    }

    /**
     * Get transaction statistics
     */
    public Map<String, Object> getTransactionStats() {
        try {
            logger.debug("Calculating transaction statistics");

            List<Transaction> allTransactions = transactionRepository.findAll();
            
            Map<String, Object> stats = new HashMap<>();
            
            // Total transactions
            stats.put("totalTransactions", allTransactions.size());
            
            // Total amount
            BigDecimal totalAmount = allTransactions.stream()
                .map(t -> t.getAmount() != null ? t.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalAmount", totalAmount);
            
            // Average transaction amount
            if (!allTransactions.isEmpty()) {
                BigDecimal avgAmount = totalAmount.divide(BigDecimal.valueOf(allTransactions.size()), 2, BigDecimal.ROUND_HALF_UP);
                stats.put("averageAmount", avgAmount);
            }
            
            // Transactions by status
            Map<String, Long> statusCounts = allTransactions.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getStatus() != null ? t.getStatus() : "UNKNOWN",
                    Collectors.counting()
                ));
            stats.put("transactionsByStatus", statusCounts);
            
            // Transactions by payment method
            Map<String, Long> paymentMethodCounts = allTransactions.stream()
                .collect(Collectors.groupingBy(
                    t -> t.getPaymentMethod() != null ? t.getPaymentMethod() : "UNKNOWN",
                    Collectors.counting()
                ));
            stats.put("transactionsByPaymentMethod", paymentMethodCounts);
            
            // Recent transactions (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long recentTransactions = allTransactions.stream()
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
            stats.put("recentTransactions", recentTransactions);

            logger.info("Successfully calculated transaction statistics");
            return stats;

        } catch (Exception e) {
            logger.error("Error calculating transaction statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to calculate transaction statistics", e);
        }
    }

    /**
     * Validate transaction data
     */
    private void validateTransaction(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null");
        }
        
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be greater than zero");
        }
        
        if (transaction.getCharityId() == null || transaction.getCharityId().trim().isEmpty()) {
            throw new IllegalArgumentException("Charity ID is required");
        }
        
        if (transaction.getDonorName() == null || transaction.getDonorName().trim().isEmpty()) {
            throw new IllegalArgumentException("Donor name is required");
        }
        
        if (transaction.getPaymentMethod() == null || transaction.getPaymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required");
        }
    }

    /**
     * Generate a unique transaction ID
     */
    private String generateTransactionId() {
        return "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Process blockchain transaction asynchronously
     */
    private void processBlockchainTransaction(Transaction transaction) {
        try {
            logger.info("Processing blockchain transaction for transaction ID: {}", transaction.getId());
            
            // Simulate blockchain processing
            Thread.sleep(2000);
            
            // Update transaction status
            transaction.setStatus("CONFIRMED");
            transaction.setTransactionHash("0x" + UUID.randomUUID().toString().replace("-", ""));
            transaction.setUpdatedAt(LocalDateTime.now());
            
            transactionRepository.save(transaction);
            
            logger.info("Successfully processed blockchain transaction for ID: {}", transaction.getId());
            
        } catch (Exception e) {
            logger.error("Error processing blockchain transaction for ID {}: {}", 
                transaction.getId(), e.getMessage(), e);
            
            // Update transaction status to failed
            transaction.setStatus("FAILED");
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
        }
    }

    /**
     * Get transactions by date range
     */
    public List<Transaction> getTransactionsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            logger.debug("Fetching transactions between {} and {}", startDate, endDate);

            List<Transaction> allTransactions = transactionRepository.findAll();
            
            List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> t.getCreatedAt() != null && 
                    !t.getCreatedAt().isBefore(startDate) && 
                    !t.getCreatedAt().isAfter(endDate))
                .collect(Collectors.toList());

            logger.info("Successfully retrieved {} transactions in date range", filteredTransactions.size());
            return filteredTransactions;

        } catch (Exception e) {
            logger.error("Error fetching transactions by date range: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transactions by date range", e);
        }
    }

    /**
     * Get transactions by amount range
     */
    public List<Transaction> getTransactionsByAmountRange(BigDecimal minAmount, BigDecimal maxAmount) {
        try {
            logger.debug("Fetching transactions between {} and {}", minAmount, maxAmount);

            List<Transaction> allTransactions = transactionRepository.findAll();
            
            List<Transaction> filteredTransactions = allTransactions.stream()
                .filter(t -> t.getAmount() != null && 
                    t.getAmount().compareTo(minAmount) >= 0 && 
                    t.getAmount().compareTo(maxAmount) <= 0)
                .collect(Collectors.toList());

            logger.info("Successfully retrieved {} transactions in amount range", filteredTransactions.size());
            return filteredTransactions;

        } catch (Exception e) {
            logger.error("Error fetching transactions by amount range: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch transactions by amount range", e);
        }
    }
} 