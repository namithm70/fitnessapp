package com.fitnessss.fitlife.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "progress_measurements")
data class ProgressMeasurement(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: LocalDate,
    val weight: Float? = null, // kg
    val bodyFatPercentage: Float? = null,
    val muscleMass: Float? = null, // kg
    val measurements: BodyMeasurements? = null,
    val notes: String? = null,
    val loggedAt: Long = System.currentTimeMillis()
)

data class BodyMeasurements(
    val chest: Float? = null, // cm
    val waist: Float? = null,
    val hips: Float? = null,
    val biceps: Float? = null,
    val forearms: Float? = null,
    val thighs: Float? = null,
    val calves: Float? = null,
    val neck: Float? = null,
    val shoulders: Float? = null
)

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: LocalDate,
    val photoType: PhotoType,
    val imageUrl: String,
    val thumbnailUrl: String? = null,
    val notes: String? = null,
    val uploadedAt: Long = System.currentTimeMillis()
)

enum class PhotoType {
    FRONT, BACK, SIDE, CUSTOM
}

@Entity(tableName = "personal_records")
data class PersonalRecord(
    @PrimaryKey
    val id: String,
    val userId: String,
    val exerciseId: String,
    val exerciseName: String,
    val recordType: RecordType,
    val value: Float,
    val unit: String,
    val date: LocalDate,
    val workoutSessionId: String? = null,
    val notes: String? = null,
    val achievedAt: Long = System.currentTimeMillis()
)

enum class RecordType {
    MAX_WEIGHT, MAX_REPS, MAX_DURATION, MAX_DISTANCE, MAX_VOLUME
}

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: AchievementCategory,
    val iconUrl: String,
    val points: Int,
    val requirements: AchievementRequirements,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

data class AchievementRequirements(
    val workoutCount: Int? = null,
    val totalWorkoutTime: Int? = null, // minutes
    val consecutiveDays: Int? = null,
    val weightLoss: Float? = null, // kg
    val muscleGain: Float? = null, // kg
    val personalRecords: Int? = null,
    val streakDays: Int? = null
)

enum class AchievementCategory {
    WORKOUT, PROGRESS, CONSISTENCY, MILESTONE, SOCIAL
}

@Entity(tableName = "user_achievements")
data class UserAchievement(
    @PrimaryKey
    val id: String,
    val userId: String,
    val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "workout_stats")
data class WorkoutStats(
    @PrimaryKey
    val userId: String,
    val totalWorkouts: Int = 0,
    val totalWorkoutTime: Int = 0, // minutes
    val totalCaloriesBurned: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val personalRecords: Int = 0,
    val lastWorkoutDate: LocalDate? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
