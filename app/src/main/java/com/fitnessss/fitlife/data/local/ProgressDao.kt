package com.fitnessss.fitlife.data.local

import androidx.room.*
import com.fitnessss.fitlife.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ProgressDao {
    // Progress measurements operations
    @Query("SELECT * FROM progress_measurements WHERE userId = :userId ORDER BY date DESC")
    fun getProgressMeasurementsByUser(userId: String): Flow<List<ProgressMeasurement>>

    @Query("SELECT * FROM progress_measurements WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getProgressMeasurementsByDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<ProgressMeasurement>>

    @Query("SELECT * FROM progress_measurements WHERE userId = :userId ORDER BY date DESC LIMIT 1")
    suspend fun getLatestMeasurement(userId: String): ProgressMeasurement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressMeasurement(measurement: ProgressMeasurement)

    @Update
    suspend fun updateProgressMeasurement(measurement: ProgressMeasurement)

    @Delete
    suspend fun deleteProgressMeasurement(measurement: ProgressMeasurement)

    // Progress photos operations
    @Query("SELECT * FROM progress_photos WHERE userId = :userId ORDER BY date DESC")
    fun getProgressPhotosByUser(userId: String): Flow<List<ProgressPhoto>>

    @Query("SELECT * FROM progress_photos WHERE userId = :userId AND photoType = :photoType ORDER BY date DESC")
    fun getProgressPhotosByType(userId: String, photoType: String): Flow<List<ProgressPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressPhoto(photo: ProgressPhoto)

    @Update
    suspend fun updateProgressPhoto(photo: ProgressPhoto)

    @Delete
    suspend fun deleteProgressPhoto(photo: ProgressPhoto)

    // Personal records operations
    @Query("SELECT * FROM personal_records WHERE userId = :userId ORDER BY date DESC")
    fun getPersonalRecordsByUser(userId: String): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE userId = :userId AND exerciseId = :exerciseId ORDER BY value DESC")
    fun getPersonalRecordsByExercise(userId: String, exerciseId: String): Flow<List<PersonalRecord>>

    @Query("SELECT * FROM personal_records WHERE userId = :userId AND recordType = :recordType ORDER BY value DESC")
    fun getPersonalRecordsByType(userId: String, recordType: String): Flow<List<PersonalRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPersonalRecord(record: PersonalRecord)

    @Update
    suspend fun updatePersonalRecord(record: PersonalRecord)

    @Delete
    suspend fun deletePersonalRecord(record: PersonalRecord)

    // Achievements operations
    @Query("SELECT * FROM achievements ORDER BY points ASC")
    fun getAllAchievements(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE category = :category")
    fun getAchievementsByCategory(category: String): Flow<List<Achievement>>

    @Query("SELECT * FROM user_achievements WHERE userId = :userId")
    fun getUserAchievements(userId: String): Flow<List<UserAchievement>>

    @Query("SELECT a.* FROM achievements a INNER JOIN user_achievements ua ON a.id = ua.achievementId WHERE ua.userId = :userId")
    fun getUnlockedAchievements(userId: String): Flow<List<Achievement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserAchievement(userAchievement: UserAchievement)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    // Workout stats operations
    @Query("SELECT * FROM workout_stats WHERE userId = :userId")
    suspend fun getWorkoutStats(userId: String): WorkoutStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkoutStats(stats: WorkoutStats)

    @Update
    suspend fun updateWorkoutStats(stats: WorkoutStats)

    // Statistics and analytics
    @Query("SELECT weight FROM progress_measurements WHERE userId = :userId AND weight IS NOT NULL ORDER BY date DESC LIMIT 30")
    suspend fun getRecentWeights(userId: String): List<Float>

    @Query("SELECT weight FROM progress_measurements WHERE userId = :userId AND weight IS NOT NULL ORDER BY date ASC LIMIT 1")
    suspend fun getStartingWeight(userId: String): Float?

    @Query("SELECT weight FROM progress_measurements WHERE userId = :userId AND weight IS NOT NULL ORDER BY date DESC LIMIT 1")
    suspend fun getCurrentWeight(userId: String): Float?

    @Query("SELECT COUNT(*) FROM personal_records WHERE userId = :userId")
    suspend fun getTotalPersonalRecords(userId: String): Int

    @Query("SELECT COUNT(*) FROM user_achievements WHERE userId = :userId")
    suspend fun getTotalAchievements(userId: String): Int
}
