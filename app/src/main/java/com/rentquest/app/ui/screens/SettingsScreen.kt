package com.rentquest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rentquest.app.domain.model.Cluster
import com.rentquest.app.domain.model.WalletConnectionState
import com.rentquest.app.ui.components.*
import com.rentquest.app.ui.theme.*
import com.rentquest.app.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onDisconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by viewModel.walletState.collectAsState()
    val cluster by viewModel.cluster.collectAsState()
    val customRpcUrl by viewModel.customRpcUrl.collectAsState()
    val useCustomRpc by viewModel.useCustomRpc.collectAsState()
    val sweepPoints by viewModel.currentWalletSweepPoints.collectAsState()
    
    var showClearDataDialog by remember { mutableStateOf(false) }
    var customRpcInput by remember { mutableStateOf(customRpcUrl ?: "") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text("Settings") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = Gray50
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SWEEP Points section (show when connected)
            sweepPoints?.let { points ->
                SweepPointsCard(
                    points = points.points,
                    accountsSwept = points.accountsSwept,
                    twitterBonus = points.twitterBonusEarned
                )
            }
            
            // Wallet section
            SettingsSection(title = "Wallet") {
                val session = (walletState as? WalletConnectionState.Connected)?.session
                
                if (session != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Emerald400,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = session.publicKey.take(8) + "..." + session.publicKey.takeLast(8),
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray400
                            )
                        }
                        
                        TextButton(
                            onClick = {
                                viewModel.disconnectWallet()
                                onDisconnect()
                            }
                        ) {
                            Text("Disconnect", color = ErrorRed)
                        }
                    }
                } else {
                    Text(
                        text = "Not connected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                }
            }
            
            // Network section
            SettingsSection(title = "Network") {
                Text(
                    text = "Select Cluster",
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
                            onClick = { viewModel.setCluster(clusterOption) },
                            label = { Text(clusterOption.displayName) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Purple500,
                                selectedLabelColor = Gray50
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Custom RPC toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Custom RPC Endpoint",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray300
                        )
                        Text(
                            text = "Use your own RPC for better performance",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    
                    Switch(
                        checked = useCustomRpc,
                        onCheckedChange = { viewModel.setUseCustomRpc(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Purple500,
                            checkedTrackColor = Purple700
                        )
                    )
                }
                
                // Custom RPC input
                if (useCustomRpc) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = customRpcInput,
                        onValueChange = { customRpcInput = it },
                        label = { Text("RPC URL") },
                        placeholder = { Text("https://...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Purple500,
                            unfocusedBorderColor = Gray600,
                            focusedLabelColor = Purple400,
                            unfocusedLabelColor = Gray500,
                            cursorColor = Purple500
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.setCustomRpcUrl(customRpcInput.takeIf { it.isNotBlank() }) },
                        enabled = customRpcInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple500
                        )
                    ) {
                        Text("Save RPC URL")
                    }
                }
            }
            
            // Data section
            SettingsSection(title = "Data") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear All Data",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray300
                        )
                        Text(
                            text = "Remove history, stats, and settings",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    
                    TextButton(
                        onClick = { showClearDataDialog = true }
                    ) {
                        Text("Clear", color = ErrorRed)
                    }
                }
            }
            
            // About section
            SettingsSection(title = "About") {
                SettingsRow(
                    label = "Version",
                    value = "1.0.0"
                )
                SettingsRow(
                    label = "Build",
                    value = "MVP"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "RentQuest helps you reclaim SOL locked in empty token accounts. We never access your private keys - all transactions are signed securely in your wallet app.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Clear data confirmation dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data?") },
            text = { Text("This will remove all history, stats, achievements, and settings. This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        showClearDataDialog = false
                        onDisconnect()
                    }
                ) {
                    Text("Clear", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark,
            titleContentColor = Gray50,
            textContentColor = Gray300
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = Purple400,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            content()
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray300
        )
    }
}
