package com.example.workoutlevelup.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutlevelup.data.WorkoutDay
import com.example.workoutlevelup.ui.theme.*
import com.example.workoutlevelup.viewmodel.WorkoutViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: WorkoutViewModel,
    onDayClick: (LocalDate) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val workoutMap by viewModel.workoutDayMap.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .padding(16.dp)
    ) {
        // ── Header stats ────────────────────────────────────────────
        StatsHeader(
            streak = uiState.currentStreak,
            totalCompleted = uiState.totalCompleted,
            currentLevel = uiState.currentLevel
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Monthly calendar ────────────────────────────────────────
        MonthCalendar(
            yearMonth = uiState.currentYearMonth,
            selectedDate = uiState.selectedDate,
            workoutMap = workoutMap,
            onPrevMonth = {
                viewModel.setCurrentYearMonth(uiState.currentYearMonth.minusMonths(1))
            },
            onNextMonth = {
                viewModel.setCurrentYearMonth(uiState.currentYearMonth.plusMonths(1))
            },
            onDayClick = { date ->
                viewModel.setSelectedDate(date)
                onDayClick(date)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Legend ──────────────────────────────────────────────────
        CalendarLegend()
    }
}

@Composable
private fun StatsHeader(streak: Int, totalCompleted: Int, currentLevel: Int) {
    val levelColor = when (currentLevel) {
        1 -> Level1Color; 2 -> Level2Color; 3 -> Level3Color; else -> Level4Color
    }
    val rankName = when (currentLevel) {
        1 -> "E-Rank Initiate"; 2 -> "E-Rank Hunter"
        3 -> "B-Rank Hunter";  else -> "S-Rank Hunter"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateGray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(label = "🔥 Streak", value = "$streak days")
            StatItem(label = "✅ Total", value = "$totalCompleted done")
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("⚔️ Rank", fontSize = 12.sp, color = NeutralGray)
                Text(
                    rankName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = levelColor
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = NeutralGray)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

@Composable
private fun MonthCalendar(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    workoutMap: Map<String, WorkoutDay>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SlateGray)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // Month navigation header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous", tint = NeonBlue)
                }
                Text(
                    text = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                            " ${yearMonth.year}",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next", tint = NeonBlue)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Day-of-week headers
            Row(modifier = Modifier.fillMaxWidth()) {
                DayOfWeek.values().let { days ->
                    val ordered = days.drop(0) // Mon first
                    listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { label ->
                        Text(
                            text = label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NeutralGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calendar grid
            val firstDayOfMonth = yearMonth.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // 1=Mon … 7=Sun
            val daysInMonth = yearMonth.lengthOfMonth()
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val dayIndex = row * 7 + col - (firstDayOfWeek - 1) + 1
                        if (dayIndex < 1 || dayIndex > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f).height(40.dp))
                        } else {
                            val date = yearMonth.atDay(dayIndex)
                            val dateStr = date.format(fmt)
                            val workout = workoutMap[dateStr]
                            val isToday = date == today
                            val isSelected = date == selectedDate

                            DayCell(
                                day = dayIndex,
                                isToday = isToday,
                                isSelected = isSelected,
                                isCompleted = workout?.isCompleted == true,
                                hasLog = workout != null,
                                isFuture = date.isAfter(today),
                                onClick = { onDayClick(date) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    isCompleted: Boolean,
    hasLog: Boolean,
    isFuture: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected  -> NeonBlue.copy(alpha = 0.8f)
        isToday     -> AccentPurple.copy(alpha = 0.5f)
        isCompleted -> SuccessGreen.copy(alpha = 0.25f)
        else        -> Color.Transparent
    }
    val textColor = when {
        isFuture    -> NeutralGray
        isCompleted -> SuccessGreen
        else        -> Color.White
    }

    Box(
        modifier = modifier
            .height(42.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(enabled = !isFuture, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
            if (isCompleted) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
            }
        }
    }
}

@Composable
private fun CalendarLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LegendItem(color = SuccessGreen, label = "Completed")
        LegendItem(color = AccentPurple, label = "Today")
        LegendItem(color = NeonBlue, label = "Selected")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 11.sp, color = NeutralGray)
    }
}
