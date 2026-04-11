package com.example.myhealthtracker.ui.weight

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.data.local.entity.WeightLogEntity
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.WeightViewModel

@Composable
fun WeightScreen(
    onBack: () -> Unit,
    viewModel: WeightViewModel = hiltViewModel()
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
                text = "Weight Tracker",
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
            // ── Current Weight Card ──
            CurrentWeightCard(
                latestWeight = uiState.latestWeight,
                latestBmi = uiState.latestBmi,
                bmiCategory = uiState.bmiCategory,
                weightChange = uiState.weightChange,
                isGaining = uiState.isGaining
            )

            // ── Log Weight Card ──
            LogWeightCard(
                newWeight = uiState.newWeight,
                newNote = uiState.newNote,
                onWeightChange = viewModel::updateNewWeight,
                onNoteChange = viewModel::updateNewNote,
                onLog = viewModel::logWeight,
                savedSuccess = uiState.savedSuccess
            )

            // ── Weight History Chart ──
            if (uiState.logs.size >= 2) {
                WeightChartCard(logs = uiState.logs)
            }

            // ── Weight History List ──
            if (uiState.logs.isNotEmpty()) {
                WeightHistoryList(
                    logs = uiState.logs,
                    onDelete = viewModel::deleteLog,
                    formatDate = viewModel::formatDate
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun CurrentWeightCard(
    latestWeight: Double,
    latestBmi: Double,
    bmiCategory: String,
    weightChange: Double,
    isGaining: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FitTrackColors.SurfaceCard
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FitTrackColors.TealContainer.copy(alpha = 0.5f),
                            FitTrackColors.SurfaceCard
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Current Weight",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitTrackColors.TextSecondary
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (latestWeight > 0)
                            String.format("%.1f", latestWeight)
                        else "--",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = FitTrackColors.TextPrimary
                    )
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.titleLarge,
                        color = FitTrackColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Weight change indicator
                if (latestWeight > 0 && weightChange != 0.0) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isGaining)
                                Icons.Default.TrendingUp
                            else
                                Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = if (isGaining)
                                FitTrackColors.Coral
                            else
                                FitTrackColors.GreenSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${if (isGaining) "+" else ""}${
                                String.format("%.1f", weightChange)
                            } kg since last entry",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isGaining)
                                FitTrackColors.Coral
                            else
                                FitTrackColors.GreenSuccess
                        )
                    }
                }

                // BMI row
                if (latestBmi > 0) {
                    HorizontalDivider(color = FitTrackColors.SurfaceBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeightStatItem(
                            label = "BMI",
                            value = String.format("%.1f", latestBmi),
                            color = getBmiColor(latestBmi)
                        )
                        WeightStatItem(
                            label = "Category",
                            value = bmiCategory,
                            color = getBmiColor(latestBmi)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeightStatItem(
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

fun getBmiColor(bmi: Double): Color = when {
    bmi < 18.5 -> FitTrackColors.Amber
    bmi < 25.0 -> FitTrackColors.GreenSuccess
    bmi < 30.0 -> FitTrackColors.Coral
    else -> Color(0xFFFF3333)
}

@Composable
fun LogWeightCard(
    newWeight: String,
    newNote: String,
    onWeightChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onLog: () -> Unit,
    savedSuccess: Boolean
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Log Weight",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitTrackColors.TextPrimary
            )

            OutlinedTextField(
                value = newWeight,
                onValueChange = onWeightChange,
                label = { Text("Weight (kg)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.MonitorWeight,
                        contentDescription = null,
                        tint = FitTrackColors.TealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.TealPrimary,
                    unfocusedBorderColor = FitTrackColors.SurfaceBorder,
                    focusedLabelColor = FitTrackColors.TealPrimary,
                    unfocusedLabelColor = FitTrackColors.TextSecondary,
                    cursorColor = FitTrackColors.TealPrimary,
                    focusedTextColor = FitTrackColors.TextPrimary,
                    unfocusedTextColor = FitTrackColors.TextPrimary
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = newNote,
                onValueChange = onNoteChange,
                label = { Text("Note (optional)") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = null,
                        tint = FitTrackColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.TealPrimary,
                    unfocusedBorderColor = FitTrackColors.SurfaceBorder,
                    focusedLabelColor = FitTrackColors.TealPrimary,
                    unfocusedLabelColor = FitTrackColors.TextSecondary,
                    cursorColor = FitTrackColors.TealPrimary,
                    focusedTextColor = FitTrackColors.TextPrimary,
                    unfocusedTextColor = FitTrackColors.TextPrimary
                ),
                singleLine = true
            )

            Button(
                onClick = onLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitTrackColors.TealPrimary,
                    contentColor = Color(0xFF001A17)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Weight",
                    fontWeight = FontWeight.Bold
                )
            }

            if (savedSuccess) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = FitTrackColors.GreenSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Weight logged successfully!",
                        style = MaterialTheme.typography.bodySmall,
                        color = FitTrackColors.GreenSuccess
                    )
                }
            }
        }
    }
}

@Composable
fun WeightChartCard(logs: List<WeightLogEntity>) {
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

            val recentLogs = logs.take(10).reversed()
            val maxWeight = recentLogs.maxOf { it.weightKg }
            val minWeight = recentLogs.minOf { it.weightKg }
            val range = (maxWeight - minWeight).coerceAtLeast(1.0)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                recentLogs.forEach { log ->
                    val normalizedHeight = ((log.weightKg - minWeight) / range).toFloat()
                    val barHeight = (0.3f + normalizedHeight * 0.7f)
                    val isLatest = log == recentLogs.last()

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 2.dp)
                            .fillMaxHeight(barHeight)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = if (isLatest) listOf(
                                        FitTrackColors.TealPrimary,
                                        FitTrackColors.TealSecondary
                                    ) else listOf(
                                        FitTrackColors.TextDisabled,
                                        FitTrackColors.SurfaceBorder
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
fun WeightHistoryList(
    logs: List<WeightLogEntity>,
    onDelete: (WeightLogEntity) -> Unit,
    formatDate: (String) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "History",
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
                logs.take(15).forEach { log ->
                    WeightHistoryItem(
                        log = log,
                        onDelete = { onDelete(log) },
                        formatDate = formatDate
                    )
                    if (log != logs.take(15).last()) {
                        HorizontalDivider(
                            color = FitTrackColors.SurfaceBorder,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeightHistoryItem(
    log: WeightLogEntity,
    onDelete: () -> Unit,
    formatDate: (String) -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
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
                    .size(40.dp)
                    .background(
                        FitTrackColors.TealContainer,
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.MonitorWeight,
                    contentDescription = null,
                    tint = FitTrackColors.TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = "${String.format("%.1f", log.weightKg)} kg",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FitTrackColors.TextPrimary
                )
                Text(
                    text = formatDate(log.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
                if (!log.note.isNullOrEmpty()) {
                    Text(
                        text = log.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.TextDisabled
                    )
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (log.bmi != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getBmiColor(log.bmi).copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "BMI ${String.format("%.1f", log.bmi)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = getBmiColor(log.bmi),
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
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
}