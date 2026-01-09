package com.rentquest.app.data.rpc

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

/**
 * JSON-RPC 2.0 Request
 */
@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class JsonRpcRequest(
    @EncodeDefault val jsonrpc: String = "2.0",
    @EncodeDefault val id: Int = 1,
    val method: String,
    @EncodeDefault val params: JsonArray = JsonArray(emptyList())
)

/**
 * JSON-RPC 2.0 Response
 */
@Serializable
data class JsonRpcResponse<T>(
    val jsonrpc: String,
    val id: Int? = null,
    val result: T? = null,
    val error: JsonRpcError? = null
)

/**
 * JSON-RPC Error
 */
@Serializable
data class JsonRpcError(
    val code: Int,
    val message: String,
    val data: JsonElement? = null
)

/**
 * Response for getTokenAccountsByOwner
 */
@Serializable
data class TokenAccountsResponse(
    val context: RpcContext,
    val value: List<TokenAccountInfo>
)

@Serializable
data class RpcContext(
    val slot: Long
)

@Serializable
data class TokenAccountInfo(
    val pubkey: String,
    val account: AccountData
)

@Serializable
data class AccountData(
    val lamports: Long,
    val owner: String,
    val data: ParsedAccountData,
    val executable: Boolean,
    val rentEpoch: JsonElement? = null  // Can be UInt64 max value, ignore it
)

@Serializable
data class ParsedAccountData(
    val program: String,
    val parsed: ParsedTokenAccountInfo,
    val space: Int
)

@Serializable
data class ParsedTokenAccountInfo(
    val info: TokenAccountParsedInfo,
    val type: String
)

@Serializable
data class TokenAccountParsedInfo(
    val mint: String,
    val owner: String,
    val tokenAmount: TokenAmount,
    val closeAuthority: String? = null,
    val delegate: String? = null,
    val delegatedAmount: TokenAmount? = null,
    val state: String = "initialized"
)

@Serializable
data class TokenAmount(
    val amount: String,
    val decimals: Int,
    val uiAmount: Double?,
    val uiAmountString: String
)

/**
 * Response for getLatestBlockhash
 */
@Serializable
data class BlockhashResponse(
    val context: RpcContext,
    val value: BlockhashValue
)

@Serializable
data class BlockhashValue(
    val blockhash: String,
    val lastValidBlockHeight: Long
)

/**
 * Response for getSignatureStatuses
 */
@Serializable
data class SignatureStatusesResponse(
    val context: RpcContext,
    val value: List<SignatureStatus?>
)

@Serializable
data class SignatureStatus(
    val slot: Long,
    val confirmations: Int?,
    val err: JsonElement?,
    val confirmationStatus: String?
)
