package com.rentquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rentquest.app.domain.model.Cluster
import com.rentquest.app.domain.model.WalletConnectionState
import com.rentquest.app.ui.components.*
import com.rentquest.app.ui.theme.*
import com.rentquest.app.ui.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectScreen(
    viewModel: MainViewModel,
    activityResultSender: ActivityResultSender,
    onConnected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by viewModel.walletState.collectAsState()
    val cluster by viewModel.cluster.collectAsState()
    
    // Navigate when connected
    LaunchedEffect(walletState) {
        if (walletState is WalletConnectionState.Connected) {
            onConnected()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Logo / Title
        Text(
            text = "🧹",
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "RentQuest",
            style = MaterialTheme.typography.displaySmall,
            color = Gray50,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Wallet Spring Cleaning",
            style = MaterialTheme.typography.titleMedium,
            color = Gray400
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Connection state UI
        when (val state = walletState) {
            is WalletConnectionState.Disconnected -> {
                DisconnectedContent(
                    cluster = cluster,
                    onClusterChange = { viewModel.setCluster(it) },
                    onConnect = { viewModel.connectWallet(activityResultSender) }
                )
            }
            
            is WalletConnectionState.Connecting -> {
                LoadingState(message = "Connecting to wallet...")
            }
            
            is WalletConnectionState.Connected -> {
                // Will navigate away
                LoadingState(message = "Connected! Loading...")
            }
            
            is WalletConnectionState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.retryConnection(activityResultSender) }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Footer
        Text(
            text = "Secure connection via Mobile Wallet Adapter",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your keys never leave your wallet",
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisconnectedContent(
    cluster: Cluster,
    onClusterChange: (Cluster) -> Unit,
    onConnect: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Cluster selector
        Card(
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Network",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gray400
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Cluster.entries.forEach { clusterOption ->
                        FilterChip(
                            selected = cluster == clusterOption,
                            onClick = { onClusterChange(clusterOption) },
                            label = { Text(clusterOption.displayName) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple500,
                                selectedLabelColor = Gray50
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Connect button
        PrimaryButton(
            text = "Connect Wallet",
            onClick = onConnect,
            icon = Icons.Default.AccountBalanceWallet
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Supported wallets info
        Text(
            text = "Works with Phantom, Solflare, Ultimate, and other MWA wallets",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(64.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Connection Failed",
            style = MaterialTheme.typography.titleLarge,
            color = Gray300
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        PrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            icon = Icons.Default.Refresh
        )
    }
}
