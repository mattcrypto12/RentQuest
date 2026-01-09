# Contributing to RentQuest

Thank you for your interest in contributing to RentQuest! This document provides guidelines for contributing to the project.

## 🚀 Getting Started

1. Fork the repository
2. Clone your fork locally
3. Set up the development environment (see README.md)
4. Create a feature branch from `main`

## 📋 Development Process

### Branching Strategy

- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/*` - New features
- `fix/*` - Bug fixes
- `hotfix/*` - Urgent production fixes

### Commit Messages

Follow conventional commits format:

```
type(scope): description

[optional body]

[optional footer]
```

Types:
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation only
- `style` - Code style (formatting, etc.)
- `refactor` - Code refactoring
- `test` - Adding tests
- `chore` - Build, dependencies, etc.

Examples:
```
feat(scan): add Token-2022 account detection
fix(mwa): handle wallet disconnection gracefully
docs(readme): update installation instructions
```

## 🔍 Code Review Process

1. Open a Pull Request against `develop` (or `main` for hotfixes)
2. Fill out the PR template completely
3. Ensure all CI checks pass
4. Request review from maintainers
5. Address feedback and iterate
6. Once approved, maintainers will merge

## 🧪 Testing Requirements

- All new features must include unit tests
- All bug fixes must include regression tests
- Maintain minimum 80% code coverage for new code
- Run `./gradlew test` before submitting PR

## 📏 Code Style

### Kotlin Style Guide

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Prefer immutability (`val` over `var`)
- Use data classes for models
- Use sealed classes for state representation

### Compose Guidelines

- Keep composables small and focused
- Extract reusable components
- Use `remember` and `rememberSaveable` appropriately
- Follow unidirectional data flow

### Architecture Guidelines

- Follow Clean Architecture principles
- Keep use cases single-purpose
- Don't bypass the domain layer
- Use suspend functions for async operations

## ⚠️ Safety-Critical Code

The following areas require extra scrutiny:

### `isClosable()` Function

Any changes to the account closing logic MUST:
1. Include comprehensive test coverage
2. Be reviewed by at least 2 maintainers
3. Include a security analysis
4. Not introduce any risk of closing accounts with funds

### Transaction Building

Changes to transaction serialization MUST:
1. Be tested on devnet before mainnet
2. Include transaction simulation tests
3. Follow Solana transaction format exactly

## 🐛 Bug Reports

When reporting bugs, include:

1. **Description**: Clear description of the issue
2. **Steps to Reproduce**: Minimal steps to reproduce
3. **Expected Behavior**: What should happen
4. **Actual Behavior**: What actually happens
5. **Environment**: Android version, device, wallet app
6. **Logs**: Relevant error logs or stack traces

## 💡 Feature Requests

When requesting features:

1. **Problem**: What problem does this solve?
2. **Proposed Solution**: How would you solve it?
3. **Alternatives**: Other approaches considered
4. **Additional Context**: Mockups, examples, etc.

## 📝 Documentation

- Update README.md for user-facing changes
- Update AGENT_ONBOARDING.md for architecture changes
- Add inline comments for complex logic
- Keep code self-documenting where possible

## 🔐 Security

- Never commit API keys or secrets
- Report security vulnerabilities privately (see SECURITY.md)
- Follow secure coding practices
- Don't log sensitive information

## 📄 License

By contributing, you agree that your contributions will be licensed under the MIT License.

## 🤝 Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Focus on the code, not the person
- Welcome newcomers

## ❓ Questions?

- Open a GitHub Discussion for general questions
- Open an Issue for bugs or feature requests
- Reach out to maintainers for sensitive matters

---

Thank you for contributing to RentQuest! Together we can help Solana users reclaim their rent.
