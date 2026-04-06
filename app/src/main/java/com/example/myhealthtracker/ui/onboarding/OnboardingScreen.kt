package com.example.myhealthtracker.ui.onboarding

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FitTrackColors.Background)
    ) {
        // Background gradient glow
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Step indicator dots
            StepIndicator(
                totalSteps = 4,
                currentStep = uiState.currentStep
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Animated page content
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                                slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                                slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "onboarding_step"
            ) { step ->
                when (step) {
                    0 -> WelcomeStep()
                    1 -> NameAgeStep(
                        name = uiState.name,
                        age = uiState.age,
                        onNameChange = viewModel::updateName,
                        onAgeChange = viewModel::updateAge
                    )
                    2 -> BodyMetricsStep(
                        heightCm = uiState.heightCm,
                        weightKg = uiState.weightKg,
                        sex = uiState.sex,
                        onHeightChange = viewModel::updateHeight,
                        onWeightChange = viewModel::updateWeight,
                        onSexChange = viewModel::updateSex
                    )
                    3 -> GoalsStep(
                        stepGoal = uiState.stepGoal,
                        waterGoalMl = uiState.waterGoalMl,
                        onStepGoalChange = viewModel::updateStepGoal,
                        onWaterGoalChange = viewModel::updateWaterGoal
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (uiState.currentStep > 0) {
                    OutlinedButton(
                        onClick = viewModel::previousStep,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = FitTrackColors.TextSecondary
                        ),
                        border = BorderStroke(1.dp, FitTrackColors.SurfaceBorder)
                    ) {
                        Text("Back")
                    }
                }

                Button(
                    onClick = {
                        if (uiState.currentStep == 3) {
                            viewModel.completeOnboarding(onComplete)
                        } else {
                            viewModel.nextStep()
                        }
                    },
                    modifier = Modifier.weight(if (uiState.currentStep > 0) 2f else 1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FitTrackColors.TealPrimary,
                        contentColor = Color(0xFF001A17)
                    )
                ) {
                    Text(
                        text = if (uiState.currentStep == 3) "Let's Go! 🚀" else "Continue",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StepIndicator(totalSteps: Int, currentStep: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isPast = index < currentStep
            val width by animateDpAsState(
                targetValue = if (isActive) 32.dp else 8.dp,
                animationSpec = tween(300),
                label = "indicator_width"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            isActive -> FitTrackColors.TealPrimary
                            isPast -> FitTrackColors.TealSecondary
                            else -> FitTrackColors.SurfaceBorder
                        }
                    )
            )
        }
    }
}

@Composable
fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // App icon glow
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            FitTrackColors.TealDim,
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(80.dp)
                    .background(FitTrackColors.TealContainer, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.DirectionsRun,
                    contentDescription = null,
                    tint = FitTrackColors.TealPrimary,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Text(
            text = "FitTrack",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = FitTrackColors.TextPrimary
        )

        Text(
            text = "Your personal health companion.\nTrack steps, routes, water & more.",
            style = MaterialTheme.typography.bodyLarge,
            color = FitTrackColors.TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Feature chips
        listOf(
            Pair("👟", "Step Counter"),
            Pair("🗺️", "GPS Routes"),
            Pair("💧", "Hydration"),
            Pair("📊", "Analytics")
        ).chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { (emoji, label) ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = FitTrackColors.SurfaceCard
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = 16.dp,
                                vertical = 10.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = emoji, fontSize = 16.sp)
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                color = FitTrackColors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NameAgeStep(
    name: String,
    age: String,
    onNameChange: (String) -> Unit,
    onAgeChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OnboardingStepHeader(
            icon = Icons.Default.Person,
            title = "What's your name?",
            subtitle = "Let's make this personal"
        )

        OnboardingTextField(
            value = name,
            onValueChange = onNameChange,
            label = "Your name",
            placeholder = "e.g. Alex",
            keyboardType = KeyboardType.Text
        )

        OnboardingTextField(
            value = age,
            onValueChange = onAgeChange,
            label = "Your age",
            placeholder = "e.g. 25",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun BodyMetricsStep(
    heightCm: String,
    weightKg: String,
    sex: String,
    onHeightChange: (String) -> Unit,
    onWeightChange: (String) -> Unit,
    onSexChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OnboardingStepHeader(
            icon = Icons.Default.FitnessCenter,
            title = "Body Metrics",
            subtitle = "Used to calculate accurate calories"
        )

        OnboardingTextField(
            value = heightCm,
            onValueChange = onHeightChange,
            label = "Height (cm)",
            placeholder = "e.g. 175",
            keyboardType = KeyboardType.Number
        )

        OnboardingTextField(
            value = weightKg,
            onValueChange = onWeightChange,
            label = "Weight (kg)",
            placeholder = "e.g. 70",
            keyboardType = KeyboardType.Decimal
        )

        // Sex selector
        Text(
            text = "Biological sex",
            style = MaterialTheme.typography.labelLarge,
            color = FitTrackColors.TextSecondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("MALE", "FEMALE", "OTHER").forEach { option ->
                val isSelected = sex == option
                FilterChip(
                    selected = isSelected,
                    onClick = { onSexChange(option) },
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
}

@Composable
fun GoalsStep(
    stepGoal: String,
    waterGoalMl: String,
    onStepGoalChange: (String) -> Unit,
    onWaterGoalChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        OnboardingStepHeader(
            icon = Icons.Default.Flag,
            title = "Set Your Goals",
            subtitle = "You can always change these later"
        )

        OnboardingTextField(
            value = stepGoal,
            onValueChange = onStepGoalChange,
            label = "Daily step goal",
            placeholder = "e.g. 10000",
            keyboardType = KeyboardType.Number
        )

        // Step goal presets
        Text(
            text = "Quick presets",
            style = MaterialTheme.typography.labelMedium,
            color = FitTrackColors.TextSecondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("5000", "7500", "10000", "15000").forEach { preset ->
                FilterChip(
                    selected = stepGoal == preset,
                    onClick = { onStepGoalChange(preset) },
                    label = {
                        Text(
                            text = "%,d".format(preset.toIntOrNull() ?: 0),
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = FitTrackColors.TealContainer,
                        selectedLabelColor = FitTrackColors.TealPrimary
                    )
                )
            }
        }

        OnboardingTextField(
            value = waterGoalMl,
            onValueChange = onWaterGoalChange,
            label = "Daily water goal (ml)",
            placeholder = "e.g. 2500",
            keyboardType = KeyboardType.Number
        )
    }
}

@Composable
fun OnboardingStepHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .background(FitTrackColors.TealContainer, RoundedCornerShape(14.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FitTrackColors.TealPrimary,
                modifier = Modifier.size(26.dp)
            )
        }
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FitTrackColors.TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = FitTrackColors.TextSecondary
            )
        }
    }
}

@Composable
fun OnboardingTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
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