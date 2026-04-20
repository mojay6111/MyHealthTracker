package com.example.myhealthtracker

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myhealthtracker.ui.navigation.FitTrackNavHost
import com.example.myhealthtracker.ui.navigation.Screen
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.theme.FitTrackTheme
import com.example.myhealthtracker.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.myhealthtracker.service.StepCounterService
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableLongStateOf



data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val activityGranted =
            permissions[Manifest.permission.ACTIVITY_RECOGNITION] ?: false
        if (activityGranted) startStepService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { controller ->
                controller.hide(android.view.WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
        requestPermissionsAndStart()

        setContent {
            FitTrackTheme {
                FitTrackApp(
                    viewModel = viewModel,
                    onFinish = { finish() }
                )
            }
        }
    }

    private fun requestPermissionsAndStart() {
        val perms = mutableListOf(
            Manifest.permission.ACTIVITY_RECOGNITION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(perms.toTypedArray())
    }

    private fun startStepService() {
        val intent = Intent(this, StepCounterService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}

@Composable
fun FitTrackBottomNav(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onItemClick: (BottomNavItem) -> Unit,
    onStartActivity: () -> Unit,
    onNavigateToWater: () -> Unit,
    onNavigateToWeight: () -> Unit,
    onNavigateToSleep: () -> Unit,
    onNavigateToShare: () -> Unit,
    onNavigateToAchievements: () -> Unit
) {
    var showMoreSheet by remember { mutableStateOf(false) }

    if (showMoreSheet) {
        QuickActionsSheet(
            onDismiss = { showMoreSheet = false },
            onWater = { showMoreSheet = false; onNavigateToWater() },
            onWeight = { showMoreSheet = false; onNavigateToWeight() },
            onSleep = { showMoreSheet = false; onNavigateToSleep() },
            onShare = { showMoreSheet = false; onNavigateToShare() },
            onAchievements = { showMoreSheet = false; onNavigateToAchievements() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        NavigationBar(
            containerColor = FitTrackColors.SurfaceCard.copy(alpha = 0.95f),
            contentColor = FitTrackColors.TextSecondary,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
        ) {
            // Item 1 — Home
            val homeItem = items[0]
            val homeSelected = currentRoute == homeItem.screen.route
            NavigationBarItem(
                selected = homeSelected,
                onClick = { onItemClick(homeItem) },
                icon = {
                    Icon(
                        imageVector = if (homeSelected)
                            homeItem.selectedIcon
                        else homeItem.unselectedIcon,
                        contentDescription = homeItem.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = homeItem.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (homeSelected)
                            FontWeight.SemiBold
                        else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitTrackColors.TealPrimary,
                    selectedTextColor = FitTrackColors.TealPrimary,
                    indicatorColor = FitTrackColors.TealContainer,
                    unselectedIconColor = FitTrackColors.TextSecondary,
                    unselectedTextColor = FitTrackColors.TextDisabled
                )
            )

            // Item 2 — Routes
            val routesItem = items[1]
            val routesSelected = currentRoute == routesItem.screen.route
            NavigationBarItem(
                selected = routesSelected,
                onClick = { onItemClick(routesItem) },
                icon = {
                    Icon(
                        imageVector = if (routesSelected)
                            routesItem.selectedIcon
                        else routesItem.unselectedIcon,
                        contentDescription = routesItem.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = routesItem.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (routesSelected)
                            FontWeight.SemiBold
                        else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitTrackColors.TealPrimary,
                    selectedTextColor = FitTrackColors.TealPrimary,
                    indicatorColor = FitTrackColors.TealContainer,
                    unselectedIconColor = FitTrackColors.TextSecondary,
                    unselectedTextColor = FitTrackColors.TextDisabled
                )
            )

            // Center FAB — Start
            NavigationBarItem(
                selected = false,
                onClick = onStartActivity,
                icon = {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        FitTrackColors.TealPrimary,
                                        FitTrackColors.TealSecondary
                                    )
                                ),
                                shape = CircleShape
                            )
                            .offset(y = (-8).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsRun,
                            contentDescription = "Start",
                            tint = Color(0xFF001A17),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                label = {
                    Text(
                        text = "Start",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.TealPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = FitTrackColors.TealPrimary,
                    unselectedTextColor = FitTrackColors.TealPrimary
                )
            )

            // Item 3 — Stats
            val statsItem = items[2]
            val statsSelected = currentRoute == statsItem.screen.route
            NavigationBarItem(
                selected = statsSelected,
                onClick = { onItemClick(statsItem) },
                icon = {
                    Icon(
                        imageVector = if (statsSelected)
                            statsItem.selectedIcon
                        else statsItem.unselectedIcon,
                        contentDescription = statsItem.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = statsItem.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (statsSelected)
                            FontWeight.SemiBold
                        else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitTrackColors.TealPrimary,
                    selectedTextColor = FitTrackColors.TealPrimary,
                    indicatorColor = FitTrackColors.TealContainer,
                    unselectedIconColor = FitTrackColors.TextSecondary,
                    unselectedTextColor = FitTrackColors.TextDisabled
                )
            )

            // Item 4 — More
            NavigationBarItem(
                selected = false,
                onClick = { showMoreSheet = true },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.GridView,
                        contentDescription = "More",
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = "More",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FitTrackColors.TealPrimary,
                    selectedTextColor = FitTrackColors.TealPrimary,
                    indicatorColor = FitTrackColors.TealContainer,
                    unselectedIconColor = FitTrackColors.TextSecondary,
                    unselectedTextColor = FitTrackColors.TextDisabled
                )
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionsSheet(
    onDismiss: () -> Unit,
    onWater: () -> Unit,
    onWeight: () -> Unit,
    onSleep: () -> Unit,
    onShare: () -> Unit,
    onAchievements: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FitTrackColors.SurfaceCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        FitTrackColors.SurfaceBorder,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FitTrackColors.TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    emoji = "💧",
                    label = "Water",
                    sublabel = "Log intake",
                    color = FitTrackColors.Amber,
                    onClick = onWater
                )
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    emoji = "🏋️",
                    label = "Weight",
                    sublabel = "Log weight",
                    color = FitTrackColors.TealPrimary,
                    onClick = onWeight
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    emoji = "🌙",
                    label = "Sleep",
                    sublabel = "Log sleep",
                    color = FitTrackColors.Purple,
                    onClick = onSleep
                )
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    emoji = "🏆",
                    label = "Achievements",
                    sublabel = "View badges",
                    color = FitTrackColors.Amber,
                    onClick = onAchievements
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionItem(
                    modifier = Modifier.weight(1f),
                    emoji = "📤",
                    label = "Share",
                    sublabel = "Share progress",
                    color = FitTrackColors.Coral,
                    onClick = onShare
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun QuickActionItem(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    sublabel: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceElevated
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = sublabel,
                style = MaterialTheme.typography.labelSmall,
                color = FitTrackColors.TextSecondary
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityPickerSheet(
    onDismiss: () -> Unit,
    onActivitySelected: (String) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FitTrackColors.SurfaceCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        FitTrackColors.SurfaceBorder,
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Start Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FitTrackColors.TextPrimary
            )
            Text(
                text = "Choose your activity type",
                style = MaterialTheme.typography.bodyMedium,
                color = FitTrackColors.TextSecondary
            )

            val activities = listOf(
                Triple("WALK", "🚶", FitTrackColors.TealPrimary),
                Triple("RUN", "🏃", FitTrackColors.Coral),
                Triple("CYCLE", "🚴", FitTrackColors.Amber),
                Triple("HIKE", "🥾", FitTrackColors.GreenSuccess)
            )

            activities.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { (type, emoji, color) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onActivitySelected(type) },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = FitTrackColors.SurfaceElevated
                            ),
                            border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = emoji,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Text(
                                    text = type.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Text(
                                    text = when (type) {
                                        "WALK" -> "< 7 km/h"
                                        "RUN" -> "7-20 km/h"
                                        "CYCLE" -> "> 20 km/h"
                                        else -> "Any pace"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FitTrackColors.TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FitTrackApp(
    viewModel: MainViewModel,
    onFinish: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    var lastBackPress by remember { mutableLongStateOf(0L) }

    val onboardingComplete by viewModel.onboardingComplete
        .collectAsState(initial = false)

    val startDestination = if (onboardingComplete)
        Screen.Dashboard.route
    else
        Screen.Onboarding.route

    val bottomNavScreens = setOf(
        Screen.Dashboard.route,
        Screen.Routes.route,
        Screen.Stats.route,
        Screen.Profile.route
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomNav = currentRoute in bottomNavScreens

    val bottomNavItems = listOf(
        BottomNavItem(
            screen = Screen.Dashboard,
            label = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home
        ),
        BottomNavItem(
            screen = Screen.Routes,
            label = "Routes",
            selectedIcon = Icons.Filled.Map,
            unselectedIcon = Icons.Outlined.Map
        ),
        BottomNavItem(
            screen = Screen.Stats,
            label = "Stats",
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart
        )
    )

    var showActivityPicker by remember { mutableStateOf(false) }

    // Back handler
    BackHandler {
        val now = System.currentTimeMillis()
        when {
            currentRoute == Screen.Dashboard.route -> {
                if (now - lastBackPress < 2000) {
                    onFinish()
                } else {
                    lastBackPress = now
                }
            }
            else -> navController.popBackStack()
        }
    }

    if (showActivityPicker) {
        ActivityPickerSheet(
            onDismiss = { showActivityPicker = false },
            onActivitySelected = { activityType ->
                showActivityPicker = false
                navController.navigate(
                    Screen.ActiveSession.createRoute(activityType)
                )
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background)
    ) {
        FitTrackNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        )

        AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            FitTrackBottomNav(
                items = bottomNavItems,
                currentRoute = currentRoute,
                onItemClick = { item ->
                    navController.navigate(item.screen.route) {
                        popUpTo(
                            navController.graph.findStartDestination().id
                        ) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onStartActivity = { showActivityPicker = true },
                onNavigateToWater = {
                    navController.navigate(Screen.Water.route)
                },
                onNavigateToWeight = {
                    navController.navigate(Screen.Weight.route)
                },
                onNavigateToSleep = {
                    navController.navigate(Screen.Sleep.route)
                },
                onNavigateToShare = {
                    navController.navigate(Screen.Share.route)
                },
                onNavigateToAchievements = {
                    navController.navigate(Screen.Achievements.route)
                }
            )
        }
    }
}