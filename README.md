# RentQuest 🧹💰

> **Wallet Spring Cleaning for Solana Mobile** — Reclaim SOL from empty token accounts

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Solana](https://img.shields.io/badge/Blockchain-Solana-blue.svg)](https://solana.com)
[![MWA](https://img.shields.io/badge/Mobile%20Wallet%20Adapter-2.0.3-teal.svg)](https://solanamobile.com)

## 🎯 What is RentQuest?

Every token account on Solana costs **~0.002 SOL** in rent. If you've airdropped, swapped, or experimented with tokens, your wallet likely has dozens of empty accounts holding your SOL hostage.

**RentQuest** scans your wallet, identifies reclaimable accounts, and batch-closes them — returning your SOL with satisfying animations and achievement unlocks.

### 💡 The Problem

```
Token Account A: 0 USDC → 0.00203928 SOL locked
Token Account B: 0 BONK → 0.00203928 SOL locked
Token Account C: 0 JUP  → 0.00203928 SOL locked
... × 50 accounts = 0.10+ SOL lost!
```

### ✨ The Solution

RentQuest makes wallet cleanup:
- **Safe** — Only closes zero-balance accounts you own
- **Fast** — Batches up to 8 closures per transaction
- **Fun** — Gamified with achievements and animations

---

## 📱 Features

| Feature | Description |
|---------|-------------|
| 🔍 **Smart Scan** | Detects empty SPL Token & Token-2022 accounts |
| 🔐 **MWA Integration** | Secure signing via Phantom, Solflare, etc. |
| ⚡ **Batch Close** | Up to 8 accounts per transaction |
| 🎮 **Achievements** | Unlock badges as you clean |
| 📊 **History** | Track all recovery operations |
| 🌐 **Multi-Cluster** | Mainnet, Devnet, or custom RPC |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       UI Layer                               │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐        │
│  │Onboarding│ │  Scan    │ │ Progress │ │ History  │        │
│  │  Screen  │ │  Screen  │ │  Screen  │ │  Screen  │        │
│  └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘        │
│       └────────────┴────────────┴────────────┘               │
│                          │                                   │
│                    MainViewModel                             │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────┴───────────────────────────────────┐
│                     Domain Layer                             │
│  ┌─────────────────┐ ┌─────────────────┐ ┌────────────────┐  │
│  │ ScanTokenAccounts│ │BatchCloseAccounts│ │ExecuteClose    │ │
│  │    UseCase       │ │    UseCase       │ │Transactions    │ │
│  └─────────────────┘ └─────────────────┘ └────────────────┘  │
└──────────────────────────┬───────────────────────────────────┘
                           │
┌──────────────────────────┴───────────────────────────────────┐
│                      Data Layer                              │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────────┐  │
│  │ SolanaRpc    │ │  MwaClient   │ │  DataStoreManager    │  │
│  │   Client     │ │              │ │                      │  │
│  └──────────────┘ └──────────────┘ └──────────────────────┘  │
└──────────────────────────────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK 34
- A Solana mobile wallet (Phantom, Solflare, Ultimate, etc.)

### Build & Run

```bash
# Clone the repository
git clone https://github.com/your-org/RentQuest.git
cd RentQuest

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

### Test on Devnet

1. Open app → Settings → Switch to **Devnet**
2. Get Devnet SOL from a faucet
3. Create test token accounts
4. Scan and close!

---

## 🔒 Security

### Account Safety

The app uses strict safety checks before closing any account:

```kotlin
fun isClosable(account: TokenAccount): Boolean {
    // ✅ Amount must be exactly zero
    if (account.amount != 0L) return false
    
    // ✅ User must be the owner
    if (account.owner != connectedWallet) return false
    
    // ✅ No close authority restrictions
    if (account.closeAuthority != null && 
        account.closeAuthority != account.owner) return false
    
    return true
}
```

### Transaction Security

- All transactions signed locally via Mobile Wallet Adapter
- No private keys ever leave the wallet app
- Each transaction is individually confirmed on-chain

---

## 📁 Project Structure

```
app/src/main/java/com/rentquest/app/
├── data/
│   ├── local/
│   │   └── DataStoreManager.kt    # Preferences & history
│   ├── rpc/
│   │   ├── RpcModels.kt           # JSON-RPC DTOs
│   │   └── SolanaRpcClient.kt     # HTTP client
│   ├── solana/
│   │   ├── SolanaUtils.kt         # Base58, PublicKey
│   │   └── TransactionBuilder.kt  # TX construction
│   └── wallet/
│       └── MwaClient.kt           # Mobile Wallet Adapter
├── domain/
│   ├── model/
│   │   ├── WalletModels.kt        # Session, Cluster
│   │   ├── TokenModels.kt         # TokenAccount, ScanResult
│   │   ├── TransactionModels.kt   # CloseTransaction, Status
│   │   └── AchievementModels.kt   # Gamification
│   └── usecase/
│       ├── ScanTokenAccountsUseCase.kt
│       ├── BatchCloseAccountsUseCase.kt
│       └── ExecuteCloseTransactionsUseCase.kt
└── ui/
    ├── components/
    │   ├── Animations.kt          # Loot & achievements
    │   └── CommonComponents.kt    # Buttons, cards, states
    ├── navigation/
    │   └── Navigation.kt          # Screen definitions
    ├── screens/
    │   ├── OnboardingScreen.kt
    │   ├── ConnectScreen.kt
    │   ├── ScanScreen.kt
    │   ├── CloseProgressScreen.kt
    │   ├── HistoryScreen.kt
    │   └── SettingsScreen.kt
    ├── theme/
    │   ├── Color.kt
    │   ├── Theme.kt
    │   └── Type.kt
    ├── viewmodel/
    │   └── MainViewModel.kt
    └── MainActivity.kt
```

---

## 🎮 Achievements

| Badge | Name | Requirement |
|-------|------|-------------|
| 🧹 | First Sweep | Close your first account |
| 💨 | Dust Buster | Close 10 accounts |
| 🧑‍🔧 | The Janitor | Close 50 accounts |

---

## 🛠️ Tech Stack

| Component | Technology |
|-----------|------------|
| Language | Kotlin 1.9.21 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Clean Architecture |
| Wallet | Mobile Wallet Adapter 2.0.3 |
| Network | OkHttp 4.12.0 |
| Serialization | kotlinx-serialization-json |
| Storage | DataStore Preferences |
| Build | Gradle 8.2 + AGP 8.2.0 |

---

## 🧪 Testing

```bash
# Unit tests
./gradlew test

# With coverage
./gradlew testDebugUnitTestCoverage
```

### Test Coverage

| Module | Coverage |
|--------|----------|
| Use Cases | ~90% |
| Transaction Builder | ~85% |
| Solana Utils | ~95% |

---

## 📄 License

MIT License — see [LICENSE](LICENSE) file.

---

## 🤝 Contributing

Contributions welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) first.

---

## 🔗 Links

- [Solana Mobile Documentation](https://docs.solanamobile.com)
- [Mobile Wallet Adapter](https://github.com/solana-mobile/mobile-wallet-adapter)
- [SPL Token Program](https://spl.solana.com/token)

---

<p align="center">
  <strong>Built for the Solana Mobile ecosystem</strong><br>
  <em>Reclaim your rent. Clean your wallet. Level up.</em>
</p>
