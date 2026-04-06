package com.example.myhealthtracker.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myhealthtracker.ui.dashboard.DashboardScreen
import com.example.myhealthtracker.ui.onboarding.OnboardingScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.session.ActiveSessionScreen
import com.example.myhealthtracker.ui.profile.ProfileScreen
import com.example.myhealthtracker.ui.water.WaterScreen
import com.example.myhealthtracker.ui.weight.WeightScreen
import com.example.myhealthtracker.ui.stats.StatsScreen
import com.example.myhealthtracker.ui.routes.RoutesScreen
import com.example.myhealthtracker.ui.routes.RouteDetailScreen

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object Routes : Screen("routes")
    data object Stats : Screen("stats")
    data object Profile : Screen("profile")
    data object Water : Screen("water")
    data object Weight : Screen("weight")
    data object ActiveSession : Screen("active_session/{activityType}") {
        fun createRoute(activityType: String) = "active_session/$activityType"
    }
    data object RouteDetail : Screen("route_detail/{sessionId}") {
        fun createRoute(sessionId: Long) = "route_detail/$sessionId"
    }
}

@Composable
fun FitTrackNavHost(
    navController: NavHostController,
    startDestination: String,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 }
        },
        exitTransition = {
            fadeOut(tween(200))
        },
        popEnterTransition = {
            fadeIn(tween(300))
        },
        popExitTransition = {
            fadeOut(tween(200)) + slideOutHorizontally(tween(300)) { it / 4 }
        }
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToRoutes = { navController.navigate(Screen.Routes.route) },
                onNavigateToStats = { navController.navigate(Screen.Stats.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToWater = { navController.navigate(Screen.Water.route) },
                onNavigateToWeight = { navController.navigate(Screen.Weight.route) },
                onStartActivity = { type ->
                    navController.navigate(Screen.ActiveSession.createRoute(type))
                }
            )
        }

        composable(Screen.ActiveSession.route) { backStackEntry ->
            val activityType = backStackEntry.arguments
                ?.getString("activityType") ?: "WALK"
            ActiveSessionScreen(
                activityType = activityType,
                onFinish = { navController.popBackStack() }
            )
        }

        composable(Screen.Routes.route) {
            RoutesScreen(
                onBack = { navController.popBackStack() },
                onSessionClick = { sessionId ->
                    navController.navigate(
                        Screen.RouteDetail.createRoute(sessionId)
                    )
                }
            )
        }

        composable(Screen.RouteDetail.route) { backStackEntry ->
            val sessionId = backStackEntry.arguments
                ?.getString("sessionId")?.toLongOrNull() ?: return@composable
            RouteDetailScreen(
                sessionId = sessionId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Water.route) {
            WaterScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Weight.route) {
            WeightScreen(onBack = { navController.popBackStack() })
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = FitTrackColors.TextPrimary
        )
    }
}