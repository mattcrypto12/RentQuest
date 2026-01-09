package com.rentquest.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rentquest.app.domain.model.CloseOperationState
import com.rentquest.app.domain.model.TransactionStatus
import com.rentquest.app.ui.components.*
import com.rentquest.app.ui.theme.*
import com.rentquest.app.ui.viewmodel.MainViewModel

/**
 * Open Twitter/X with pre-filled text
 * Tries X app first, falls back to browser
 */
private fun openTwitterWithText(context: Context, text: String) {
    // Try X app directly first
    val xAppIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        setPackage("com.twitter.android")
    }
    
    try {
        context.startActivity(xAppIntent)
    } catch (e: Exception) {
        // X app not installed, open in browser
        val encodedText = java.net.URLEncoder.encode(text, "UTF-8")
        val webIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://x.com/intent/post?text=$encodedText")
        }
        context.startActivity(webIntent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloseProgressScreen(
    viewModel: MainViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val closeState by viewModel.closeState.collectAsState()
    
    // Start closing when screen opens
    LaunchedEffect(Unit) {
        if (closeState is CloseOperationState.Idle) {
            viewModel.closeSelectedAccounts()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text("Closing Accounts") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = Gray50
            )
        )
        
        when (val state = closeState) {
            is CloseOperationState.Idle -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingState(message = "Preparing transactions...")
                }
            }
            
            is CloseOperationState.InProgress -> {
                InProgressContent(state = state)
            }
            
            is CloseOperationState.Complete -> {
                CompleteContent(
                    entry = state.historyEntry,
                    viewModel = viewModel,
                    onDone = {
                        viewModel.resetCloseState()
                        onComplete()
                    }
                )
            }
            
            is CloseOperationState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.closeSelectedAccounts() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InProgressContent(state: CloseOperationState.InProgress) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        // Progress indicator
        TransactionProgressIndicator(
            currentStep = state.currentIndex + 1,
            totalSteps = state.totalTransactions,
            statusText = getStatusText(state.currentTransaction.status)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Current transaction info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Status icon with animation
                when (state.currentTransaction.status) {
                    TransactionStatus.SIGNING -> {
                        CircularProgressIndicator(
                            color = Purple500,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Approve in your wallet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray300
                        )
                    }
                    TransactionStatus.SENDING -> {
                        CircularProgressIndicator(
                            color = SolanaGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Sending to network",
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray300
                        )
                    }
                    TransactionStatus.CONFIRMING -> {
                        CircularProgressIndicator(
                            color = Emerald400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Confirming on-chain",
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray300
                        )
                    }
                    else -> {
                        CircularProgressIndicator(
                            color = Gray500,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Transaction details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Accounts in batch",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                    Text(
                        text = state.currentTransaction.accountCount.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray300,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "SOL to reclaim",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray500
                    )
                    Text(
                        text = String.format("%.6f", state.currentTransaction.estimatedRentSol),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Emerald400,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Show signature if available
                state.currentTransaction.signature?.let { sig ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Gray700)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Signature",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gray500
                    )
                    Text(
                        text = sig.take(20) + "..." + sig.takeLast(20),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray400
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Completed transactions
        if (state.completedSignatures.isNotEmpty()) {
            Text(
                text = "${state.completedSignatures.size} transaction(s) completed",
                style = MaterialTheme.typography.bodyMedium,
                color = Emerald400
            )
        }
    }
}

@Composable
private fun CompleteContent(
    entry: com.rentquest.app.domain.model.CloseHistoryEntry,
    viewModel: MainViewModel,
    onDone: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val showTwitterBonus by viewModel.showTwitterBonusEarned.collectAsState()
    val twitterBonusAmount by viewModel.twitterBonusAmount.collectAsState()
    val alreadyShared by viewModel.alreadySharedThisEntry.collectAsState()
    
    // Check if this entry was already shared on first render
    LaunchedEffect(entry.id) {
        viewModel.checkIfAlreadyShared(entry.id)
    }
    
    // Show Twitter bonus popup if earned
    if (showTwitterBonus) {
        TwitterBonusEarnedPopup(
            bonusPoints = twitterBonusAmount,
            onDismiss = { viewModel.dismissTwitterBonusEarned() }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Success icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Emerald400,
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Success!",
            style = MaterialTheme.typography.displaySmall,
            color = Gray50,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Your wallet is cleaner and lighter",
            style = MaterialTheme.typography.bodyLarge,
            color = Gray400
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Summary card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Rent Collected",
                    style = MaterialTheme.typography.labelLarge,
                    color = Gray500
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = String.format("%.6f", entry.solReclaimed),
                    style = MaterialTheme.typography.displayMedium,
                    color = Emerald400,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "SOL",
                    style = MaterialTheme.typography.titleMedium,
                    color = Emerald400
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Divider(color = Gray700)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = entry.accountsClosed.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Gray300,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Accounts",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = entry.signatures.size.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = Gray300,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Twitter share button - shows different state if already shared
        if (alreadyShared) {
            // Already shared - show disabled/shared state
            OutlinedButton(
                onClick = {
                    // Still allow sharing again but no bonus
                    val shareText = viewModel.getTwitterShareText(entry.solReclaimed, entry.accountsClosed)
                    openTwitterWithText(context, shareText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Gray600)
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Emerald400
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Shared ✓ (Share again)",
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray400
                )
            }
        } else {
            TwitterShareButton(
                onClick = {
                    // Award bonus points for sharing (points = accounts closed)
                    // Uses history ID to ensure one bonus per close operation
                    viewModel.onTwitterShareInitiated(entry.id, entry.accountsClosed)
                    
                    // Open X/Twitter app directly
                    val shareText = viewModel.getTwitterShareText(entry.solReclaimed, entry.accountsClosed)
                    openTwitterWithText(context, shareText)
                },
                accountsClosed = entry.accountsClosed
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        PrimaryButton(
            text = "Done",
            onClick = onDone,
            icon = Icons.Default.Check
        )
    }
}

private fun getStatusText(status: TransactionStatus): String {
    return when (status) {
        TransactionStatus.PENDING -> "Preparing..."
        TransactionStatus.SIGNING -> "Waiting for signature..."
        TransactionStatus.SENDING -> "Sending to network..."
        TransactionStatus.CONFIRMING -> "Confirming on-chain..."
        TransactionStatus.CONFIRMED -> "Confirmed!"
        TransactionStatus.FAILED -> "Failed"
    }
}
