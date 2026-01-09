# Code Walkthrough

This document provides a detailed walkthrough of the main execution flows in RentQuest.

---

## 1. App Startup Flow

### Sequence

```
Application Launch
       │
       ▼
MainActivity.onCreate()
       │
       ▼
Check DataStore for onboarding status
       │
       ├─── Not completed ─── Show OnboardingScreen
       │
       └─── Completed ─── Check saved session
                               │
                               ├─── Valid session ─── Navigate to ScanScreen
                               │
                               └─── No session ─── Show ConnectScreen
```

### Key Files
- `MainActivity.kt`: Entry point, sets up NavHost
- `MainViewModel.kt`: Initializes state from DataStore
- `DataStoreManager.kt`: Retrieves persisted data

### Code Path
1. `MainActivity.onCreate()` creates `MainViewModel`
2. ViewModel constructor loads `DataStoreManager`
3. `init` block collects saved cluster, session, stats
4. `startScreen` computed from `hasCompletedOnboarding` and session validity

---

## 2. Wallet Connection Flow

### Sequence

```
User taps "Connect Wallet"
       │
       ▼
MwaClient.connect() called
       │
       ▼
Android Intent to wallet app
       │
       ▼
User approves in wallet
       │
       ▼
Receive authorization result
       │
       ├─── Success ─── Create WalletSession
       │                      │
       │                      ▼
       │               Save to DataStore
       │                      │
       │                      ▼
       │               Navigate to ScanScreen
       │
       └─── Failure ─── Show error message
```

### Key Files
- `ConnectScreen.kt`: UI and button handler
- `MwaClient.kt`: MWA protocol implementation
- `MainViewModel.kt`: Manages connection state

### MWA Protocol Details
1. Create `ActivityResultLauncher` for MWA intent
2. Build `AuthorizeRequest` with cluster and app identity
3. Launch wallet app via intent
4. Handle `AuthorizationResult` in callback
5. Extract public key, auth token, wallet name

### Code Example
```kotlin
// In MwaClient.kt
suspend fun connect(cluster: Cluster): Result<WalletSession> {
    val request = AuthorizeRequest(
        identityUri = Uri.parse("https://rentquest.app"),
        identityName = "RentQuest",
        cluster = cluster.toMwaCluster()
    )
    
    return withContext(Dispatchers.Main) {
        // Launch intent and await result
        val result = intentSender.authorize(request)
        
        result.mapCatching { auth ->
            WalletSession(
                publicKey = auth.publicKey.toBase58(),
                authToken = auth.authToken,
                walletName = auth.walletUriBase
            )
        }
    }
}
```

---

## 3. Token Account Scanning Flow

### Sequence

```
ScanScreen loads with connected wallet
       │
       ▼
MainViewModel.scanForClosableAccounts()
       │
       ▼
SolanaRpcClient.getTokenAccountsByOwner()
       │
       ├─── SPL Token Program accounts
       │
       └─── Token-2022 Program accounts
              │
              ▼
       Combine all accounts
              │
              ▼
       Filter via ScanTokenAccountsUseCase.isClosable()
              │
              ▼
       Update UI with ScanState.Complete
```

### Key Files
- `ScanScreen.kt`: Displays scanning UI and results
- `SolanaRpcClient.kt`: Fetches on-chain data
- `ScanTokenAccountsUseCase.kt`: Contains safety filtering

### RPC Call Details
```kotlin
// getTokenAccountsByOwner request
{
    "jsonrpc": "2.0",
    "id": 1,
    "method": "getTokenAccountsByOwner",
    "params": [
        "OWNER_PUBKEY",
        { "programId": "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA" },
        { "encoding": "jsonParsed" }
    ]
}
```

### Safety Filtering (Critical)
```kotlin
// In ScanTokenAccountsUseCase.kt
fun isClosable(account: TokenAccount, walletAddress: String): Boolean {
    // 1. Must have zero balance
    if (account.amount != 0L) return false
    
    // 2. Must be owned by connected wallet
    if (account.owner != walletAddress) return false
    
    // 3. Close authority must be null or the owner
    if (account.closeAuthority != null && 
        account.closeAuthority != account.owner) return false
    
    return true
}
```

---

## 4. Batch Close Execution Flow

### Sequence

```
User selects accounts and taps "Close Selected"
       │
       ▼
BatchCloseAccountsUseCase.execute()
       │
       ▼
Group accounts into batches (max 8 per tx)
       │
       ▼
For each batch:
       │
       ▼
TransactionBuilder.buildCloseAccountsTransaction()
       │
       ▼
MwaClient.signAndSendTransactions()
       │
       ▼
Wait for confirmation via SolanaRpcClient.confirmTransaction()
       │
       ▼
Update progress, stats, and history
       │
       ▼
Check for new achievement unlocks
       │
       ▼
Show completion with LootAnimation
```

### Key Files
- `BatchCloseAccountsUseCase.kt`: Batching logic
- `ExecuteCloseTransactionsUseCase.kt`: Execution orchestration
- `TransactionBuilder.kt`: Creates Solana transactions
- `CloseProgressScreen.kt`: Shows progress UI

### Transaction Building Details

