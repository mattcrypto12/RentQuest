package com.rentquest.app.data.rpc

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.rentquest.app.data.solana.SolanaBase58
import com.rentquest.app.domain.model.Cluster
import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.domain.model.TokenProgram
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory

/**
 * Client for Solana JSON-RPC API
 */
class SolanaRpcClient(
    context: Context? = null,
    private val customRpcUrl: String? = null
) {
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }
    
    private val httpClient: OkHttpClient
    private val appContext: Context? = context
    
    init {
        val builder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
        
        // Find and bind to WiFi network specifically
        if (context != null) {
            try {
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                
                android.util.Log.e("SolanaRpcClient", "=== NETWORK DEBUG ===")
                
                // Try to find WiFi network specifically (wlan0, not tun*)
                var wifiNetwork: android.net.Network? = null
                for (network in cm.allNetworks) {
                    val caps = cm.getNetworkCapabilities(network)
                    val linkProps = cm.getLinkProperties(network)
                    val interfaceName = linkProps?.interfaceName ?: ""
                    val isRealWifi = interfaceName.startsWith("wlan")
                    val hasInternet = caps?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                    
                    android.util.Log.e("SolanaRpcClient", "Network: $network, interface: $interfaceName, isRealWifi: $isRealWifi, Internet: $hasInternet")
                    
                    // Prefer wlan0 over tun* interfaces
                    if (isRealWifi && hasInternet) {
                        wifiNetwork = network
                        android.util.Log.e("SolanaRpcClient", "Found real WiFi network: $network ($interfaceName)")
                        break  // Use first real WiFi found
                    }
                }
                
                val networkToBind = wifiNetwork ?: cm.activeNetwork
                
                if (networkToBind != null) {
                    val linkProps = cm.getLinkProperties(networkToBind)
                    android.util.Log.e("SolanaRpcClient", "Binding to network: $networkToBind")
                    android.util.Log.e("SolanaRpcClient", "Interface: ${linkProps?.interfaceName}")
                    android.util.Log.e("SolanaRpcClient", "Link addresses: ${linkProps?.linkAddresses}")
                    android.util.Log.e("SolanaRpcClient", "DNS servers: ${linkProps?.dnsServers}")
                    
                    val socketFactory = networkToBind.getSocketFactory()
                    builder.socketFactory(socketFactory)
                } else {
                    android.util.Log.e("SolanaRpcClient", "No network found!")
                }
            } catch (e: Exception) {
                android.util.Log.e("SolanaRpcClient", "Failed to setup network: ${e.message}", e)
            }
        } else {
            android.util.Log.e("SolanaRpcClient", "No context provided - using default network")
        }
        
        httpClient = builder.build()
    }
    
    private fun getRpcUrl(cluster: Cluster): String {
        return customRpcUrl ?: cluster.rpcUrl
    }
    
    /**
     * Get all token accounts owned by a wallet
     */
    suspend fun getTokenAccountsByOwner(
        ownerPubkey: String,
        cluster: Cluster
    ): Result<List<TokenAccount>> = withContext(Dispatchers.IO) {
        runCatching {
            val accounts = mutableListOf<TokenAccount>()
            
            // Fetch Token Program accounts
            val tokenAccounts = fetchTokenAccounts(
                ownerPubkey, 
                TokenProgram.TOKEN_PROGRAM_ID, 
                cluster
            )
            accounts.addAll(tokenAccounts)
            
            // Fetch Token-2022 accounts
            val token2022Accounts = fetchTokenAccounts(
                ownerPubkey,
                TokenProgram.TOKEN_2022_PROGRAM_ID,
                cluster
            )
            accounts.addAll(token2022Accounts)
            
            accounts
        }
    }
    
    private suspend fun fetchTokenAccounts(
        owner: String,
        programId: String,
        cluster: Cluster
    ): List<TokenAccount> {
        val params = buildJsonArray {
            add(owner)
            addJsonObject {
                put("programId", programId)
            }
            addJsonObject {
                put("encoding", "jsonParsed")
            }
        }
        
        val request = JsonRpcRequest(
            method = "getTokenAccountsByOwner",
            params = params
        )
        
        val responseBody = executeRequest(getRpcUrl(cluster), request)
        val response = json.decodeFromString<JsonRpcResponse<TokenAccountsResponse>>(responseBody)
        
        if (response.error != null) {
            throw Exception("RPC Error: ${response.error.message}")
        }
        
        return response.result?.value?.map { accountInfo ->
            val parsed = accountInfo.account.data.parsed.info
            TokenAccount(
                address = accountInfo.pubkey,
                mint = parsed.mint,
                owner = parsed.owner,
                amount = parsed.tokenAmount.amount.toLongOrNull() ?: 0L,
                decimals = parsed.tokenAmount.decimals,
                programId = programId,
                closeAuthority = parsed.closeAuthority,
                rentLamports = accountInfo.account.lamports
            )
        } ?: emptyList()
    }
    
    /**
     * Get latest blockhash for transaction
     */
    suspend fun getLatestBlockhash(cluster: Cluster): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val params = buildJsonArray {
                addJsonObject {
                    put("commitment", "finalized")
                }
            }
            
            val request = JsonRpcRequest(
                method = "getLatestBlockhash",
                params = params
            )
            
            val responseBody = executeRequest(getRpcUrl(cluster), request)
            val response = json.decodeFromString<JsonRpcResponse<BlockhashResponse>>(responseBody)
            
            if (response.error != null) {
                throw Exception("RPC Error: ${response.error.message}")
            }
            
            response.result?.value?.blockhash 
                ?: throw Exception("No blockhash in response")
        }
    }
    
    /**
     * Send a signed transaction
     */
    suspend fun sendTransaction(
        signedTransaction: ByteArray,
        cluster: Cluster
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val base64Tx = android.util.Base64.encodeToString(
                signedTransaction, 
                android.util.Base64.NO_WRAP
            )
            
            val params = buildJsonArray {
                add(base64Tx)
                addJsonObject {
                    put("encoding", "base64")
                    put("skipPreflight", false)
                    put("preflightCommitment", "confirmed")
                }
            }
            
            val request = JsonRpcRequest(
                method = "sendTransaction",
                params = params
            )
            
            val responseBody = executeRequest(getRpcUrl(cluster), request)
            val response = json.decodeFromString<JsonRpcResponse<String>>(responseBody)
            
            if (response.error != null) {
                throw Exception("RPC Error: ${response.error.message}")
            }
            
            response.result ?: throw Exception("No signature in response")
        }
    }
    
    /**
     * Confirm a transaction (poll until confirmed or timeout)
     */
    suspend fun confirmTransaction(
        signature: String,
        cluster: Cluster,
        maxRetries: Int = 30,
        delayMs: Long = 1000
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            repeat(maxRetries) { attempt ->
                val params = buildJsonArray {
                    addJsonArray { add(signature) }
                    addJsonObject {
                        put("searchTransactionHistory", true)
                    }
                }
                
                val request = JsonRpcRequest(
                    method = "getSignatureStatuses",
                    params = params
                )
                
                val responseBody = executeRequest(getRpcUrl(cluster), request)
                val response = json.decodeFromString<JsonRpcResponse<SignatureStatusesResponse>>(responseBody)
                
                val status = response.result?.value?.firstOrNull()
                
                if (status != null) {
                    if (status.err != null) {
                        throw Exception("Transaction failed on-chain")
                    }
                    
                    val confirmationStatus = status.confirmationStatus
                    if (confirmationStatus == "confirmed" || confirmationStatus == "finalized") {
                        return@runCatching true
                    }
                }
                
                delay(delayMs)
            }
            
            throw Exception("Transaction confirmation timeout")
        }
    }
    
    private fun executeRequest(url: String, rpcRequest: JsonRpcRequest): String {
        val jsonString = json.encodeToString(rpcRequest)
        android.util.Log.e("SolanaRpcClient", "REQUEST URL: $url")
        android.util.Log.e("SolanaRpcClient", "REQUEST BODY: $jsonString")
        
        val requestBody = jsonString.toRequestBody("application/json; charset=utf-8".toMediaType())
        
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .post(requestBody)
            .build()
        
        httpClient.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: ""
            android.util.Log.e("SolanaRpcClient", "RESPONSE CODE: ${response.code}")
            android.util.Log.e("SolanaRpcClient", "RESPONSE BODY: $body")
            if (!response.isSuccessful) {
                throw Exception("HTTP ${response.code}: ${response.message}")
            }
            if (body.isEmpty()) {
                throw Exception("Empty response body")
            }
            return body
        }
    }
}
