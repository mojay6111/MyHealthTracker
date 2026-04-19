package com.example.myhealthtracker.ui.achievements

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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myhealthtracker.data.local.entity.AchievementEntity
import com.example.myhealthtracker.ui.theme.FitTrackColors
import com.example.myhealthtracker.ui.viewmodel.AchievementsViewModel
import androidx.compose.ui.draw.clip

@Composable
fun AchievementsScreen(
    onBack: () -> Unit,
    viewModel: AchievementsViewModel = hiltViewModel()
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
                    text = "Achievements",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            // Badge count
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = FitTrackColors.TealContainer
            ) {
                Text(
                    text = "${uiState.unlockedCount}/${uiState.totalCount}",
                    style = MaterialTheme.typography.labelLarge,
                    color = FitTrackColors.TealPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    )
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = 20.dp,
                vertical = 12.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Summary card ──
            item {
                AchievementSummaryCard(
                    unlockedCount = uiState.unlockedCount,
                    totalCount = uiState.totalCount,
                    streakDays = uiState.currentStreak
                )
            }

            // ── Recently unlocked ──
            if (uiState.recentlyUnlocked.isNotEmpty()) {
                item {
                    Text(
                        text = "Recently Unlocked 🎉",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = FitTrackColors.TextPrimary
                    )
                }
                items(uiState.recentlyUnlocked) { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        isNew = true
                    )
                }
            }

            // ── Steps category ──
            item {
                CategoryHeader(
                    emoji = "👟",
                    title = "Steps",
                    color = FitTrackColors.TealPrimary
                )
            }
            items(uiState.stepAchievements) { achievement ->
                AchievementCard(achievement = achievement)
            }

            // ── Streak category ──
            item {
                CategoryHeader(
                    emoji = "🔥",
                    title = "Streaks",
                    color = FitTrackColors.Coral
                )
            }
            items(uiState.streakAchievements) { achievement ->
                AchievementCard(achievement = achievement)
            }

            // ── Distance category ──
            item {
                CategoryHeader(
                    emoji = "🗺️",
                    title = "Distance & Speed",
                    color = FitTrackColors.Amber
                )
            }
            items(uiState.distanceAchievements) { achievement ->
                AchievementCard(achievement = achievement)
            }

            // ── Hydration category ──
            item {
                CategoryHeader(
                    emoji = "💧",
                    title = "Hydration",
                    color = FitTrackColors.Purple
                )
            }
            items(uiState.hydrationAchievements) { achievement ->
                AchievementCard(achievement = achievement)
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun AchievementSummaryCard(
    unlockedCount: Int,
    totalCount: Int,
    streakDays: Int
) {
    val progress = if (totalCount > 0)
        unlockedCount.toFloat() / totalCount
    else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "achievement_progress"
    )

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
                            FitTrackColors.TealDim,
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
                    text = "🏆",
                    fontSize = 48.sp
                )
                Text(
                    text = "$unlockedCount Badges Earned",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = FitTrackColors.TextPrimary
                )
                Text(
                    text = "$unlockedCount of $totalCount achievements unlocked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = FitTrackColors.TextSecondary
                )

                // Progress bar
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FitTrackColors.TealPrimary,
                        trackColor = FitTrackColors.SurfaceBorder
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% complete",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.TextSecondary,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                }

                if (streakDays > 0) {
                    HorizontalDivider(color = FitTrackColors.SurfaceBorder)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔥", fontSize = 20.sp)
                        Text(
                            text = "$streakDays day streak",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FitTrackColors.Coral
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(
    emoji: String,
    title: String,
    color: Color
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Text(text = emoji, fontSize = 20.sp)
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun AchievementCard(
    achievement: AchievementEntity,
    isNew: Boolean = false
) {
    val isUnlocked = achievement.unlockedAt != null

    val scale by animateFloatAsState(
        targetValue = if (isUnlocked) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "badge_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                FitTrackColors.SurfaceCard
            else
                FitTrackColors.SurfaceCard.copy(alpha = 0.6f)
        ),
        border = if (isUnlocked) BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(
                    FitTrackColors.TealPrimary.copy(alpha = 0.5f),
                    FitTrackColors.TealSecondary.copy(alpha = 0.2f)
                )
            )
        ) else BorderStroke(
            1.dp,
            FitTrackColors.SurfaceBorder
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = if (isUnlocked) Brush.linearGradient(
                            colors = listOf(
                                FitTrackColors.TealContainer,
                                FitTrackColors.TealDim
                            )
                        ) else Brush.linearGradient(
                            colors = listOf(
                                FitTrackColors.SurfaceBorder,
                                FitTrackColors.SurfaceBorder
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Text(
                    text = getBadgeEmoji(achievement.id, isUnlocked),
                    fontSize = 24.sp
                )
            }

            // Text
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked)
                            FitTrackColors.TextPrimary
                        else
                            FitTrackColors.TextDisabled
                    )
                    if (isNew) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = FitTrackColors.TealPrimary
                        ) {
                            Text(
                                text = "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF001A17),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                            )
                        }
                    }
                }
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isUnlocked)
                        FitTrackColors.TextSecondary
                    else
                        FitTrackColors.TextDisabled
                )
                if (isUnlocked && achievement.unlockedAt != null) {
                    val date = java.text.SimpleDateFormat(
                        "MMM dd, yyyy",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date(achievement.unlockedAt))
                    Text(
                        text = "Unlocked $date",
                        style = MaterialTheme.typography.labelSmall,
                        color = FitTrackColors.TealSecondary
                    )
                }
            }

            // Lock/unlock icon
            if (isUnlocked) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Unlocked",
                    tint = FitTrackColors.TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = FitTrackColors.TextDisabled,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun getBadgeEmoji(id: String, isUnlocked: Boolean): String {
    if (!isUnlocked) return "🔒"
    return when (id) {
        "FIRST_1K_STEPS" -> "🐣"
        "FIRST_5K_STEPS" -> "👟"
        "FIRST_10K_STEPS" -> "🎉"
        "FIRST_20K_STEPS" -> "⚡"
        "TOTAL_100K_STEPS" -> "💯"
        "TOTAL_1M_STEPS" -> "🏆"
        "STREAK_3_DAYS" -> "🔥"
        "STREAK_7_DAYS" -> "🦁"
        "STREAK_30_DAYS" -> "💪"
        "FIRST_5KM_RUN" -> "🏃"
        "FIRST_10KM_RUN" -> "🥈"
        "TOTAL_100KM" -> "🗺️"
        "FIRST_ROUTE" -> "🧭"
        "SPEED_12KMH" -> "⚡"
        "FIRST_WATER_GOAL" -> "💧"
        "WATER_GOAL_7_DAYS" -> "🌊"
        else -> "🏅"
    }
}