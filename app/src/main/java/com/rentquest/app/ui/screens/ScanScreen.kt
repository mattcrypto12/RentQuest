package com.rentquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentquest.app.domain.model.ScanState
import com.rentquest.app.domain.model.WalletConnectionState
import com.rentquest.app.ui.components.*
import com.rentquest.app.ui.theme.*
import com.rentquest.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    viewModel: MainViewModel,
    onStartClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by viewModel.walletState.collectAsState()
    val scanState by viewModel.scanState.collectAsState()
    val selectedAccounts by viewModel.selectedAccounts.collectAsState()
    
    val session = (walletState as? WalletConnectionState.Connected)?.session
    
    // Auto-scan on first load
    LaunchedEffect(Unit) {
        if (scanState is ScanState.Idle && session != null) {
            viewModel.scanForClosableAccounts()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top bar with wallet info
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = "Scan Results",
                        style = MaterialTheme.typography.titleLarge
                    )
                    session?.let {
                        WalletAddressChip(address = it.publicKey)
                    }
                }
            },
            actions = {
                IconButton(onClick = { viewModel.scanForClosableAccounts() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Rescan"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = Gray50
            )
        )
        
        when (val state = scanState) {
            is ScanState.Idle -> {
                EmptyState(
                    icon = Icons.Default.Search,
                    title = "Ready to Scan",
                    description = "Tap the button below to scan your wallet for closable token accounts",
                    modifier = Modifier.weight(1f)
                ) {
                    PrimaryButton(
                        text = "Scan Wallet",
                        onClick = { viewModel.scanForClosableAccounts() },
                        modifier = Modifier.width(200.dp)
                    )
                }
            }
            
            is ScanState.Scanning -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingState(message = "Scanning your wallet...")
                }
            }
            
            is ScanState.Complete -> {
                val result = state.result
                
                if (result.closableAccounts.isEmpty()) {
                    EmptyState(
                        icon = Icons.Default.CheckCircle,
                        title = "All Clean!",
                        description = "No closable token accounts found. Your wallet is already optimized!",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Stats row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            label = "Accounts",
                            value = result.closableCount.toString(),
                            icon = Icons.Default.AccountBalance,
                            modifier = Modifier.weight(1f),
                            valueColor = Purple400
                        )
                        StatCard(
                            label = "Reclaimable",
                            value = String.format("%.4f", result.totalReclaimableSol),
                            icon = Icons.Default.Savings,
                            modifier = Modifier.weight(1f),
                            valueColor = Emerald400
                        )
                    }
                    
                    // Selection controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedAccounts.size} selected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray400
                        )
                        
                        Row {
                            TextButton(onClick = { viewModel.selectAllAccounts() }) {
                                Text("Select All", color = Purple400)
                            }
                            TextButton(onClick = { viewModel.clearSelection() }) {
                                Text("Clear", color = Gray400)
                            }
                        }
                    }
                    
                    // Account list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(result.closableAccounts) { account ->
                            TokenAccountCard(
                                account = account,
                                isSelected = account.address in selectedAccounts,
                                onToggleSelection = { viewModel.toggleAccountSelection(account.address) }
                            )
                        }
                    }
                    
                    // Bottom action bar
                    if (selectedAccounts.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = SurfaceDark,
                            tonalElevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${selectedAccounts.size} accounts",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Gray400
                                    )
                                    Text(
                                        text = String.format("%.6f SOL", viewModel.selectedTotalRentSol),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Emerald400,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                PrimaryButton(
                                    text = "Reclaim SOL",
                                    onClick = onStartClose,
                                    icon = Icons.Default.Savings
                                )
                            }
                        }
                    }
                }
            }
            
            is ScanState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.scanForClosableAccounts() },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
