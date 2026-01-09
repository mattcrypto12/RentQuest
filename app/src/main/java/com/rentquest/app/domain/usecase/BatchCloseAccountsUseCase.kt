package com.rentquest.app.domain.usecase

import com.rentquest.app.domain.model.BatchConfig
import com.rentquest.app.domain.model.CloseTransaction
import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.domain.model.TokenProgram
import com.rentquest.app.domain.model.TransactionStatus
import java.util.UUID

/**
 * Use case for batching token accounts into close transactions
 */
class BatchCloseAccountsUseCase {
    
    /**
     * Groups token accounts into batched transactions
     * 
     * @param accounts List of token accounts to close
     * @param maxPerTransaction Maximum accounts per transaction (default from config)
     * @return List of CloseTransaction objects ready for signing
     */
    fun execute(
        accounts: List<TokenAccount>,
        maxPerTransaction: Int = calculateOptimalBatchSize(accounts)
    ): List<CloseTransaction> {
        if (accounts.isEmpty()) {
            return emptyList()
        }
        
        // Chunk accounts into batches
        return accounts.chunked(maxPerTransaction).map { batch ->
            CloseTransaction(
                id = UUID.randomUUID().toString(),
                accounts = batch,
                estimatedRentLamports = batch.sumOf { it.rentLamports },
                status = TransactionStatus.PENDING,
                signature = null
            )
        }
    }
    
    companion object {
        /**
         * Calculate optimal batch size based on account types
         * Token-2022 accounts may have extensions, so we use smaller batches
         */
        fun calculateOptimalBatchSize(accounts: List<TokenAccount>): Int {
            val hasToken2022 = accounts.any { account ->
                account.programId == TokenProgram.TOKEN_2022_PROGRAM_ID
            }
            
            return if (hasToken2022) {
                BatchConfig.MAX_CLOSE_INSTRUCTIONS_TOKEN_2022
            } else {
                BatchConfig.MAX_CLOSE_INSTRUCTIONS_PER_TX
            }
        }
    }
}
