package com.rentquest.app

import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.domain.model.TokenProgram
import com.rentquest.app.domain.usecase.BatchCloseAccountsUseCase
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for batching logic
 */
class BatchCloseAccountsUseCaseTest {
    
    private val batchUseCase = BatchCloseAccountsUseCase()
    
    @Test
    fun `execute creates single batch for small account list`() {
        val accounts = createAccounts(5)
        
        val batches = batchUseCase.execute(accounts)
        
        assertEquals(1, batches.size)
        assertEquals(5, batches[0].accounts.size)
    }
    
    @Test
    fun `execute creates multiple batches for large account list`() {
        val accounts = createAccounts(20)
        
        val batches = batchUseCase.execute(accounts)
        
        assertEquals(3, batches.size) // 8 + 8 + 4
        assertEquals(8, batches[0].accounts.size)
        assertEquals(8, batches[1].accounts.size)
        assertEquals(4, batches[2].accounts.size)
    }
    
    @Test
    fun `execute respects custom batch size`() {
        val accounts = createAccounts(10)
        
        val batches = batchUseCase.execute(accounts, maxPerTransaction = 4)
        
        assertEquals(3, batches.size) // 4 + 4 + 2
        assertEquals(4, batches[0].accounts.size)
        assertEquals(4, batches[1].accounts.size)
        assertEquals(2, batches[2].accounts.size)
    }
    
    @Test
    fun `execute returns empty list for empty input`() {
        val batches = batchUseCase.execute(emptyList())
        
        assertTrue(batches.isEmpty())
    }
    
    @Test
    fun `execute calculates correct rent totals per batch`() {
        val accounts = createAccounts(5)
        val expectedRent = 5 * TokenAccount.DEFAULT_RENT_LAMPORTS
        
        val batches = batchUseCase.execute(accounts)
        
        assertEquals(expectedRent, batches[0].estimatedRentLamports)
    }
    
    @Test
    fun `execute generates unique IDs for each batch`() {
        val accounts = createAccounts(20)
        
        val batches = batchUseCase.execute(accounts)
        
        val uniqueIds = batches.map { it.id }.toSet()
        assertEquals(batches.size, uniqueIds.size)
    }
    
    @Test
    fun `calculateOptimalBatchSize returns 8 for standard token accounts`() {
        val accounts = createAccounts(5, TokenProgram.TOKEN_PROGRAM_ID)
        
        val batchSize = BatchCloseAccountsUseCase.calculateOptimalBatchSize(accounts)
        
        assertEquals(8, batchSize)
    }
    
    @Test
    fun `calculateOptimalBatchSize returns 6 for Token-2022 accounts`() {
        val accounts = createAccounts(5, TokenProgram.TOKEN_2022_PROGRAM_ID)
        
        val batchSize = BatchCloseAccountsUseCase.calculateOptimalBatchSize(accounts)
        
        assertEquals(6, batchSize)
    }
    
    @Test
    fun `calculateOptimalBatchSize returns 6 for mixed accounts`() {
        val standardAccounts = createAccounts(3, TokenProgram.TOKEN_PROGRAM_ID)
        val token2022Accounts = createAccounts(2, TokenProgram.TOKEN_2022_PROGRAM_ID)
        val mixedAccounts = standardAccounts + token2022Accounts
        
        val batchSize = BatchCloseAccountsUseCase.calculateOptimalBatchSize(mixedAccounts)
        
        assertEquals(6, batchSize) // Conservative for Token-2022
    }
    
    private fun createAccounts(count: Int, programId: String = TokenProgram.TOKEN_PROGRAM_ID): List<TokenAccount> {
        return (1..count).map { index ->
            TokenAccount(
                address = "Address$index" + "1".repeat(32),
                mint = "Mint$index" + "1".repeat(36),
                owner = "Owner111111111111111111111111111111111111",
                amount = 0,
                decimals = 9,
                programId = programId,
                closeAuthority = null,
                rentLamports = TokenAccount.DEFAULT_RENT_LAMPORTS
            )
        }
    }
}
