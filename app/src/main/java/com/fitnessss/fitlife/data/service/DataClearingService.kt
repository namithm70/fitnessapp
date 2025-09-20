package com.fitnessss.fitlife.data.service

import com.fitnessss.fitlife.data.UserProgressManager
import com.fitnessss.fitlife.data.UserProfileManager
import com.fitnessss.fitlife.data.local.HistoryDao
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataClearingService @Inject constructor(
    private val historyDao: HistoryDao
) {
    
    private val currentUserId = "current_user" // TODO: Get from auth
    
    /**
     * Clears all user data including:
     * - Workout history and progress
     * - Nutrition data and logged foods
     * - Activity history
     * - User progress data
     * - Weight entries and body measurements
     */
    suspend fun clearAllUserData() {
        try {
            // Clear history data from database
            historyDao.deleteAllActivitiesForUser(currentUserId)
            
            // Clear UserProgressManager data
            UserProgressManager.clearAllData()
            
            // Clear NutritionManager data
            NutritionManager.clearAllData()
            
            // Reset UserProfileManager to default values (keep basic profile)
            UserProfileManager.resetToDefaults()
            
        } catch (e: Exception) {
            throw Exception("Failed to clear user data: ${e.message}")
        }
    }
    
    /**
     * Clears only workout and progress data
     */
    suspend fun clearWorkoutData() {
        try {
            UserProgressManager.clearAllData()
        } catch (e: Exception) {
            throw Exception("Failed to clear workout data: ${e.message}")
        }
    }
    
    /**
     * Clears only nutrition data
     */
    suspend fun clearNutritionData() {
        try {
            NutritionManager.clearAllData()
        } catch (e: Exception) {
            throw Exception("Failed to clear nutrition data: ${e.message}")
        }
    }
    
    /**
     * Clears only history data
     */
    suspend fun clearHistoryData() {
        try {
            historyDao.deleteAllActivitiesForUser(currentUserId)
        } catch (e: Exception) {
            throw Exception("Failed to clear history data: ${e.message}")
        }
    }
}
