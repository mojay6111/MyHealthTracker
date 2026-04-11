package com.example.myhealthtracker.ui.stats

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.StatsViewModel
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = FitTrackColors.TextPrimary
                )
            }
            Text(
                text = "Statistics",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = FitTrackColors.TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Summary Cards ──
            SummaryCardsRow(
                totalSteps = uiState.totalStepsEver,
                totalDistance = viewModel.formatDistance(uiState.totalDistanceEver),
                totalCalories = viewModel.formatCalories(uiState.totalCaloriesEver),
                currentStreak = uiState.currentStreak
            )

            // ── Tabs ──
            TabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = FitTrackColors.SurfaceCard,
                contentColor = FitTrackColors.TealPrimary,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.BottomStart)
                            .offset(x = tabPositions[uiState.selectedTab].left)
                            .width(tabPositions[uiState.selectedTab].width)
                            .height(2.dp)
                            .background(FitTrackColors.TealPrimary)
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                listOf("Week", "Month", "Activity").forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (uiState.selectedTab == index)
                                    FitTrackColors.TealPrimary
                                else
                                    FitTrackColors.TextSecondary
                            )
                        }
                    )
                }
            }

            // ── Tab Content ──
            when (uiState.selectedTab) {
                0 -> {
                    // Weekly view
                    if (uiState.weeklySteps.isNotEmpty()) {
                        StepBarChart(
                            title = "Steps This Week",
                            records = uiState.weeklySteps,
                            stepGoal = uiState.stepGoal,
                            getDayLabel = viewModel::getDayLabel
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Default.DirectionsWalk,
                            message = "No step data this week yet.\nStart walking to see your stats!"
                        )
                    }

                    // Best day card
                    if (uiState.bestDaySteps > 0) {
                        BestDayCard(
                            steps = uiState.bestDaySteps,
                            date = viewModel.formatDate(uiState.bestDayDate)
                        )
                    }

                    // Avg daily steps
                    if (uiState.avgDailySteps > 0) {
                        AverageDailyCard(avgSteps = uiState.avgDailySteps)
                    }
                }

                1 -> {
                    // Monthly view
                    if (uiState.monthlySteps.isNotEmpty()) {
                        MonthlyStepsChart(
                            records = uiState.monthlySteps,
                            stepGoal = uiState.stepGoal
                        )
                    } else {
                        EmptyState(
                            icon = Icons.Default.CalendarMonth,
                            message = "No step data this month yet."
                        )
                    }

                    // Weight trend
                    if (uiState.weightLogs.isNotEmpty()) {
                        WeightTrendCard(
                            logs = uiState.weightLogs
                        )
                    }
                }

                2 -> {
                    // Activity sessions
                    if (uiState.recentSessions.isNotEmpty()) {
                        uiState.recentSessions.forEach { session ->
                            ActivitySessionCard(
                                session = session,
                                formatDate = viewModel::formatDate,
                                formatDistance = viewModel::formatDistance,
                                formatCalories = viewModel::formatCalories
                            )
                        }
                    } else {
                        EmptyState(
                            icon = Icons.Default.DirectionsRun,
                            message = "No activities recorded yet.\nStart an activity to see it here!"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SummaryCardsRow(
    totalSteps: Int,
    totalDistance: String,
    totalCalories: String,
    currentStreak: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "All Time",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FitTrackColors.TextPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatSummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.DirectionsWalk,
                value = "%,d".format(totalSteps),
                label = "Total Steps",
                color = FitTrackColors.TealPrimary
            )
            StatSummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.LocalFireDepartment,
                value = totalCalories,
                label = "Calories",
                color = FitTrackColors.Coral
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatSummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Route,
                value = totalDistance,
                label = "Distance",
                color = FitTrackColors.Amber
            )
            StatSummaryCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.Whatshot,
                value = "$currentStreak days",
                label = "Streak",
                color = FitTrackColors.GreenSuccess
            )
        }
    }
}

@Composable
fun StatSummaryCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color.copy(alpha = 0.15f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
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
}

