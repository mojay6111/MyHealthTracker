package com.example.myhealthtracker.ui.sleep

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.data.local.entity.SleepLogEntity
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.SleepViewModel

@Composable
fun SleepScreen(
    onBack: () -> Unit,
    viewModel: SleepViewModel = hiltViewModel()
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
                text = "Sleep Tracker",
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
            // ── Sleep Summary Card ──
            SleepSummaryCard(
                avgDuration = uiState.avgDuration,
                avgQuality = uiState.avgQuality,
                totalLogs = uiState.logs.size,
                formatDuration = viewModel::formatDuration
            )

            // ── Log Sleep Card ──
            LogSleepCard(
                bedTime = uiState.bedTime,
                wakeTime = uiState.wakeTime,
                qualityRating = uiState.qualityRating,
                notes = uiState.notes,
                savedSuccess = uiState.savedSuccess,
                onBedTimeChange = viewModel::updateBedTime,
                onWakeTimeChange = viewModel::updateWakeTime,
                onQualityChange = viewModel::updateQuality,
                onNotesChange = viewModel::updateNotes,
                onLog = viewModel::logSleep,
                getQualityLabel = viewModel::getQualityLabel,
                getQualityColor = viewModel::getQualityColor
            )

            // ── Sleep History ──
            if (uiState.logs.isNotEmpty()) {
                SleepHistorySection(
                    logs = uiState.logs,
                    onDelete = viewModel::deleteLog,
                    formatDate = viewModel::formatDate,
                    formatDuration = viewModel::formatDuration,
                    getQualityLabel = viewModel::getQualityLabel,
                    getQualityColor = viewModel::getQualityColor
                )
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun SleepSummaryCard(
    avgDuration: Int,
    avgQuality: Float,
    totalLogs: Int,
    formatDuration: (Int) -> String
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
                            FitTrackColors.PurpleDim,
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
                    text = "Sleep Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = FitTrackColors.TextSecondary
                )

                // Moon icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            FitTrackColors.PurpleDim,
                            CircleShape
                        )
                ) {
                    Text(
                        text = "🌙",
                        fontSize = 32.sp
                    )
                }

                if (avgDuration > 0) {
                    Text(
                        text = formatDuration(avgDuration),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = FitTrackColors.TextPrimary
                    )
                    Text(
                        text = "Average sleep duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = FitTrackColors.TextSecondary
                    )

                    HorizontalDivider(color = FitTrackColors.SurfaceBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SleepStatItem(
                            value = "${"%.1f".format(avgQuality)}/5",
                            label = "Avg Quality",
                            color = FitTrackColors.Purple
                        )
                        SleepStatItem(
                            value = "$totalLogs",
                            label = "Nights Logged",
                            color = FitTrackColors.TealPrimary
                        )
                        SleepStatItem(
                            value = if (avgDuration >= 420) "✓" else "✗",
                            label = "7h+ Goal",
                            color = if (avgDuration >= 420)
                                FitTrackColors.GreenSuccess
                            else FitTrackColors.Coral
                        )
                    }
                } else {
                    Text(
                        text = "No sleep data yet.\nLog your first night below!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = FitTrackColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun SleepStatItem(
    value: String,
    label: String,
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
fun LogSleepCard(
    bedTime: String,
    wakeTime: String,
    qualityRating: Int,
    notes: String,
    savedSuccess: Boolean,
    onBedTimeChange: (String) -> Unit,
    onWakeTimeChange: (String) -> Unit,
    onQualityChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onLog: () -> Unit,
    getQualityLabel: (Int) -> String,
    getQualityColor: (Int) -> Long
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
                text = "Log Last Night",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = FitTrackColors.TextPrimary
            )

            // Bed time + Wake time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🌙 Bed Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitTrackColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = bedTime,
                        onValueChange = onBedTimeChange,
                        placeholder = { Text("22:00") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitTrackColors.Purple,
                            unfocusedBorderColor = FitTrackColors.SurfaceBorder,
                            focusedTextColor = FitTrackColors.TextPrimary,
                            unfocusedTextColor = FitTrackColors.TextPrimary,
                            cursorColor = FitTrackColors.Purple
                        ),
                        singleLine = true
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "☀️ Wake Time",
                        style = MaterialTheme.typography.labelMedium,
                        color = FitTrackColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    OutlinedTextField(
                        value = wakeTime,
                        onValueChange = onWakeTimeChange,
                        placeholder = { Text("06:00") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FitTrackColors.Amber,
                            unfocusedBorderColor = FitTrackColors.SurfaceBorder,
                            focusedTextColor = FitTrackColors.TextPrimary,
                            unfocusedTextColor = FitTrackColors.TextPrimary,
                            cursorColor = FitTrackColors.Amber
                        ),
                        singleLine = true
                    )
                }
            }

            // Quality Rating
            Text(
                text = "Sleep Quality — ${getQualityLabel(qualityRating)}",
                style = MaterialTheme.typography.labelLarge,
                color = Color(getQualityColor(qualityRating))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..5).forEach { rating ->
                    val isSelected = rating <= qualityRating
                    IconButton(
                        onClick = { onQualityChange(rating) },
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                if (isSelected)
                                    Color(getQualityColor(qualityRating))
                                        .copy(alpha = 0.2f)
                                else
                                    FitTrackColors.SurfaceBorder,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Text(
                            text = when (rating) {
                                1 -> "😴"
                                2 -> "😪"
                                3 -> "🙂"
                                4 -> "😊"
                                else -> "🌟"
                            },
                            fontSize = 20.sp
                        )
                    }
                }
            }

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Notes (optional)") },
                placeholder = { Text("How did you feel?") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FitTrackColors.Purple,
                    unfocusedBorderColor = FitTrackColors.SurfaceBorder,
                    focusedLabelColor = FitTrackColors.Purple,
                    unfocusedLabelColor = FitTrackColors.TextSecondary,
                    cursorColor = FitTrackColors.Purple,
                    focusedTextColor = FitTrackColors.TextPrimary,
                    unfocusedTextColor = FitTrackColors.TextPrimary
                ),
                maxLines = 3
            )

            // Log Button
            Button(
                onClick = onLog,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitTrackColors.Purple,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Bedtime,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Sleep",
                    fontWeight = FontWeight.Bold
                )
            }

            if (savedSuccess) {
                Row(
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
                        text = "Sleep logged successfully!",
                        style = MaterialTheme.typography.bodySmall,
                        color = FitTrackColors.GreenSuccess
                    )
                }
            }
        }
    }
}

