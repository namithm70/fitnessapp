package com.fitnessss.fitlife.data.service

import com.fitnessss.fitlife.data.local.HistoryDao
import com.fitnessss.fitlife.data.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryLoggingService @Inject constructor(
    private val historyDao: HistoryDao
) {
    private val now: LocalDateTime
        get() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

    private val today: LocalDate
        get() = now.date

    suspend fun logWorkoutSession(
        userId: String,
        workoutId: String,
        workoutName: String,
        duration: Int,
        caloriesBurned: Int?,
        exercisesCompleted: Int,
        setsCompleted: Int,
        notes: String? = null
    ) {
        println("DEBUG: HistoryLoggingService.logWorkoutSession called with workoutName: $workoutName")
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.WORKOUT_COMPLETED,
            title = "Completed $workoutName",
            description = "Completed $workoutName with $exercisesCompleted exercises and $setsCompleted sets (${caloriesBurned ?: 0} calories burned)",
            date = today,
            timestamp = now,
            duration = duration,
            calories = caloriesBurned,
            notes = notes
        )
        println("DEBUG: Inserting activity to database: ${activity.title}")
        historyDao.insertActivity(activity)
        println("DEBUG: Activity inserted successfully")
    }

    suspend fun logNutritionEntry(
        userId: String,
        nutritionLogId: String,
        mealType: MealType,
        totalCalories: Float,
        foodItems: List<LoggedFood>,
        foodNames: List<String> = emptyList(),
        notes: String? = null
    ) {
        // Create detailed description with food items
        val foodDescription = if (foodItems.isNotEmpty() && foodNames.isNotEmpty()) {
            val foodDetails = foodItems.zip(foodNames).joinToString(", ") { (food, name) ->
                "$name (${food.amount}${food.unit}, ${food.calories.toInt()} cal)"
            }
            "Ate: $foodDetails"
        } else if (foodItems.isNotEmpty()) {
            val foodDetails = foodItems.joinToString(", ") { 
                "${it.amount}${it.unit} (${it.calories.toInt()} cal)"
            }
            "Ate: $foodDetails"
        } else {
            "Logged ${mealType.name.lowercase().capitalize()}"
        }
        
        // Create a more specific title based on food items
        val title = if (foodNames.isNotEmpty()) {
            if (foodNames.size == 1) {
                "Ate ${foodNames.first()}"
            } else {
                "Ate ${foodNames.first()} +${foodNames.size - 1} more"
            }
        } else {
            "Logged ${mealType.name.lowercase().capitalize()}"
        }
        
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.NUTRITION_LOGGED,
            title = title,
            description = "$foodDescription (${totalCalories.toInt()} calories)",
            date = today,
            timestamp = now,
            calories = totalCalories.toInt(),
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logWaterIntake(
        userId: String,
        amount: Float,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.WATER_INTAKE,
            title = "Drank ${amount.toInt()}ml of water",
            description = "Logged water intake",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logProgressMeasurement(
        userId: String,
        measurementId: String,
        weight: Float?,
        bodyFatPercentage: Float?,
        measurements: BodyMeasurements?,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.PROGRESS_MEASUREMENT,
            title = "Recorded progress measurements",
            description = "Updated progress tracking data",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logPersonalRecord(
        userId: String,
        recordId: String,
        exerciseName: String,
        recordType: RecordType,
        value: Float,
        unit: String,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.PERSONAL_RECORD,
            title = "New $recordType record: $value $unit",
            description = "Achieved new personal record in $exerciseName",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logAchievement(
        userId: String,
        achievementId: String,
        achievementName: String,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.ACHIEVEMENT_UNLOCKED,
            title = "Unlocked: $achievementName",
            description = "Achievement unlocked",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logSteps(
        userId: String,
        stepsCount: Int,
        distance: Float? = null,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.STEPS_TRACKED,
            title = "Walked $stepsCount steps",
            description = "Daily step count recorded",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logSleep(
        userId: String,
        sleepHours: Float,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.SLEEP_LOGGED,
            title = "Slept ${sleepHours} hours",
            description = "Sleep duration recorded",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logMood(
        userId: String,
        mood: Mood,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.MOOD_LOGGED,
            title = "Mood: ${mood.name.lowercase().capitalize()}",
            description = "Mood tracking entry",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logEnergyLevel(
        userId: String,
        energyLevel: EnergyLevel,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.ENERGY_LEVEL_LOGGED,
            title = "Energy: ${energyLevel.name.lowercase().capitalize()}",
            description = "Energy level tracking entry",
            date = today,
            timestamp = now,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logCardioSession(
        userId: String,
        duration: Int,
        caloriesBurned: Int?,
        distance: Float?,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.CARDIO_SESSION,
            title = "Cardio session",
            description = "Completed cardio workout",
            date = today,
            timestamp = now,
            duration = duration,
            calories = caloriesBurned,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logStretching(
        userId: String,
        duration: Int,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.STRETCHING_SESSION,
            title = "Stretching session",
            description = "Completed stretching routine",
            date = today,
            timestamp = now,
            duration = duration,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logMeditation(
        userId: String,
        duration: Int,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.MEDITATION_SESSION,
            title = "Meditation session",
            description = "Completed meditation",
            date = today,
            timestamp = now,
            duration = duration,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }

    suspend fun logGymVisit(
        userId: String,
        gymName: String,
        duration: Int?,
        notes: String? = null
    ) {
        val activity = ActivityHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            activityType = ActivityType.GYM_VISIT,
            title = "Visited $gymName",
            description = "Gym visit recorded",
            date = today,
            timestamp = now,
            duration = duration,
            notes = notes
        )
        historyDao.insertActivity(activity)
    }
}

private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
}
