package com.fitnessss.fitlife.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.fitnessss.fitlife.data.service.AIService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val aiService: AIService
) : ViewModel() {
    
    fun getAIService(): AIService {
        return aiService
    }
}
