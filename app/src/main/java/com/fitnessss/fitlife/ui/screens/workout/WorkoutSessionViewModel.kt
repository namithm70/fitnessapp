package com.fitnessss.fitlife.ui.screens.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.UserProgressManager
import com.fitnessss.fitlife.data.WorkoutSession as UserProgressWorkoutSession
import com.fitnessss.fitlife.data.CompletedExercise
import com.fitnessss.fitlife.data.service.HistoryLoggingService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class WorkoutSessionViewModel @Inject constructor(
    private val historyLoggingService: HistoryLoggingService
) : ViewModel() {

    private val currentUserId = "current_user" // TODO: Get from auth

    fun saveWorkoutSession(
        workoutId: String,
        exercises: List<SessionExercise>,
        completedSets: Int,
        elapsedSeconds: Int
    ) {
        println("DEBUG: saveWorkoutSession called - workoutId: $workoutId, exercises: ${exercises.size}, completedSets: $completedSets, elapsedSeconds: $elapsedSeconds")
        // Calculate calories burned for each exercise individually
        val totalCaloriesBurned = calculateTotalCaloriesBurned(exercises, completedSets, elapsedSeconds)
        
        // Convert SessionExercise to CompletedExercise
        val completedExercises = exercises.map { exercise ->
            CompletedExercise(
                name = exercise.name,
                sets = exercise.sets,
                reps = List(exercise.sets) { 
                    // Estimate reps based on exercise target
                    when {
                        exercise.target.contains("reps") -> {
                            val repsMatch = Regex("(\\d+)").find(exercise.target)
                            repsMatch?.value?.toIntOrNull() ?: 10
                        }
                        exercise.target.contains("seconds") || exercise.target.contains("sec") -> {
                            val secondsMatch = Regex("(\\d+)").find(exercise.target)
                            secondsMatch?.value?.toIntOrNull() ?: 30
                        }
                        else -> 10
                    }
                }
            )
        }
        
        val workoutSession = UserProgressWorkoutSession(
            id = UUID.randomUUID().toString(),
            workoutType = when {
                workoutId.startsWith("ai_") -> "AI Generated"
                workoutId == "strength" -> "Strength"
                workoutId == "cardio" -> "Cardio"
                workoutId in listOf("hiit", "hiit_burn") -> "HIIT"
                workoutId == "yoga" -> "Yoga"
                else -> "General"
            },
            workoutName = when {
                workoutId.startsWith("ai_") -> {
                    // Remove "ai_" prefix and format the name properly
                    workoutId.removePrefix("ai_").replace("_", " ").split(" ").joinToString(" ") { 
                        it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } 
                    }
                }
                workoutId == "quick_start" -> "Quick Start Workout"
                workoutId == "custom" -> "Custom Workout"
                workoutId == "strength" -> "Strength Training"
                workoutId == "cardio" -> "Cardio Workout"
                workoutId == "hiit" -> "HIIT Training"
                workoutId == "yoga" -> "Yoga Session"
                workoutId == "full_body_blast" -> "Full Body Blast"
                workoutId == "morning_cardio" -> "Morning Cardio"
                workoutId == "hiit_burn" -> "HIIT Burn"
                workoutId == "7-minute_workout" -> "7-Minute Workout"
                else -> workoutId.replace("_", " ").split(" ").joinToString(" ") { 
                    it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } 
                }
            },
            duration = elapsedSeconds / 60,
            exercisesCompleted = exercises.size,
            setsCompleted = completedSets,
            caloriesBurned = totalCaloriesBurned,
            date = LocalDateTime.now(),
            exercises = completedExercises
        )
        
        // Save to UserProgressManager (existing functionality)
        println("DEBUG: Saving workout session - Name: ${workoutSession.workoutName}, Type: ${workoutSession.workoutType}, Duration: ${workoutSession.duration}, Calories: ${workoutSession.caloriesBurned}")
        UserProgressManager.addWorkoutSession(workoutSession)
        println("DEBUG: Workout session saved successfully. Total workouts: ${UserProgressManager.workoutSessions.value.size}")
        
        // Log to History database
        viewModelScope.launch {
            try {
                println("DEBUG: Logging workout to history - workoutId: $workoutId, workoutName: ${workoutSession.workoutName}")
                historyLoggingService.logWorkoutSession(
                    userId = currentUserId,
                    workoutId = workoutId,
                    workoutName = workoutSession.workoutName,
                    duration = workoutSession.duration,
                    caloriesBurned = workoutSession.caloriesBurned,
                    exercisesCompleted = workoutSession.exercisesCompleted,
                    setsCompleted = workoutSession.setsCompleted,
                    notes = "Completed ${workoutSession.workoutType} workout"
                )
                println("DEBUG: Successfully logged workout to history")
            } catch (e: Exception) {
                // Log error but don't fail the workout completion
                println("DEBUG: Error logging workout to history: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Calculate total calories burned based on individual exercises
     */
    private fun calculateTotalCaloriesBurned(
        exercises: List<SessionExercise>,
        completedSets: Int,
        elapsedSeconds: Int
    ): Int {
        var totalCalories = 0
        
        exercises.forEach { exercise ->
            val exerciseCalories = calculateExerciseCalories(exercise, completedSets)
            totalCalories += exerciseCalories
            println("DEBUG: Exercise '${exercise.name}' burned $exerciseCalories calories")
        }
        
        // Add base metabolic rate contribution during workout time
        val baseMetabolicRate = 1.5 // calories per minute during exercise
        val baseCalories = (elapsedSeconds / 60.0 * baseMetabolicRate).toInt()
        totalCalories += baseCalories
        
        println("DEBUG: Total calories burned: $totalCalories (including base metabolic rate: $baseCalories)")
        return totalCalories
    }
    
    /**
     * Calculate calories burned for a specific exercise
     */
    private fun calculateExerciseCalories(exercise: SessionExercise, completedSets: Int): Int {
        val exerciseName = exercise.name.lowercase()
        val sets = exercise.sets
        val target = exercise.target.lowercase()
        
        // Extract duration or reps from target
        val duration = extractDuration(target)
        val reps = extractReps(target)
        
        // Calculate calories based on exercise type and intensity
        val caloriesPerSet = when {
            // High-intensity cardio exercises
            exerciseName.contains("burpee") -> 15
            exerciseName.contains("jumping jack") -> 12
            exerciseName.contains("mountain climber") -> 14
            exerciseName.contains("high knee") -> 13
            exerciseName.contains("jump squat") -> 16
            exerciseName.contains("plank jack") -> 10
            exerciseName.contains("running") -> 18
            exerciseName.contains("butt kick") -> 11
            
            // Strength exercises
            exerciseName.contains("push-up") -> 8
            exerciseName.contains("pull-up") -> 12
            exerciseName.contains("squat") -> 10
            exerciseName.contains("lunge") -> 9
            exerciseName.contains("deadlift") -> 15
            exerciseName.contains("plank") -> 6
            exerciseName.contains("crunch") -> 5
            exerciseName.contains("dip") -> 8
            exerciseName.contains("wall sit") -> 7
            exerciseName.contains("step-up") -> 8
            
            // Yoga and flexibility
            exerciseName.contains("yoga") || exerciseName.contains("pose") -> 3
            exerciseName.contains("downward dog") -> 4
            exerciseName.contains("warrior") -> 5
            exerciseName.contains("tree") -> 3
            exerciseName.contains("triangle") -> 4
            exerciseName.contains("child") -> 2
            
            // Warm-up and cool-down
            exerciseName.contains("warm") || exerciseName.contains("cool") -> 4
            exerciseName.contains("jog") -> 12
            exerciseName.contains("walk") -> 6
            
            // Default for unknown exercises
            else -> 8
        }
        
        // Adjust calories based on duration or reps
        val adjustedCalories = when {
            duration > 0 -> {
                // For time-based exercises, calculate based on duration
                val durationMultiplier = duration / 30.0 // Base on 30 seconds
                (caloriesPerSet * durationMultiplier).toInt()
            }
            reps > 0 -> {
                // For rep-based exercises, calculate based on reps
                val repMultiplier = reps / 10.0 // Base on 10 reps
                (caloriesPerSet * repMultiplier).toInt()
            }
            else -> caloriesPerSet
        }
        
        // Multiply by number of sets
        return adjustedCalories * sets
    }
    
    /**
     * Extract duration in seconds from target string
     */
    private fun extractDuration(target: String): Int {
        val patterns = listOf(
            Regex("(\\d+)\\s*seconds?"),
            Regex("(\\d+)\\s*sec"),
            Regex("(\\d+)\\s*minutes?"),
            Regex("(\\d+)\\s*min")
        )
        
        patterns.forEach { pattern ->
            val match = pattern.find(target)
            if (match != null) {
                val value = match.groupValues[1].toIntOrNull() ?: 0
                return if (target.contains("minute") || target.contains("min")) {
                    value * 60
                } else {
                    value
                }
            }
        }
        return 0
    }
    
    /**
     * Extract number of reps from target string
     */
    private fun extractReps(target: String): Int {
        val patterns = listOf(
            Regex("(\\d+)\\s*reps?"),
            Regex("(\\d+)-\\d+\\s*reps?"), // For ranges like "10-15 reps"
            Regex("(\\d+)\\s*per\\s*leg"), // For exercises like "10 per leg"
            Regex("(\\d+)\\s*each"), // For exercises like "30 each"
            Regex("(\\d+)\\s*each\\s*side") // For exercises like "30 each side"
        )
        
        patterns.forEach { pattern ->
            val match = pattern.find(target)
            if (match != null) {
                val value = match.groupValues[1].toIntOrNull() ?: 0
                return when {
                    target.contains("per leg") -> value * 2 // Both legs
                    target.contains("each side") -> value * 2 // Both sides
                    target.contains("each") -> value
                    else -> value
                }
            }
        }
        return 0
    }
}

// Move SessionExercise data class here so it can be used by the ViewModel
data class SessionExercise(
    val id: String,
    val name: String,
    val sets: Int,
    val target: String
)
