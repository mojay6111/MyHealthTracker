package com.example.myhealthtracker.ui.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.DashboardViewModel
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToWater: () -> Unit,
    onStartActivity: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 48.dp, bottom = 200.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header ──
        DashboardHeader(
            greeting = uiState.greeting,
            userName = uiState.userName,
            onProfileClick = onNavigateToProfile,
            scrollOffset = scrollState.value
        )

        // ── Step Ring Card ──
        StepRingCard(
            steps = uiState.steps,
            stepGoal = uiState.stepGoal,
            progress = uiState.progressFraction,
            calories = uiState.calories,
            distanceMetres = uiState.distanceMetres,
            formattedSteps = viewModel.formatSteps(uiState.steps),
            formattedCalories = viewModel.formatCalories(uiState.calories),
            formattedDistance = viewModel.formatDistance(uiState.distanceMetres)
        )

        // ── Quick Stats Row ──
        QuickStatsRow(
            calories = uiState.calories,
            distanceMetres = uiState.distanceMetres,
            waterMl = uiState.waterMl,
            waterGoalMl = uiState.waterGoalMl,
            formattedCalories = viewModel.formatCalories(uiState.calories),
            formattedDistance = viewModel.formatDistance(uiState.distanceMetres),
            onWaterClick = onNavigateToWater
        )

        // ── Start Activity Row ──
        StartActivityRow(onStartActivity = onStartActivity)

        // ── Weekly Steps Chart ──
        if (uiState.weeklyRecords.isNotEmpty()) {
            WeeklyStepsCard(
                records = uiState.weeklyRecords,
                stepGoal = uiState.stepGoal,
                getDayLabel = viewModel::getDayLabel
            )
        }
    }
}

@Composable
fun DashboardHeader(
    greeting: String,
    userName: String,
    onProfileClick: () -> Unit,
    scrollOffset: Int = 0
) {
    // Collapse header as user scrolls — max collapse at 300px scroll
    val collapseProgress = (scrollOffset / 300f).coerceIn(0f, 1f)

    // Greeting fades out as we scroll
    val greetingAlpha = (1f - collapseProgress * 2f).coerceIn(0f, 1f)

    // Name font size shrinks from 24sp to 16sp
    val nameFontSize = (24f - (collapseProgress * 8f)).coerceIn(16f, 24f)

    // Header height shrinks
    val topPadding = (48f - (collapseProgress * 24f)).coerceIn(24f, 48f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPadding.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            // Greeting fades out on scroll
            if (greetingAlpha > 0f) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitTrackColors.TextSecondary.copy(alpha = greetingAlpha),
                    modifier = Modifier.graphicsLayer { alpha = greetingAlpha }
                )
            }
            // Name shrinks but stays visible
            Text(
                text = if (userName.isNotEmpty()) userName else "Athlete",
                fontSize = nameFontSize.sp,
                fontWeight = FontWeight.Bold,
                color = FitTrackColors.TextPrimary
            )
        }
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(44.dp)
                .background(FitTrackColors.SurfaceCard, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = FitTrackColors.TealPrimary
            )
        }
    }
}

@Composable
fun StepRingCard(
    steps: Int,
    stepGoal: Int,
    progress: Float,
    calories: Double,
    distanceMetres: Double,
    formattedSteps: String,
    formattedCalories: String,
    formattedDistance: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1200, easing = EaseOutCubic),
        label = "step_progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FitTrackColors.SurfaceCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Today's Steps",
                style = MaterialTheme.typography.titleMedium,
                color = FitTrackColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Animated Step Ring
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(200.dp)
            ) {
                Canvas(modifier = Modifier.size(200.dp)) {
                    val strokeWidth = 18.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    val startAngle = -90f
                    val sweepAngle = 360f * animatedProgress

                    // Background track
                    drawCircle(
                        color = FitTrackColors.SurfaceBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                FitTrackColors.TealSecondary,
                                FitTrackColors.TealPrimary,
                                FitTrackColors.TealPrimary
                            ),
                            center = center
                        ),
                        startAngle = startAngle,
                        sweepAngle = sweepAngle.coerceAtLeast(0f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )

                    // Dot at progress end
                    if (animatedProgress > 0.01f) {
                        val angleRad = Math.toRadians(
                            (startAngle + sweepAngle).toDouble()
                        )
                        val dotX = center.x + radius * cos(angleRad).toFloat()
                        val dotY = center.y + radius * sin(angleRad).toFloat()
                        drawCircle(
                            color = FitTrackColors.TealPrimary,
                            radius = strokeWidth / 2,
                            center = Offset(dotX, dotY)
                        )
                    }
                }

                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedSteps,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = FitTrackColors.TextPrimary
                    )
                    Text(
                        text = "of $stepGoal",
                        style = MaterialTheme.typography.bodySmall,
                        color = FitTrackColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitTrackColors.TealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Stats below ring
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                RingStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = formattedCalories,
                    label = "Burned",
                    color = FitTrackColors.Coral
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(FitTrackColors.SurfaceBorder)
                )
                RingStatItem(
                    icon = Icons.Default.Route,
                    value = formattedDistance,
                    label = "Distance",
                    color = FitTrackColors.TealPrimary
                )
            }
        }
    }
}

