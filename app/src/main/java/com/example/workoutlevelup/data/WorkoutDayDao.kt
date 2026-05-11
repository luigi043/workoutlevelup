package com.example.workoutlevelup.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDayDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workoutDay: WorkoutDay)

    @Update
    suspend fun update(workoutDay: WorkoutDay)

    @Delete
    suspend fun delete(workoutDay: WorkoutDay)

    @Query("SELECT * FROM workout_day ORDER BY date ASC")
    fun getAllWorkoutDays(): Flow<List<WorkoutDay>>

    @Query("SELECT * FROM workout_day WHERE date = :date")
    suspend fun getWorkoutDayByDate(date: String): WorkoutDay?

    @Query("SELECT * FROM workout_day WHERE date LIKE :yearMonth || '%' ORDER BY date ASC")
    fun getWorkoutsForMonth(yearMonth: String): Flow<List<WorkoutDay>>

    @Query("SELECT * FROM workout_day WHERE isCompleted = 1 ORDER BY date DESC")
    fun getCompletedWorkouts(): Flow<List<WorkoutDay>>

    @Query("SELECT COUNT(*) FROM workout_day WHERE isCompleted = 1")
    fun getCompletedCount(): Flow<Int>

    @Query("SELECT * FROM workout_day ORDER BY date DESC LIMIT 1")
    suspend fun getLatestWorkoutDay(): WorkoutDay?

    @Query("SELECT * FROM workout_day WHERE isCompleted = 1 ORDER BY date DESC")
    suspend fun getCompletedWorkoutsSuspend(): List<WorkoutDay>
}
