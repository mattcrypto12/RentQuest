# Architecture Decision Records

This document captures key architectural decisions made during the development of RentQuest.

---

## ADR-001: Use Clean Architecture with MVVM

### Context
We needed an architecture that separates concerns, is testable, and works well with Jetpack Compose.

### Decision
Adopt Clean Architecture (data/domain/ui layers) combined with MVVM pattern for the UI layer.

### Rationale
- **Testability**: Use cases can be unit tested without Android dependencies
- **Maintainability**: Clear separation of concerns
- **Scalability**: Easy to add new features without touching existing code
- **Compose Compatibility**: MVVM with StateFlow works seamlessly with Compose

### Consequences
- Slightly more boilerplate than a simpler architecture
- Clear boundaries between layers
- Easy to swap implementations (e.g., mock RPC client for testing)

---

## ADR-002: Manual Dependency Injection

### Context
Needed to manage dependencies across layers without excessive complexity.

### Decision
Use manual dependency injection rather than Hilt/Dagger/Koin.

### Rationale
- **Simplicity**: No build-time code generation
- **Transparency**: All wiring visible in code
- **Faster Builds**: No annotation processing overhead
- **MVP Appropriate**: Sufficient for current app size

### Consequences
- Must manually create and pass dependencies
- No automatic lifecycle management
- May need to migrate to DI framework if app grows significantly

---

## ADR-003: Mobile Wallet Adapter for Signing

### Context
Need a secure way to sign transactions without handling private keys.

### Decision
Use Solana Mobile's Mobile Wallet Adapter (MWA) exclusively for transaction signing.

### Rationale
- **Security**: Private keys never leave the wallet app
- **User Trust**: Users sign in their trusted wallet
- **Standard**: Official Solana Mobile SDK
- **UX**: Seamless integration with mobile wallets

### Consequences
- Requires a compatible wallet app installed
- Cannot work in isolation (needs external wallet)
- Dependent on MWA protocol version compatibility

---

## ADR-004: DataStore for Local Persistence

### Context
Need to persist user preferences, session data, and history.

### Decision
Use Jetpack DataStore Preferences for all local storage.

### Rationale
- **Modern**: Replacement for SharedPreferences
- **Coroutines**: Built-in suspend function support
- **Type Safety**: Better than SharedPreferences
- **Sufficient**: No need for Room (no relational data)

### Consequences
- Simple key-value storage only
- History stored as JSON serialized strings
- May need Room if data model becomes more complex

---

## ADR-005: OkHttp for RPC Calls

### Context
Need an HTTP client for Solana JSON-RPC communication.

### Decision
Use OkHttp directly rather than Retrofit or Ktor.

### Rationale
- **Lightweight**: Minimal dependencies
- **Control**: Full control over request/response handling
- **Simple API**: Only need POST for JSON-RPC
- **No Retrofit Overhead**: Simple RPC doesn't benefit from Retrofit's abstractions

### Consequences
- Manual request building
- Manual response parsing (with kotlinx.serialization)
- May want Retrofit if API surface grows

---

## ADR-006: Custom Transaction Serialization

### Context
Need to build and serialize Solana transactions in Kotlin.

### Decision
Implement custom transaction builder instead of using a third-party Solana SDK.

### Rationale
- **Minimal Scope**: Only need close account transactions
- **Size Control**: Avoid pulling in large SDKs
- **Understanding**: Full knowledge of what we're signing
- **No Suitable SDK**: No mature Kotlin Solana SDK at time of development

### Consequences
- Limited to close account functionality
- Must maintain serialization code ourselves
- Well-tested for our specific use case

---

## ADR-007: Batch Size Limits

### Context
Need to determine optimal number of close instructions per transaction.

### Decision
- Standard Token Program: Max 8 accounts per transaction
- Token-2022 Program: Max 6 accounts per transaction

### Rationale
- **Transaction Size**: Solana transactions have 1232 byte limit
- **Compute Units**: Multiple instructions consume CUs
- **Token-2022**: Larger instruction encoding
- **Safety Margin**: Leave room for overhead

### Consequences
- Multiple transactions needed for large cleanups
- Predictable transaction sizes
- Minimizes partial failures

---

## ADR-008: Single ViewModel Architecture

### Context
Need to manage state across multiple screens.

### Decision
Use a single `MainViewModel` shared across all screens.

### Rationale
- **Shared State**: Wallet session, stats used everywhere
- **Simplicity**: No inter-ViewModel communication needed
- **Navigation**: Easier to handle cross-screen flows
- **MVP Size**: App is small enough for one ViewModel

### Consequences
- ViewModel may grow large over time
- All screens coupled through one state holder
- Consider splitting if complexity increases

---

## ADR-009: Result-Based Error Handling

### Context
Need consistent error handling across async operations.

### Decision
Use `Result<T>` for single operations and sealed classes for multi-state flows.

### Rationale
- **Kotlin Standard**: `Result<T>` is built-in
- **Explicit**: Forces handling of success/failure
- **Sealed Classes**: Perfect for scan/close state machines
- **Composable**: Works well with coroutines

### Consequences
- Must handle both success and failure paths
- Consistent error propagation pattern
- Clear state transitions in UI

---

## ADR-010: Achievement System Design

### Context
Want to gamify the experience with achievements.

### Decision
Implement simple threshold-based achievements evaluated on stats update.

### Rationale
- **Simplicity**: Easy to understand and extend
- **Immediate Feedback**: Unlock checked after each operation
- **Persistence**: Stats stored in DataStore
- **No Server**: All local, no network dependency

### Consequences
- Achievements are client-side only
- Easy to add new achievements
- Resetting data resets achievements

---

## ADR-011: Cluster Configuration

### Context
Need to support testing on devnet and production on mainnet.

### Decision
Support MAINNET_BETA, DEVNET, and CUSTOM RPC endpoints.

### Rationale
- **Testing**: Devnet for development and testing
- **Production**: Mainnet for real usage
- **Flexibility**: Custom RPC for power users/enterprise

### Consequences
- Must handle cluster switching properly
- RPC URLs configurable per cluster
- Some features may behave differently per cluster

---

## ADR-012: No Token Metadata Resolution

### Context
Could fetch token names, symbols, icons for better UX.

### Decision
MVP will not resolve token metadata; show mint addresses only.

### Rationale
- **Complexity**: Requires Metaplex metadata fetching
- **Speed**: Additional RPC calls slow scanning
- **Scope**: MVP focuses on core functionality
- **Future**: Can add in later version

### Consequences
- Users see mint addresses, not token names
- Simpler implementation
- Faster scanning
- Clear upgrade path for future

---

## Summary

These decisions prioritize:
1. **Security** (MWA, isClosable safety)
2. **Simplicity** (manual DI, single ViewModel)
3. **Performance** (OkHttp, batch limits)
4. **User Experience** (achievements, animations)
5. **Maintainability** (Clean Architecture, sealed classes)

Future versions may revisit these decisions as requirements evolve.
