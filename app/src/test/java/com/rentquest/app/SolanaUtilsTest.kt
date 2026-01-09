package com.rentquest.app

import com.rentquest.app.data.solana.PublicKey
import com.rentquest.app.data.solana.SolanaBase58
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for Solana utility functions
 */
class SolanaUtilsTest {
    
    // Known Base58 test vectors
    companion object {
        // Empty bytes
        val EMPTY_BYTES = byteArrayOf()
        const val EMPTY_BASE58 = ""
        
        // Single byte
        val SINGLE_BYTE = byteArrayOf(0x61)
        const val SINGLE_BASE58 = "2g"
        
        // Sample Solana public key (32 bytes)
        const val SAMPLE_PUBKEY_BASE58 = "11111111111111111111111111111111"
    }
    
    @Test
    fun `encode empty byte array returns empty string`() {
        val encoded = SolanaBase58.encode(EMPTY_BYTES)
        assertEquals("", encoded)
    }
    
    @Test
    fun `encode single byte works correctly`() {
        val encoded = SolanaBase58.encode(SINGLE_BYTE)
        assertEquals(SINGLE_BASE58, encoded)
    }
    
    @Test
    fun `decode empty string returns empty byte array`() {
        val decoded = SolanaBase58.decode("")
        assertTrue(decoded.isEmpty())
    }
    
    @Test
    fun `decode and encode roundtrip maintains data integrity`() {
        val original = "So11111111111111111111111111111111111111112"
        
        val decoded = SolanaBase58.decode(original)
        val reencoded = SolanaBase58.encode(decoded)
        
        assertEquals(original, reencoded)
    }
    
    @Test
    fun `decode system program address produces 32 zero bytes`() {
        val systemProgram = SAMPLE_PUBKEY_BASE58
        
        val decoded = SolanaBase58.decode(systemProgram)
        
        assertEquals(32, decoded.size)
        assertTrue(decoded.all { it == 0.toByte() })
    }
    
    @Test
    fun `PublicKey validates 32 byte length`() {
        val validBytes = ByteArray(32) { it.toByte() }
        val publicKey = PublicKey(validBytes)
        
        assertEquals(32, publicKey.bytes.size)
    }
    
    @Test
    fun `PublicKey toBase58 returns valid encoding`() {
        val zeroBytes = ByteArray(32) { 0 }
        val publicKey = PublicKey(zeroBytes)
        
        val base58 = publicKey.toBase58()
        
        assertEquals(SAMPLE_PUBKEY_BASE58, base58)
    }
    
    @Test
    fun `PublicKey fromBase58 parses correctly`() {
        val base58 = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        
        val publicKey = PublicKey.fromBase58(base58)
        
        assertEquals(32, publicKey.bytes.size)
        assertEquals(base58, publicKey.toBase58())
    }
    
    @Test
    fun `PublicKey equals compares byte content`() {
        val bytes = ByteArray(32) { 42 }
        val pk1 = PublicKey(bytes.copyOf())
        val pk2 = PublicKey(bytes.copyOf())
        val pk3 = PublicKey(ByteArray(32) { 0 })
        
        assertEquals(pk1, pk2)
        assertNotEquals(pk1, pk3)
    }
    
    @Test
    fun `PublicKey hashCode is consistent with equals`() {
        val bytes = ByteArray(32) { 42 }
        val pk1 = PublicKey(bytes.copyOf())
        val pk2 = PublicKey(bytes.copyOf())
        
        assertEquals(pk1.hashCode(), pk2.hashCode())
    }
    
    @Test
    fun `Base58 alphabet does not contain confusing characters`() {
        val alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz"
        
        assertFalse('0' in alphabet) // Zero
        assertFalse('O' in alphabet) // Capital O
        assertFalse('I' in alphabet) // Capital I
        assertFalse('l' in alphabet) // Lowercase L
    }
    
    @Test
    fun `decode invalid Base58 character throws exception`() {
        assertThrows(io.github.novacrypto.base58.BadCharacterException::class.java) {
            SolanaBase58.decode("Invalid0Character") // Contains '0'
        }
    }
    
    @Test
    fun `encode handles leading zero bytes correctly`() {
        val bytesWithLeadingZeros = byteArrayOf(0, 0, 0, 1, 2, 3)
        
        val encoded = SolanaBase58.encode(bytesWithLeadingZeros)
        val decoded = SolanaBase58.decode(encoded)
        
        assertArrayEquals(bytesWithLeadingZeros, decoded)
    }
}
