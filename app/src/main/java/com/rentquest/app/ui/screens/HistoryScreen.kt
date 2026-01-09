package com.rentquest.app.ui.screens

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
import androidx.compose.ui.unit.dp
import com.rentquest.app.domain.model.Achievement
import com.rentquest.app.domain.model.CloseHistoryEntry
import com.rentquest.app.ui.components.*
import com.rentquest.app.ui.theme.*
import com.rentquest.app.ui.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val history by viewModel.history.collectAsState()
    val userStats by viewModel.userStats.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        TopAppBar(
            title = { Text("History") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = BackgroundDark,
                titleContentColor = Gray50
            )
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats summary
            item {
                StatsCard(stats = userStats)
            }
            
            // Achievements section
            item {
                AchievementsSection(stats = userStats)
            }
            
            // History header
            if (history.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray300,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            
            // History items
            if (history.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.History,
                        title = "No History Yet",
                        description = "Your closed account history will appear here"
                    )
                }
            } else {
                items(history) { entry ->
                    HistoryEntryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun StatsCard(stats: com.rentquest.app.domain.model.UserStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Lifetime Stats",
                style = MaterialTheme.typography.titleMedium,
                color = Gray300
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    value = stats.totalAccountsClosed.toString(),
                    label = "Accounts Closed",
                    icon = Icons.Default.AccountBalance,
                    color = Purple400
                )
                
                StatItem(
                    value = String.format("%.4f", stats.totalSolReclaimed),
                    label = "SOL Reclaimed",
                    icon = Icons.Default.Savings,
                    color = Emerald400
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

@Composable
private fun AchievementsSection(stats: com.rentquest.app.domain.model.UserStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                color = Gray300
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Achievement.entries.forEach { achievement ->
                    val isUnlocked = achievement in stats.unlockedAchievementSet
                    AchievementBadge(
                        achievement = achievement,
                        isUnlocked = isUnlocked
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementBadge(
    achievement: Achievement,
    isUnlocked: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isUnlocked) SurfaceVariantDark else Gray700.copy(alpha = 0.5f),
            modifier = Modifier.size(60.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isUnlocked) achievement.emoji else "🔒",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = achievement.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isUnlocked) Gray300 else Gray600
        )
    }
}

@Composable
private fun HistoryEntryCard(entry: CloseHistoryEntry) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }
    val formattedDate = remember(entry.timestamp) {
        dateFormat.format(Date(entry.timestamp))
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Emerald500.copy(alpha = 0.2f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${entry.accountsClosed} accounts closed",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray300,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
                Text(
                    text = entry.cluster,
                    style = MaterialTheme.typography.labelSmall,
                    color = Gray600
                )
            }
            
            // SOL amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "+${String.format("%.4f", entry.solReclaimed)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Emerald400,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "SOL",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
    }
}
