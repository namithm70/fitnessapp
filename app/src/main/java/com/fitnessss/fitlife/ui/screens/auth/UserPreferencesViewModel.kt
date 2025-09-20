package com.fitnessss.fitlife.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.FitnessGoal
import com.fitnessss.fitlife.data.FitnessLevel
import com.fitnessss.fitlife.data.Gender
import com.fitnessss.fitlife.data.UserProfileManager
import com.fitnessss.fitlife.data.service.DataSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserPreferencesViewModel @Inject constructor(
    private val dataSyncManager: DataSyncManager
) : ViewModel() {
    
    init {
        println("DEBUG: UserPreferencesViewModel - Initialized")
    }
    
    private val _preferencesState = MutableStateFlow<PreferencesState>(PreferencesState.Idle)
    val preferencesState: StateFlow<PreferencesState> = _preferencesState
    
    fun savePreferences(
        fitnessGoal: FitnessGoal,
        fitnessLevel: FitnessLevel,
        workoutsPerWeek: Int,
        gender: Gender,
        age: Int,
        height: Double,
        weight: Double
    ) {
        viewModelScope.launch {
            println("DEBUG: UserPreferencesViewModel - Saving user preferences")
            _preferencesState.value = PreferencesState.Loading
            
            try {
                // Update the user profile with the preferences
                UserProfileManager.updateProfile(
                    fitnessGoal = fitnessGoal,
                    fitnessLevel = fitnessLevel,
                    gender = gender,
                    age = age,
                    height = height,
                    weight = weight
                )
                
                // Also update workouts per week (this might need to be stored separately)
                // For now, we'll store it in the user profile notes field
                UserProfileManager.updateProfile(
                    notes = "Workouts per week: $workoutsPerWeek"
                )
                
                println("DEBUG: UserPreferencesViewModel - User preferences saved successfully")
                
                // Sync the updated profile to Firebase
                dataSyncManager.syncUserProfile()
                println("DEBUG: UserPreferencesViewModel - Profile synced to Firebase")
                
                _preferencesState.value = PreferencesState.Success
            } catch (e: Exception) {
                println("DEBUG: UserPreferencesViewModel - Error saving preferences: ${e.message}")
                _preferencesState.value = PreferencesState.Error(
                    e.message ?: "Failed to save preferences"
                )
            }
        }
    }
}

sealed class PreferencesState {
    object Idle : PreferencesState()
    object Loading : PreferencesState()
    object Success : PreferencesState()
    data class Error(val message: String) : PreferencesState()
}
