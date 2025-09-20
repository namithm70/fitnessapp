package com.fitnessss.fitlife.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class GeneratedWorkout(
    val name: String,
    val type: String,
    val icon: ImageVector,
    val duration: Int,
    val estimatedCalories: Int,
    val exercises: List<GeneratedExercise>
)

data class GeneratedExercise(
    val name: String,
    val sets: Int,
    val reps: Int
)
