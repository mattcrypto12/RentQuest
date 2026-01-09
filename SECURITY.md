# Security Policy

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 1.x.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

We take security vulnerabilities seriously. If you discover a security issue, please report it responsibly.

### DO NOT

- Open a public GitHub issue for security vulnerabilities
- Disclose the vulnerability publicly before it's fixed
- Exploit the vulnerability beyond what's necessary to demonstrate it

### DO

1. **Email us directly** at security@rentquest.app (or maintainer email)
2. Include the following information:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Any suggested fixes

### What to Expect

- **Acknowledgment**: Within 48 hours of your report
- **Initial Assessment**: Within 1 week
- **Resolution Timeline**: Depends on severity, typically 2-4 weeks
- **Credit**: We'll credit you in the security advisory (unless you prefer anonymity)

## Security Considerations

### Critical Safety: Account Closing

The most security-sensitive part of RentQuest is the `isClosable()` function that determines whether a token account can be safely closed.

**Safety Invariants:**
1. Never close accounts with non-zero token balance
2. Never close accounts not owned by the connected wallet
3. Never close accounts with third-party close authorities

Any vulnerability that could bypass these checks is considered **CRITICAL**.

### Wallet Integration

RentQuest uses Mobile Wallet Adapter (MWA) for all cryptographic operations:

- Private keys never leave the wallet app
- All transactions are signed by the user in their wallet
- No mnemonics or seed phrases are ever handled by RentQuest

### Data Storage

- Wallet session tokens are stored in DataStore (encrypted at rest by Android)
- No sensitive data is transmitted to external servers
- RPC calls go directly to Solana nodes

### Third-Party Dependencies

We regularly audit dependencies for known vulnerabilities:

| Dependency | Security Notes |
|------------|----------------|
| Mobile Wallet Adapter | Official Solana Mobile SDK |
| OkHttp | Industry-standard HTTP client |
| kotlinx-serialization | Official Kotlin serialization |
| DataStore | Official Android storage |

## Security Best Practices

When contributing to RentQuest:

1. **Never bypass safety checks** in `isClosable()`
2. **Never log sensitive data** (private keys, auth tokens)
3. **Always validate RPC responses** before processing
4. **Use parameterized queries** if adding database support
5. **Keep dependencies updated** to latest secure versions

## Known Limitations

### RPC Trust

RentQuest trusts the configured RPC endpoint. A malicious RPC could potentially:
- Return incorrect account data
- Cause transaction failures

Mitigation: Use trusted RPC providers (official Solana, Helius, etc.)

### Wallet Trust

RentQuest trusts the connected wallet app. Users should:
- Only use verified wallet apps (Phantom, Solflare, etc.)
- Keep wallet apps updated

## Security Audits

RentQuest has not yet undergone a formal security audit. 

For production use with significant funds, we recommend:
- Testing on devnet first
- Starting with small operations
- Reviewing transaction details before signing

## Contact

For security concerns:
- Email: security@rentquest.app
- PGP Key: [Available upon request]

---

Thank you for helping keep RentQuest and its users safe!
