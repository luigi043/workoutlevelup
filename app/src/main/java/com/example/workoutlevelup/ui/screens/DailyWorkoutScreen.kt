package com.example.workoutlevelup.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.workoutlevelup.model.Exercise
import com.example.workoutlevelup.model.Workout
import com.example.workoutlevelup.model.WorkoutPlan
import com.example.workoutlevelup.ui.theme.*
import com.example.workoutlevelup.viewmodel.WorkoutViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate

@Composable
fun DailyWorkoutScreen(
    viewModel: WorkoutViewModel,
    selectedDate: LocalDate,
    onWorkoutComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val workoutMap by viewModel.workoutDayMap.collectAsState()

    val fmt = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val existingLog = workoutMap[selectedDate.format(fmt)]
    val alreadyDone = existingLog?.isCompleted == true

    val workout = remember(selectedDate, uiState.currentLevel) {
        viewModel.getWorkoutPlanForDate(selectedDate)
    }

    // Checked exercises state
    val checkedExercises = remember(workout) {
        mutableStateMapOf<String, Boolean>().apply {
            workout.exercises.forEach { put(it.name, false) }
        }
    }

    // Elapsed timer state
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (isTimerRunning) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    val allChecked = checkedExercises.values.all { it }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        // ── Top bar ────────────────────────────────────────────────
        WorkoutTopBar(
            selectedDate = selectedDate,
            workout = workout,
            elapsedSeconds = elapsedSeconds,
            isTimerRunning = isTimerRunning,
            onTimerToggle = { isTimerRunning = !isTimerRunning }
        )

        if (alreadyDone) {
            // Show completion summary
            CompletionSummary(existingLog!!.totalTimeSeconds, existingLog.completedExercises)
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                item {
                    RoundsInfo(rounds = workout.rounds)
                }

                items(workout.exercises) { exercise ->
                    ExerciseCheckItem(
                        exercise = exercise,
                        isChecked = checkedExercises[exercise.name] == true,
                        onCheckedChange = { checkedExercises[exercise.name] = it }
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

            // ── Complete button ──────────────────────────────────────
            AnimatedVisibility(
                visible = allChecked,
                enter = fadeIn() + slideInVertically { it },
                exit  = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            isTimerRunning = false
                            viewModel.completeWorkout(
                                date = selectedDate,
                                totalTimeSeconds = elapsedSeconds,
                                completedExercises = checkedExercises.keys.toList()
                            )
                            onWorkoutComplete()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Mark Workout Complete!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkoutTopBar(
    selectedDate: LocalDate,
    workout: Workout,
    elapsedSeconds: Int,
    isTimerRunning: Boolean,
    onTimerToggle: () -> Unit
) {
    val levelPlan = WorkoutPlan.levels.firstOrNull { it.workouts.contains(workout) }
    val levelColor = when (levelPlan?.level ?: 1) {
        1 -> Level1Color; 2 -> Level2Color; 3 -> Level3Color; else -> Level4Color
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateGray)
            .padding(16.dp)
    ) {
        Column {
            Text(
                selectedDate.toString(),
                fontSize = 12.sp, color = NeutralGray
            )
            Text(
                workout.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                workout.subtitle,
                fontSize = 14.sp,
                color = levelColor
            )

            Spacer(Modifier.height(12.dp))

            // Timer row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Timer,
                    contentDescription = null,
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = formatTime(elapsedSeconds),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonBlue
                )
                IconButton(
                    onClick = onTimerToggle,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isTimerRunning) DangerRed else SuccessGreen)
                ) {
                    Icon(
                        if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isTimerRunning) "Pause" else "Start",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RoundsInfo(rounds: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(AccentPurple.copy(alpha = 0.2f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Loop, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            "Complete all exercises for $rounds rounds",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ExerciseCheckItem(
    exercise: Exercise,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val description = buildString {
        when {
            exercise.reps != null       -> append("${exercise.reps} reps")
            exercise.durationSeconds != null -> {
                val secs = exercise.durationSeconds
                if (secs >= 60) append("${secs / 60} min") else append("${secs}s")
            }
        }
        if (exercise.perSide) append(" each side")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isChecked) SuccessGreen.copy(0.15f) else SlateGray)
            .border(
                1.dp,
                if (isChecked) SuccessGreen.copy(0.5f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onCheckedChange(!isChecked) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = SuccessGreen,
                uncheckedColor = NeutralGray
            )
        )
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                exercise.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (isChecked) SuccessGreen else Color.White,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                description,
                fontSize = 13.sp,
                color = NeutralGray
            )
        }
        if (isChecked) {
            Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen)
        }
    }
}

@Composable
private fun CompletionSummary(totalTimeSeconds: Int, completedExercises: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("✅", fontSize = 64.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Workout Completed!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = SuccessGreen
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Time: ${formatTime(totalTimeSeconds)}",
            fontSize = 18.sp,
            color = NeonBlue
        )
        Spacer(Modifier.height(16.dp))
        if (completedExercises.isNotEmpty()) {
            Text(
                "Exercises: ${completedExercises.replace(",", " • ")}",
                fontSize = 14.sp,
                color = NeutralGray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