@Composable
fun RingStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = FitTrackColors.TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FitTrackColors.TextSecondary
            )
        }
    }
}

@Composable
fun QuickStatsRow(
    calories: Double,
    distanceMetres: Double,
    waterMl: Int,
    waterGoalMl: Int,
    formattedCalories: String,
    formattedDistance: String,
    onWaterClick: () -> Unit
) {
    val waterProgress = (waterMl.toFloat() / waterGoalMl.toFloat()).coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Water card
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onWaterClick() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = FitTrackColors.SurfaceCard
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = FitTrackColors.Amber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Water",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitTrackColors.TextSecondary
                    )
                }
                Text(
                    text = "${waterMl}ml",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary
                )
                LinearProgressIndicator(
                    progress = { waterProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = FitTrackColors.Amber,
                    trackColor = FitTrackColors.SurfaceBorder
                )
                Text(
                    text = "of ${waterGoalMl}ml",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextDisabled
                )
            }
        }

        // Active minutes card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = FitTrackColors.SurfaceCard
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = FitTrackColors.GreenSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitTrackColors.TextSecondary
                    )
                }
                Text(
                    text = formattedDistance,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedCalories,
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextDisabled
                )
            }
        }
    }
}

@Composable
fun StartActivityRow(onStartActivity: (String) -> Unit) {
    val activities = listOf(
        Triple("Walk", Icons.Default.DirectionsWalk, FitTrackColors.TealPrimary),
        Triple("Run", Icons.Default.DirectionsRun, FitTrackColors.Coral),
        Triple("Cycle", Icons.Default.DirectionsBike, FitTrackColors.Amber),
        Triple("Hike", Icons.Default.Terrain, FitTrackColors.GreenSuccess)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Start Activity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FitTrackColors.TextPrimary
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(activities) { (label, icon, color) ->
                ActivityChip(
                    label = label,
                    icon = icon,
                    color = color,
                    onClick = { onStartActivity(label.uppercase()) }
                )
            }
        }
    }
}

@Composable
fun ActivityChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun WeeklyStepsCard(
    records: List<com.example.myhealthtracker.data.local.entity.StepRecordEntity>,
    stepGoal: Int,
    getDayLabel: (String) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = FitTrackColors.SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitTrackColors.TextPrimary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxSteps = records.maxOfOrNull { it.steps }?.coerceAtLeast(1) ?: 1
                records.forEach { record ->
                    val barProgress = record.steps.toFloat() / maxSteps.toFloat()
                    val isGoalMet = record.steps >= stepGoal
                    WeeklyBar(
                        dayLabel = getDayLabel(record.date),
                        progress = barProgress,
                        isGoalMet = isGoalMet,
                        steps = record.steps
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyBar(
    dayLabel: String,
    progress: Float,
    isGoalMet: Boolean,
    steps: Int
) {
    val animatedHeight by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(800, easing = EaseOutCubic),
        label = "bar_height"
    )
    val barColor = if (isGoalMet) FitTrackColors.TealPrimary else FitTrackColors.TextDisabled

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(80.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight()
                    .background(FitTrackColors.SurfaceBorder, RoundedCornerShape(6.dp))
            )
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .fillMaxHeight(animatedHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(barColor, barColor.copy(alpha = 0.6f))
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }
        Text(
            text = dayLabel,
            style = MaterialTheme.typography.labelSmall,
            color = FitTrackColors.TextSecondary
        )
    }
}

