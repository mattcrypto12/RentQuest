package com.rentquest.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rentquest.app.RentQuestApp
import com.rentquest.app.domain.model.WalletConnectionState
import com.rentquest.app.ui.components.AchievementUnlockAnimation
import com.rentquest.app.ui.components.LootAnimation
import com.rentquest.app.ui.navigation.Screen
import com.rentquest.app.ui.screens.*
import com.rentquest.app.ui.theme.BackgroundDark
import com.rentquest.app.ui.theme.RentQuestTheme
import com.rentquest.app.ui.viewmodel.MainViewModel
import com.rentquest.app.ui.viewmodel.MainViewModelFactory
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender

class MainActivity : ComponentActivity() {
    
    private lateinit var activityResultSender: ActivityResultSender
    
    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as RentQuestApp).dataStoreManager)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        activityResultSender = ActivityResultSender(this)
        
        setContent {
            RentQuestTheme {
                RentQuestApp(
                    viewModel = viewModel,
                    activityResultSender = activityResultSender
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentQuestApp(
    viewModel: MainViewModel,
    activityResultSender: ActivityResultSender
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    val walletState by viewModel.walletState.collectAsState()
    val onboardingComplete by viewModel.onboardingComplete.collectAsState()
    val showLootAnimation by viewModel.showLootAnimation.collectAsState()
    val pendingAchievements by viewModel.pendingAchievements.collectAsState()
    
    val isConnected = walletState is WalletConnectionState.Connected
    
    // Determine start destination
    val startDestination = when {
        !onboardingComplete -> Screen.Onboarding.route
        else -> Screen.Connect.route
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Scaffold(
            containerColor = BackgroundDark,
            bottomBar = {
                // Show bottom nav only when connected and on main screens
                if (isConnected && currentDestination?.route in Screen.bottomNavItems.map { it.route }) {
                    NavigationBar(
                        containerColor = BackgroundDark
                    ) {
                        Screen.bottomNavItems.forEach { screen ->
                            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onComplete = {
                            viewModel.completeOnboarding()
                            navController.navigate(Screen.Connect.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(Screen.Connect.route) {
                    ConnectScreen(
                        viewModel = viewModel,
                        activityResultSender = activityResultSender,
                        onConnected = {
                            navController.navigate(Screen.Scan.route) {
                                popUpTo(Screen.Connect.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(Screen.Scan.route) {
                    ScanScreen(
                        viewModel = viewModel,
                        onStartClose = {
                            navController.navigate(Screen.CloseProgress.route)
                        }
                    )
                }
                
                composable(Screen.CloseProgress.route) {
                    CloseProgressScreen(
                        viewModel = viewModel,
                        onComplete = {
                            navController.navigate(Screen.Scan.route) {
                                popUpTo(Screen.Scan.route) { inclusive = true }
                            }
                        }
                    )
                }
                
                composable(Screen.History.route) {
                    HistoryScreen(viewModel = viewModel)
                }
                
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onDisconnect = {
                            navController.navigate(Screen.Connect.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
        
        // Loot animation overlay
        showLootAnimation?.let { amount ->
            LootAnimation(
                solAmount = amount,
                onDismiss = { viewModel.dismissLootAnimation() }
            )
        }
        
        // Achievement unlock overlay
        pendingAchievements.firstOrNull()?.let { achievement ->
            AchievementUnlockAnimation(
                achievement = achievement,
                onDismiss = { viewModel.dismissAchievement() }
            )
        }
    }
}
