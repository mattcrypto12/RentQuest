package com.rentquest.app

import com.rentquest.app.data.solana.PublicKey
import com.rentquest.app.data.solana.TransactionBuilder
import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.domain.model.TokenProgram
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for transaction building logic
 */
class TransactionBuilderTest {
    
    @Test
    fun `createCloseAccountInstruction has correct program ID for standard token`() {
        val accountPubkey = createTestPubkey(1)
        val destinationPubkey = createTestPubkey(2)
        val ownerPubkey = createTestPubkey(3)
        val programId = PublicKey.fromBase58(TokenProgram.TOKEN_PROGRAM_ID)
        
        val instruction = TransactionBuilder.createCloseAccountInstruction(
            accountToClose = accountPubkey,
            destination = destinationPubkey,
            authority = ownerPubkey,
            programId = programId
        )
        
        assertEquals(TokenProgram.TOKEN_PROGRAM_ID, instruction.programId.toBase58())
    }
    
    @Test
    fun `createCloseAccountInstruction has correct program ID for Token-2022`() {
        val accountPubkey = createTestPubkey(1)
        val destinationPubkey = createTestPubkey(2)
        val ownerPubkey = createTestPubkey(3)
        val programId = PublicKey.fromBase58(TokenProgram.TOKEN_2022_PROGRAM_ID)
        
        val instruction = TransactionBuilder.createCloseAccountInstruction(
            accountToClose = accountPubkey,
            destination = destinationPubkey,
            authority = ownerPubkey,
            programId = programId
        )
        
        assertEquals(TokenProgram.TOKEN_2022_PROGRAM_ID, instruction.programId.toBase58())
    }
    
    @Test
    fun `createCloseAccountInstruction has correct accounts order`() {
        val accountPubkey = createTestPubkey(1)
        val destinationPubkey = createTestPubkey(2)
        val ownerPubkey = createTestPubkey(3)
        val programId = PublicKey.fromBase58(TokenProgram.TOKEN_PROGRAM_ID)
        
        val instruction = TransactionBuilder.createCloseAccountInstruction(
            accountToClose = accountPubkey,
            destination = destinationPubkey,
            authority = ownerPubkey,
            programId = programId
        )
        
        assertEquals(3, instruction.accounts.size)
        assertEquals(accountPubkey, instruction.accounts[0].publicKey)
        assertEquals(destinationPubkey, instruction.accounts[1].publicKey)
        assertEquals(ownerPubkey, instruction.accounts[2].publicKey)
    }
    
    @Test
    fun `createCloseAccountInstruction has correct account metadata`() {
        val accountPubkey = createTestPubkey(1)
        val destinationPubkey = createTestPubkey(2)
        val ownerPubkey = createTestPubkey(3)
        val programId = PublicKey.fromBase58(TokenProgram.TOKEN_PROGRAM_ID)
        
        val instruction = TransactionBuilder.createCloseAccountInstruction(
            accountToClose = accountPubkey,
            destination = destinationPubkey,
            authority = ownerPubkey,
            programId = programId
        )
        
        // Account being closed: writable, not signer
        assertTrue(instruction.accounts[0].isWritable)
        assertFalse(instruction.accounts[0].isSigner)
        
        // Destination: writable, not signer
        assertTrue(instruction.accounts[1].isWritable)
        assertFalse(instruction.accounts[1].isSigner)
        
        // Owner: not writable, is signer
        assertFalse(instruction.accounts[2].isWritable)
        assertTrue(instruction.accounts[2].isSigner)
    }
    
    @Test
    fun `createCloseAccountInstruction data contains close instruction opcode`() {
        val programId = PublicKey.fromBase58(TokenProgram.TOKEN_PROGRAM_ID)
        val instruction = TransactionBuilder.createCloseAccountInstruction(
            accountToClose = createTestPubkey(1),
            destination = createTestPubkey(2),
            authority = createTestPubkey(3),
            programId = programId
        )
        
        // CloseAccount instruction opcode is 9
        assertEquals(1, instruction.data.size)
        assertEquals(9.toByte(), instruction.data[0])
    }
    
