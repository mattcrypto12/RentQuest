package com.rentquest.app.data.wallet

import android.net.Uri
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.ConnectionIdentity
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.Solana
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.rentquest.app.data.solana.SolanaBase58
import com.rentquest.app.domain.model.Cluster
import com.rentquest.app.domain.model.WalletSession

/**
 * Client for Mobile Wallet Adapter (MWA) operations
 * Handles wallet connection, authorization, and transaction signing
 */
class MwaClient(
    private val activityResultSender: ActivityResultSender
) {
    companion object {
        private val IDENTITY_URI = Uri.parse("https://rentquest.app")
        private val ICON_URI = Uri.parse("favicon.ico")
        private const val APP_NAME = "RentQuest"
    }
    
    private val connectionIdentity = ConnectionIdentity(
        identityUri = IDENTITY_URI,
        iconUri = ICON_URI,
        identityName = APP_NAME
    )
    
    private fun createAdapter(cluster: Cluster): MobileWalletAdapter {
        val blockchain = when (cluster) {
            Cluster.MAINNET_BETA -> Solana.Mainnet
            Cluster.DEVNET -> Solana.Devnet
        }
        return MobileWalletAdapter(connectionIdentity = connectionIdentity).apply {
            this.blockchain = blockchain
        }
    }
    
    /**
     * Connect and authorize with wallet
     */
    suspend fun connect(cluster: Cluster): Result<WalletSession> = runCatching {
        val adapter = createAdapter(cluster)
        val result = adapter.connect(activityResultSender)
        
        when (result) {
            is TransactionResult.Success -> {
                val authResult = result.authResult
                WalletSession(
                    publicKey = SolanaBase58.encode(authResult.accounts.first().publicKey),
                    authToken = authResult.authToken,
                    walletName = authResult.walletUriBase?.host ?: "Unknown Wallet"
                )
            }
            is TransactionResult.Failure -> throw Exception(result.message)
            is TransactionResult.NoWalletFound -> throw Exception(result.message)
        }
    }
    
    /**
     * Reauthorize with existing auth token
     */
    suspend fun reauthorize(
        authToken: String,
        cluster: Cluster
    ): Result<WalletSession> = runCatching {
        val adapter = createAdapter(cluster).apply {
            this.authToken = authToken
        }
        val result = adapter.connect(activityResultSender)
        
        when (result) {
            is TransactionResult.Success -> {
                val authResult = result.authResult
                WalletSession(
                    publicKey = SolanaBase58.encode(authResult.accounts.first().publicKey),
                    authToken = authResult.authToken,
                    walletName = authResult.walletUriBase?.host ?: "Unknown Wallet"
                )
            }
            is TransactionResult.Failure -> throw Exception(result.message)
            is TransactionResult.NoWalletFound -> throw Exception(result.message)
        }
    }
    
    /**
     * Sign and send transactions
     * Returns list of transaction signatures
     */
    suspend fun signAndSendTransactions(
        transactions: Array<ByteArray>,
        authToken: String,
        cluster: Cluster
    ): Result<List<String>> = runCatching {
        val adapter = createAdapter(cluster).apply {
            this.authToken = authToken
        }
        
        val result = adapter.transact(activityResultSender) { authResult ->
            signAndSendTransactions(transactions)
        }
        
        when (result) {
            is TransactionResult.Success -> {
                result.payload.signatures.map { SolanaBase58.encode(it) }
            }
            is TransactionResult.Failure -> {
                throw Exception("Transaction failed: ${result.message}")
            }
            is TransactionResult.NoWalletFound -> {
                throw Exception("No wallet found: ${result.message}")
            }
        }
    }
    
    /**
     * Sign transactions without sending (returns signed transaction bytes)
     */
    suspend fun signTransactions(
        transactions: Array<ByteArray>,
        authToken: String,
        cluster: Cluster
    ): Result<List<ByteArray>> = runCatching {
        val adapter = createAdapter(cluster).apply {
            this.authToken = authToken
        }
        
        val result = adapter.transact(activityResultSender) { authResult ->
            signTransactions(transactions)
        }
        
        when (result) {
            is TransactionResult.Success -> {
                result.payload.signedPayloads.toList()
            }
            is TransactionResult.Failure -> {
                throw Exception("Signing failed: ${result.message}")
            }
            is TransactionResult.NoWalletFound -> {
                throw Exception("No wallet found: ${result.message}")
            }
        }
    }
    
    /**
     * Disconnect and deauthorize
     */
    suspend fun disconnect(authToken: String, cluster: Cluster): Result<Unit> = runCatching {
        val adapter = createAdapter(cluster).apply {
            this.authToken = authToken
        }
        val result = adapter.disconnect(activityResultSender)
        when (result) {
            is TransactionResult.Success -> Unit
            is TransactionResult.Failure -> throw Exception(result.message)
            is TransactionResult.NoWalletFound -> throw Exception(result.message)
        }
    }
}
