package com.fitnessss.fitlife.data.service

import com.fitnessss.fitlife.data.UserProgressManager
import com.fitnessss.fitlife.data.UserProfileManager
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSyncManager @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firebaseAuthService: FirebaseAuthService
) {
    
    private val syncScope = CoroutineScope(Dispatchers.IO)
    private var isInitialized = false
    
    fun initialize() {
        if (isInitialized) return
        
        syncScope.launch {
            // Start listening to Firebase data changes
            startFirebaseListeners()
            
            // Load initial data from Firebase
            firebaseService.loadAllData()
            
            isInitialized = true
            println("DataSyncManager initialized successfully")
        }
    }
    
    private suspend fun startFirebaseListeners() {
        // Listen to workout sessions changes
        firebaseService.getWorkoutSessionsFlow().collectLatest { sessions ->
            UserProgressManager.workoutSessions.value = sessions
            println("Synced ${sessions.size} workout sessions from Firebase")
        }
        
        // Listen to weight entries changes
        firebaseService.getWeightEntriesFlow().collectLatest { entries ->
            UserProgressManager.weightEntries.value = entries
            println("Synced ${entries.size} weight entries from Firebase")
        }
        
        // Listen to body measurements changes
        firebaseService.getBodyMeasurementsFlow().collectLatest { measurements ->
            UserProgressManager.bodyMeasurements.value = measurements
            println("Synced ${measurements.size} body measurements from Firebase")
        }
        
        // Listen to activity history changes
        firebaseService.getActivityHistoryFlow().collectLatest { activities ->
            // Note: Activity history is managed by the local database
            // We'll sync it periodically instead of real-time
            println("Received ${activities.size} activities from Firebase")
        }
    }
    
    fun syncUserProfile() {
        syncScope.launch {
            UserProfileManager.userProfile?.let { profile ->
                firebaseService.syncUserProfile(profile)
                println("Synced user profile to Firebase")
            }
        }
    }
    
    fun syncWorkoutSessions() {
        syncScope.launch {
            firebaseService.syncWorkoutSessions(UserProgressManager.workoutSessions.value)
            println("Synced ${UserProgressManager.workoutSessions.value.size} workout sessions to Firebase")
        }
    }
    
    fun syncWeightEntries() {
        syncScope.launch {
            firebaseService.syncWeightEntries(UserProgressManager.weightEntries.value)
            println("Synced ${UserProgressManager.weightEntries.value.size} weight entries to Firebase")
        }
    }
    
    fun syncBodyMeasurements() {
        syncScope.launch {
            firebaseService.syncBodyMeasurements(UserProgressManager.bodyMeasurements.value)
            println("Synced ${UserProgressManager.bodyMeasurements.value.size} body measurements to Firebase")
        }
    }
    
    fun syncNutritionData() {
        syncScope.launch {
            firebaseService.syncNutritionData(NutritionManager.loggedFoods, NutritionManager.waterGlasses)
            println("Synced nutrition data to Firebase")
        }
    }
    
    fun syncAllData() {
        syncScope.launch {
            firebaseService.syncAllData()
        }
    }
    
    fun syncOnUserAction() {
        // Sync data when user performs actions
        syncUserProfile()
        syncWorkoutSessions()
        syncWeightEntries()
        syncBodyMeasurements()
        syncNutritionData()
    }
    
    fun isUserLoggedIn(): Boolean {
        return firebaseAuthService.isUserLoggedIn
    }
    
    fun getCurrentUserId(): String? {
        return firebaseAuthService.currentUser?.uid
    }
}
