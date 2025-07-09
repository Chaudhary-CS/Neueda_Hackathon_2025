# 🤝 Contributing to Trace the Change

Thank you for your interest in contributing to our blockchain charity platform! This document provides guidelines and information for contributors.

## 📋 Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Style Guidelines](#code-style-guidelines)
- [Commit Message Convention](#commit-message-convention)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## 🚀 Getting Started

### Prerequisites

- Node.js 18.x or higher
- npm or pnpm package manager
- Git
- MetaMask browser extension (for testing crypto features)
- AstraDB account (free tier available)

### Quick Start

1. **Fork the repository**
   ```bash
   # Fork on GitHub, then clone your fork
   git clone https://github.com/your-username/Neueda_Hackathon_2025.git
   cd Neueda_Hackathon_2025
   ```

2. **Set up your development environment**
   ```bash
   # Install dependencies
   npm install
   
   # Copy environment template
   cp .env.example .env
   
   # Configure your environment variables
   # (See README.md for detailed setup instructions)
   ```

3. **Start development server**
   ```bash
   npm run dev
   ```

## 🔧 Development Setup

### Environment Configuration

Create a `.env` file with the following variables:

```env
# AstraDB Configuration
ASTRADB_TOKEN=your_astradb_token
ASTRADB_ID=your_astradb_id
ASTRADB_REGION=your_astradb_region
ASTRADB_NAMESPACE=charity_donations

# Optional: Development overrides
NODE_ENV=development
NEXT_PUBLIC_APP_URL=http://localhost:3000
```

### Available Scripts

| Script | Description |
|--------|-------------|
| `npm run dev` | Start development server |
| `npm run build` | Build for production |
| `npm run start` | Start production server |
| `npm run lint` | Run ESLint |
| `npm run type-check` | Run TypeScript type checking |

## 📝 Code Style Guidelines

### TypeScript/JavaScript

- Use TypeScript for all new code
- Follow ESLint configuration
- Use meaningful variable and function names
- Add JSDoc comments for complex functions

### React Components

- Use functional components with hooks
- Follow the component structure:
  ```tsx
  import React from 'react'
  
  interface ComponentProps {
    // Define props interface
  }
  
  export function ComponentName({ prop }: ComponentProps) {
    // Component logic
    return (
      // JSX
    )
  }
  ```

### CSS/Styling

- Use Tailwind CSS for styling
- Follow utility-first approach
- Use CSS modules for complex components
- Maintain responsive design principles

## 🏷️ Commit Message Convention

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### Types

| Type | Description |
|------|-------------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Code style changes (formatting, etc.) |
| `refactor` | Code refactoring |
| `test` | Adding or updating tests |
| `chore` | Maintenance tasks |

### Examples

```bash
# Feature
git commit -m "feat: add MetaMask wallet integration"

# Bug fix
git commit -m "fix: resolve donation form validation issue"

# Documentation
git commit -m "docs: update AstraDB setup instructions"

# Refactor
git commit -m "refactor: optimize charity listing component"
```

## 🔄 Pull Request Process

### Before Submitting

1. **Ensure your code works**
   - Run `npm run dev` and test locally
   - Verify all features work as expected
   - Test on different screen sizes

2. **Follow coding standards**
   - Run `npm run lint` and fix any issues
   - Ensure TypeScript compilation passes
   - Follow the established code style

3. **Update documentation**
   - Update README.md if needed
   - Add comments for complex logic
   - Update any relevant documentation

### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Documentation update
- [ ] Refactoring
- [ ] Other (please describe)

## Testing
- [ ] Tested locally
- [ ] Verified on different devices
- [ ] Checked accessibility
- [ ] Tested with MetaMask integration

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] No console errors
- [ ] Responsive design maintained
```

## 🧪 Testing Guidelines

### Manual Testing Checklist

- [ ] **Cross-browser compatibility**
  - Chrome, Firefox, Safari, Edge
- [ ] **Responsive design**
  - Mobile, tablet, desktop
- [ ] **MetaMask integration**
  - Wallet connection
  - Transaction signing
  - Error handling
- [ ] **Form validation**
  - Required fields
  - Input validation
  - Error messages
- [ ] **Navigation**
  - All links work correctly
  - Back/forward browser buttons
  - Direct URL access

### Testing Tools

- **Browser DevTools** for debugging
- **MetaMask** for crypto testing
- **Lighthouse** for performance
- **Accessibility tools** for WCAG compliance

## 📚 Documentation

### Code Documentation

- Add JSDoc comments for functions
- Include TypeScript interfaces
- Document complex business logic
- Add inline comments for non-obvious code

### User Documentation

- Update README.md for new features
- Include screenshots for UI changes
- Document configuration changes
- Provide troubleshooting guides

## 🐛 Bug Reports

When reporting bugs, please include:

1. **Environment details**
   - Browser and version
   - Operating system
   - Node.js version
   - MetaMask version (if applicable)

2. **Steps to reproduce**
   - Clear, step-by-step instructions
   - Expected vs actual behavior

3. **Additional context**
   - Screenshots or screen recordings
   - Console errors
   - Network tab information

## 🎯 Feature Requests

When suggesting new features:

1. **Describe the problem**
   - What issue does this solve?
   - Who would benefit?

2. **Propose a solution**
   - How should it work?
   - Any technical considerations?

3. **Consider implementation**
   - Is it feasible?
   - What resources are needed?

## 🤝 Community Guidelines

- Be respectful and inclusive
- Help others learn and grow
- Provide constructive feedback
- Follow the project's code of conduct
- Celebrate contributions and achievements

## 📞 Getting Help

- **GitHub Issues**: For bugs and feature requests
- **Discussions**: For questions and general discussion
- **Documentation**: Check README.md and inline docs first

---

<div align="center">

**Thank you for contributing to Trace the Change! 🌟**

*Together, we're building a more transparent and impactful charitable giving platform.*

</div> 