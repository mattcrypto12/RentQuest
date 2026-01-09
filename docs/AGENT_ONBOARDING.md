# Agent Onboarding Guide 🤖

> Quick-start guide for AI agents working on the RentQuest codebase

## 🎯 Project Purpose

**RentQuest** is an Android app for Solana Mobile that helps users reclaim SOL locked in empty token accounts. When users swap/trade tokens and end up with zero-balance accounts, those accounts still hold ~0.002 SOL in rent. This app:

1. Scans the user's wallet for empty token accounts
2. Batch-closes them (up to 8 per transaction)
3. Returns the rent SOL to the user

## 🏗️ Architecture Overview

```
UI Layer (Compose) → ViewModel → Use Cases → Data Layer (RPC, MWA, Local)
```

### Key Design Decisions

- **No DI Framework**: Manual dependency injection for simplicity
- **StateFlow for State**: All UI state flows through `MainViewModel`
- **Suspend Functions**: Coroutines everywhere, no callbacks
- **Sealed Classes**: For representing discrete states (scan, close, transactions)

## 📁 Directory Map

```
app/src/main/java/com/rentquest/app/
├── data/           # External interfaces
│   ├── local/      # DataStore persistence
│   ├── rpc/        # Solana JSON-RPC client
│   ├── solana/     # Base58, transaction building
│   └── wallet/     # Mobile Wallet Adapter client
├── domain/         # Business logic
│   ├── model/      # Data classes (TokenAccount, etc.)
│   └── usecase/    # ScanTokenAccounts, BatchClose, ExecuteClose
└── ui/             # Jetpack Compose UI
    ├── components/ # Reusable composables
    ├── navigation/ # Screen routing
    ├── screens/    # Full-screen composables
    ├── theme/      # Colors, typography
    └── viewmodel/  # MainViewModel
```

## 🔑 Critical Files

| File | Importance | Description |
|------|------------|-------------|
| `ScanTokenAccountsUseCase.kt` | ⭐⭐⭐ | Contains `isClosable()` - THE safety check |
| `MainViewModel.kt` | ⭐⭐⭐ | All state management |
| `MwaClient.kt` | ⭐⭐ | Wallet connection & signing |
| `SolanaRpcClient.kt` | ⭐⭐ | On-chain data fetching |
| `TransactionBuilder.kt` | ⭐⭐ | Creates close instructions |

## ⚠️ Safety-Critical Code

### The `isClosable()` Function

This is the MOST IMPORTANT function in the codebase. It determines whether an account can safely be closed:

```kotlin
// In ScanTokenAccountsUseCase.kt
fun isClosable(account: TokenAccount, walletAddress: String): Boolean {
    // Must have zero balance
    if (account.amount != 0L) return false
    
    // User must own the account
    if (account.owner != walletAddress) return false
    
    // No third-party close authority
    if (account.closeAuthority != null && 
        account.closeAuthority != account.owner) return false
    
    return true
}
```

**NEVER** modify this function without understanding the consequences. Closing accounts with non-zero balances would LOSE USER FUNDS.

## 🔄 Core User Flows

### 1. Wallet Connection Flow
```
ConnectScreen → MwaClient.connect() → Store session → Navigate to ScanScreen
```

### 2. Scan Flow
```
ScanScreen → SolanaRpcClient.getTokenAccountsByOwner() 
          → Filter via isClosable() 
          → Display closable accounts
```

### 3. Close Flow
```
ScanScreen (Select All) → BatchCloseAccountsUseCase.execute()
                       → ExecuteCloseTransactionsUseCase.execute()
                       → MwaClient.signAndSendTransactions()
                       → Confirm on-chain
                       → Update stats & history
```

## 📦 Key Dependencies

| Package | Usage |
|---------|-------|
| `com.solanamobile:mobile-wallet-adapter-clientlib-ktx` | Wallet signing |
| `com.squareup.okhttp3:okhttp` | HTTP for RPC |
| `org.jetbrains.kotlinx:kotlinx-serialization-json` | JSON parsing |
| `androidx.datastore:datastore-preferences` | Local persistence |
| `io.github.novacrypto:Base58` | Solana address encoding |

## 🧪 Running Tests

```bash
# All unit tests
./gradlew test

# Specific test file
./gradlew test --tests "ScanTokenAccountsUseCaseTest"
```

## 🔧 Common Tasks

### Add a New Screen
1. Create `NewScreen.kt` in `ui/screens/`
2. Add route to `Navigation.kt`
3. Add navigation in `MainActivity.kt`

### Add a New Achievement
1. Add enum value in `AchievementModels.kt`
2. Update `getNewlyUnlocked()` logic
3. Add display assets if needed

### Modify RPC Client
1. Add DTO in `RpcModels.kt`
2. Add method in `SolanaRpcClient.kt`
3. Use from use case

## 🌐 Clusters

The app supports multiple Solana clusters:
- **MAINNET_BETA**: Production (real funds!)
- **DEVNET**: Testing (free test SOL)
- **CUSTOM**: User-provided RPC URL

Default is MAINNET_BETA. Change via Settings screen.

## 📝 Code Style

- **Kotlin Coroutines**: Use `suspend` functions, not callbacks
- **State**: Use `StateFlow` for observable state
- **Error Handling**: Use `Result<T>` or sealed classes
- **Compose**: Single-activity architecture
- **Naming**: Descriptive names, no abbreviations

## 🚨 Things to Avoid

1. **NEVER** bypass `isClosable()` safety checks
2. **NEVER** hardcode private keys or mnemonics
3. **NEVER** close accounts without user confirmation
4. **NEVER** use blocking calls on the main thread
5. **NEVER** store wallet session data insecurely

## 📚 Further Reading

- [ARCHITECTURE_DECISIONS.md](docs/ARCHITECTURE_DECISIONS.md) - ADRs
- [CODE_WALKTHROUGH.md](docs/CODE_WALKTHROUGH.md) - Execution flow details
- [QUICK_REFERENCE.md](docs/QUICK_REFERENCE.md) - API cheat sheet

---

**TL;DR**: Scan for empty accounts → Batch into transactions → Sign with MWA → Confirm on-chain → Update stats. The `isClosable()` function is sacred. Don't touch it without deep understanding.
