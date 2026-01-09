package com.rentquest.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation routes for the app
 */
sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Onboarding : Screen(
        route = "onboarding",
        title = "Welcome",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )
    
    data object Connect : Screen(
        route = "connect",
        title = "Connect",
        selectedIcon = Icons.Filled.AccountBalanceWallet,
        unselectedIcon = Icons.Outlined.AccountBalanceWallet
    )
    
    data object Scan : Screen(
        route = "scan",
        title = "Scan",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search
    )
    
    data object CloseProgress : Screen(
        route = "close_progress",
        title = "Progress",
        selectedIcon = Icons.Filled.Sync,
        unselectedIcon = Icons.Outlined.Sync
    )
    
    data object History : Screen(
        route = "history",
        title = "History",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )
    
    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
    
    companion object {
        /**
         * Bottom navigation items (shown when connected)
         */
        val bottomNavItems = listOf(Scan, History, Settings)
    }
}