@Composable
fun SleepHistorySection(
    logs: List<SleepLogEntity>,
    onDelete: (SleepLogEntity) -> Unit,
    formatDate: (String) -> String,
    formatDuration: (Int) -> String,
    getQualityLabel: (Int) -> String,
    getQualityColor: (Int) -> Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Recent Nights",
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
                logs.forEach { log ->
                    SleepLogItem(
                        log = log,
                        onDelete = { onDelete(log) },
                        formatDate = formatDate,
                        formatDuration = formatDuration,
                        getQualityLabel = getQualityLabel,
                        getQualityColor = getQualityColor
                    )
                    if (log != logs.last()) {
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
fun SleepLogItem(
    log: SleepLogEntity,
    onDelete: () -> Unit,
    formatDate: (String) -> String,
    formatDuration: (Int) -> String,
    getQualityLabel: (Int) -> String,
    getQualityColor: (Int) -> Long
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
                    .size(44.dp)
                    .background(
                        FitTrackColors.PurpleDim,
                        CircleShape
                    )
            ) {
                Text(text = "🌙", fontSize = 20.sp)
            }
            Column {
                Text(
                    text = formatDate(log.date),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FitTrackColors.TextPrimary
                )
                Text(
                    text = formatDuration(log.durationMinutes),
                    style = MaterialTheme.typography.labelMedium,
                    color = FitTrackColors.Purple
                )
                Text(
                    text = getQualityLabel(log.qualityRating),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(getQualityColor(log.qualityRating))
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