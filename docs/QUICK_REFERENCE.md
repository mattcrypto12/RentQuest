# Quick Reference

> Cheat sheet for RentQuest development

---

## 📦 Key Classes

### Models

| Class | Location | Purpose |
|-------|----------|---------|
| `TokenAccount` | `domain/model/TokenModels.kt` | Token account data |
| `WalletSession` | `domain/model/WalletModels.kt` | MWA session info |
| `CloseTransaction` | `domain/model/TransactionModels.kt` | Batch close operation |
| `Achievement` | `domain/model/AchievementModels.kt` | Gamification badges |
| `UserStats` | `domain/model/AchievementModels.kt` | Cumulative stats |
| `Cluster` | `domain/model/WalletModels.kt` | Network selection |

### Use Cases

| Class | Purpose |
|-------|---------|
| `ScanTokenAccountsUseCase` | Fetch & filter closable accounts |
| `BatchCloseAccountsUseCase` | Group accounts into batches |
| `ExecuteCloseTransactionsUseCase` | Execute batch closes |

### Data Layer

| Class | Purpose |
|-------|---------|
| `SolanaRpcClient` | JSON-RPC HTTP calls |
| `MwaClient` | Mobile Wallet Adapter |
| `DataStoreManager` | Local persistence |
| `TransactionBuilder` | Create Solana transactions |
| `SolanaBase58` | Base58 encoding/decoding |
| `PublicKey` | 32-byte Solana address |

---

## 🌐 RPC Endpoints

```kotlin
object RpcEndpoints {
    const val MAINNET = "https://api.mainnet-beta.solana.com"
    const val DEVNET = "https://api.devnet.solana.com"
}
```

---

## 🏷️ Program IDs

```kotlin
object TokenProgram {
    const val TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TOKEN_2022_PROGRAM_ID = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"
}
```

---

## 💰 Constants

```kotlin
const val LAMPORTS_PER_SOL = 1_000_000_000L
const val DEFAULT_RENT_LAMPORTS = 2_039_280L  // ~0.00204 SOL
const val MAX_ACCOUNTS_PER_TX = 8
const val MAX_ACCOUNTS_PER_TX_2022 = 6
```

---

## 🔐 Safety Check

```kotlin
fun isClosable(account: TokenAccount, wallet: String): Boolean {
    return account.amount == 0L &&
           account.owner == wallet &&
           (account.closeAuthority == null || 
            account.closeAuthority == account.owner)
}
```

---

## 📍 Navigation Routes

```kotlin
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Connect : Screen("connect")
    object Scan : Screen("scan")
    object Progress : Screen("progress")
    object History : Screen("history")
    object Settings : Screen("settings")
}
```

---

## 🎨 Theme Colors

```kotlin
// Primary
val Purple600 = Color(0xFF7C3AED)
val Purple700 = Color(0xFF6D28D9)

// Accent
val Emerald400 = Color(0xFF34D399)
val Emerald500 = Color(0xFF10B981)

// Solana Brand
val SolanaPurple = Color(0xFF9945FF)
val SolanaGreen = Color(0xFF14F195)
```

---

## 📊 State Classes

### Scan State
```kotlin
sealed class ScanState {
    object Idle
    object Scanning
    data class Complete(val result: ScanResult)
    data class Error(val message: String)
}
```

### Close State
```kotlin
sealed class CloseOperationState {
    object Idle
    data class InProgress(val current: Int, val total: Int, val currentTx: CloseTransaction?)
    data class Complete(val results: List<CloseTransactionResult>)
    data class Error(val message: String)
}
```

### Wallet State
```kotlin
sealed class WalletConnectionState {
    object Disconnected
    object Connecting
    data class Connected(val session: WalletSession)
    data class Error(val message: String)
}
```

---

## 🧪 Test Utilities

### Create Mock Account
```kotlin
fun mockTokenAccount(
    address: String = "Test${UUID.randomUUID()}",
    amount: Long = 0,
    owner: String = "Owner111...",
    programId: String = TokenProgram.TOKEN_PROGRAM_ID
) = TokenAccount(
    address = address,
    mint = "Mint111...",
    owner = owner,
    amount = amount,
    decimals = 9,
    programId = programId,
    closeAuthority = null,
    rentLamports = DEFAULT_RENT_LAMPORTS
)
```

---

## 🔧 Gradle Commands

```bash
# Build
./gradlew assembleDebug
./gradlew assembleRelease

# Install
./gradlew installDebug

# Test
./gradlew test
./gradlew testDebugUnitTest

# Clean
./gradlew clean
```

---

## 📁 File Locations

| Type | Path |
|------|------|
| Models | `app/src/main/java/com/rentquest/app/domain/model/` |
| Use Cases | `app/src/main/java/com/rentquest/app/domain/usecase/` |
| RPC Client | `app/src/main/java/com/rentquest/app/data/rpc/` |
| MWA Client | `app/src/main/java/com/rentquest/app/data/wallet/` |
| Screens | `app/src/main/java/com/rentquest/app/ui/screens/` |
| ViewModel | `app/src/main/java/com/rentquest/app/ui/viewmodel/` |
| Tests | `app/src/test/java/com/rentquest/app/` |

---

## 🏆 Achievements

| Achievement | Threshold | Emoji |
|-------------|-----------|-------|
| FIRST_SWEEP | 1 account | 🧹 |
| DUST_BUSTER | 10 accounts | 💨 |
| JANITOR | 50 accounts | 🧑‍🔧 |

---

## 📝 DataStore Keys

```kotlin
object PreferenceKeys {
    val CLUSTER = stringPreferencesKey("cluster")
    val CUSTOM_RPC_URL = stringPreferencesKey("custom_rpc_url")
    val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    val WALLET_SESSION = stringPreferencesKey("wallet_session")
    val USER_STATS = stringPreferencesKey("user_stats")
    val CLOSE_HISTORY = stringPreferencesKey("close_history")
}
```

---

## 🚨 Error Codes

| RPC Error | Meaning |
|-----------|---------|
| -32600 | Invalid request |
| -32601 | Method not found |
| -32602 | Invalid params |
| -32603 | Internal error |
| -32002 | Transaction simulation failed |

---

## 🔗 Useful Links

- [Solana JSON-RPC Docs](https://docs.solana.com/api)
- [MWA Documentation](https://docs.solanamobile.com/getting-started/overview)
- [SPL Token Docs](https://spl.solana.com/token)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
