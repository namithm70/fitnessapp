package com.fitnessss.fitlife.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val muscleGroups: List<MuscleGroup>,
    val workoutType: WorkoutType,
    val difficulty: Difficulty,
    val estimatedDuration: Int, // minutes
    val estimatedCalories: Int,
    val equipment: List<Equipment>,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isCustom: Boolean = false,
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val muscleGroups: List<MuscleGroup>,
    val equipment: List<Equipment>,
    val instructions: List<String>,
    val tips: List<String>,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val gifUrl: String? = null,
    val restTime: Int = 60, // seconds
    val notes: String? = null
)

data class ExerciseSet(
    val reps: Int? = null,
    val duration: Int? = null, // seconds
    val weight: Float? = null, // kg
    val distance: Float? = null, // meters
    val isWarmup: Boolean = false
)

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey
    val id: String,
    val workoutId: String,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val notes: String? = null,
    val rating: Int? = null, // 1-5 stars
    val caloriesBurned: Int? = null
)

data class CompletedExercise(
    val exerciseId: String,
    val sets: List<CompletedSet>,
    val notes: String? = null
)

data class CompletedSet(
    val reps: Int? = null,
    val duration: Int? = null,
    val weight: Float? = null,
    val distance: Float? = null,
    val isCompleted: Boolean = true
)

enum class MuscleGroup {
    CHEST, BACK, SHOULDERS, BICEPS, TRICEPS, FOREARMS,
    CORE, QUADS, HAMSTRINGS, GLUTES, CALVES, FULL_BODY
}

enum class WorkoutType {
    STRENGTH, CARDIO, FLEXIBILITY, HIIT, YOGA, PILATES, SPORTS
}

enum class Difficulty {
    BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
}

enum class Equipment {
    NONE, DUMBBELLS, BARBELL, KETTLEBELL, RESISTANCE_BANDS,
    PULL_UP_BAR, DIP_BARS, BENCH, TREADMILL, STATIONARY_BIKE,
    ROWING_MACHINE, ELLIPTICAL, SMITH_MACHINE, CABLE_MACHINE
}
