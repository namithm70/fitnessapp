package com.fitnessss.fitlife.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.service.DataClearingService
import com.fitnessss.fitlife.data.service.FirebaseAuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val dataClearingService: DataClearingService,
    private val firebaseAuthService: FirebaseAuthService
) : ViewModel() {

    /**
     * Clears all user data including workouts, nutrition, history, and progress
     */
    fun clearAllUserData(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                dataClearingService.clearAllUserData()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to clear data")
            }
        }
    }

    /**
     * Clears only workout data
     */
    fun clearWorkoutData(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                dataClearingService.clearWorkoutData()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to clear workout data")
            }
        }
    }

    /**
     * Clears only nutrition data
     */
    fun clearNutritionData(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                dataClearingService.clearNutritionData()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to clear nutrition data")
            }
        }
    }

    /**
     * Clears only history data
     */
    fun clearHistoryData(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                dataClearingService.clearHistoryData()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to clear history data")
            }
        }
    }
    
    /**
     * Signs out the current user
     */
    fun signOut() {
        firebaseAuthService.signOut()
    }
}
