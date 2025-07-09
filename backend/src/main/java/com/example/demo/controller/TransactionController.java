package com.example.demo.controller;

import com.example.demo.entity.Transaction;
import com.example.demo.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "APIs for managing donation transactions")
@CrossOrigin(origins = "${cors.allowed-origins}")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions", description = "Retrieve all donation transactions")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions(
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort by field") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction") @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            logger.info("Fetching transactions with page={}, size={}, sortBy={}, sortDir={}", page, size, sortBy, sortDir);
            
            List<Transaction> transactions = transactionService.getAllTransactions(page, size, sortBy, sortDir);
            
            ApiResponse<List<Transaction>> response = new ApiResponse<>(
                true, 
                "Transactions retrieved successfully", 
                transactions
            );
            
            logger.info("Successfully retrieved {} transactions", transactions.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving transactions: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve transactions", null));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get transaction by ID", description = "Retrieve a specific transaction by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transaction"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Transaction>> getTransactionById(
            @Parameter(description = "Transaction ID") @PathVariable String id) {
        
        try {
            logger.info("Fetching transaction with ID: {}", id);
            
            Transaction transaction = transactionService.getTransactionById(id);
            
            if (transaction == null) {
                logger.warn("Transaction not found with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Transaction not found", null));
            }
            
            ApiResponse<Transaction> response = new ApiResponse<>(
                true, 
                "Transaction retrieved successfully", 
                transaction
            );
            
            logger.info("Successfully retrieved transaction with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving transaction with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve transaction", null));
        }
    }

    @PostMapping
    @Operation(summary = "Create new transaction", description = "Create a new donation transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Transaction created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Transaction>> createTransaction(
            @Parameter(description = "Transaction data") @Valid @RequestBody Transaction transaction) {
        
        try {
            logger.info("Creating new transaction: {}", transaction);
            
            // Validate transaction data
            if (transaction.getAmount() == null || transaction.getAmount() <= 0) {
                logger.warn("Invalid transaction amount: {}", transaction.getAmount());
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid transaction amount", null));
            }
            
            Transaction createdTransaction = transactionService.createTransaction(transaction);
            
            ApiResponse<Transaction> response = new ApiResponse<>(
                true, 
                "Transaction created successfully", 
                createdTransaction
            );
            
            logger.info("Successfully created transaction with ID: {}", createdTransaction.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid transaction data: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error creating transaction: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to create transaction", null));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update transaction", description = "Update an existing transaction")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction updated successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "400", description = "Invalid transaction data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Transaction>> updateTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String id,
            @Parameter(description = "Updated transaction data") @Valid @RequestBody Transaction transaction) {
        
        try {
            logger.info("Updating transaction with ID: {}", id);
            
            Transaction updatedTransaction = transactionService.updateTransaction(id, transaction);
            
            if (updatedTransaction == null) {
                logger.warn("Transaction not found for update with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Transaction not found", null));
            }
            
            ApiResponse<Transaction> response = new ApiResponse<>(
                true, 
                "Transaction updated successfully", 
                updatedTransaction
            );
            
            logger.info("Successfully updated transaction with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid transaction data for update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            logger.error("Error updating transaction with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to update transaction", null));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete transaction", description = "Delete a transaction by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Transaction not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(
            @Parameter(description = "Transaction ID") @PathVariable String id) {
        
        try {
            logger.info("Deleting transaction with ID: {}", id);
            
            boolean deleted = transactionService.deleteTransaction(id);
            
            if (!deleted) {
                logger.warn("Transaction not found for deletion with ID: {}", id);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Transaction not found", null));
            }
            
            ApiResponse<Void> response = new ApiResponse<>(
                true, 
                "Transaction deleted successfully", 
                null
            );
            
            logger.info("Successfully deleted transaction with ID: {}", id);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deleting transaction with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to delete transaction", null));
        }
    }

    @GetMapping("/charity/{charityId}")
    @Operation(summary = "Get transactions by charity", description = "Retrieve all transactions for a specific charity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved transactions"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<List<Transaction>>> getTransactionsByCharity(
            @Parameter(description = "Charity ID") @PathVariable String charityId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        
        try {
            logger.info("Fetching transactions for charity ID: {}", charityId);
            
            List<Transaction> transactions = transactionService.getTransactionsByCharity(charityId, page, size);
            
            ApiResponse<List<Transaction>> response = new ApiResponse<>(
                true, 
                "Transactions retrieved successfully", 
                transactions
            );
            
            logger.info("Successfully retrieved {} transactions for charity ID: {}", transactions.size(), charityId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving transactions for charity ID {}: {}", charityId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve transactions", null));
        }
    }

    @GetMapping("/stats")
    @Operation(summary = "Get transaction statistics", description = "Retrieve transaction statistics and analytics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved statistics"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTransactionStats() {
        
        try {
            logger.info("Fetching transaction statistics");
            
            Map<String, Object> stats = transactionService.getTransactionStats();
            
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                true, 
                "Statistics retrieved successfully", 
                stats
            );
            
            logger.info("Successfully retrieved transaction statistics");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error retrieving transaction statistics: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to retrieve statistics", null));
        }
    }

    // Generic API Response wrapper
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private long timestamp;

        public ApiResponse(boolean success, String message, T data) {
            this.success = success;
            this.message = message;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters and setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
