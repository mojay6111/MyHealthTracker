package com.example.myhealthtracker.ui.share

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.theme.FitTrackTheme
import com.example.myhealthtracker.ui.viewmodel.ShareCardViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShareCardScreen(
    onBack: () -> Unit,
    viewModel: ShareCardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var cardBitmap by remember { mutableStateOf<Bitmap?>(null) }

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
                    text = "Share Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your daily progress card",
                style = MaterialTheme.typography.bodyMedium,
                color = FitTrackColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            // ── Share Card Preview ──
            ShareCard(
                userName = uiState.userName,
                steps = uiState.formattedSteps,
                stepGoal = uiState.stepGoal,
                progress = uiState.progressFraction,
                calories = uiState.formattedCalories,
                distance = uiState.formattedDistance,
                date = uiState.todayDate,
                onBitmapReady = { bitmap ->
                    cardBitmap = bitmap
                }
            )

            // ── Share Button ──
            Button(
                onClick = {
                    cardBitmap?.let { viewModel.shareCard(it) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitTrackColors.TealPrimary,
                    contentColor = Color(0xFF001A17)
                ),
                enabled = !uiState.isSharing
            ) {
                if (uiState.isSharing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF001A17),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Share Card",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // Hint
            Text(
                text = "Share to Instagram, WhatsApp, or anywhere!",
                style = MaterialTheme.typography.labelSmall,
                color = FitTrackColors.TextDisabled,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ShareCard(
    userName: String,
    steps: String,
    stepGoal: Int,
    progress: Float,
    calories: String,
    distance: String,
    date: String,
    onBitmapReady: (Bitmap) -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "share_progress"
    )

    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.85f)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF0A1520),
                        Color(0xFF0F2030)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 1000f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        FitTrackColors.TealPrimary.copy(alpha = 0.5f),
                        FitTrackColors.TealSecondary.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(28.dp)
            )
    ) {
        // Background glow effect
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-60).dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FitTrackColors.TealDim,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "FitTrack",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = FitTrackColors.TealPrimary
                    )
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelMedium,
                        color = FitTrackColors.TextSecondary
                    )
                }
                // App icon circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            FitTrackColors.TealContainer,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = FitTrackColors.TealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // ── Name ──
            Column {
                Text(
                    text = if (userName.isNotEmpty()) userName else "Athlete",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary
                )
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.bodySmall,
                    color = FitTrackColors.TextSecondary
                )
            }

            // ── Step Ring ──
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(160.dp)
                    .align(Alignment.CenterHorizontally)
            ) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Track
                    drawCircle(
                        color = FitTrackColors.SurfaceBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    // Progress
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                FitTrackColors.TealSecondary,
                                FitTrackColors.TealPrimary,
                                FitTrackColors.TealPrimary
                            ),
                            center = center
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        ),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        )
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = steps,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = FitTrackColors.TextPrimary
                    )
                    Text(
                        text = "steps",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.TealPrimary
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% of goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.TextSecondary
                    )
                }
            }

            // ── Stats Row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareStatItem(
                    emoji = "🔥",
                    value = calories,
                    label = "Burned"
                )
                ShareStatDivider()
                ShareStatItem(
                    emoji = "📍",
                    value = distance,
                    label = "Distance"
                )
                ShareStatDivider()
                ShareStatItem(
                    emoji = "🎯",
                    value = "%,d".format(stepGoal),
                    label = "Goal"
                )
            }

            // ── Footer ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tracked with ",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextDisabled
                )
                Text(
                    text = "FitTrack",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TealPrimary
                )
                Text(
                    text = " 💪",
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextDisabled
                )
            }
        }
    }
}

@Composable
fun ShareStatItem(
    emoji: String,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
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
fun ShareStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(FitTrackColors.SurfaceBorder)
    )
}