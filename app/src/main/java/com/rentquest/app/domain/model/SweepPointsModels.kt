package com.rentquest.app.domain.model

import kotlinx.serialization.Serializable

/**
 * SWEEP (Solana Wallet Empty Entry Points) earned per wallet address
 * Points track accounts closed - 1 point per account, no cap
 * Used for potential future airdrop eligibility
 */
@Serializable
data class SweepPoints(
    val walletAddress: String,
    val points: Int,
    val accountsSwept: Int,
    val twitterShareCount: Int = 0,
    val twitterBonusEarned: Int = 0,
    val sharedHistoryIds: Set<String> = emptySet(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    companion object {
        const val POINTS_PER_ACCOUNT = 1
        
        /**
         * SWEEP = Solana Wallet Empty Entry Points
         * Earn 1 point for each empty token account you close
         */
        const val SWEEP_MEANING = "Solana Wallet Empty Entry Points"
        
        /**
         * Calculate points for accounts swept (no cap)
         */
        fun calculateNewPoints(currentPoints: Int, accountsSwept: Int): Int {
            val earnedPoints = accountsSwept * POINTS_PER_ACCOUNT
            return currentPoints + earnedPoints
        }
    }
    
    val totalPoints: Int get() = points + twitterBonusEarned
}

/**
 * Global leaderboard entry (for future use)
 */
@Serializable
data class LeaderboardEntry(
    val walletAddress: String,
    val points: Int,
    val rank: Int
)
