package com.dodamsoft.ajangajang.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.dodamsoft.ajangajang.ui.checklist.ChecklistScreen
import com.dodamsoft.ajangajang.ui.main.MainScaffold
import com.dodamsoft.ajangajang.ui.main.PreferencesViewModel
import com.dodamsoft.ajangajang.ui.onboarding.OnboardingScreen
import com.dodamsoft.ajangajang.ui.profile.ProfileManageScreen

@Composable
fun RootNavHost(
    modifier: Modifier = Modifier,
    prefsViewModel: PreferencesViewModel = viewModel(factory = PreferencesViewModel.Factory),
) {
    val rootNavController = rememberNavController()
    val prefs by prefsViewModel.preferences.collectAsState()
    val loaded by prefsViewModel.loaded.collectAsState()

    if (!loaded) return

    val startDestination = if (prefs.onboardingCompleted) MainGraphRoute else OnboardingRoute

    NavHost(
        navController = rootNavController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<OnboardingRoute> {
            OnboardingScreen(
                onFinished = {
                    rootNavController.navigate(MainGraphRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                },
            )
        }
        composable<MainGraphRoute> {
            MainScaffold(
                onOpenChecklist = { months -> rootNavController.navigate(ChecklistRoute(months)) },
                onOpenProfileManage = { rootNavController.navigate(ProfileManageRoute) },
            )
        }
        composable<ChecklistRoute> { entry ->
            val route = entry.toRoute<ChecklistRoute>()
            ChecklistScreen(
                months = route.months,
                onBack = { rootNavController.popBackStack() },
            )
        }
        composable<ProfileManageRoute> {
            ProfileManageScreen(
                onBack = { rootNavController.popBackStack() },
            )
        }
    }
}
