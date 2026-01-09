package com.rentquest.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rentquest.app.ui.components.PrimaryButton
import com.rentquest.app.ui.theme.*
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val icon: ImageVector
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "🧹",
        title = "Spring Clean Your Wallet",
        description = "Reclaim SOL locked in unused token accounts. When you receive tokens, Solana creates an account that requires rent. Empty accounts still hold that rent.",
        icon = Icons.Default.CleaningServices
    ),
    OnboardingPage(
        emoji = "🔍",
        title = "Scan & Discover",
        description = "We scan your wallet for token accounts with zero balance. These are safe to close and their rent can be returned to you.",
        icon = Icons.Default.Search
    ),
    OnboardingPage(
        emoji = "💰",
        title = "Reclaim Your SOL",
        description = "Close empty accounts with a few taps. Each account returns ~0.002 SOL. If you have many, it adds up quickly!",
        icon = Icons.Default.Savings
    ),
    OnboardingPage(
        emoji = "🔐",
        title = "Secure by Design",
        description = "We never see your private keys. All transactions are signed in your wallet app. You approve every action.",
        icon = Icons.Default.Security
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onComplete) {
                Text("Skip", color = Gray400)
            }
        }
        
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            OnboardingPageContent(
                page = onboardingPages[page],
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(onboardingPages.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Purple500 else Gray600)
                )
            }
        }
        
        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            
            if (pagerState.currentPage == onboardingPages.size - 1) {
                PrimaryButton(
                    text = "Get Started",
                    onClick = onComplete,
                    modifier = Modifier.weight(1f)
                )
            } else {
                PrimaryButton(
                    text = "Next",
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = page.emoji,
            fontSize = 80.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Gray50,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Gray400,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
