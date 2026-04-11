package com.example.myhealthtracker.ui.water

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.data.local.entity.WaterLogEntity
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.WaterViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.foundation.Canvas

@Composable
fun WaterScreen(
    onBack: () -> Unit,
    viewModel: WaterViewModel = hiltViewModel()
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
                text = "Hydration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = FitTrackColors.TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Water Ring ──
            WaterRingCard(
                totalMl = uiState.totalMl,
                goalMl = uiState.goalMl,
                progress = uiState.progress
            )

            // ── Quick Add Buttons ──
            QuickAddSection(onAdd = viewModel::addWater)

            // ── Custom Amount ──
            CustomAmountSection(
                customAmount = uiState.customAmount,
                onAmountChange = viewModel::updateCustomAmount,
                onAdd = viewModel::addCustomAmount
            )

            // ── Today's Log ──
            if (uiState.todayLogs.isNotEmpty()) {
                TodayLogSection(
                    logs = uiState.todayLogs,
                    onDelete = viewModel::deleteLog
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun WaterRingCard(
    totalMl: Int,
    goalMl: Int,
    progress: Float
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "water_progress"
    )

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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Today's Intake",
                style = MaterialTheme.typography.titleMedium,
                color = FitTrackColors.TextSecondary
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(180.dp)
            ) {
                Canvas(modifier = Modifier.size(180.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background track
                    drawCircle(
                        color = FitTrackColors.SurfaceBorder,
                        radius = radius,
                        center = center,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )

                    // Progress arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(
                                FitTrackColors.Amber.copy(alpha = 0.6f),
                                FitTrackColors.Amber,
                                FitTrackColors.Amber
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
                        text = "${totalMl}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = FitTrackColors.TextPrimary
                    )
                    Text(
                        text = "ml",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitTrackColors.Amber
                    )
                    Text(
                        text = "of ${goalMl}ml",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.TextSecondary
                    )
                }
            }

            // Progress info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                WaterStatChip(
                    label = "Remaining",
                    value = "${(goalMl - totalMl).coerceAtLeast(0)}ml",
                    color = FitTrackColors.Amber
                )
                WaterStatChip(
                    label = "Progress",
                    value = "${(progress * 100).toInt()}%",
                    color = FitTrackColors.TealPrimary
                )
                WaterStatChip(
                    label = "Glasses",
                    value = "${totalMl / 250}",
                    color = FitTrackColors.Purple
                )
            }
        }
    }
}

@Composable
fun WaterStatChip(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = FitTrackColors.TextSecondary
        )
    }
}

@Composable
fun QuickAddSection(onAdd: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Quick Add",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FitTrackColors.TextPrimary
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf(
                Pair(150, "Small\n150ml"),
                Pair(250, "Glass\n250ml"),
                Pair(350, "Bottle\n350ml"),
                Pair(500, "Large\n500ml")
            ).forEach { (amount, label) ->
                QuickAddButton(
                    label = label,
                    amount = amount,
                    onClick = { onAdd(amount) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickAddButton(
    label: String,
    amount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.AmberDim
        ),
        border = BorderStroke(1.dp, FitTrackColors.Amber.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = FitTrackColors.Amber,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = FitTrackColors.Amber,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CustomAmountSection(
    customAmount: String,
    onAmountChange: (String) -> Unit,
    onAdd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = customAmount,
                onValueChange = onAmountChange,
                label = { Text("Custom (ml)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.Amber,
                    unfocusedBorderColor = FitTrackColors.SurfaceBorder,
                    focusedLabelColor = FitTrackColors.Amber,
                    unfocusedLabelColor = FitTrackColors.TextSecondary,
                    cursorColor = FitTrackColors.Amber,
                    focusedTextColor = FitTrackColors.TextPrimary,
                    unfocusedTextColor = FitTrackColors.TextPrimary
                ),
                singleLine = true
            )
            Button(
                onClick = onAdd,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitTrackColors.Amber,
                    contentColor = Color(0xFF3D2800)
                ),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TodayLogSection(
    logs: List<WaterLogEntity>,
    onDelete: (WaterLogEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Today's Log",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = FitTrackColors.TextPrimary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = FitTrackColors.SurfaceCard
            )
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                logs.reversed().forEach { log ->
                    WaterLogItem(
                        log = log,
                        onDelete = { onDelete(log) }
                    )
                }
            }
        }
    }
}

@Composable
fun WaterLogItem(
    log: WaterLogEntity,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = timeFormat.format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        FitTrackColors.AmberDim,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = null,
                    tint = FitTrackColors.Amber,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = "${log.amountMl} ml",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FitTrackColors.TextPrimary
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Delete",
                tint = FitTrackColors.TextDisabled,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}