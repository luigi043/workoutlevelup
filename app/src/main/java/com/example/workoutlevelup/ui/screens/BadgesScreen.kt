package com.example.workoutlevelup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutlevelup.model.Badge
import com.example.workoutlevelup.model.WorkoutPlan
import com.example.workoutlevelup.ui.theme.*
import com.example.workoutlevelup.viewmodel.WorkoutViewModel

@Composable
fun BadgesScreen(viewModel: WorkoutViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    val earnedIds = uiState.earnedBadgeIds
    val earned = WorkoutPlan.badges.filter { it.id in earnedIds }
    val locked = WorkoutPlan.badges.filter { it.id !in earnedIds }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // ── Progress header ─────────────────────────────────────────
        item {
            ProgressHeader(
                streak = uiState.currentStreak,
                longestStreak = uiState.longestStreak,
                totalCompleted = uiState.totalCompleted,
                currentLevel = uiState.currentLevel,
                earnedCount = earned.size,
                totalBadges = WorkoutPlan.badges.size
            )
        }

        // ── Level progress bar ──────────────────────────────────────
        item {
            LevelProgressCard(
                currentLevel = uiState.currentLevel,
                totalCompleted = uiState.totalCompleted
            )
        }

        // ── Earned badges ───────────────────────────────────────────
        if (earned.isNotEmpty()) {
            item {
                SectionTitle("🏆 Earned Badges (${earned.size})")
            }
            items(earned) { badge ->
                BadgeCard(badge = badge, isEarned = true)
            }
        }

        // ── Locked badges ───────────────────────────────────────────
        if (locked.isNotEmpty()) {
            item {
                SectionTitle("🔒 Locked Badges (${locked.size})")
            }
            items(locked) { badge ->
                BadgeCard(badge = badge, isEarned = false)
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun ProgressHeader(
    streak: Int,
    longestStreak: Int,
    totalCompleted: Int,
    currentLevel: Int,
    earnedCount: Int,
    totalBadges: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Your Progress",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressStat("🔥", "$streak", "Current streak")
                ProgressStat("📈", "$longestStreak", "Best streak")
                ProgressStat("✅", "$totalCompleted", "Workouts done")
                ProgressStat("🏅", "$earnedCount/$totalBadges", "Badges")
            }
        }
    }
}

@Composable
private fun ProgressStat(icon: String, value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(icon, fontSize = 22.sp)
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NeonBlue)
        Text(label, fontSize = 10.sp, color = NeutralGray)
    }
}

@Composable
private fun LevelProgressCard(currentLevel: Int, totalCompleted: Int) {
    val levelThresholds = listOf(0, 14, 30, 60)
    val nextThreshold = if (currentLevel < 4) levelThresholds[currentLevel] else 100
    val prevThreshold = levelThresholds[currentLevel - 1]
    val progress = if (currentLevel < 4)
        (totalCompleted - prevThreshold).toFloat() / (nextThreshold - prevThreshold)
    else 1f

    val levelColor = when (currentLevel) {
        1 -> Level1Color; 2 -> Level2Color; 3 -> Level3Color; else -> Level4Color
    }
    val rankName = when (currentLevel) {
        1 -> "E-Rank Initiate"; 2 -> "E-Rank Hunter"
        3 -> "B-Rank Hunter";  else -> "⚡ S-Rank Hunter"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateGray)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Level $currentLevel", color = NeutralGray, fontSize = 12.sp)
                    Text(
                        rankName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = levelColor
                    )
                }
                if (currentLevel < 4) {
                    Text(
                        "$totalCompleted / $nextThreshold workouts",
                        fontSize = 12.sp,
                        color = NeutralGray
                    )
                } else {
                    Text("MAX LEVEL", fontSize = 12.sp, color = GoldYellow, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = levelColor,
                trackColor = DeepNavy
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun BadgeCard(badge: Badge, isEarned: Boolean) {
    val unlockHint = buildString {
        if (badge.requiredDays > 0)    append("${badge.requiredDays} workouts")
        if (badge.requiredStreak > 0)  append("${badge.requiredStreak}-day streak")
        if (badge.requiredLevel > 0)   append("Reach Level ${badge.requiredLevel}")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isEarned) 1f else 0.45f),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEarned) SlateGray else DeepNavy
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isEarned) GoldYellow.copy(0.15f) else NeutralGray.copy(0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(badge.emoji, fontSize = 26.sp)
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    badge.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (isEarned) GoldYellow else NeutralGray
                )
                Text(
                    badge.description,
                    fontSize = 13.sp,
                    color = if (isEarned) Color.White else NeutralGray
                )
                if (!isEarned && unlockHint.isNotBlank()) {
                    Text(
                        "Unlock: $unlockHint",
                        fontSize = 11.sp,
                        color = AccentPurple.copy(0.8f)
                    )
                }
            }

            if (isEarned) {
                Text("✓", fontSize = 20.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }
    }
}
