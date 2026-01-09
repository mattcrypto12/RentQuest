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
 * 
 * NOTE: Public RPC endpoints are rate-limited. For production,
 * replace with Helius, QuickNode, or another RPC provider.
 * Get a free Helius API key at https://helius.dev
 */
enum class Cluster(
    val displayName: String,
    val rpcUrl: String,
    val mwaCluster: String
) {
    MAINNET_BETA(
        displayName = "Mainnet-Beta",
        rpcUrl = "https://mainnet.helius-rpc.com/?api-key=823a6643-301b-494a-94be-f6ab2ca883b7",
        mwaCluster = "mainnet-beta"
    ),
    DEVNET(
        displayName = "Devnet",
        rpcUrl = "https://devnet.helius-rpc.com/?api-key=823a6643-301b-494a-94be-f6ab2ca883b7",
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
