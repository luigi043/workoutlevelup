package com.example.workoutlevelup.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing a single logged workout day.
 * [date] uses "YYYY-MM-DD" format as the primary key for easy calendar lookup.
 */
@Entity(tableName = "workout_day")
data class WorkoutDay(
    @PrimaryKey val date: String,           // "YYYY-MM-DD"
    val dayNumber: Int,                      // sequential training day (1, 2, 3 …)
    val isCompleted: Boolean = false,
    val totalTimeSeconds: Int = 0,
    val level: Int = 1,                      // 1-4 matching the training plan levels
    val workoutType: Int = 1,                // 1 = Strength, 2 = Cardio/Core
    val completedExercises: String = ""      // comma-separated completed exercise names
)
