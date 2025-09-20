package com.fitnessss.fitlife.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class ThemeManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    
    var isDarkMode by mutableStateOf(loadThemePreference())
        private set
    
    private fun loadThemePreference(): Boolean {
        // Check if this is the first launch (no preference saved yet)
        val isFirstLaunch = !prefs.contains("is_dark_mode")
        
        if (isFirstLaunch) {
            // On first launch, default to dark mode and save it
            prefs.edit().putBoolean("is_dark_mode", true).apply()
            return true
        }
        
        val savedTheme = prefs.getBoolean("is_dark_mode", true)
        return savedTheme
    }

    fun toggleTheme() {
        isDarkMode = !isDarkMode
        saveThemePreference()
    }

    fun setDarkTheme(enabled: Boolean) {
        isDarkMode = enabled
        saveThemePreference()
    }
    
    private fun saveThemePreference() {
        prefs.edit().putBoolean("is_dark_mode", isDarkMode).apply()
    }
}

val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("ThemeManager not provided")
}
