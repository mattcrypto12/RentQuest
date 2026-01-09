package com.rentquest.app

import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.domain.model.TokenProgram
import com.rentquest.app.domain.usecase.ScanTokenAccountsUseCase
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for the critical isClosable() logic
 */
class ScanTokenAccountsUseCaseTest {
    
    private val walletPubkey = "7nYFJ88m1h7KWTfHDQnxQpnTkbKTQPh7C4ZGDR1qCj1J"
    
    @Test
    fun `isClosable returns true for zero balance account owned by wallet`() {
        val account = createAccount(
            amount = 0,
            owner = walletPubkey,
            closeAuthority = null
        )
        
        assertTrue(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    @Test
    fun `isClosable returns false for account with balance`() {
        val account = createAccount(
            amount = 1000000, // 0.001 tokens
            owner = walletPubkey,
            closeAuthority = null
        )
        
        assertFalse(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    @Test
    fun `isClosable returns false for account owned by different wallet`() {
        val account = createAccount(
            amount = 0,
            owner = "DifferentWallet11111111111111111111111111111",
            closeAuthority = null
        )
        
        assertFalse(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    @Test
    fun `isClosable returns true when closeAuthority is wallet`() {
        val account = createAccount(
            amount = 0,
            owner = "DifferentOwner11111111111111111111111111111",
            closeAuthority = walletPubkey
        )
        
        assertTrue(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    @Test
    fun `isClosable returns false when closeAuthority is different wallet`() {
        val account = createAccount(
            amount = 0,
            owner = walletPubkey,
            closeAuthority = "DifferentAuthority11111111111111111111111"
        )
        
        assertFalse(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    @Test
    fun `isClosable returns false for unknown program`() {
        val account = createAccount(
            amount = 0,
            owner = walletPubkey,
            closeAuthority = null,
            programId = "UnknownProgram111111111111111111111111111"
        )
        
        assertFalse(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    @Test
    fun `isClosable returns true for Token-2022 program`() {
        val account = createAccount(
            amount = 0,
            owner = walletPubkey,
            closeAuthority = null,
            programId = TokenProgram.TOKEN_2022_PROGRAM_ID
        )
        
        assertTrue(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    @Test
    fun `isClosable returns false for very small non-zero balance`() {
        // Even 1 lamport of tokens should NOT be closable
        val account = createAccount(
            amount = 1,
            owner = walletPubkey,
            closeAuthority = null
        )
        
        assertFalse(ScanTokenAccountsUseCase.isClosable(account, walletPubkey))
    }
    
    private fun createAccount(
        amount: Long,
        owner: String,
        closeAuthority: String?,
        programId: String = TokenProgram.TOKEN_PROGRAM_ID
    ): TokenAccount {
        return TokenAccount(
            address = "TokenAccount1111111111111111111111111111111",
            mint = "MintAddress11111111111111111111111111111111",
            owner = owner,
            amount = amount,
            decimals = 9,
            programId = programId,
            closeAuthority = closeAuthority,
            rentLamports = TokenAccount.DEFAULT_RENT_LAMPORTS
        )
    }
}
