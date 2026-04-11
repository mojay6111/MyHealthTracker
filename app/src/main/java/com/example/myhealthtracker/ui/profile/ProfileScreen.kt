package com.example.myhealthtracker.ui.profile

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ──
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            FitTrackColors.TealContainer,
                            FitTrackColors.Background
                        )
                    )
                )
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = FitTrackColors.TextPrimary
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    FitTrackColors.TealPrimary,
                                    FitTrackColors.TealSecondary
                                )
                            ),
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = if (uiState.name.isNotEmpty())
                            uiState.name.first().uppercase()
                        else "?",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF001A17)
                    )
                }

                Text(
                    text = if (uiState.name.isNotEmpty()) uiState.name else "Your Name",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary
                )

                // BMI badge
                if (uiState.bmi > 0) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = FitTrackColors.TealContainer
                    ) {
                        Text(
                            text = "BMI ${String.format("%.1f", uiState.bmi)} · ${uiState.bmiCategory}",
                            style = MaterialTheme.typography.labelMedium,
                            color = FitTrackColors.TealPrimary,
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 6.dp
                            )
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Personal Info ──
            ProfileSection(title = "Personal Info") {
                ProfileTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = "Full Name",
                    icon = Icons.Default.Person,
                    keyboardType = KeyboardType.Text
                )
                ProfileTextField(
                    value = uiState.age,
                    onValueChange = viewModel::updateAge,
                    label = "Age",
                    icon = Icons.Default.Cake,
                    keyboardType = KeyboardType.Number
                )
                // Sex selector
                Text(
                    text = "Biological Sex",
                    style = MaterialTheme.typography.labelLarge,
                    color = FitTrackColors.TextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MALE", "FEMALE", "OTHER").forEach { option ->
                        val isSelected = uiState.sex == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateSex(option) },
                            label = {
                                Text(
                                    text = option.lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FitTrackColors.TealContainer,
                                selectedLabelColor = FitTrackColors.TealPrimary
                            )
                        )
                    }
                }
            }

            // ── Body Metrics ──
            ProfileSection(title = "Body Metrics") {
                ProfileTextField(
                    value = uiState.heightCm,
                    onValueChange = viewModel::updateHeight,
                    label = "Height (cm)",
                    icon = Icons.Default.Height,
                    keyboardType = KeyboardType.Number
                )
                ProfileTextField(
                    value = uiState.weightKg,
                    onValueChange = viewModel::updateWeight,
                    label = "Weight (kg)",
                    icon = Icons.Default.MonitorWeight,
                    keyboardType = KeyboardType.Decimal
                )
            }

            // ── Daily Goals ──
            ProfileSection(title = "Daily Goals") {
                ProfileTextField(
                    value = uiState.stepGoal,
                    onValueChange = viewModel::updateStepGoal,
                    label = "Step Goal",
                    icon = Icons.Default.DirectionsWalk,
                    keyboardType = KeyboardType.Number
                )
                ProfileTextField(
                    value = uiState.waterGoalMl,
                    onValueChange = viewModel::updateWaterGoal,
                    label = "Water Goal (ml)",
                    icon = Icons.Default.WaterDrop,
                    keyboardType = KeyboardType.Number
                )
            }

            // ── Notifications ──
            ProfileSection(title = "Notifications") {
                ProfileToggleRow(
                    title = "Enable Notifications",
                    subtitle = "Step goals and reminders",
                    icon = Icons.Default.Notifications,
                    checked = uiState.notificationsEnabled,
                    onCheckedChange = viewModel::updateNotifications
                )
                ProfileToggleRow(
                    title = "Sedentary Alerts",
                    subtitle = "Alert when inactive too long",
                    icon = Icons.Default.AccessTime,
                    checked = uiState.sedentaryAlertEnabled,
                    onCheckedChange = viewModel::updateSedentaryAlert
                )
                ProfileToggleRow(
                    title = "Hydration Reminders",
                    subtitle = "Remind to drink water",
                    icon = Icons.Default.WaterDrop,
                    checked = uiState.hydrationReminders,
                    onCheckedChange = viewModel::updateHydrationReminders
                )
            }

            // ── Units ──
            ProfileSection(title = "Units") {
                Text(
                    text = "Measurement System",
                    style = MaterialTheme.typography.labelLarge,
                    color = FitTrackColors.TextSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("METRIC", "IMPERIAL").forEach { option ->
                        val isSelected = uiState.unitSystem == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateUnitSystem(option) },
                            label = {
                                Text(
                                    text = option.lowercase()
                                        .replaceFirstChar { it.uppercase() }
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FitTrackColors.TealContainer,
                                selectedLabelColor = FitTrackColors.TealPrimary
                            )
                        )
                    }
                }
            }

            // ── Save Button ──
            Button(
                onClick = viewModel::saveProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FitTrackColors.TealPrimary,
                    contentColor = Color(0xFF001A17)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Changes",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Success message
            if (uiState.savedSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = FitTrackColors.GreenDim
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FitTrackColors.GreenSuccess
                        )
                        Text(
                            text = "Profile saved successfully!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = FitTrackColors.GreenSuccess
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ProfileSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = FitTrackColors.TealPrimary
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = FitTrackColors.SurfaceCard
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                content = content
            )
        }
    }
}

@Composable
fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FitTrackColors.TealPrimary,
                modifier = Modifier.size(20.dp)
            )
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
}

@Composable
fun ProfileToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        FitTrackColors.TealContainer,
                        RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = FitTrackColors.TealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = FitTrackColors.TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = FitTrackColors.TextSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF001A17),
                checkedTrackColor = FitTrackColors.TealPrimary,
                uncheckedThumbColor = FitTrackColors.TextDisabled,
                uncheckedTrackColor = FitTrackColors.SurfaceBorder
            )
        )
    }
}