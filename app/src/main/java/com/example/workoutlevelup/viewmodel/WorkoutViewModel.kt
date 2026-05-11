package com.example.workoutlevelup.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.workoutlevelup.data.AppDatabase
import com.example.workoutlevelup.data.WorkoutDay
import com.example.workoutlevelup.model.Badge
import com.example.workoutlevelup.model.WorkoutPlan
import com.example.workoutlevelup.repository.WorkoutRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class UiState(
    val allWorkoutDays: List<WorkoutDay> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val currentLevel: Int = 1,
    val earnedBadgeIds: Set<String> = emptySet(),
    val selectedDate: LocalDate = LocalDate.now(),
    val currentYearMonth: YearMonth = YearMonth.now()
)

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WorkoutRepository(
        AppDatabase.getDatabase(application).workoutDayDao()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    /** Map of "YYYY-MM-DD" → WorkoutDay for fast calendar lookup */
    val workoutDayMap: StateFlow<Map<String, WorkoutDay>> =
        repository.allWorkoutDays
            .map { list -> list.associateBy { it.date } }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        viewModelScope.launch {
            repository.allWorkoutDays.collect { days ->
                val completed = days.filter { it.isCompleted }
                val streak = repository.calculateStreak()
                val longestStreak = calculateLongestStreak(completed.map { it.date })
                val totalCompleted = completed.size
                val currentLevel = determineLevel(totalCompleted)
                val earnedBadges = computeEarnedBadges(totalCompleted, streak, currentLevel)

                _uiState.update {
                    it.copy(
                        allWorkoutDays = days,
                        currentStreak = streak,
                        longestStreak = longestStreak,
                        totalCompleted = totalCompleted,
                        currentLevel = currentLevel,
                        earnedBadgeIds = earnedBadges
                    )
                }
            }
        }
    }

    /** Determine which level the user is currently at based on completed workouts. */
    private fun determineLevel(completed: Int): Int = when {
        completed >= 60 -> 4
        completed >= 30 -> 3
        completed >= 14 -> 2
        else -> 1
    }

    /** Get or create today's WorkoutDay, then mark it complete. */
    fun completeWorkout(
        date: LocalDate = LocalDate.now(),
        totalTimeSeconds: Int,
        completedExercises: List<String>
    ) {
        viewModelScope.launch {
            val dateStr = date.format(fmt)
            val currentLevel = _uiState.value.currentLevel
            val dayNumber = repository.getLatestDayNumber() + 1

            val existing = repository.getWorkoutDayByDate(dateStr)
            val updated = (existing ?: WorkoutDay(
                date = dateStr,
                dayNumber = dayNumber,
                level = currentLevel,
                workoutType = if (dayNumber % 2 == 0) 2 else 1
            )).copy(
                isCompleted = true,
                totalTimeSeconds = totalTimeSeconds,
                completedExercises = completedExercises.joinToString(",")
            )
            repository.upsert(updated)
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun setCurrentYearMonth(ym: YearMonth) {
        _uiState.update { it.copy(currentYearMonth = ym) }
    }

    /** Returns the WorkoutDay for a given date, or null if not yet logged. */
    suspend fun getWorkoutForDate(date: LocalDate): WorkoutDay? =
        repository.getWorkoutDayByDate(date.format(fmt))

    /** Earned badges based on current progress. */
    private fun computeEarnedBadges(
        totalCompleted: Int,
        streak: Int,
        level: Int
    ): Set<String> {
        val earned = mutableSetOf<String>()
        for (badge in WorkoutPlan.badges) {
            val dayOk = badge.requiredDays == 0 || totalCompleted >= badge.requiredDays
            val lvlOk = badge.requiredLevel == 0 || level >= badge.requiredLevel
            val strkOk = badge.requiredStreak == 0 || streak >= badge.requiredStreak
            if (dayOk && lvlOk && strkOk) earned.add(badge.id)
        }
        return earned
    }

    private fun calculateLongestStreak(sortedDates: List<String>): Int {
        if (sortedDates.isEmpty()) return 0
        val dates = sortedDates.map { LocalDate.parse(it, fmt) }.sorted()
        var longest = 1
        var current = 1
        for (i in 1 until dates.size) {
            current = if (dates[i] == dates[i - 1].plusDays(1)) current + 1 else 1
            if (current > longest) longest = current
        }
        return longest
    }

    fun getWorkoutPlanForDate(date: LocalDate): com.example.workoutlevelup.model.Workout {
        val dayNumber = workoutDayMap.value[date.format(fmt)]?.dayNumber
            ?: (_uiState.value.allWorkoutDays.size + 1)
        return WorkoutPlan.getWorkoutForDay(dayNumber, _uiState.value.currentLevel)
    }
}
