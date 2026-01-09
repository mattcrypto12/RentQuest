# Privacy Policy for SweepQuest

**Last Updated: January 9, 2026**

## Overview

SweepQuest is a mobile application that helps users reclaim SOL locked in empty Solana token accounts. We are committed to protecting your privacy and being transparent about our practices.

## Data Collection

### What We Do NOT Collect

SweepQuest does **not** collect, store, or transmit:

- Personal information (name, email, phone number)
- Wallet private keys or seed phrases
- Transaction history beyond the current session
- Device identifiers or tracking data
- Analytics or usage data
- Location data

### What the App Accesses Locally

To function, SweepQuest accesses the following on your device only:

- **Public wallet address**: Used to query your token accounts from the Solana blockchain (public data)
- **Network connectivity**: To communicate with Solana RPC endpoints

This data never leaves your device except as blockchain queries to public Solana RPC endpoints.

## Third-Party Services

### Solana RPC Providers

SweepQuest connects to Solana RPC providers (such as Helius) to:
- Query your token account balances
- Submit signed transactions to the network

These are standard blockchain operations. The RPC provider may have its own privacy policy regarding request logging.

### Mobile Wallet Adapter

SweepQuest uses Solana Mobile Wallet Adapter to request transaction signatures from your wallet (Seed Vault). Your private keys never leave the secure enclave of your device.

## Data Security

- All transaction signing occurs within your device's Seed Vault
- No private keys are ever exposed to SweepQuest
- No data is stored on external servers
- No accounts or registration required

## Children's Privacy

SweepQuest is not intended for use by children under 13 years of age.

## Changes to This Policy

We may update this Privacy Policy from time to time. Changes will be reflected in the "Last Updated" date above.

## Contact

For questions about this Privacy Policy, please open an issue at:
https://github.com/mattcrypto12/SweepQuest/issues

## Open Source

SweepQuest is open source software. You can review the complete source code at:
https://github.com/mattcrypto12/SweepQuest
