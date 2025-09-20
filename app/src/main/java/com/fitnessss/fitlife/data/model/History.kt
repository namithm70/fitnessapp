package com.fitnessss.fitlife.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

@Entity(tableName = "activity_history")
data class ActivityHistory(
    @PrimaryKey
    val id: String,
    val userId: String,
    val activityType: ActivityType,
    val title: String,
    val description: String,
    val date: LocalDate,
    val timestamp: LocalDateTime,
    val duration: Int? = null, // minutes
    val calories: Int? = null,
    val isCompleted: Boolean = true,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)



@Entity(tableName = "daily_summaries")
data class DailySummary(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: LocalDate,
    val totalActivities: Int = 0,
    val totalWorkouts: Int = 0,
    val totalWorkoutTime: Int = 0, // minutes
    val totalCaloriesBurned: Int = 0,
    val totalCaloriesConsumed: Int = 0,
    val totalWaterIntake: Float = 0f, // ml
    val totalSteps: Int = 0,
    val averageHeartRate: Int? = null,
    val sleepHours: Float? = null,
    val averageMood: Mood? = null,
    val averageEnergyLevel: EnergyLevel? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weekly_summaries")
data class WeeklySummary(
    @PrimaryKey
    val id: String,
    val userId: String,
    val weekStartDate: LocalDate,
    val totalActivities: Int = 0,
    val totalWorkouts: Int = 0,
    val totalWorkoutTime: Int = 0, // minutes
    val totalCaloriesBurned: Int = 0,
    val totalCaloriesConsumed: Int = 0,
    val totalWaterIntake: Float = 0f, // ml
    val totalSteps: Int = 0,
    val averageHeartRate: Int? = null,
    val averageSleepHours: Float? = null,
    val averageMood: Mood? = null,
    val averageEnergyLevel: EnergyLevel? = null,
    val mostActiveDay: LocalDate? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "monthly_summaries")
data class MonthlySummary(
    @PrimaryKey
    val id: String,
    val userId: String,
    val year: Int,
    val month: Int,
    val totalActivities: Int = 0,
    val totalWorkouts: Int = 0,
    val totalWorkoutTime: Int = 0, // minutes
    val totalCaloriesBurned: Int = 0,
    val totalCaloriesConsumed: Int = 0,
    val totalWaterIntake: Float = 0f, // ml
    val totalSteps: Int = 0,
    val averageHeartRate: Int? = null,
    val averageSleepHours: Float? = null,
    val averageMood: Mood? = null,
    val averageEnergyLevel: EnergyLevel? = null,
    val mostActiveWeek: LocalDate? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "activity_streaks")
data class ActivityStreak(
    @PrimaryKey
    val id: String,
    val userId: String,
    val activityType: String,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastActivityDate: LocalDate? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey
    val id: String,
    val userId: String,
    val milestoneType: MilestoneType,
    val title: String,
    val description: String,
    val value: Float,
    val unit: String,
    val achievedAt: LocalDateTime,
    val activityHistoryId: String? = null,
    val isCelebrated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ActivityType {
    WORKOUT_COMPLETED,
    NUTRITION_LOGGED,
    WATER_INTAKE,
    PROGRESS_MEASUREMENT,
    PROGRESS_PHOTO,
    PERSONAL_RECORD,
    ACHIEVEMENT_UNLOCKED,
    STEPS_TRACKED,
    SLEEP_LOGGED,
    MOOD_LOGGED,
    ENERGY_LEVEL_LOGGED,
    CARDIO_SESSION,
    STRETCHING_SESSION,
    MEDITATION_SESSION,
    GYM_VISIT,
    WEIGHT_MEASUREMENT,
    BODY_FAT_MEASUREMENT,
    MEASUREMENTS_TAKEN,
    PHOTO_TAKEN,
    RECORD_BROKEN,
    GOAL_ACHIEVED,
    CHALLENGE_COMPLETED,
    STREAK_MAINTAINED,
    WORKOUT_PLAN_CREATED,
    MEAL_PLAN_CREATED
}

enum class Mood {
    EXCELLENT, GREAT, GOOD, OKAY, POOR, TERRIBLE
}

enum class EnergyLevel {
    VERY_HIGH, HIGH, MEDIUM, LOW, VERY_LOW
}

enum class MilestoneType {
    WORKOUT_COUNT,
    WORKOUT_TIME,
    CALORIES_BURNED,
    STREAK_DAYS,
    WEIGHT_LOSS,
    MUSCLE_GAIN,
    PERSONAL_RECORDS,
    ACHIEVEMENTS,
    STEPS_COUNT,
    WATER_INTAKE,
    NUTRITION_GOALS,
    PROGRESS_PHOTOS
}
