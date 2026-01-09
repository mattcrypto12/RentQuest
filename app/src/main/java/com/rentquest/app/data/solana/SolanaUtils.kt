package com.rentquest.app.data.solana

import io.github.novacrypto.base58.Base58

/**
 * Base58 encoding/decoding utilities for Solana
 */
object SolanaBase58 {
    
    fun encode(bytes: ByteArray): String {
        return Base58.base58Encode(bytes)
    }
    
    fun decode(value: String): ByteArray {
        return Base58.base58Decode(value)
    }
}

/**
 * Represents a Solana public key (32 bytes)
 */
class PublicKey(val bytes: ByteArray) {
    
    init {
        require(bytes.size == SIZE) { "Public key must be $SIZE bytes" }
    }
    
    fun toBase58(): String = SolanaBase58.encode(bytes)
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublicKey) return false
        return bytes.contentEquals(other.bytes)
    }
    
    override fun hashCode(): Int = bytes.contentHashCode()
    
    override fun toString(): String = toBase58()
    
    companion object {
        const val SIZE = 32
        
        fun fromBase58(base58: String): PublicKey {
            return PublicKey(SolanaBase58.decode(base58))
        }
    }
}

/**
 * Account metadata for transaction building
 */
data class AccountMeta(
    val publicKey: PublicKey,
    val isSigner: Boolean,
    val isWritable: Boolean
)

/**
 * Instruction for transaction building
 */
data class Instruction(
    val programId: PublicKey,
    val accounts: List<AccountMeta>,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Instruction) return false
        return programId == other.programId &&
               accounts == other.accounts &&
               data.contentEquals(other.data)
    }
    
    override fun hashCode(): Int {
        var result = programId.hashCode()
        result = 31 * result + accounts.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
