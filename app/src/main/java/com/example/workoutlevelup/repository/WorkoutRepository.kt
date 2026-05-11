package com.example.workoutlevelup.repository

import com.example.workoutlevelup.data.WorkoutDay
import com.example.workoutlevelup.data.WorkoutDayDao
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WorkoutRepository(private val dao: WorkoutDayDao) {

    val allWorkoutDays: Flow<List<WorkoutDay>> = dao.getAllWorkoutDays()
    val completedCount: Flow<Int> = dao.getCompletedCount()
    val completedWorkouts: Flow<List<WorkoutDay>> = dao.getCompletedWorkouts()

    fun getWorkoutsForMonth(yearMonth: String): Flow<List<WorkoutDay>> =
        dao.getWorkoutsForMonth(yearMonth)

    suspend fun getWorkoutDayByDate(date: String): WorkoutDay? =
        dao.getWorkoutDayByDate(date)

    suspend fun insert(workoutDay: WorkoutDay) = dao.insert(workoutDay)

    suspend fun update(workoutDay: WorkoutDay) = dao.update(workoutDay)

    suspend fun upsert(workoutDay: WorkoutDay) = dao.insert(workoutDay) // REPLACE strategy

    /** Calculates the current streak (consecutive completed days). */
    suspend fun calculateStreak(): Int {
        val completedDates = dao.getCompletedWorkoutsSuspend()
            .map { it.date }
            .toSortedSet(reverseOrder())

        if (completedDates.isEmpty()) return 0

        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var streak = 0
        var checkDate = LocalDate.now()

        // If today isn't completed, start checking from yesterday.
        // If yesterday isn't completed either, the streak is broken.
        if (!completedDates.contains(checkDate.format(fmt))) {
            checkDate = checkDate.minusDays(1)
            if (!completedDates.contains(checkDate.format(fmt))) {
                return 0
            }
        }

        while (true) {
            val dateStr = checkDate.format(fmt)
            if (completedDates.contains(dateStr)) {
                streak++
                checkDate = checkDate.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }

    suspend fun getLatestDayNumber(): Int =
        dao.getLatestWorkoutDay()?.dayNumber ?: 0
}
