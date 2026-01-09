package com.rentquest.app.domain.usecase

import com.rentquest.app.data.rpc.SolanaRpcClient
import com.rentquest.app.domain.model.Cluster
import com.rentquest.app.domain.model.ScanResult
import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.domain.model.TokenProgram

/**
 * Use case for scanning wallet for closable token accounts
 */
class ScanTokenAccountsUseCase(
    private val rpcClient: SolanaRpcClient
) {
    
    /**
     * Scans the wallet for token accounts that can be closed
     */
    suspend fun execute(
        walletPubkey: String,
        cluster: Cluster
    ): Result<ScanResult> = runCatching {
        // Fetch all token accounts
        val allAccounts = rpcClient.getTokenAccountsByOwner(walletPubkey, cluster)
            .getOrThrow()
        
        // Filter to closable accounts only
        val closableAccounts = allAccounts.filter { account ->
            isClosable(account, walletPubkey)
        }
        
        // Calculate total reclaimable rent
        val totalReclaimable = closableAccounts.sumOf { it.rentLamports }
        
        ScanResult(
            closableAccounts = closableAccounts,
            totalReclaimableLamports = totalReclaimable,
            totalAccountsScanned = allAccounts.size
        )
    }
    
    companion object {
        /**
         * Determines if a token account is safe to close
         * 
         * CRITICAL SAFETY LOGIC:
         * 1. Account MUST have zero balance (NEVER close accounts with tokens)
         * 2. Account MUST be owned by a known token program
         * 3. Wallet MUST be the owner OR the closeAuthority
         */
        fun isClosable(account: TokenAccount, walletPubkey: String): Boolean {
            // RULE 1: Must have zero balance - this is the PRIMARY safety check
            // NEVER close an account that contains tokens
            if (account.amount != 0L) {
                return false
            }
            
            // RULE 2: Must be a known token program
            if (!TokenProgram.isTokenProgram(account.programId)) {
                return false
            }
            
            // RULE 3: Check authority
            // If closeAuthority is set, it must be the wallet
            // Otherwise, owner must be the wallet
            val closeAuthority = account.closeAuthority
            return if (closeAuthority != null) {
                closeAuthority == walletPubkey
            } else {
                account.owner == walletPubkey
            }
        }
    }
}
