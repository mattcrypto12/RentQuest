package com.rentquest.app.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rentquest.app.data.local.DataStoreManager
import com.rentquest.app.data.rpc.SolanaRpcClient
import com.rentquest.app.data.wallet.MwaClient
import com.rentquest.app.domain.model.*
import com.rentquest.app.domain.usecase.*
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Main ViewModel for the entire app
 * Manages wallet connection, scanning, closing, and settings state
 */
class MainViewModel(
    private val dataStoreManager: DataStoreManager,
    private val appContext: Context
) : ViewModel() {
    
    // ==================== Wallet State ====================
    
    private val _walletState = MutableStateFlow<WalletConnectionState>(WalletConnectionState.Disconnected)
    val walletState: StateFlow<WalletConnectionState> = _walletState.asStateFlow()
    
    private var mwaClient: MwaClient? = null
    private var currentSession: WalletSession? = null
    
    // ==================== Scan State ====================
    
    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()
    
    private val _selectedAccounts = MutableStateFlow<Set<String>>(emptySet())
    val selectedAccounts: StateFlow<Set<String>> = _selectedAccounts.asStateFlow()
    
    // ==================== Close State ====================
    
    private val _closeState = MutableStateFlow<CloseOperationState>(CloseOperationState.Idle)
    val closeState: StateFlow<CloseOperationState> = _closeState.asStateFlow()
    
    // ==================== Settings ====================
    
    private val _cluster = MutableStateFlow(Cluster.MAINNET_BETA)
    val cluster: StateFlow<Cluster> = _cluster.asStateFlow()
    
    private val _customRpcUrl = MutableStateFlow<String?>(null)
    val customRpcUrl: StateFlow<String?> = _customRpcUrl.asStateFlow()
    
    private val _useCustomRpc = MutableStateFlow(false)
    val useCustomRpc: StateFlow<Boolean> = _useCustomRpc.asStateFlow()
    
    // ==================== User Data ====================
    
    val history: StateFlow<List<CloseHistoryEntry>> = dataStoreManager.historyFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    
    val userStats: StateFlow<UserStats> = dataStoreManager.userStatsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserStats())
    
    val onboardingComplete: StateFlow<Boolean> = dataStoreManager.onboardingCompleteFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    // ==================== SWEEP Points ====================
    
    private val _currentWalletSweepPoints = MutableStateFlow<SweepPoints?>(null)
    val currentWalletSweepPoints: StateFlow<SweepPoints?> = _currentWalletSweepPoints.asStateFlow()
    
    private val _pointsEarnedThisSession = MutableStateFlow(0)
    val pointsEarnedThisSession: StateFlow<Int> = _pointsEarnedThisSession.asStateFlow()
    
    private val _showPointsAnimation = MutableStateFlow<Int?>(null)
    val showPointsAnimation: StateFlow<Int?> = _showPointsAnimation.asStateFlow()
    
    private val _totalWalletsWithRent = MutableStateFlow(0)
    val totalWalletsWithRent: StateFlow<Int> = _totalWalletsWithRent.asStateFlow()
    
    fun loadTotalWallets() {
        viewModelScope.launch {
            val allWallets = dataStoreManager.getAllWalletsForAirdrop()
            _totalWalletsWithRent.value = allWallets.count { it.accountsSwept > 0 }
        }
    }
    
    // ==================== UI Events ====================
    
    private val _showLootAnimation = MutableStateFlow<Double?>(null)
    val showLootAnimation: StateFlow<Double?> = _showLootAnimation.asStateFlow()
    
    private val _pendingAchievements = MutableStateFlow<List<Achievement>>(emptyList())
    val pendingAchievements: StateFlow<List<Achievement>> = _pendingAchievements.asStateFlow()
    
    // ==================== Use Cases ====================
    
    private val rpcClient: SolanaRpcClient
        get() {
            val customUrl = if (_useCustomRpc.value) _customRpcUrl.value else null
            return SolanaRpcClient(appContext, customUrl)
        }
    
    private val scanUseCase: ScanTokenAccountsUseCase
        get() = ScanTokenAccountsUseCase(rpcClient)
    
    private val batchUseCase = BatchCloseAccountsUseCase()
    
    // ==================== Init ====================
    
    init {
        // Load persisted settings
        viewModelScope.launch {
            dataStoreManager.clusterFlow.collect { cluster ->
                _cluster.value = cluster
            }
        }
        
        viewModelScope.launch {
            dataStoreManager.customRpcUrlFlow.collect { url ->
                _customRpcUrl.value = url
            }
        }
        
        viewModelScope.launch {
            dataStoreManager.useCustomRpcFlow.collect { use ->
                _useCustomRpc.value = use
            }
        }
    }
    
    // ==================== Wallet Actions ====================
    
    fun connectWallet(sender: ActivityResultSender) {
        viewModelScope.launch {
            _walletState.value = WalletConnectionState.Connecting
            
            mwaClient = MwaClient(sender)
            
            mwaClient?.connect(_cluster.value)
                ?.onSuccess { session ->
                    currentSession = session
                    dataStoreManager.saveSession(session.publicKey, session.authToken)
                    _walletState.value = WalletConnectionState.Connected(session)
                    
                    // Load SWEEP points for this wallet
                    loadSweepPointsForWallet(session.publicKey)
                }
                ?.onFailure { error ->
                    _walletState.value = WalletConnectionState.Error(
                        error.message ?: "Failed to connect wallet"
                    )
                }
        }
    }
    
    private fun loadSweepPointsForWallet(walletAddress: String) {
        viewModelScope.launch {
            dataStoreManager.sweepPointsForWallet(walletAddress).collect { points ->
                _currentWalletSweepPoints.value = points
            }
        }
    }
    
    fun disconnectWallet() {
        viewModelScope.launch {
            currentSession?.let { session ->
                mwaClient?.disconnect(session.authToken, _cluster.value)
            }
            currentSession = null
            dataStoreManager.clearSession()
            _walletState.value = WalletConnectionState.Disconnected
            _scanState.value = ScanState.Idle
            _selectedAccounts.value = emptySet()
            _closeState.value = CloseOperationState.Idle
            _currentWalletSweepPoints.value = null
            _pointsEarnedThisSession.value = 0
        }
    }
    
    fun retryConnection(sender: ActivityResultSender) {
        _walletState.value = WalletConnectionState.Disconnected
        connectWallet(sender)
    }
    
    // ==================== Scan Actions ====================
    
    fun scanForClosableAccounts() {
        val session = (walletState.value as? WalletConnectionState.Connected)?.session ?: return
        
        viewModelScope.launch {
            _scanState.value = ScanState.Scanning
            
            scanUseCase.execute(session.publicKey, _cluster.value)
                .onSuccess { result ->
                    _scanState.value = ScanState.Complete(result)
                    // Auto-select all closable accounts
                    _selectedAccounts.value = result.closableAccounts.map { it.address }.toSet()
                }
                .onFailure { error ->
                    _scanState.value = ScanState.Error(error.message ?: "Failed to scan wallet")
                }
        }
    }
    
    fun toggleAccountSelection(address: String) {
        val current = _selectedAccounts.value.toMutableSet()
        if (address in current) {
            current.remove(address)
        } else {
            current.add(address)
        }
        _selectedAccounts.value = current
    }
    
    fun selectAllAccounts() {
        val result = (scanState.value as? ScanState.Complete)?.result ?: return
        _selectedAccounts.value = result.closableAccounts.map { it.address }.toSet()
    }
    
    fun clearSelection() {
        _selectedAccounts.value = emptySet()
    }
    
    // ==================== Close Actions ====================
    
    fun closeSelectedAccounts() {
        val session = (walletState.value as? WalletConnectionState.Connected)?.session ?: return
        val result = (scanState.value as? ScanState.Complete)?.result ?: return
        val selected = _selectedAccounts.value
        
        val accountsToClose = result.closableAccounts.filter { it.address in selected }
        if (accountsToClose.isEmpty()) return
        
        val transactions = batchUseCase.execute(accountsToClose)
        
        val mwa = mwaClient ?: return
        val executeUseCase = ExecuteCloseTransactionsUseCase(rpcClient, mwa)
        
        viewModelScope.launch {
            executeUseCase.execute(
                transactions = transactions,
                walletPubkey = session.publicKey,
                authToken = session.authToken,
                cluster = _cluster.value
            ).collect { result ->
                when (result) {
                    is CloseTransactionResult.Progress -> {
                        _closeState.value = CloseOperationState.InProgress(
                            currentTransaction = result.transaction,
                            currentIndex = result.index,
                            totalTransactions = result.total,
                            completedSignatures = emptyList()
                        )
                    }
                    
                    is CloseTransactionResult.Success -> {
                        val current = _closeState.value as? CloseOperationState.InProgress
                        if (current != null) {
                            _closeState.value = current.copy(
                                completedSignatures = current.completedSignatures + result.signature
                            )
                        }
                    }
                    
                    is CloseTransactionResult.Failure -> {
                        // Continue with next transaction, don't stop
                    }
                    
                    is CloseTransactionResult.Complete -> {
                        // Save to history
                        dataStoreManager.addHistoryEntry(result.historyEntry)
                        
                        // Update stats and check achievements
                        val newAchievements = dataStoreManager.updateStats(
                            accountsClosed = result.historyEntry.accountsClosed,
                            lamportsReclaimed = result.historyEntry.lamportsReclaimed
                        )
                        
                        // Award SWEEP points
                        val (updatedPoints, pointsEarned) = dataStoreManager.awardSweepPoints(
                            walletAddress = session.publicKey,
                            accountsSwept = result.historyEntry.accountsClosed
                        )
                        _currentWalletSweepPoints.value = updatedPoints
                        _pointsEarnedThisSession.value += pointsEarned
                        
                        // Show points animation if earned any
                        if (pointsEarned > 0) {
                            _showPointsAnimation.value = pointsEarned
                        }
                        
                        // Show loot animation
                        if (result.historyEntry.lamportsReclaimed > 0) {
                            _showLootAnimation.value = result.historyEntry.solReclaimed
                        }
                        
                        // Queue achievements
                        if (newAchievements.isNotEmpty()) {
                            _pendingAchievements.value = newAchievements
                        }
                        
                        _closeState.value = CloseOperationState.Complete(result.historyEntry)
                    }
                }
            }
        }
    }
    
    fun resetCloseState() {
        _closeState.value = CloseOperationState.Idle
        _scanState.value = ScanState.Idle
        _selectedAccounts.value = emptySet()
    }
    
    // ==================== UI Event Handling ====================
    
    fun dismissLootAnimation() {
        _showLootAnimation.value = null
    }
    
    fun dismissPointsAnimation() {
        _showPointsAnimation.value = null
    }
    
    fun dismissAchievement() {
        val current = _pendingAchievements.value
        if (current.isNotEmpty()) {
            _pendingAchievements.value = current.drop(1)
        }
    }
    
    // ==================== Twitter Share ====================
    
    private val _showTwitterBonusEarned = MutableStateFlow(false)
    val showTwitterBonusEarned: StateFlow<Boolean> = _showTwitterBonusEarned.asStateFlow()
    
    /**
     * Generate share text for Twitter
     */
    fun getTwitterShareText(rentCollected: Double, accountsClosed: Int): String {
        val solAmount = String.format("%.4f", rentCollected)
        return "Just swept $accountsClosed empty token accounts and collected $solAmount SOL in rent with @RentQuestApp!\n\n" +
               "+$accountsClosed SWEEP points earned\n\n" +
               "Stop leaving rent locked in dead tokens\n" +
               "https://dappstore.solanamobile.com/apps/rentquest\n\n" +
               "#Solana #SolanaSeeker #RentQuest"
    }
    
    private val _twitterBonusAmount = MutableStateFlow(0)
    val twitterBonusAmount: StateFlow<Int> = _twitterBonusAmount.asStateFlow()
    
    private val _alreadySharedThisEntry = MutableStateFlow(false)
    val alreadySharedThisEntry: StateFlow<Boolean> = _alreadySharedThisEntry.asStateFlow()
    
    /**
     * Check if a specific close operation has already been shared
     */
    fun checkIfAlreadyShared(historyId: String) {
        val session = currentSession ?: return
        viewModelScope.launch {
            _alreadySharedThisEntry.value = dataStoreManager.hasSharedHistoryEntry(session.publicKey, historyId)
        }
    }
    
    /**
     * Called when user initiates Twitter share - awards bonus points = accounts closed
     * Only awards once per historyId to prevent double-claiming
     */
    fun onTwitterShareInitiated(historyId: String, accountsClosed: Int) {
        val session = currentSession ?: return
        
        viewModelScope.launch {
            val (updatedPoints, bonusAwarded, wasAlreadyShared) = dataStoreManager.awardTwitterShareBonus(
                session.publicKey, 
                historyId, 
                accountsClosed
            )
            _currentWalletSweepPoints.value = updatedPoints
            
            if (wasAlreadyShared) {
                // Already shared this close operation - no bonus
                _alreadySharedThisEntry.value = true
            } else if (bonusAwarded > 0) {
                _twitterBonusAmount.value = bonusAwarded
                _showTwitterBonusEarned.value = true
                _alreadySharedThisEntry.value = true
            }
        }
    }
    
    fun dismissTwitterBonusEarned() {
        _showTwitterBonusEarned.value = false
    }
    
    fun resetShareState() {
        _alreadySharedThisEntry.value = false
    }

    // ==================== Settings Actions ====================
    
    fun setCluster(cluster: Cluster) {
        viewModelScope.launch {
            _cluster.value = cluster
            dataStoreManager.setCluster(cluster)
            
            // If connected, disconnect (cluster change requires reconnection)
            if (walletState.value is WalletConnectionState.Connected) {
                disconnectWallet()
            }
        }
    }
    
    fun setCustomRpcUrl(url: String?) {
        viewModelScope.launch {
            _customRpcUrl.value = url
            dataStoreManager.setCustomRpcUrl(url)
        }
    }
    
    fun setUseCustomRpc(use: Boolean) {
        viewModelScope.launch {
            _useCustomRpc.value = use
            dataStoreManager.setUseCustomRpc(use)
        }
    }
    
    fun completeOnboarding() {
        viewModelScope.launch {
            dataStoreManager.setOnboardingComplete()
        }
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            dataStoreManager.clearAllData()
            disconnectWallet()
        }
    }
    
    // ==================== Computed Properties ====================
    
    val selectedAccountsCount: Int
        get() = _selectedAccounts.value.size
    
    val selectedTotalRent: Long
        get() {
            val result = (scanState.value as? ScanState.Complete)?.result ?: return 0
            return result.closableAccounts
                .filter { it.address in _selectedAccounts.value }
                .sumOf { it.rentLamports }
        }
    
    val selectedTotalRentSol: Double
        get() = selectedTotalRent / 1_000_000_000.0
}

/**
 * Factory for creating MainViewModel with dependencies
 */
class MainViewModelFactory(
    private val dataStoreManager: DataStoreManager,
    private val appContext: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(dataStoreManager, appContext) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
