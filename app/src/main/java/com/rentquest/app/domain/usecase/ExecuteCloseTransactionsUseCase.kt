package com.rentquest.app.domain.usecase

import com.rentquest.app.data.rpc.SolanaRpcClient
import com.rentquest.app.data.solana.PublicKey
import com.rentquest.app.data.solana.TransactionBuilder
import com.rentquest.app.data.wallet.MwaClient
import com.rentquest.app.domain.model.CloseHistoryEntry
import com.rentquest.app.domain.model.CloseTransaction
import com.rentquest.app.domain.model.Cluster
import com.rentquest.app.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

/**
 * Result types for close transaction execution
 */
sealed class CloseTransactionResult {
    data class Progress(
        val transaction: CloseTransaction,
        val index: Int,
        val total: Int
    ) : CloseTransactionResult()
    
    data class Success(
        val transaction: CloseTransaction,
        val signature: String
    ) : CloseTransactionResult()
    
    data class Failure(
        val transaction: CloseTransaction,
        val error: String
    ) : CloseTransactionResult()
    
    data class Complete(
        val historyEntry: CloseHistoryEntry
    ) : CloseTransactionResult()
}

/**
 * Use case for executing close transactions
 * Handles signing via MWA, sending, and confirming transactions
 */
class ExecuteCloseTransactionsUseCase(
    private val rpcClient: SolanaRpcClient,
    private val mwaClient: MwaClient
) {
    
    /**
     * Executes all close transactions with progress updates
     */
    fun execute(
        transactions: List<CloseTransaction>,
        walletPubkey: String,
        authToken: String,
        cluster: Cluster
    ): Flow<CloseTransactionResult> = flow {
        val walletKey = PublicKey.fromBase58(walletPubkey)
        val completedSignatures = mutableListOf<String>()
        var totalAccountsClosed = 0
        var totalLamportsReclaimed = 0L
        
        transactions.forEachIndexed { index, transaction ->
            // Emit progress - signing
            emit(CloseTransactionResult.Progress(
                transaction = transaction.copy(status = TransactionStatus.SIGNING),
                index = index,
                total = transactions.size
            ))
            
            try {
                // Get latest blockhash
                val blockhash = rpcClient.getLatestBlockhash(cluster).getOrThrow()
                
                // Build unsigned transaction
                val txBytes = TransactionBuilder.buildCloseAccountsTransaction(
                    accounts = transaction.accounts,
                    walletPubkey = walletKey,
                    recentBlockhash = blockhash
                )
                
                // Sign and send via MWA
                emit(CloseTransactionResult.Progress(
                    transaction = transaction.copy(status = TransactionStatus.SENDING),
                    index = index,
                    total = transactions.size
                ))
                
                val signatures = mwaClient.signAndSendTransactions(
                    transactions = arrayOf(txBytes),
                    authToken = authToken,
                    cluster = cluster
                ).getOrThrow()
                
                val signature = signatures.first()
                
                // Confirm transaction
                emit(CloseTransactionResult.Progress(
                    transaction = transaction.copy(
                        status = TransactionStatus.CONFIRMING,
                        signature = signature
                    ),
                    index = index,
                    total = transactions.size
                ))
                
                val confirmed = rpcClient.confirmTransaction(signature, cluster).getOrThrow()
                
                if (confirmed) {
                    completedSignatures.add(signature)
                    totalAccountsClosed += transaction.accounts.size
                    totalLamportsReclaimed += transaction.estimatedRentLamports
                    
                    emit(CloseTransactionResult.Success(
                        transaction = transaction.copy(
                            status = TransactionStatus.CONFIRMED,
                            signature = signature
                        ),
                        signature = signature
                    ))
                } else {
                    emit(CloseTransactionResult.Failure(
                        transaction = transaction.copy(status = TransactionStatus.FAILED),
                        error = "Transaction confirmation timeout"
                    ))
                }
                
            } catch (e: Exception) {
                emit(CloseTransactionResult.Failure(
                    transaction = transaction.copy(status = TransactionStatus.FAILED),
                    error = e.message ?: "Unknown error"
                ))
                // Continue with next transaction
            }
        }
        
        // Emit final summary
        emit(CloseTransactionResult.Complete(
            historyEntry = CloseHistoryEntry(
                id = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
                accountsClosed = totalAccountsClosed,
                lamportsReclaimed = totalLamportsReclaimed,
                signatures = completedSignatures,
                cluster = cluster.name
            )
        ))
    }
}
