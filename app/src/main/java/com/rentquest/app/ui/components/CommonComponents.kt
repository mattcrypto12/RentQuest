package com.rentquest.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rentquest.app.domain.model.TokenAccount
import com.rentquest.app.ui.theme.*

/**
 * Primary action button
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Purple500,
            contentColor = Gray50,
            disabledContainerColor = Gray700,
            disabledContentColor = Gray500
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Gray50,
                strokeWidth = 2.dp
            )
        } else {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Secondary outlined button
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (enabled) Purple500 else Gray700),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Purple400,
            disabledContentColor = Gray500
        )
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Twitter share button - prominent CTA for sharing sweep results
 */
@Composable
fun TwitterShareButton(
    onClick: () -> Unit,
    accountsClosed: Int,
    modifier: Modifier = Modifier
) {
    val buttonColor = Color(0xFF1DA1F2) // Twitter blue
    
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = buttonColor,
            contentColor = Color.White
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Share on 𝕏 (+$accountsClosed SWEEP)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Twitter bonus earned popup - shows dynamic points based on accounts closed
 */
@Composable
fun TwitterBonusEarnedPopup(
    bonusPoints: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Awesome!", color = Purple400)
            }
        },
        icon = {
            Text("🧹", style = MaterialTheme.typography.headlineLarge)
        },
        title = {
            Text(
                "+$bonusPoints SWEEP Points!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Emerald400
            )
        },
        text = {
            Text(
                "Thanks for spreading the word! You earned a bonus SWEEP point for sharing.",
                style = MaterialTheme.typography.bodyLarge,
                color = Gray300,
                textAlign = TextAlign.Center
            )
        },
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Stat display card
 */
@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    valueColor: Color = Emerald400
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = valueColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Gray400
            )
        }
    }
}

/**
 * Token account card for selection
 */
@Composable
fun TokenAccountCard(
    account: TokenAccount,
    isSelected: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onToggleSelection() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Purple700.copy(alpha = 0.3f) else SurfaceDark
        ),
        border = if (isSelected) BorderStroke(2.dp, Purple500) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelection() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Purple500,
                    uncheckedColor = Gray500
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Account info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.address.take(8) + "..." + account.address.takeLast(8),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray300,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Mint: ${account.mint.take(6)}...${account.mint.takeLast(4)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            
            // Rent amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format("%.4f", account.rentInSol),
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

/**
 * Empty state display
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gray500,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Gray300,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray500,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

/**
 * Loading state display
 */
@Composable
fun LoadingState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = Purple500,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray300
        )
    }
}

/**
 * Error state display
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            tint = ErrorRed,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Something went wrong",
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
        SecondaryButton(
            text = "Try Again",
            onClick = onRetry,
            icon = Icons.Default.Refresh,
            modifier = Modifier.width(200.dp)
        )
    }
}

/**
 * Wallet address chip
 */
@Composable
fun WalletAddressChip(
    address: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceVariantDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Emerald500)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = address.take(4) + "..." + address.takeLast(4),
                style = MaterialTheme.typography.bodyMedium,
                color = Gray300,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Progress indicator with status
 */
@Composable
fun TransactionProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    statusText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = currentStep.toFloat() / totalSteps.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Purple500,
            trackColor = Gray700
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$currentStep of $totalSteps",
            style = MaterialTheme.typography.bodyMedium,
            color = Gray400
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

/**
 * SWEEP Points display badge - clickable to show info modal
 */
@Composable
fun SweepPointsBadge(
    points: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        shape = RoundedCornerShape(12.dp),
        color = Purple500.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, Purple500.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // SWEEP icon
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = Purple400,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = "$points",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Purple400
            )
            Text(
                text = "SWEEP",
                style = MaterialTheme.typography.labelSmall,
                color = Gray400
            )
        }
    }
}

/**
 * SWEEP Points card for larger display
 * Shows base points + Twitter bonus = total
 */
