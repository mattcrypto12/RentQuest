package com.rentquest.app.data.solana

import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.domain.model.TokenProgram
import java.io.ByteArrayOutputStream

/**
 * Builds Solana transactions for closing token accounts
 */
object TransactionBuilder {
    
    // SPL Token CloseAccount instruction index
    private const val CLOSE_ACCOUNT_INSTRUCTION_INDEX: Byte = 9
    
    /**
     * Creates a CloseAccount instruction for SPL Token
     * 
     * Accounts:
     * 0. [writable] Account to close
     * 1. [writable] Destination for rent SOL
     * 2. [signer] Owner/Authority
     */
    fun createCloseAccountInstruction(
        accountToClose: PublicKey,
        destination: PublicKey,
        authority: PublicKey,
        programId: PublicKey
    ): Instruction {
        return Instruction(
            programId = programId,
            accounts = listOf(
                AccountMeta(accountToClose, isSigner = false, isWritable = true),
                AccountMeta(destination, isSigner = false, isWritable = true),
                AccountMeta(authority, isSigner = true, isWritable = false)
            ),
            data = byteArrayOf(CLOSE_ACCOUNT_INSTRUCTION_INDEX)
        )
    }
    
    /**
     * Builds an unsigned transaction message for closing multiple token accounts
     * Returns the serialized message bytes (ready for signing)
     */
    fun buildCloseAccountsTransaction(
        accounts: List<TokenAccount>,
        walletPubkey: PublicKey,
        recentBlockhash: String
    ): ByteArray {
        // Create close instructions for each account
        val instructions = accounts.map { account ->
            val programId = PublicKey.fromBase58(account.programId)
            val accountToClose = PublicKey.fromBase58(account.address)
            
            createCloseAccountInstruction(
                accountToClose = accountToClose,
                destination = walletPubkey,
                authority = walletPubkey,
                programId = programId
            )
        }
        
        // Collect all unique accounts
        val accountMetas = mutableListOf<AccountMeta>()
        
        // Add wallet as first account (signer, writable - receives rent)
        accountMetas.add(AccountMeta(walletPubkey, isSigner = true, isWritable = true))
        
        // Add program IDs and instruction accounts
        val programIds = mutableSetOf<PublicKey>()
        
        instructions.forEach { instruction ->
            programIds.add(instruction.programId)
            instruction.accounts.forEach { meta ->
                // Skip wallet, already added
                if (meta.publicKey != walletPubkey) {
                    val existing = accountMetas.find { it.publicKey == meta.publicKey }
                    if (existing == null) {
                        accountMetas.add(meta)
                    } else if (meta.isWritable && !existing.isWritable) {
                        // Upgrade to writable if needed
                        val index = accountMetas.indexOf(existing)
                        accountMetas[index] = meta
                    }
                }
            }
        }
        
        // Add program IDs (non-signer, non-writable)
        programIds.forEach { programId ->
            if (accountMetas.none { it.publicKey == programId }) {
                accountMetas.add(AccountMeta(programId, isSigner = false, isWritable = false))
            }
        }
        
        // Sort accounts: signers first, then writable, then readonly
        val sortedAccounts = accountMetas.sortedWith(
            compareByDescending<AccountMeta> { it.isSigner }
                .thenByDescending { it.isWritable }
        )
        
        // Serialize transaction message
        return serializeLegacyMessage(sortedAccounts, recentBlockhash, instructions)
    }
    
    /**
     * Serializes a legacy transaction message
     */
    private fun serializeLegacyMessage(
        accounts: List<AccountMeta>,
        recentBlockhash: String,
        instructions: List<Instruction>
    ): ByteArray {
        val output = ByteArrayOutputStream()
        
        // Header
        val numSigners = accounts.count { it.isSigner }
        val numReadonlySigners = accounts.count { it.isSigner && !it.isWritable }
        val numReadonlyUnsigned = accounts.count { !it.isSigner && !it.isWritable }
        
        output.write(numSigners)
        output.write(numReadonlySigners)
        output.write(numReadonlyUnsigned)
        
        // Account addresses
        writeCompactU16(output, accounts.size)
        accounts.forEach { meta ->
            output.write(meta.publicKey.bytes)
        }
        
        // Recent blockhash
        output.write(SolanaBase58.decode(recentBlockhash))
        
        // Instructions
        writeCompactU16(output, instructions.size)
        instructions.forEach { instruction ->
            // Program ID index
            val programIndex = accounts.indexOfFirst { it.publicKey == instruction.programId }
            output.write(programIndex)
            
            // Account indices
            writeCompactU16(output, instruction.accounts.size)
            instruction.accounts.forEach { accountMeta ->
                val accountIndex = accounts.indexOfFirst { it.publicKey == accountMeta.publicKey }
                output.write(accountIndex)
            }
            
            // Data
            writeCompactU16(output, instruction.data.size)
            output.write(instruction.data)
        }
        
        return output.toByteArray()
    }
    
    /**
     * Writes a compact-u16 encoded value
     */
    private fun writeCompactU16(output: ByteArrayOutputStream, value: Int) {
        var remaining = value
        while (true) {
            val byte = remaining and 0x7F
            remaining = remaining shr 7
            if (remaining == 0) {
                output.write(byte)
                break
            } else {
                output.write(byte or 0x80)
            }
        }
    }
}
