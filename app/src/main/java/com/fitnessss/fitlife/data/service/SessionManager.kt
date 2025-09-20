package com.fitnessss.fitlife.data.service

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_LAST_LOGIN_TIME = "last_login_time"
        private const val KEY_SESSION_DURATION = "session_duration"
    }
    
    /**
     * Check if user is currently logged in
     */
    fun isLoggedIn(): Boolean {
        val loggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIME, 0L)
        val sessionDuration = prefs.getLong(KEY_SESSION_DURATION, 24 * 60 * 60 * 1000L) // Default 24 hours
        
        val currentTime = System.currentTimeMillis()
        val sessionExpired = (currentTime - lastLoginTime) > sessionDuration
        
        println("DEBUG: SessionManager - isLoggedIn check:")
        println("DEBUG: SessionManager - loggedIn from prefs: $loggedIn")
        println("DEBUG: SessionManager - lastLoginTime: $lastLoginTime")
        println("DEBUG: SessionManager - currentTime: $currentTime")
        println("DEBUG: SessionManager - sessionDuration: $sessionDuration")
        println("DEBUG: SessionManager - sessionExpired: $sessionExpired")
        
        if (loggedIn && sessionExpired) {
            println("DEBUG: SessionManager - Session expired, logging out")
            logout()
            return false
        }
        
        println("DEBUG: SessionManager - Final isLoggedIn result: $loggedIn")
        return loggedIn
    }
    
    /**
     * Set user as logged in
     */
    fun setLoggedIn(userId: String? = null) {
        println("DEBUG: SessionManager - Setting user as logged in")
        println("DEBUG: SessionManager - userId: $userId")
        
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_LAST_LOGIN_TIME, System.currentTimeMillis())
            .apply()
        
        if (userId != null) {
            prefs.edit().putString(KEY_USER_ID, userId).apply()
            println("DEBUG: SessionManager - User ID saved: $userId")
        }
        
        println("DEBUG: SessionManager - Login state saved successfully")
    }
    
    /**
     * Log out user
     */
    fun logout() {
        println("DEBUG: SessionManager - Logging out user")
        
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_LAST_LOGIN_TIME)
            .apply()
        
        println("DEBUG: SessionManager - Logout completed")
    }
    
    /**
     * Get current user ID
     */
    fun getUserId(): String? {
        val userId = prefs.getString(KEY_USER_ID, null)
        println("DEBUG: SessionManager - Getting user ID: $userId")
        return userId
    }
    
    /**
     * Get last login time
     */
    fun getLastLoginTime(): Long {
        val lastLoginTime = prefs.getLong(KEY_LAST_LOGIN_TIME, 0L)
        println("DEBUG: SessionManager - Getting last login time: $lastLoginTime")
        return lastLoginTime
    }
    
    /**
     * Set session duration in milliseconds
     */
    fun setSessionDuration(durationMs: Long) {
        println("DEBUG: SessionManager - Setting session duration: $durationMs ms")
        prefs.edit().putLong(KEY_SESSION_DURATION, durationMs).apply()
    }
    
    /**
     * Get session duration in milliseconds
     */
    fun getSessionDuration(): Long {
        val duration = prefs.getLong(KEY_SESSION_DURATION, 24 * 60 * 60 * 1000L)
        println("DEBUG: SessionManager - Getting session duration: $duration ms")
        return duration
    }
    
    /**
     * Check if session is about to expire (within 1 hour)
     */
    fun isSessionExpiringSoon(): Boolean {
        val lastLoginTime = getLastLoginTime()
        val sessionDuration = getSessionDuration()
        val currentTime = System.currentTimeMillis()
        val timeUntilExpiry = sessionDuration - (currentTime - lastLoginTime)
        val oneHour = 60 * 60 * 1000L
        
        val expiringSoon = timeUntilExpiry <= oneHour && timeUntilExpiry > 0
        
        println("DEBUG: SessionManager - Checking if session expiring soon:")
        println("DEBUG: SessionManager - timeUntilExpiry: $timeUntilExpiry ms")
        println("DEBUG: SessionManager - oneHour: $oneHour ms")
        println("DEBUG: SessionManager - expiringSoon: $expiringSoon")
        
        return expiringSoon
    }
    
    /**
     * Refresh session (extend login time)
     */
    fun refreshSession() {
        println("DEBUG: SessionManager - Refreshing session")
        val currentTime = System.currentTimeMillis()
        prefs.edit().putLong(KEY_LAST_LOGIN_TIME, currentTime).apply()
        println("DEBUG: SessionManager - Session refreshed, new login time: $currentTime")
    }
}
