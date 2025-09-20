package com.fitnessss.fitlife.ui.screens.nutrition

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.service.HistoryLoggingService
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutritionViewModel @Inject constructor(
    private val historyLoggingService: HistoryLoggingService
) : ViewModel() {
    
    init {
        // Initialize the NutritionManager with the history logging service
        NutritionManager.initializeWithHistoryService(historyLoggingService)
    }
    
    // Add food with history logging
    fun addFoodWithHistory(loggedFood: LoggedFood) {
        NutritionManager.addFood(loggedFood)
    }
    
    // Add water with history logging
    fun addWaterWithHistory() {
        NutritionManager.addWaterGlass()
    }
    
    // Remove water with history logging
    fun removeWaterWithHistory() {
        NutritionManager.removeWaterGlass()
    }
    
    // Remove food
    fun removeFood(loggedFood: LoggedFood) {
        NutritionManager.removeFood(loggedFood)
    }
}
