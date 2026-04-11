package com.example.myhealthtracker.ui.routes

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.data.local.entity.ActivitySessionEntity
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.RoutesViewModel

@Composable
fun RoutesScreen(
    onBack: () -> Unit,
    onSessionClick: (Long) -> Unit,
    viewModel: RoutesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background)
    ) {
        // ── Top Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = FitTrackColors.TextPrimary
                    )
                }
                Text(
                    text = "My Routes",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // ── Filter Chips ──
        val filters = listOf("All", "Walk", "Run", "Cycle", "Hike")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.forEach { filter ->
                val isSelected = uiState.selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setFilter(filter) },
                    label = {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FitTrackColors.TealContainer,
                        selectedLabelColor = FitTrackColors.TealPrimary,
                        containerColor = FitTrackColors.SurfaceCard,
                        labelColor = FitTrackColors.TextSecondary
                    )
                )
            }
        }

        // ── Summary Stats ──
        if (uiState.sessions.isNotEmpty()) {
            RoutesSummaryRow(
                totalSessions = uiState.sessions.size,
                totalDistance = viewModel.formatDistance(
                    uiState.sessions.sumOf { it.distanceMetres }
                ),
                totalCalories = viewModel.formatCalories(
                    uiState.sessions.sumOf { it.caloriesBurned }
                )
            )
        }

        // ── Sessions List ──
        if (uiState.filteredSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                FitTrackColors.TealContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = FitTrackColors.TealPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Text(
                        text = "No routes yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FitTrackColors.TextPrimary
                    )
                    Text(
                        text = "Start an activity from the\ndashboard to record your first route!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitTrackColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp)
            ) {
                items(uiState.filteredSessions) { session ->
                    RouteSessionCard(
                        session = session,
                        onClick = { onSessionClick(session.id) },
                        formatDistance = viewModel::formatDistance,
                        formatCalories = viewModel::formatCalories,
                        formatDate = viewModel::formatDate,
                        formatDuration = viewModel::formatDuration
                    )
                }
            }
        }
    }
}

@Composable
fun RoutesSummaryRow(
    totalSessions: Int,
    totalDistance: String,
    totalCalories: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RouteSummaryChip(
            modifier = Modifier.weight(1f),
            value = "$totalSessions",
            label = "Activities",
            color = FitTrackColors.TealPrimary
        )
        RouteSummaryChip(
            modifier = Modifier.weight(1f),
            value = totalDistance,
            label = "Total Distance",
            color = FitTrackColors.Amber
        )
        RouteSummaryChip(
            modifier = Modifier.weight(1f),
            value = totalCalories,
            label = "Calories",
            color = FitTrackColors.Coral
        )
    }
}

@Composable
fun RouteSummaryChip(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FitTrackColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RouteSessionCard(
    session: ActivitySessionEntity,
    onClick: () -> Unit,
    formatDistance: (Double) -> String,
    formatCalories: (Double) -> String,
    formatDate: (String) -> String,
    formatDuration: (Long) -> String
) {
    val activityColor = when (session.activityType) {
        "RUN" -> FitTrackColors.Coral
        "CYCLE" -> FitTrackColors.Amber
        "HIKE" -> FitTrackColors.GreenSuccess
        "SWIM" -> FitTrackColors.Purple
        else -> FitTrackColors.TealPrimary
    }
    val activityIcon = when (session.activityType) {
        "RUN" -> Icons.Default.DirectionsRun
        "CYCLE" -> Icons.Default.DirectionsBike
        "HIKE" -> Icons.Default.Terrain
        "SWIM" -> Icons.Default.Pool
        else -> Icons.Default.DirectionsWalk
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Map preview placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                activityColor.copy(alpha = 0.2f),
                                FitTrackColors.SurfaceElevated
                            )
                        ),
                        shape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (session.routePointsJson != null) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = activityColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = activityIcon,
                            contentDescription = null,
                            tint = activityColor.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No map recorded",
                            style = MaterialTheme.typography.labelSmall,
                            color = FitTrackColors.TextDisabled
                        )
                    }
                }

                // Activity type badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = activityColor.copy(alpha = 0.9f)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = activityIcon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = session.activityType
                                .lowercase()
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Session details
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(session.date),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTrackColors.TextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = FitTrackColors.TextDisabled,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RouteStatItem(
                        icon = Icons.Default.Route,
                        value = formatDistance(session.distanceMetres),
                        label = "Distance",
                        color = activityColor
                    )
                    RouteStatItem(
                        icon = Icons.Default.Timer,
                        value = formatDuration(session.durationSeconds),
                        label = "Duration",
                        color = FitTrackColors.Purple
                    )
                    RouteStatItem(
                        icon = Icons.Default.LocalFireDepartment,
                        value = formatCalories(session.caloriesBurned),
                        label = "Calories",
                        color = FitTrackColors.Coral
                    )
                    if (session.avgSpeedKmh > 0) {
                        RouteStatItem(
                            icon = Icons.Default.Speed,
                            value = "${"%.1f".format(session.avgSpeedKmh)}km/h",
                            label = "Avg Speed",
                            color = FitTrackColors.Amber
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RouteStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = FitTrackColors.TextPrimary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FitTrackColors.TextSecondary
        )
    }
}