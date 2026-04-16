package com.dodamsoft.ajangajang.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.dodamsoft.ajangajang.ui.checklist.ChecklistStageSelectScreen
import com.dodamsoft.ajangajang.ui.home.HomeScreen
import com.dodamsoft.ajangajang.ui.nav.ChecklistTabRoute
import com.dodamsoft.ajangajang.ui.nav.HomeTabRoute
import com.dodamsoft.ajangajang.ui.nav.RecordsTabRoute
import com.dodamsoft.ajangajang.ui.nav.SettingsTabRoute
import com.dodamsoft.ajangajang.ui.records.RecordsScreen
import com.dodamsoft.ajangajang.ui.settings.SettingsScreen

private data class BottomTab(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val matches: (NavDestination) -> Boolean,
)

private val tabs = listOf(
    BottomTab(
        route = HomeTabRoute,
        label = "홈",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        matches = { it.hasRoute<HomeTabRoute>() },
    ),
    BottomTab(
        route = ChecklistTabRoute,
        label = "체크리스트",
        selectedIcon = Icons.Filled.Checklist,
        unselectedIcon = Icons.Outlined.Checklist,
        matches = { it.hasRoute<ChecklistTabRoute>() },
    ),
    BottomTab(
        route = RecordsTabRoute,
        label = "기록",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
        matches = { it.hasRoute<RecordsTabRoute>() },
    ),
    BottomTab(
        route = SettingsTabRoute,
        label = "설정",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        matches = { it.hasRoute<SettingsTabRoute>() },
    ),
)

@Composable
fun MainScaffold(
    onOpenChecklist: (Int) -> Unit,
    onOpenProfileManage: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any(tab.matches) == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(HomeTabRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                            )
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HomeTabRoute,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<HomeTabRoute> {
                HomeScreen(
                    onStartChecklist = onOpenChecklist,
                    onOpenChecklistBrowser = {
                        navController.navigate(ChecklistTabRoute) {
                            popUpTo(HomeTabRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenProfileManage = onOpenProfileManage,
                )
            }
            composable<ChecklistTabRoute> {
                ChecklistStageSelectScreen(
                    onStageClick = onOpenChecklist,
                    onBack = null,
                )
            }
            composable<RecordsTabRoute> {
                RecordsScreen(
                    onStartChecklist = {
                        navController.navigate(ChecklistTabRoute) {
                            popUpTo(HomeTabRoute) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable<SettingsTabRoute> {
                SettingsScreen(
                    onOpenProfileManage = onOpenProfileManage,
                )
            }
        }
    }
}
