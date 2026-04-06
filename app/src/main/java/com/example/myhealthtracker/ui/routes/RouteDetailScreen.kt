package com.example.myhealthtracker.ui.routes

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.RouteDetailViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.*

@Composable
fun RouteDetailScreen(
    sessionId: Long,
    onBack: () -> Unit,
    viewModel: RouteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    val activityColor = when (uiState.activityType) {
        "RUN" -> FitTrackColors.Coral
        "CYCLE" -> FitTrackColors.Amber
        "HIKE" -> FitTrackColors.GreenSuccess
        "SWIM" -> FitTrackColors.Purple
        else -> FitTrackColors.TealPrimary
    }

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
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = uiState.activityType
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = FitTrackColors.TextPrimary
                    )
                    Text(
                        text = uiState.formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = FitTrackColors.TextSecondary
                    )
                }
            }

            // Activity color badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = activityColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = uiState.formattedDistance,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = activityColor,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    )
                )
            }
        }

        // ── Map View ──
        if (uiState.routePoints.isNotEmpty()) {
            RouteMapView(
                routePoints = uiState.routePoints,
                activityColor = activityColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(FitTrackColors.SurfaceCard),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = FitTrackColors.TextDisabled,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "No route map recorded",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitTrackColors.TextSecondary
                    )
                }
            }
        }

        // ── Stats ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main stats grid
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = FitTrackColors.SurfaceCard
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Session Summary",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTrackColors.TextPrimary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetailStatItem(
                            icon = Icons.Default.Timer,
                            value = uiState.formattedDuration,
                            label = "Duration",
                            color = FitTrackColors.Purple
                        )
                        DetailStatItem(
                            icon = Icons.Default.Route,
                            value = uiState.formattedDistance,
                            label = "Distance",
                            color = activityColor
                        )
                        DetailStatItem(
                            icon = Icons.Default.LocalFireDepartment,
                            value = uiState.formattedCalories,
                            label = "Calories",
                            color = FitTrackColors.Coral
                        )
                    }

                    HorizontalDivider(color = FitTrackColors.SurfaceBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetailStatItem(
                            icon = Icons.Default.Speed,
                            value = uiState.formattedAvgSpeed,
                            label = "Avg Speed",
                            color = FitTrackColors.Amber
                        )
                        DetailStatItem(
                            icon = Icons.Default.Speed,
                            value = uiState.formattedMaxSpeed,
                            label = "Max Speed",
                            color = FitTrackColors.TealPrimary
                        )
                        if (uiState.steps > 0) {
                            DetailStatItem(
                                icon = Icons.Default.DirectionsWalk,
                                value = "%,d".format(uiState.steps),
                                label = "Steps",
                                color = FitTrackColors.GreenSuccess
                            )
                        }
                    }
                }
            }

            // Pace card (for walk/run)
            if (uiState.avgPaceMinPerKm > 0 &&
                uiState.activityType in listOf("WALK", "RUN", "HIKE")
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitTrackColors.SurfaceCard
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    activityColor.copy(alpha = 0.15f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = activityColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Average Pace",
                                style = MaterialTheme.typography.labelMedium,
                                color = FitTrackColors.TextSecondary
                            )
                            Text(
                                text = uiState.formattedPace,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = activityColor
                            )
                        }
                    }
                }
            }

            // Notes card
            val notes = uiState.notes
            if (!notes.isNullOrEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitTrackColors.SurfaceCard
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.labelMedium,
                            color = FitTrackColors.TextSecondary
                        )
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitTrackColors.TextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RouteMapView(
    routePoints: List<LatLng>,
    activityColor: Color,
    modifier: Modifier = Modifier
) {
    val boundsBuilder = LatLngBounds.builder()
    routePoints.forEach { boundsBuilder.include(it) }
    val bounds = remember(routePoints) {
        if (routePoints.isNotEmpty()) boundsBuilder.build() else null
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            routePoints.firstOrNull() ?: LatLng(-1.286389, 36.817223),
            15f
        )
    }

    LaunchedEffect(bounds) {
        bounds?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngBounds(it, 80),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        properties = MapProperties(mapType = MapType.NORMAL),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            scrollGesturesEnabled = true,
            zoomGesturesEnabled = true
        )
    ) {
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = activityColor,
                width = 14f,
                jointType = JointType.ROUND,
                startCap = RoundCap(),
                endCap = RoundCap()
            )
        }

        // Start marker
        routePoints.firstOrNull()?.let { start ->
            Circle(
                center = start,
                radius = 10.0,
                fillColor = FitTrackColors.GreenSuccess,
                strokeColor = Color.White,
                strokeWidth = 3f
            )
        }

        // End marker
        routePoints.lastOrNull()?.let { end ->
            Circle(
                center = end,
                radius = 10.0,
                fillColor = FitTrackColors.Coral,
                strokeColor = Color.White,
                strokeWidth = 3f
            )
        }
    }
}

@Composable
fun DetailStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    CircleShape
                )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
        }
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