```kotlin
// Close Account instruction structure
struct CloseAccountInstruction {
    programId: TOKEN_PROGRAM_ID,
    accounts: [
        { pubkey: accountToClose, isWritable: true,  isSigner: false },
        { pubkey: destination,    isWritable: true,  isSigner: false },
        { pubkey: owner,          isWritable: false, isSigner: true  }
    ],
    data: [9]  // CloseAccount instruction opcode
}
```

### Batch Size Logic
```kotlin
fun calculateOptimalBatchSize(accounts: List<TokenAccount>): Int {
    val hasToken2022 = accounts.any { 
        it.programId == TokenProgram.TOKEN_2022_PROGRAM_ID 
    }
    return if (hasToken2022) 6 else 8
}
```

---

## 5. Transaction Execution Flow

### Sequence

```
ExecuteCloseTransactionsUseCase.execute(batches)
       │
       ▼
For each CloseTransaction in batches:
       │
       ▼
Emit CloseOperationState.InProgress(transaction)
       │
       ▼
Fetch recent blockhash
       │
       ▼
Build transaction message
       │
       ▼
Request signature via MWA
       │
       ├─── User rejects ─── Mark transaction FAILED
       │
       └─── User approves ─── Continue
              │
              ▼
       Send to RPC endpoint
              │
              ▼
       Poll for confirmation (30 retries)
              │
              ├─── Confirmed ─── Mark SUCCESS
              │                       │
              │                       ▼
              │               Update stats
              │                       │
              │                       ▼
              │               Add to history
              │
              └─── Timeout/Error ─── Mark FAILED
```

### Key Code
```kotlin
// ExecuteCloseTransactionsUseCase.kt
fun execute(transactions: List<CloseTransaction>): Flow<CloseTransactionResult> = flow {
    for (tx in transactions) {
        emit(CloseTransactionResult.Pending(tx.id))
        
        try {
            // Get fresh blockhash
            val blockhash = rpcClient.getLatestBlockhash()
            
            // Build transaction bytes
            val txBytes = transactionBuilder.buildCloseAccountsTransaction(
                accountsToClose = tx.accounts.map { PublicKey.fromBase58(it.address) },
                ownerPubkey = PublicKey.fromBase58(session.publicKey),
                destinationPubkey = PublicKey.fromBase58(session.publicKey),
                recentBlockhash = blockhash,
                programId = tx.accounts.first().programId
            )
            
            // Sign via MWA
            val signature = mwaClient.signAndSendTransactions(listOf(txBytes))
            
            // Wait for confirmation
            val confirmed = rpcClient.confirmTransaction(signature, timeout = 60_000)
            
            if (confirmed) {
                emit(CloseTransactionResult.Success(tx.id, signature, tx.estimatedRentLamports))
            } else {
                emit(CloseTransactionResult.Failure(tx.id, "Transaction not confirmed"))
            }
        } catch (e: Exception) {
            emit(CloseTransactionResult.Failure(tx.id, e.message ?: "Unknown error"))
        }
    }
}
```

---

## 6. Achievement Unlock Flow

### Sequence

```
Transaction confirmed successfully
       │
       ▼
Update UserStats (increment totalAccountsClosed, totalSolRecovered)
       │
       ▼
Achievement.getNewlyUnlocked(updatedStats)
       │
       ▼
Compare thresholds to stats
       │
       ├─── New achievement unlocked ─── Add to unlockedAchievements
       │                                        │
       │                                        ▼
       │                               Emit AchievementState.JustUnlocked
       │                                        │
       │                                        ▼
       │                               Show AchievementUnlockAnimation
       │
       └─── No new achievements ─── Continue
```

### Achievement Thresholds
```kotlin
enum class Achievement(val threshold: Int) {
    FIRST_SWEEP(1),    // Close 1 account
    DUST_BUSTER(10),   // Close 10 accounts
    JANITOR(50)        // Close 50 accounts
}
```

---

## 7. Error Handling Patterns

### RPC Errors
```kotlin
sealed class RpcResult<T> {
    data class Success<T>(val data: T) : RpcResult<T>()
    data class Error<T>(val code: Int, val message: String) : RpcResult<T>()
}
```

### Transaction Errors
```kotlin
sealed class CloseTransactionResult {
    data class Pending(val transactionId: String)
    data class Success(val transactionId: String, val signature: String, val lamports: Long)
    data class Failure(val transactionId: String, val error: String)
}
```

### UI Error States
```kotlin
sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Complete(val result: ScanResult) : ScanState()
    data class Error(val message: String) : ScanState()
}
```

---

## 8. Data Persistence Flow

### What's Persisted
| Data | Storage Key | Type |
|------|-------------|------|
| Cluster | `cluster` | Enum string |
| Custom RPC | `custom_rpc_url` | String |
| Onboarding | `onboarding_completed` | Boolean |
| Session | `wallet_session` | JSON |
| Stats | `user_stats` | JSON |
| History | `close_history` | JSON array |

### Persistence Points
- **Cluster**: On settings change
- **Session**: On connect/disconnect
- **Stats**: After each successful close
- **History**: After each close operation

---

## Summary

The app follows a predictable flow:
1. **Connect** → MWA authorization
2. **Scan** → RPC fetch + safety filter
3. **Batch** → Group accounts
4. **Execute** → Sign + send + confirm
5. **Update** → Stats + history + achievements

Each step has clear error handling and state updates. The `isClosable()` function is the critical safety gate that prevents any accidental fund loss.
