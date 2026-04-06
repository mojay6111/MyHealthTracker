package com.example.myhealthtracker.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object FitTrackColors {
    // Base surfaces
    val Background = Color(0xFF0A0F14)
    val SurfaceCard = Color(0xFF111820)
    val SurfaceElevated = Color(0xFF17212D)
    val SurfaceBorder = Color(0xFF1E2D3D)

    // Teal accent (primary)
    val TealPrimary = Color(0xFF00E5CC)
    val TealSecondary = Color(0xFF00B4A0)
    val TealContainer = Color(0xFF00332E)
    val TealDim = Color(0x3300E5CC)

    // Coral (calories)
    val Coral = Color(0xFFFF6B6B)
    val CoralDim = Color(0x33FF6B6B)
    val CoralContainer = Color(0xFF3D1515)

    // Amber (water)
    val Amber = Color(0xFFFFBB57)
    val AmberDim = Color(0x33FFBB57)

    // Purple (sleep)
    val Purple = Color(0xFFBB86FC)
    val PurpleDim = Color(0x33BB86FC)

    // Green (goals)
    val GreenSuccess = Color(0xFF4CAF50)
    val GreenDim = Color(0x334CAF50)

    // Text
    val TextPrimary = Color(0xFFF0F4F8)
    val TextSecondary = Color(0xFF8899AA)
    val TextDisabled = Color(0xFF445566)
}

private val DarkColorScheme = darkColorScheme(
    primary = FitTrackColors.TealPrimary,
    onPrimary = Color(0xFF001A17),
    primaryContainer = FitTrackColors.TealContainer,
    onPrimaryContainer = FitTrackColors.TealPrimary,
    secondary = FitTrackColors.Coral,
    onSecondary = Color(0xFF3D1515),
    secondaryContainer = FitTrackColors.CoralContainer,
    onSecondaryContainer = FitTrackColors.Coral,
    tertiary = FitTrackColors.Amber,
    onTertiary = Color(0xFF3D2800),
    background = FitTrackColors.Background,
    onBackground = FitTrackColors.TextPrimary,
    surface = FitTrackColors.SurfaceCard,
    onSurface = FitTrackColors.TextPrimary,
    surfaceVariant = FitTrackColors.SurfaceElevated,
    onSurfaceVariant = FitTrackColors.TextSecondary,
    outline = FitTrackColors.SurfaceBorder,
    error = FitTrackColors.Coral
)

@Composable
fun FitTrackTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = FitTrackTypography,
        content = content
    )
}