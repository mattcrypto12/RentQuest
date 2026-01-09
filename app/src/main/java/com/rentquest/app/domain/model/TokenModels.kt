package com.rentquest.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Represents a single SPL Token account
 */
@Serializable
data class TokenAccount(
    val address: String,
    val mint: String,
    val owner: String,
    val amount: Long,
    val decimals: Int,
    val programId: String,
    val closeAuthority: String? = null,
    val rentLamports: Long = DEFAULT_RENT_LAMPORTS
) {
    companion object {
        // Default rent for a token account (165 bytes at current rate)
        const val DEFAULT_RENT_LAMPORTS = 2_039_280L
    }
    
    val isZeroBalance: Boolean
        get() = amount == 0L
        
    val rentInSol: Double
        get() = rentLamports / 1_000_000_000.0
}

/**
 * Result of scanning for closable token accounts
 */
data class ScanResult(
    val closableAccounts: List<TokenAccount>,
    val totalReclaimableLamports: Long,
    val totalAccountsScanned: Int
) {
    val totalReclaimableSol: Double
        get() = totalReclaimableLamports / 1_000_000_000.0
        
    val closableCount: Int
        get() = closableAccounts.size
}

/**
 * State of the scanning operation
 */
sealed class ScanState {
    data object Idle : ScanState()
    data object Scanning : ScanState()
    data class Complete(val result: ScanResult) : ScanState()
    data class Error(val message: String) : ScanState()
}

/**
 * Known token program IDs
 */
object TokenProgram {
    const val TOKEN_PROGRAM_ID = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
    const val TOKEN_2022_PROGRAM_ID = "TokenzQdBNbLqP5VEhdkAS6EPFLC1PHnBqCXEpPxuEb"
    
    fun isTokenProgram(programId: String): Boolean {
        return programId == TOKEN_PROGRAM_ID || programId == TOKEN_2022_PROGRAM_ID
    }
}
