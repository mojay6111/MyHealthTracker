package com.example.myhealthtracker.ui.session

import android.Manifest
import androidx.compose.animation.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.ActiveSessionViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.android.compose.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActiveSessionScreen(
    activityType: String,
    onFinish: () -> Unit,
    viewModel: ActiveSessionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        if (locationPermissions.allPermissionsGranted) {
            viewModel.startSession(activityType)
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(locationPermissions.allPermissionsGranted) {
        if (locationPermissions.allPermissionsGranted && !uiState.isTracking) {
            viewModel.startSession(activityType)
        }
    }

    LaunchedEffect(uiState.isFinished) {
        if (uiState.isFinished) {
            kotlinx.coroutines.delay(1500)
            onFinish()
        }
    }

    val activityColor = when (activityType) {
        "RUN" -> FitTrackColors.Coral
        "CYCLE" -> FitTrackColors.Amber
        "HIKE" -> FitTrackColors.GreenSuccess
        else -> FitTrackColors.TealPrimary
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Full Screen Map ──
        if (locationPermissions.allPermissionsGranted) {
            LiveMapView(
                routePoints = uiState.routePoints,
                currentLatLng = uiState.currentLatLng,
                activityColor = activityColor
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(FitTrackColors.Background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = FitTrackColors.TextSecondary,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Location permission required",
                        style = MaterialTheme.typography.titleMedium,
                        color = FitTrackColors.TextPrimary
                    )
                    Button(
                        onClick = {
                            locationPermissions.launchMultiplePermissionRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FitTrackColors.TealPrimary
                        )
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        }

        // ── Top Bar overlay ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        FitTrackColors.SurfaceCard.copy(alpha = 0.9f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = {
                    viewModel.stopSession()
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Stop",
                        tint = FitTrackColors.TextPrimary
                    )
                }
            }

            // Activity type + detected badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = activityColor.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activityIcon = when (uiState.detectedActivity) {
                        "RUN" -> Icons.Default.DirectionsRun
                        "CYCLE" -> Icons.Default.DirectionsBike
                        else -> Icons.Default.DirectionsWalk
                    }
                    Icon(
                        imageVector = activityIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = uiState.detectedActivity
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // GPS indicator
            GpsIndicator(hasLocation = uiState.currentLatLng != null)
        }

        // ── Bottom Stats Card ──
        SessionStatsCard(
            modifier = Modifier.align(Alignment.BottomCenter),
            uiState = uiState,
            activityColor = activityColor,
            onPause = viewModel::pauseSession,
            onResume = viewModel::resumeSession,
            onStop = viewModel::stopSession
        )
    }
}

@Composable
fun LiveMapView(
    routePoints: List<LatLng>,
    currentLatLng: LatLng?,
    activityColor: Color
) {
    val defaultLocation = LatLng(-1.286389, 36.817223) // Nairobi
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            currentLatLng ?: defaultLocation,
            16f
        )
    }

    // Follow user location
    LaunchedEffect(currentLatLng) {
        currentLatLng?.let { latLng ->
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(latLng, 17f),
                durationMs = 1000
            )
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = MapType.NORMAL
        ),
        uiSettings = MapUiSettings(
            myLocationButtonEnabled = false,
            zoomControlsEnabled = false,
            compassEnabled = true
        )
    ) {
        // Draw route polyline
        if (routePoints.size >= 2) {
            Polyline(
                points = routePoints,
                color = activityColor,
                width = 12f,
                jointType = JointType.ROUND,
                startCap = RoundCap(),
                endCap = RoundCap()
            )
        }

        // Current position marker
        currentLatLng?.let { latLng ->
            Circle(
                center = latLng,
                radius = 8.0,
                fillColor = activityColor,
                strokeColor = Color.White,
                strokeWidth = 3f
            )
        }
    }
}

@Composable
fun GpsIndicator(hasLocation: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "gps")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gps_blink"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .background(
                FitTrackColors.SurfaceCard.copy(alpha = 0.9f),
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.GpsFixed,
            contentDescription = "GPS",
            tint = if (hasLocation)
                FitTrackColors.GreenSuccess
            else
                FitTrackColors.Coral.copy(alpha = alpha),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SessionStatsCard(
    modifier: Modifier = Modifier,
    uiState: com.example.myhealthtracker.ui.viewmodel.ActiveSessionUiState,
    activityColor: Color,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard.copy(alpha = 0.97f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Duration — big display
            Text(
                text = uiState.formattedDuration,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = activityColor,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SessionStatItem(
                    icon = Icons.Default.Route,
                    value = uiState.formattedDistance,
                    label = "Distance",
                    color = activityColor
                )
                SessionStatDivider()
                SessionStatItem(
                    icon = Icons.Default.Speed,
                    value = uiState.formattedSpeed,
                    label = "Speed",
                    color = FitTrackColors.Purple
                )
                SessionStatDivider()
                SessionStatItem(
                    icon = Icons.Default.LocalFireDepartment,
                    value = uiState.formattedCalories,
                    label = "Calories",
                    color = FitTrackColors.Coral
                )
            }

            // Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pause / Resume
                OutlinedButton(
                    onClick = if (uiState.isPaused) onResume else onPause,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = activityColor
                    ),
                    border = BorderStroke(1.5.dp, activityColor)
                ) {
                    Icon(
                        imageVector = if (uiState.isPaused)
                            Icons.Default.PlayArrow
                        else
                            Icons.Default.Pause,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (uiState.isPaused) "Resume" else "Pause",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Stop button
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FitTrackColors.Coral,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Finish",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Paused indicator
            AnimatedVisibility(visible = uiState.isPaused) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = null,
                        tint = FitTrackColors.Amber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Session paused",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitTrackColors.Amber
                    )
                }
            }
        }
    }
}

@Composable
fun SessionStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
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

@Composable
fun SessionStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(FitTrackColors.SurfaceBorder)
    )
}