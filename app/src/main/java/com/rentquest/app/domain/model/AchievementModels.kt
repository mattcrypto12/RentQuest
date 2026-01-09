package com.rentquest.app.domain.model

import kotlinx.serialization.Serializable

/**
 * Achievements that can be unlocked
 */
enum class Achievement(
    val displayName: String,
    val description: String,
    val threshold: Int,
    val emoji: String
) {
    FIRST_SWEEP(
        displayName = "First Sweep",
        description = "Close your first token account",
        threshold = 1,
        emoji = "🧹"
    ),
    DUST_BUSTER(
        displayName = "Dust Buster",
        description = "Close 10 token accounts",
        threshold = 10,
        emoji = "💨"
    ),
    JANITOR(
        displayName = "Janitor",
        description = "Close 50 token accounts",
        threshold = 50,
        emoji = "🧽"
    );
    
    companion object {
        /**
         * Get newly unlocked achievements based on total closed
         */
        fun getNewlyUnlocked(
            previousTotal: Int,
            newTotal: Int,
            alreadyUnlocked: Set<Achievement>
        ): List<Achievement> {
            return entries.filter { achievement ->
                achievement !in alreadyUnlocked &&
                previousTotal < achievement.threshold &&
                newTotal >= achievement.threshold
            }
        }
    }
}

/**
 * User statistics
 */
@Serializable
data class UserStats(
    val totalAccountsClosed: Int = 0,
    val totalLamportsReclaimed: Long = 0L,
    val unlockedAchievements: List<String> = emptyList()
) {
    val totalSolReclaimed: Double
        get() = totalLamportsReclaimed / 1_000_000_000.0
        
    val unlockedAchievementSet: Set<Achievement>
        get() = unlockedAchievements.mapNotNull { name ->
            try { Achievement.valueOf(name) } catch (_: Exception) { null }
        }.toSet()
}
