package com.fitnessss.fitlife.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Data classes for user progress tracking
data class WorkoutSession(
    val id: String,
    val workoutType: String,
    val workoutName: String,
    val duration: Int, // in minutes
    val exercisesCompleted: Int,
    val setsCompleted: Int,
    val caloriesBurned: Int,
    val date: LocalDateTime,
    val exercises: List<CompletedExercise> = emptyList()
)

data class CompletedExercise(
    val name: String,
    val sets: Int,
    val reps: List<Int>, // reps for each set
    val weight: List<Double> = emptyList(), // weight for each set (if applicable)
    val duration: Int = 0 // for time-based exercises
)

data class WeightEntry(
    val id: String = "",
    val weight: Double, // in lbs or kg
    val date: LocalDate,
    val bodyFatPercentage: Double? = null,
    val notes: String = ""
)

data class BodyMeasurement(
    val id: String = "",
    val chest: Double = 0.0,
    val waist: Double = 0.0,
    val hips: Double = 0.0,
    val biceps: Double = 0.0,
    val thighs: Double = 0.0,
    val date: LocalDate
)

data class PersonalRecord(
    val exerciseName: String,
    val recordType: RecordType,
    val value: Double,
    val unit: String,
    val date: LocalDate
)

enum class RecordType {
    MAX_WEIGHT,
    MAX_REPS,
    BEST_TIME,
    LONGEST_DURATION
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val dateEarned: LocalDate,
    val category: AchievementCategory
)

enum class AchievementCategory {
    CONSISTENCY,
    STRENGTH,
    ENDURANCE,
    WEIGHT_LOSS,
    GENERAL_FITNESS
}

data class DailyStats(
    val date: LocalDate,
    val workoutsCompleted: Int = 0,
    val totalDuration: Int = 0, // in minutes
    val caloriesBurned: Int = 0,
    val stepsCount: Int = 0,
    val waterIntake: Double = 0.0 // in liters
)

// Singleton object to manage user progress data
object UserProgressManager {
    private const val PREFS_NAME = "user_progress_prefs"
    private var prefs: SharedPreferences? = null
    private val gson = Gson()
    
    // Current user stats
    var currentWeight: MutableState<Double> = mutableStateOf(0.0)
    var goalWeight: MutableState<Double> = mutableStateOf(0.0)
    var currentBodyFat: MutableState<Double> = mutableStateOf(0.0)
    var userHeight: MutableState<Double> = mutableStateOf(0.0) // in inches
    
