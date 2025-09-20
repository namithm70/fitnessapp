package com.fitnessss.fitlife.ui.screens.history

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.fitnessss.fitlife.data.model.ActivityType

@Composable
fun getActivityTypeIcon(activityType: ActivityType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (activityType) {
        ActivityType.WORKOUT_COMPLETED -> Icons.Filled.FitnessCenter
        ActivityType.NUTRITION_LOGGED -> Icons.Filled.Restaurant
        ActivityType.WATER_INTAKE -> Icons.Filled.WaterDrop
        ActivityType.PROGRESS_MEASUREMENT -> Icons.Filled.TrendingUp
        ActivityType.PROGRESS_PHOTO -> Icons.Filled.PhotoCamera
        ActivityType.PERSONAL_RECORD -> Icons.Filled.EmojiEvents
        ActivityType.ACHIEVEMENT_UNLOCKED -> Icons.Filled.Star
        ActivityType.STEPS_TRACKED -> Icons.Filled.DirectionsWalk
        ActivityType.SLEEP_LOGGED -> Icons.Filled.Bedtime
        ActivityType.MOOD_LOGGED -> Icons.Filled.SentimentSatisfied
        ActivityType.ENERGY_LEVEL_LOGGED -> Icons.Filled.BatteryChargingFull
        ActivityType.CARDIO_SESSION -> Icons.Filled.DirectionsRun
        ActivityType.STRETCHING_SESSION -> Icons.Filled.FitnessCenter
        ActivityType.MEDITATION_SESSION -> Icons.Filled.SelfImprovement
        ActivityType.GYM_VISIT -> Icons.Filled.LocationOn
        ActivityType.WEIGHT_MEASUREMENT -> Icons.Filled.MonitorWeight
        ActivityType.BODY_FAT_MEASUREMENT -> Icons.Filled.Analytics
        ActivityType.MEASUREMENTS_TAKEN -> Icons.Filled.Straighten
        ActivityType.PHOTO_TAKEN -> Icons.Filled.PhotoCamera
        ActivityType.RECORD_BROKEN -> Icons.Filled.Whatshot
        ActivityType.GOAL_ACHIEVED -> Icons.Filled.Flag
        ActivityType.CHALLENGE_COMPLETED -> Icons.Filled.MilitaryTech
        ActivityType.STREAK_MAINTAINED -> Icons.Filled.LocalFireDepartment
        ActivityType.WORKOUT_PLAN_CREATED -> Icons.Filled.Assignment
        ActivityType.MEAL_PLAN_CREATED -> Icons.Filled.RestaurantMenu
    }
}

@Composable
fun getActivityTypeColor(activityType: ActivityType): Color {
    return when (activityType) {
        ActivityType.WORKOUT_COMPLETED -> Color(0xFF2196F3) // Blue
        ActivityType.NUTRITION_LOGGED -> Color(0xFF4CAF50) // Green
        ActivityType.WATER_INTAKE -> Color(0xFF00BCD4) // Cyan
        ActivityType.PROGRESS_MEASUREMENT -> Color(0xFF9C27B0) // Purple
        ActivityType.PROGRESS_PHOTO -> Color(0xFF607D8B) // Blue Grey
        ActivityType.PERSONAL_RECORD -> Color(0xFFFF9800) // Orange
        ActivityType.ACHIEVEMENT_UNLOCKED -> Color(0xFFFFD700) // Gold
        ActivityType.STEPS_TRACKED -> Color(0xFF795548) // Brown
        ActivityType.SLEEP_LOGGED -> Color(0xFF3F51B5) // Indigo
        ActivityType.MOOD_LOGGED -> Color(0xFFE91E63) // Pink
        ActivityType.ENERGY_LEVEL_LOGGED -> Color(0xFFFF5722) // Deep Orange
        ActivityType.CARDIO_SESSION -> Color(0xFFF44336) // Red
        ActivityType.STRETCHING_SESSION -> Color(0xFF8BC34A) // Light Green
        ActivityType.MEDITATION_SESSION -> Color(0xFF673AB7) // Deep Purple
        ActivityType.GYM_VISIT -> Color(0xFF009688) // Teal
        ActivityType.WEIGHT_MEASUREMENT -> Color(0xFF795548) // Brown
        ActivityType.BODY_FAT_MEASUREMENT -> Color(0xFF607D8B) // Blue Grey
        ActivityType.MEASUREMENTS_TAKEN -> Color(0xFF9E9E9E) // Grey
        ActivityType.PHOTO_TAKEN -> Color(0xFF607D8B) // Blue Grey
        ActivityType.RECORD_BROKEN -> Color(0xFFFF5722) // Deep Orange
        ActivityType.GOAL_ACHIEVED -> Color(0xFF4CAF50) // Green
        ActivityType.CHALLENGE_COMPLETED -> Color(0xFFFF9800) // Orange
        ActivityType.STREAK_MAINTAINED -> Color(0xFFFF5722) // Deep Orange
        ActivityType.WORKOUT_PLAN_CREATED -> Color(0xFF2196F3) // Blue
        ActivityType.MEAL_PLAN_CREATED -> Color(0xFF4CAF50) // Green
    }
}