    @Test
    fun `buildCloseAccountsTransaction creates valid message structure`() {
        val ownerPubkey = PublicKey.fromBase58(TEST_WALLET)
        val accounts = listOf(
            createTestTokenAccount(TEST_ACCOUNT_1, TokenProgram.TOKEN_PROGRAM_ID),
            createTestTokenAccount(TEST_ACCOUNT_2, TokenProgram.TOKEN_PROGRAM_ID)
        )
        val recentBlockhash = "4vJ9JU1bJJE96FWSJKvHsmmFADCg4gpZQff4P3bkLKi"
        
        val messageBytes = TransactionBuilder.buildCloseAccountsTransaction(
            accounts = accounts,
            walletPubkey = ownerPubkey,
            recentBlockhash = recentBlockhash
        )
        
        // Should produce non-empty serialized message
        assertTrue(messageBytes.isNotEmpty())
        
        // First byte should be number of required signatures (1 for owner)
        assertEquals(1.toByte(), messageBytes[0])
    }
    
    @Test
    fun `buildCloseAccountsTransaction handles empty account list`() {
        val messageBytes = TransactionBuilder.buildCloseAccountsTransaction(
            accounts = emptyList(),
            walletPubkey = PublicKey.fromBase58(TEST_WALLET),
            recentBlockhash = "4vJ9JU1bJJE96FWSJKvHsmmFADCg4gpZQff4P3bkLKi"
        )
        
        // Should still produce valid (though useless) message
        assertTrue(messageBytes.isNotEmpty())
    }
    
    @Test
    fun `buildCloseAccountsTransaction creates instruction for each account`() {
        val numAccounts = 5
        // Generate valid Base58 pubkeys for test accounts
        val testAddresses = listOf(
            "7YttLkHDoN5LN3wJV7VxEAZ6V5vv7xZKZXoR7Pp9v4Nv",
            "CuieVDEDtLo7FypA9SbLM9saXFdb1dsshEkyErMqkRQq",
            "5ZWj7a1f8tWkjBESHKgrLmXshuXxqeY9SYcfbshpAqPG",
            "7Np41oeYqPefeNQEHSv1UDhYrehxin3NStELsSKCT4K2",
            "GcGvnFwXegVJfDBCCwVGUq7WyXqYGDAKwBcvMZX6EHnX"
        )
        val accounts = testAddresses.map { 
            createTestTokenAccount(it, TokenProgram.TOKEN_PROGRAM_ID)
        }
        val ownerPubkey = PublicKey.fromBase58(TEST_WALLET)
        
        val messageBytes = TransactionBuilder.buildCloseAccountsTransaction(
            accounts = accounts,
            walletPubkey = ownerPubkey,
            recentBlockhash = "4vJ9JU1bJJE96FWSJKvHsmmFADCg4gpZQff4P3bkLKi"
        )
        
        // Message should be large enough to contain all instructions
        // Each close instruction adds minimal overhead in compiled format
        assertTrue(messageBytes.size > 32 + 32 + numAccounts) // blockhash + pubkeys + instructions
    }
    
    // Valid Base58 test addresses (32-byte public keys)
    companion object {
        const val TEST_WALLET = "11111111111111111111111111111111"
        const val TEST_ACCOUNT_1 = "TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"
        const val TEST_ACCOUNT_2 = "ATokenGPvbdGVxr1b2hvZbsiqW5xWH25efTNsLJA8knL"
    }
    
    private fun createTestPubkey(seed: Int): PublicKey {
        val bytes = ByteArray(32) { (seed + it).toByte() }
        return PublicKey(bytes)
    }
    
    private fun createTestTokenAccount(address: String, programId: String): TokenAccount {
        return TokenAccount(
            address = address,
            mint = "TestMint111111111111111111111111111111111111",
            owner = "TestOwner11111111111111111111111111111111111",
            amount = 0L,
            decimals = 9,
            programId = programId,
            closeAuthority = null,
            rentLamports = 2039280L
        )
    }
}