@Composable
fun StepBarChart(
    title: String,
    records: List<com.example.myhealthtracker.data.local.entity.StepRecordEntity>,
    stepGoal: Int,
    getDayLabel: (String) -> String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitTrackColors.TextPrimary
            )

            val maxSteps = records.maxOfOrNull { it.steps }
                ?.coerceAtLeast(stepGoal) ?: stepGoal

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                records.forEach { record ->
                    val barFraction = record.steps.toFloat() / maxSteps.toFloat()
                    val isGoalMet = record.steps >= stepGoal
                    val animatedFraction by animateFloatAsState(
                        targetValue = barFraction,
                        animationSpec = tween(800, easing = EaseOutCubic),
                        label = "bar"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (record.steps >= 1000)
                                "${record.steps / 1000}k"
                            else
                                "${record.steps}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FitTrackColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight()
                                    .background(
                                        FitTrackColors.SurfaceBorder,
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(animatedFraction)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = if (isGoalMet) listOf(
                                                FitTrackColors.TealPrimary,
                                                FitTrackColors.TealSecondary
                                            ) else listOf(
                                                FitTrackColors.TextDisabled,
                                                FitTrackColors.SurfaceBorder
                                            )
                                        ),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                            )
                        }
                        Text(
                            text = getDayLabel(record.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = FitTrackColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Goal line indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(FitTrackColors.TealPrimary, CircleShape)
                )
                Text(
                    text = "Goal met",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(FitTrackColors.TextDisabled, CircleShape)
                )
                Text(
                    text = "Below goal",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun MonthlyStepsChart(
    records: List<com.example.myhealthtracker.data.local.entity.StepRecordEntity>,
    stepGoal: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Steps This Month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitTrackColors.TextPrimary
            )

            val maxSteps = records.maxOfOrNull { it.steps }
                ?.coerceAtLeast(stepGoal) ?: stepGoal
            val daysMetGoal = records.count { it.steps >= stepGoal }
            val totalSteps = records.sumOf { it.steps }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                records.forEach { record ->
                    val fraction = record.steps.toFloat() / maxSteps.toFloat()
                    val isGoalMet = record.steps >= stepGoal
                    val animatedFraction by animateFloatAsState(
                        targetValue = fraction,
                        animationSpec = tween(600, easing = EaseOutCubic),
                        label = "monthly_bar"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(animatedFraction.coerceAtLeast(0.05f))
                            .background(
                                color = if (isGoalMet)
                                    FitTrackColors.TealPrimary
                                else
                                    FitTrackColors.SurfaceBorder,
                                shape = RoundedCornerShape(
                                    topStart = 2.dp,
                                    topEnd = 2.dp
                                )
                            )
                    )
                }
            }

            HorizontalDivider(color = FitTrackColors.SurfaceBorder)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonthlyStatItem(
                    value = "%,d".format(totalSteps),
                    label = "Total Steps"
                )
                MonthlyStatItem(
                    value = "$daysMetGoal",
                    label = "Days Goal Met"
                )
                MonthlyStatItem(
                    value = "${records.size}",
                    label = "Days Tracked"
                )
            }
        }
    }
}

@Composable
fun MonthlyStatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = FitTrackColors.TealPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FitTrackColors.TextSecondary
        )
    }
}

@Composable
fun BestDayCard(steps: Int, date: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.TealContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        FitTrackColors.TealPrimary.copy(alpha = 0.2f),
                        CircleShape
                    )
            ) {
                Text(text = "🏆", style = MaterialTheme.typography.titleLarge)
            }
            Column {
                Text(
                    text = "Personal Best Day",
                    style = MaterialTheme.typography.labelMedium,
                    color = FitTrackColors.TealSecondary
                )
                Text(
                    text = "%,d steps".format(steps),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TealPrimary
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun AverageDailyCard(avgSteps: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        FitTrackColors.Purple.copy(alpha = 0.15f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = FitTrackColors.Purple,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = "Daily Average",
                    style = MaterialTheme.typography.labelMedium,
                    color = FitTrackColors.TextSecondary
                )
                Text(
                    text = "%,d steps/day".format(avgSteps),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary
                )
            }
        }
    }
}

@Composable
fun WeightTrendCard(
    logs: List<com.example.myhealthtracker.data.local.entity.WeightLogEntity>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Weight Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitTrackColors.TextPrimary
            )

            val maxWeight = logs.maxOf { it.weightKg }
            val minWeight = logs.minOf { it.weightKg }
            val range = (maxWeight - minWeight).coerceAtLeast(1.0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                logs.forEach { log ->
                    val fraction = ((log.weightKg - minWeight) / range).toFloat()
                    val barHeight = 0.3f + fraction * 0.7f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(barHeight)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        FitTrackColors.Coral,
                                        FitTrackColors.Coral.copy(alpha = 0.5f)
                                    )
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 4.dp
                                )
                            )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Min: ${String.format("%.1f", minWeight)}kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
                Text(
                    text = "Max: ${String.format("%.1f", maxWeight)}kg",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun ActivitySessionCard(
    session: com.example.myhealthtracker.data.local.entity.ActivitySessionEntity,
    formatDate: (String) -> String,
    formatDistance: (Double) -> String,
    formatCalories: (Double) -> String
) {
    val activityIcon = when (session.activityType) {
        "RUN" -> Icons.Default.DirectionsRun
        "CYCLE" -> Icons.Default.DirectionsBike
        "HIKE" -> Icons.Default.Terrain
        "SWIM" -> Icons.Default.Pool
        else -> Icons.Default.DirectionsWalk
    }
    val activityColor = when (session.activityType) {
        "RUN" -> FitTrackColors.Coral
        "CYCLE" -> FitTrackColors.Amber
        "HIKE" -> FitTrackColors.GreenSuccess
        "SWIM" -> FitTrackColors.Purple
        else -> FitTrackColors.TealPrimary
    }
    val durationMins = session.durationSeconds / 60

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        activityColor.copy(alpha = 0.15f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    imageVector = activityIcon,
                    contentDescription = null,
                    tint = activityColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.activityType
                        .lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = FitTrackColors.TextPrimary
                )
                Text(
                    text = formatDate(session.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDistance(session.distanceMetres),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = activityColor
                )
                Text(
                    text = "${durationMins}min · ${formatCalories(session.caloriesBurned)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        FitTrackColors.TealContainer,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FitTrackColors.TealPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = FitTrackColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}