@Composable
fun SweepPointsCard(
    points: Int,
    accountsSwept: Int,
    twitterBonus: Int = 0,
    modifier: Modifier = Modifier
) {
    val totalPoints = points + twitterBonus
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Gray800
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = null,
                        tint = Purple400,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "SWEEP Points",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray50
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Purple500.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "Early Sweeper",
                        style = MaterialTheme.typography.labelSmall,
                        color = Purple400,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "$totalPoints",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = Purple400
                    )
                    Text(
                        text = if (twitterBonus > 0) "total SWEEP ($points + $twitterBonus bonus)" else "SWEEP points",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = accountsSwept.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray300
                    )
                    Text(
                        text = "accounts swept",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Explain SWEEP meaning
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Gray700.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SWEEP = Solana Wallet Empty Entry Points",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
            }
        }
    }
}

/**
 * Points earned animation popup
 */
@Composable
fun PointsEarnedPopup(
    points: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        containerColor = Gray800,
        icon = {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = Purple400,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = "+$points SWEEP",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Purple400,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Points Earned!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Gray50,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "SWEEP points are recorded on your wallet. Thanks for being an early user!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray400,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Nice!", color = Purple400)
            }
        }
    )
}

/**
 * SWEEP info modal - shows detailed info about SWEEP points
 */
@Composable
fun SweepInfoModal(
    currentPoints: Int,
    accountsSwept: Int,
    twitterBonus: Int,
    totalWallets: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.Stars,
                contentDescription = null,
                tint = Purple400,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "SWEEP Points",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray50
                )
                Text(
                    text = "Solana Wallet Empty Entry Points",
                    style = MaterialTheme.typography.bodySmall,
                    color = Purple400
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Your stats
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Gray800
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Your Stats",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray500
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total SWEEP", color = Gray300)
                            Text(
                                text = "${currentPoints + twitterBonus}",
                                color = Purple400,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Accounts Swept", color = Gray300)
                            Text("$accountsSwept", color = Emerald400)
                        }
                        if (twitterBonus > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Twitter Bonus", color = Gray300)
                                Text("+$twitterBonus", color = Purple400)
                            }
                        }
                    }
                }
                
                // How to earn
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Gray800
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How to Earn",
                            style = MaterialTheme.typography.labelMedium,
                            color = Gray500
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🧹", modifier = Modifier.padding(end = 8.dp))
                            Text("+1 per account closed", color = Gray300, style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📣", modifier = Modifier.padding(end = 8.dp))
                            Text("+N bonus when sharing N accounts on X", color = Gray300, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                
                // Global stats
                if (totalWallets > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Purple500.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.People,
                                contentDescription = null,
                                tint = Purple400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$totalWallets wallet${if (totalWallets != 1) "s" else ""} have collected rent",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Purple400
                            )
                        }
                    }
                }
                
                Text(
                    text = "Points are tracked for potential future rewards. Keep sweeping!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it!", color = Purple400)
            }
        }
    )
}

/**
 * Achievement info modal - shows how to unlock an achievement
 */
@Composable
fun AchievementInfoModal(
    achievement: com.rentquest.app.domain.model.Achievement,
    isUnlocked: Boolean,
    currentProgress: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isUnlocked) SurfaceVariantDark else Gray700,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isUnlocked) achievement.emoji else "🔒",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        },
        title = {
            Text(
                text = achievement.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = if (isUnlocked) Emerald400 else Gray50
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Description
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray300,
                    textAlign = TextAlign.Center
                )
                
                // Progress
                if (!isUnlocked) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Gray800
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.labelMedium,
                                color = Gray500
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$currentProgress / ${achievement.threshold}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Purple400
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = (currentProgress.toFloat() / achievement.threshold).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Purple500,
                                trackColor = Gray700
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${achievement.threshold - currentProgress} more accounts to go!",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Emerald500.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Emerald400,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Achievement Unlocked!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Emerald400,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isUnlocked) "Nice!" else "Got it", color = Purple400)
            }
        }
    )
}
