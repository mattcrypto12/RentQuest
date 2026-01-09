package com.rentquest.app.domain.model

/**
 * Represents a connected wallet session
 */
data class WalletSession(
    val publicKey: String,
    val authToken: String,
    val walletName: String = "Unknown Wallet"
)

/**
 * Solana cluster/network selection
 */
enum class Cluster(
    val displayName: String,
    val rpcUrl: String,
    val mwaCluster: String
) {
    MAINNET_BETA(
        displayName = "Mainnet-Beta",
        rpcUrl = "https://api.mainnet-beta.solana.com",
        mwaCluster = "mainnet-beta"
    ),
    DEVNET(
        displayName = "Devnet",
        rpcUrl = "https://api.devnet.solana.com",
        mwaCluster = "devnet"
    );
    
    companion object {
        fun fromName(name: String): Cluster {
            return entries.find { it.name == name } ?: MAINNET_BETA
        }
    }
}

/**
 * Wallet connection state
 */
sealed class WalletConnectionState {
    data object Disconnected : WalletConnectionState()
    data object Connecting : WalletConnectionState()
    data class Connected(val session: WalletSession) : WalletConnectionState()
    data class Error(val message: String) : WalletConnectionState()
}
