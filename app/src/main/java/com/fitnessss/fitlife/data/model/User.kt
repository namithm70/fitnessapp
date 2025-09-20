package com.fitnessss.fitlife.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val profileImageUrl: String? = null,
    val fitnessLevel: FitnessLevel = FitnessLevel.BEGINNER,
    val fitnessGoals: List<FitnessGoal> = emptyList(),
    val workoutDaysPerWeek: Int = 3,
    val preferredWorkoutDuration: Int = 45, // minutes
    val injuries: List<String> = emptyList(),
    val limitations: List<String> = emptyList(),
    val height: Float? = null, // cm
    val weight: Float? = null, // kg
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis()
)

enum class FitnessLevel {
    BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
}

enum class FitnessGoal {
    WEIGHT_LOSS, MUSCLE_GAIN, STRENGTH, ENDURANCE, FLEXIBILITY, GENERAL_FITNESS
}

enum class Gender {
    MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
}
