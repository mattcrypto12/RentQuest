package com.rentquest.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a batch close transaction
 */
data class CloseTransaction(
    val id: String,
    val accounts: List<TokenAccount>,
    val estimatedRentLamports: Long,
    var status: TransactionStatus = TransactionStatus.PENDING,
    var signature: String? = null
) {
    val accountCount: Int
        get() = accounts.size
        
    val estimatedRentSol: Double
        get() = estimatedRentLamports / 1_000_000_000.0
}

/**
 * Transaction status
 */
enum class TransactionStatus {
    PENDING,
    SIGNING,
    SENDING,
    CONFIRMING,
    CONFIRMED,
    FAILED
}

/**
 * State of the close operation
 */
sealed class CloseOperationState {
    data object Idle : CloseOperationState()
    
    data class InProgress(
        val currentTransaction: CloseTransaction,
        val currentIndex: Int,
        val totalTransactions: Int,
        val completedSignatures: List<String>
    ) : CloseOperationState()
    
    data class Complete(val historyEntry: CloseHistoryEntry) : CloseOperationState()
    
    data class Error(val message: String) : CloseOperationState()
}

/**
 * History entry for a completed close operation
 */
@Serializable
data class CloseHistoryEntry(
    val id: String,
    val timestamp: Long,
    val accountsClosed: Int,
    val lamportsReclaimed: Long,
    val signatures: List<String>,
    val cluster: String
) {
    val solReclaimed: Double
        get() = lamportsReclaimed / 1_000_000_000.0
}

/**
 * Configuration for batching close transactions
 */
object BatchConfig {
    const val MAX_CLOSE_INSTRUCTIONS_PER_TX = 8
    const val MAX_CLOSE_INSTRUCTIONS_TOKEN_2022 = 6
    const val MIN_CLOSE_INSTRUCTIONS_PER_TX = 1
}
