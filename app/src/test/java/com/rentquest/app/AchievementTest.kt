package com.rentquest.app

import com.rentquest.app.domain.model.Achievement
import org.junit.Assert.*
import org.junit.Test

/**
 * Tests for achievement unlock logic
 */
class AchievementTest {
    
    @Test
    fun `FIRST_SWEEP unlocks after first account closed`() {
        val unlocked = Achievement.getNewlyUnlocked(
            previousTotal = 0,
            newTotal = 1,
            alreadyUnlocked = emptySet()
        )
        
        assertTrue(Achievement.FIRST_SWEEP in unlocked)
    }
    
    @Test
    fun `FIRST_SWEEP does not unlock with zero accounts`() {
        val unlocked = Achievement.getNewlyUnlocked(
            previousTotal = 0,
            newTotal = 0,
            alreadyUnlocked = emptySet()
        )
        
        assertFalse(Achievement.FIRST_SWEEP in unlocked)
    }
    
    @Test
    fun `DUST_BUSTER unlocks at 10 accounts`() {
        val unlocked = Achievement.getNewlyUnlocked(
            previousTotal = 9,
            newTotal = 10,
            alreadyUnlocked = setOf(Achievement.FIRST_SWEEP)
        )
        
        assertTrue(Achievement.DUST_BUSTER in unlocked)
    }
    
    @Test
    fun `DUST_BUSTER does not unlock at 9 accounts`() {
        val unlocked = Achievement.getNewlyUnlocked(
            previousTotal = 8,
            newTotal = 9,
            alreadyUnlocked = setOf(Achievement.FIRST_SWEEP)
        )
        
        assertFalse(Achievement.DUST_BUSTER in unlocked)
    }
    
    @Test
    fun `JANITOR unlocks at 50 accounts`() {
        val unlocked = Achievement.getNewlyUnlocked(
            previousTotal = 49,
            newTotal = 50,
            alreadyUnlocked = setOf(Achievement.FIRST_SWEEP, Achievement.DUST_BUSTER)
        )
        
        assertTrue(Achievement.JANITOR in unlocked)
    }
    
    @Test
    fun `already unlocked achievements are not returned again`() {
        val unlocked = Achievement.getNewlyUnlocked(
            previousTotal = 49,
            newTotal = 50,
            alreadyUnlocked = setOf(Achievement.FIRST_SWEEP, Achievement.DUST_BUSTER, Achievement.JANITOR)
        )
        
        assertTrue(unlocked.isEmpty())
    }
    
    @Test
    fun `multiple achievements can unlock simultaneously`() {
        val unlocked = Achievement.getNewlyUnlocked(
            previousTotal = 0,
            newTotal = 50,
            alreadyUnlocked = emptySet()
        )
        
        assertEquals(3, unlocked.size)
        assertTrue(Achievement.FIRST_SWEEP in unlocked)
        assertTrue(Achievement.DUST_BUSTER in unlocked)
        assertTrue(Achievement.JANITOR in unlocked)
    }
    
    @Test
    fun `achievement display names are correct`() {
        assertEquals("First Sweep", Achievement.FIRST_SWEEP.displayName)
        assertEquals("Dust Buster", Achievement.DUST_BUSTER.displayName)
        assertEquals("Janitor", Achievement.JANITOR.displayName)
    }
    
    @Test
    fun `achievement descriptions are set`() {
        assertTrue(Achievement.FIRST_SWEEP.description.isNotBlank())
        assertTrue(Achievement.DUST_BUSTER.description.isNotBlank())
        assertTrue(Achievement.JANITOR.description.isNotBlank())
    }
    
    @Test
    fun `achievement thresholds are in correct order`() {
        assertTrue(Achievement.FIRST_SWEEP.threshold < Achievement.DUST_BUSTER.threshold)
        assertTrue(Achievement.DUST_BUSTER.threshold < Achievement.JANITOR.threshold)
    }
}
