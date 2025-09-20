package com.fitnessss.fitlife.data.local

import androidx.room.*
import com.fitnessss.fitlife.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface WorkoutDao {
    // Workout operations
    @Query("SELECT * FROM workouts ORDER BY createdAt DESC")
    fun getAllWorkouts(): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE id = :workoutId")
    suspend fun getWorkoutById(workoutId: String): Workout?

    @Query("SELECT * FROM workouts WHERE muscleGroups LIKE '%' || :muscleGroup || '%'")
    fun getWorkoutsByMuscleGroup(muscleGroup: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE workoutType = :workoutType")
    fun getWorkoutsByType(workoutType: String): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE difficulty = :difficulty")
    fun getWorkoutsByDifficulty(difficulty: String): Flow<List<Workout>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    // Exercise operations
    @Query("SELECT * FROM exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: String): Exercise?

    @Query("SELECT * FROM exercises WHERE muscleGroups LIKE '%' || :muscleGroup || '%'")
    fun getExercisesByMuscleGroup(muscleGroup: String): Flow<List<Exercise>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: Exercise)

    @Update
    suspend fun updateExercise(exercise: Exercise)

    @Delete
    suspend fun deleteExercise(exercise: Exercise)

    // Workout session operations
    @Query("SELECT * FROM workout_sessions WHERE userId = :userId ORDER BY startTime DESC")
    fun getWorkoutSessionsByUser(userId: String): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE userId = :userId AND date(startTime/1000, 'unixepoch') = :date")
    fun getWorkoutSessionsByDate(userId: String, date: String): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId")
    suspend fun getWorkoutSessionById(sessionId: String): WorkoutSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutSession(session: WorkoutSession)

    @Update
    suspend fun updateWorkoutSession(session: WorkoutSession)

    @Delete
    suspend fun deleteWorkoutSession(session: WorkoutSession)

    // Statistics
    @Query("SELECT COUNT(*) FROM workout_sessions WHERE userId = :userId")
    suspend fun getTotalWorkouts(userId: String): Int

    @Query("SELECT SUM((endTime - startTime) / 60000) FROM workout_sessions WHERE userId = :userId AND endTime IS NOT NULL")
    suspend fun getTotalWorkoutTime(userId: String): Int?

    @Query("SELECT SUM(caloriesBurned) FROM workout_sessions WHERE userId = :userId AND caloriesBurned IS NOT NULL")
    suspend fun getTotalCaloriesBurned(userId: String): Int?
}