    // Historical data
    val workoutSessions = mutableStateOf<List<WorkoutSession>>(emptyList())
    val weightEntries = mutableStateOf<List<WeightEntry>>(emptyList())
    val bodyMeasurements = mutableStateOf<List<BodyMeasurement>>(emptyList())
    private val _personalRecords = mutableStateOf<List<PersonalRecord>>(emptyList())
    private val _achievements = mutableStateOf<List<Achievement>>(emptyList())
    val dailyStats = mutableStateOf<List<DailyStats>>(emptyList())
    
    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            loadAllData()
        }
    }
    
    private fun saveAllData() {
        prefs?.let { preferences ->
            val editor = preferences.edit()
            
            // Save current user stats
            editor.putFloat("current_weight", currentWeight.value.toFloat())
            editor.putFloat("goal_weight", goalWeight.value.toFloat())
            editor.putFloat("current_body_fat", currentBodyFat.value.toFloat())
            editor.putFloat("user_height", userHeight.value.toFloat())
            
            // Save workout sessions
            val workoutSessionsJson = gson.toJson(workoutSessions.value)
            editor.putString("workout_sessions", workoutSessionsJson)
            
            // Save weight entries
            val weightEntriesJson = gson.toJson(weightEntries.value)
            editor.putString("weight_entries", weightEntriesJson)
            
            // Save body measurements
            val bodyMeasurementsJson = gson.toJson(bodyMeasurements.value)
            editor.putString("body_measurements", bodyMeasurementsJson)
            
            // Save personal records
            val personalRecordsJson = gson.toJson(_personalRecords.value)
            editor.putString("personal_records", personalRecordsJson)
            
            // Save achievements
            val achievementsJson = gson.toJson(_achievements.value)
            editor.putString("achievements", achievementsJson)
            
            // Save daily stats
            val dailyStatsJson = gson.toJson(dailyStats.value)
            editor.putString("daily_stats", dailyStatsJson)
            
            editor.apply()
            println("DEBUG: All user progress data saved to SharedPreferences")
        }
    }
    
    private fun loadAllData() {
        prefs?.let { preferences ->
            // Load current user stats
            currentWeight.value = preferences.getFloat("current_weight", 0.0f).toDouble()
            goalWeight.value = preferences.getFloat("goal_weight", 0.0f).toDouble()
            currentBodyFat.value = preferences.getFloat("current_body_fat", 0.0f).toDouble()
            userHeight.value = preferences.getFloat("user_height", 0.0f).toDouble()
            
            // Load workout sessions
            val workoutSessionsJson = preferences.getString("workout_sessions", "[]")
            val workoutSessionsType = object : TypeToken<List<WorkoutSession>>() {}.type
            workoutSessions.value = try {
                gson.fromJson(workoutSessionsJson, workoutSessionsType) ?: emptyList()
            } catch (e: Exception) {
                println("DEBUG: Error loading workout sessions: ${e.message}")
                emptyList()
            }
            
            // Load weight entries
            val weightEntriesJson = preferences.getString("weight_entries", "[]")
            val weightEntriesType = object : TypeToken<List<WeightEntry>>() {}.type
            weightEntries.value = try {
                gson.fromJson(weightEntriesJson, weightEntriesType) ?: emptyList()
            } catch (e: Exception) {
                println("DEBUG: Error loading weight entries: ${e.message}")
                emptyList()
            }
            
            // Load body measurements
            val bodyMeasurementsJson = preferences.getString("body_measurements", "[]")
            val bodyMeasurementsType = object : TypeToken<List<BodyMeasurement>>() {}.type
            bodyMeasurements.value = try {
                gson.fromJson(bodyMeasurementsJson, bodyMeasurementsType) ?: emptyList()
            } catch (e: Exception) {
                println("DEBUG: Error loading body measurements: ${e.message}")
                emptyList()
            }
            
            // Load personal records
            val personalRecordsJson = preferences.getString("personal_records", "[]")
            val personalRecordsType = object : TypeToken<List<PersonalRecord>>() {}.type
            _personalRecords.value = try {
                gson.fromJson(personalRecordsJson, personalRecordsType) ?: emptyList()
            } catch (e: Exception) {
                println("DEBUG: Error loading personal records: ${e.message}")
                emptyList()
            }
            
            // Load achievements
            val achievementsJson = preferences.getString("achievements", "[]")
            val achievementsType = object : TypeToken<List<Achievement>>() {}.type
            _achievements.value = try {
                gson.fromJson(achievementsJson, achievementsType) ?: emptyList()
            } catch (e: Exception) {
                println("DEBUG: Error loading achievements: ${e.message}")
                emptyList()
            }
            
            // Load daily stats
            val dailyStatsJson = preferences.getString("daily_stats", "[]")
            val dailyStatsType = object : TypeToken<List<DailyStats>>() {}.type
            dailyStats.value = try {
                gson.fromJson(dailyStatsJson, dailyStatsType) ?: emptyList()
            } catch (e: Exception) {
                println("DEBUG: Error loading daily stats: ${e.message}")
                emptyList()
            }
            
            println("DEBUG: All user progress data loaded from SharedPreferences")
            println("DEBUG: Loaded ${workoutSessions.value.size} workout sessions")
            println("DEBUG: Loaded ${dailyStats.value.size} daily stats entries")
        }
    }
    
    // Getters for historical data
    val personalRecords: List<PersonalRecord> get() = _personalRecords.value
    val achievements: List<Achievement> get() = _achievements.value
    
    // Methods to add data
    fun addWorkoutSession(session: WorkoutSession) {
        println("DEBUG: Adding workout session - Name: ${session.workoutName}, Date: ${session.date.toLocalDate()}, Duration: ${session.duration}")
        workoutSessions.value = workoutSessions.value + session
        updateDailyStats(session.date.toLocalDate(), session)
        println("DEBUG: Daily stats updated. Today's workouts: ${dailyStats.value.find { it.date == LocalDate.now() }?.workoutsCompleted ?: 0}")
        checkAndAwardAchievements()
        updatePersonalRecords(session)
        saveAllData() // Save data persistently after adding workout
    }
    
    fun addWeightEntry(entry: WeightEntry) {
        weightEntries.value = weightEntries.value + entry
        currentWeight.value = entry.weight
        if (entry.bodyFatPercentage != null) {
            currentBodyFat.value = entry.bodyFatPercentage
        }
        saveAllData() // Save data persistently after adding weight entry
    }
    
    fun addBodyMeasurement(measurement: BodyMeasurement) {
        bodyMeasurements.value = bodyMeasurements.value + measurement
        saveAllData() // Save data persistently after adding body measurement
    }
    
    private fun updateDailyStats(date: LocalDate, session: WorkoutSession) {
        println("DEBUG: updateDailyStats - Date: $date, Session: ${session.workoutName}, Duration: ${session.duration}, Calories: ${session.caloriesBurned}")
        
        val existingStats = dailyStats.value.find { it.date == date }
        println("DEBUG: Existing stats for $date: ${existingStats?.workoutsCompleted ?: 0} workouts, ${existingStats?.caloriesBurned ?: 0} calories")
        
        val updatedStats = if (existingStats != null) {
            existingStats.copy(
                workoutsCompleted = existingStats.workoutsCompleted + 1,
                totalDuration = existingStats.totalDuration + session.duration,
                caloriesBurned = existingStats.caloriesBurned + session.caloriesBurned
            )
        } else {
            DailyStats(
                date = date,
                workoutsCompleted = 1,
                totalDuration = session.duration,
                caloriesBurned = session.caloriesBurned
            )
        }
        
        println("DEBUG: Updated stats for $date: ${updatedStats.workoutsCompleted} workouts, ${updatedStats.caloriesBurned} calories")
        
        dailyStats.value = dailyStats.value.filter { it.date != date } + updatedStats
        println("DEBUG: Total daily stats entries: ${dailyStats.value.size}")
    }
    
    private fun updatePersonalRecords(session: WorkoutSession) {
        session.exercises.forEach { exercise ->
            // Check for max weight record
            val maxWeight = exercise.weight.maxOrNull()
            if (maxWeight != null && maxWeight > 0) {
                val existingRecord = _personalRecords.value.find { 
                    it.exerciseName == exercise.name && it.recordType == RecordType.MAX_WEIGHT 
                }
                if (existingRecord == null || maxWeight > existingRecord.value) {
                    val newRecord = PersonalRecord(
                        exerciseName = exercise.name,
                        recordType = RecordType.MAX_WEIGHT,
                        value = maxWeight,
                        unit = "lbs",
                        date = session.date.toLocalDate()
                    )
                    _personalRecords.value = _personalRecords.value.filter { 
                        !(it.exerciseName == exercise.name && it.recordType == RecordType.MAX_WEIGHT)
                    } + newRecord
                }
            }
            
            // Check for max reps record
            val maxReps = exercise.reps.maxOrNull()
            if (maxReps != null && maxReps > 0) {
                val existingRecord = _personalRecords.value.find { 
                    it.exerciseName == exercise.name && it.recordType == RecordType.MAX_REPS 
                }
                if (existingRecord == null || maxReps.toDouble() > existingRecord.value) {
                    val newRecord = PersonalRecord(
                        exerciseName = exercise.name,
                        recordType = RecordType.MAX_REPS,
                        value = maxReps.toDouble(),
                        unit = "reps",
                        date = session.date.toLocalDate()
                    )
                    _personalRecords.value = _personalRecords.value.filter { 
                        !(it.exerciseName == exercise.name && it.recordType == RecordType.MAX_REPS)
                    } + newRecord
                }
            }
        }
    }
    
    private fun checkAndAwardAchievements() {
        val today = LocalDate.now()
        
        // Check for workout streak
        val recentDays = (0..6).map { today.minusDays(it.toLong()) }
        val workoutDays = recentDays.filter { date ->
            dailyStats.value.any { it.date == date && it.workoutsCompleted > 0 }
        }
        
        if (workoutDays.size >= 7) {
            val streakAchievement = Achievement(
                id = "streak_7",
                title = "7-Day Streak",
                description = "Worked out for 7 consecutive days",
                iconName = "streak",
                dateEarned = today,
                category = AchievementCategory.CONSISTENCY
            )
            if (!_achievements.value.any { it.id == "streak_7" }) {
                _achievements.value = _achievements.value + streakAchievement
            }
        }
        
        // Check for total workouts milestone
        val totalWorkouts = workoutSessions.value.size
        when (totalWorkouts) {
            10 -> addAchievementIfNew(
                Achievement(
                    id = "workouts_10",
                    title = "First 10 Workouts",
                    description = "Completed your first 10 workouts",
                    iconName = "milestone",
                    dateEarned = today,
                    category = AchievementCategory.GENERAL_FITNESS
                )
            )
            50 -> addAchievementIfNew(
                Achievement(
                    id = "workouts_50",
                    title = "Fitness Enthusiast",
                    description = "Completed 50 workouts",
                    iconName = "enthusiast",
                    dateEarned = today,
                    category = AchievementCategory.GENERAL_FITNESS
                )
            )
            100 -> addAchievementIfNew(
                Achievement(
                    id = "workouts_100",
                    title = "Fitness Warrior",
                    description = "Completed 100 workouts",
                    iconName = "warrior",
                    dateEarned = today,
                    category = AchievementCategory.GENERAL_FITNESS
                )
            )
        }
        
        // Check weight loss goals
        if (weightEntries.value.size >= 2) {
            val firstWeight = weightEntries.value.minByOrNull { it.date }?.weight ?: 0.0
            val currentWeight = weightEntries.value.maxByOrNull { it.date }?.weight ?: 0.0
            val weightLoss = firstWeight - currentWeight
            
            if (weightLoss >= 5.0) {
                addAchievementIfNew(
                    Achievement(
                        id = "weight_loss_5",
                        title = "Weight Loss Goal",
                        description = "Lost 5 pounds",
                        iconName = "weight_loss",
                        dateEarned = today,
                        category = AchievementCategory.WEIGHT_LOSS
                    )
                )
            }
        }
    }
    
    private fun addAchievementIfNew(achievement: Achievement) {
        if (!_achievements.value.any { it.id == achievement.id }) {
            _achievements.value = _achievements.value + achievement
        }
    }
    
    // Analytics methods
    fun getWorkoutFrequency(days: Int = 30): Double {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        val workoutsInPeriod = workoutSessions.value.count { 
            val workoutDate = it.date.toLocalDate()
            workoutDate.isAfter(startDate) && workoutDate.isBefore(endDate.plusDays(1))
        }
        return workoutsInPeriod.toDouble() / days.toDouble()
    }
    
    fun getTotalCaloriesBurned(days: Int = 30): Int {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        return workoutSessions.value
            .filter { 
                val workoutDate = it.date.toLocalDate()
                workoutDate.isAfter(startDate) && workoutDate.isBefore(endDate.plusDays(1))
            }
            .sumOf { it.caloriesBurned }
    }
    
    fun getAverageWorkoutDuration(days: Int = 30): Double {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        val recentWorkouts = workoutSessions.value.filter { 
            val workoutDate = it.date.toLocalDate()
            workoutDate.isAfter(startDate) && workoutDate.isBefore(endDate.plusDays(1))
        }
        return if (recentWorkouts.isNotEmpty()) {
            recentWorkouts.map { it.duration }.average()
        } else 0.0
    }
    
    fun getCurrentStreak(): Int {
        val today = LocalDate.now()
        var streak = 0
        var currentDate = today
        
        while (true) {
            val hasWorkout = dailyStats.value.any { 
                it.date == currentDate && it.workoutsCompleted > 0 
            }
            if (hasWorkout) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                break
            }
        }
        
        return streak
    }
    
    fun getWeightProgress(): Pair<Double, Double>? {
        if (weightEntries.value.size < 2) return null
        val firstWeight = weightEntries.value.minByOrNull { it.date }?.weight ?: return null
        val currentWeight = weightEntries.value.maxByOrNull { it.date }?.weight ?: return null
        return Pair(firstWeight, currentWeight)
    }
    
    // Sample data initialization disabled - only real user data will be shown
    // fun initializeSampleData() {
    //     // This function is disabled to ensure only real user activities are logged
    // }
    
    /**
     * Clears all user progress data
     */
    fun clearAllData() {
        currentWeight.value = 0.0
        goalWeight.value = 0.0
        currentBodyFat.value = 0.0
        userHeight.value = 0.0
        
        workoutSessions.value = emptyList()
        weightEntries.value = emptyList()
        bodyMeasurements.value = emptyList()
        _personalRecords.value = emptyList()
        _achievements.value = emptyList()
        dailyStats.value = emptyList()
        
        // Clear persistent storage
        prefs?.edit()?.clear()?.apply()
        println("DEBUG: All user progress data cleared from SharedPreferences")
    }
    
    // Firebase sync methods
    fun syncToFirebase() {
        // This will be called by DataSyncManager
        println("UserProgressManager: Data ready for Firebase sync")
    }
}
