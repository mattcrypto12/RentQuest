package com.rentquest.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rentquest.app.domain.model.Achievement
import com.rentquest.app.domain.model.CloseHistoryEntry
import com.rentquest.app.domain.model.Cluster
import com.rentquest.app.domain.model.UserStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rentquest_settings")

/**
 * Manager for local data storage using DataStore
 */
class DataStoreManager(private val context: Context) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    companion object {
        // Settings
        private val CLUSTER_KEY = stringPreferencesKey("cluster")
        private val CUSTOM_RPC_URL_KEY = stringPreferencesKey("custom_rpc_url")
        private val USE_CUSTOM_RPC_KEY = booleanPreferencesKey("use_custom_rpc")
        
        // Session
        private val SESSION_PUBKEY_KEY = stringPreferencesKey("session_pubkey")
        private val SESSION_AUTH_TOKEN_KEY = stringPreferencesKey("session_auth_token")
        
        // Stats
        private val TOTAL_ACCOUNTS_CLOSED_KEY = intPreferencesKey("total_accounts_closed")
        private val TOTAL_LAMPORTS_RECLAIMED_KEY = longPreferencesKey("total_lamports_reclaimed")
        private val UNLOCKED_ACHIEVEMENTS_KEY = stringPreferencesKey("unlocked_achievements")
        
        // History
        private val HISTORY_JSON_KEY = stringPreferencesKey("history_json")
        
        // Onboarding
        private val ONBOARDING_COMPLETE_KEY = booleanPreferencesKey("onboarding_complete")
    }
    
    // ==================== Settings ====================
    
    val clusterFlow: Flow<Cluster> = context.dataStore.data.map { prefs ->
        val name = prefs[CLUSTER_KEY] ?: Cluster.MAINNET_BETA.name
        Cluster.fromName(name)
    }
    
    val customRpcUrlFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[CUSTOM_RPC_URL_KEY]
    }
    
    val useCustomRpcFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_CUSTOM_RPC_KEY] ?: false
    }
    
    suspend fun setCluster(cluster: Cluster) {
        context.dataStore.edit { prefs ->
            prefs[CLUSTER_KEY] = cluster.name
        }
    }
    
    suspend fun setCustomRpcUrl(url: String?) {
        context.dataStore.edit { prefs ->
            if (url != null) {
                prefs[CUSTOM_RPC_URL_KEY] = url
            } else {
                prefs.remove(CUSTOM_RPC_URL_KEY)
            }
        }
    }
    
    suspend fun setUseCustomRpc(use: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[USE_CUSTOM_RPC_KEY] = use
        }
    }
    
    // ==================== Session ====================
    
    suspend fun saveSession(pubkey: String, authToken: String) {
        context.dataStore.edit { prefs ->
            prefs[SESSION_PUBKEY_KEY] = pubkey
            prefs[SESSION_AUTH_TOKEN_KEY] = authToken
        }
    }
    
    suspend fun getSession(): Pair<String, String>? {
        val prefs = context.dataStore.data.first()
        val pubkey = prefs[SESSION_PUBKEY_KEY] ?: return null
        val authToken = prefs[SESSION_AUTH_TOKEN_KEY] ?: return null
        return pubkey to authToken
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(SESSION_PUBKEY_KEY)
            prefs.remove(SESSION_AUTH_TOKEN_KEY)
        }
    }
    
    // ==================== Stats ====================
    
    val userStatsFlow: Flow<UserStats> = context.dataStore.data.map { prefs ->
        UserStats(
            totalAccountsClosed = prefs[TOTAL_ACCOUNTS_CLOSED_KEY] ?: 0,
            totalLamportsReclaimed = prefs[TOTAL_LAMPORTS_RECLAIMED_KEY] ?: 0L,
            unlockedAchievements = parseAchievements(prefs[UNLOCKED_ACHIEVEMENTS_KEY])
        )
    }
    
    private fun parseAchievements(data: String?): List<String> {
        if (data.isNullOrBlank()) return emptyList()
        return data.split(",").filter { it.isNotBlank() }
    }
    
    suspend fun updateStats(accountsClosed: Int, lamportsReclaimed: Long): List<Achievement> {
        val currentStats = userStatsFlow.first()
        
        val newTotalClosed = currentStats.totalAccountsClosed + accountsClosed
        val newTotalLamports = currentStats.totalLamportsReclaimed + lamportsReclaimed
        
        // Check for new achievements
        val newlyUnlocked = Achievement.getNewlyUnlocked(
            previousTotal = currentStats.totalAccountsClosed,
            newTotal = newTotalClosed,
            alreadyUnlocked = currentStats.unlockedAchievementSet
        )
        
        // Save updated stats
        context.dataStore.edit { prefs ->
            prefs[TOTAL_ACCOUNTS_CLOSED_KEY] = newTotalClosed
            prefs[TOTAL_LAMPORTS_RECLAIMED_KEY] = newTotalLamports
            
            if (newlyUnlocked.isNotEmpty()) {
                val allUnlocked = currentStats.unlockedAchievements + newlyUnlocked.map { it.name }
                prefs[UNLOCKED_ACHIEVEMENTS_KEY] = allUnlocked.joinToString(",")
            }
        }
        
        return newlyUnlocked
    }
    
    // ==================== History ====================
    
    val historyFlow: Flow<List<CloseHistoryEntry>> = context.dataStore.data.map { prefs ->
        val jsonData = prefs[HISTORY_JSON_KEY] ?: "[]"
        try {
            json.decodeFromString<List<CloseHistoryEntry>>(jsonData)
        } catch (_: Exception) {
            emptyList()
        }
    }
    
    suspend fun addHistoryEntry(entry: CloseHistoryEntry) {
        context.dataStore.edit { prefs ->
            val current = prefs[HISTORY_JSON_KEY] ?: "[]"
            val history = try {
                json.decodeFromString<List<CloseHistoryEntry>>(current).toMutableList()
            } catch (_: Exception) {
                mutableListOf()
            }
            
            // Add new entry at front
            history.add(0, entry)
            
            // Keep only last 50 entries
            val trimmed = history.take(50)
            
            prefs[HISTORY_JSON_KEY] = json.encodeToString(trimmed)
        }
    }
    
    // ==================== Onboarding ====================
    
    val onboardingCompleteFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE_KEY] ?: false
    }
    
    suspend fun setOnboardingComplete() {
        context.dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETE_KEY] = true
        }
    }
    
    // ==================== Clear All ====================
    
    suspend fun clearAllData() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
