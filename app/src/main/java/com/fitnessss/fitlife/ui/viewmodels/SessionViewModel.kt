package com.fitnessss.fitlife.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.fitnessss.fitlife.data.service.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    val sessionManager: SessionManager
) : ViewModel() {
    
    init {
        println("DEBUG: SessionViewModel - Initialized")
    }
    
    fun refreshSession() {
        println("DEBUG: SessionViewModel - Refreshing session")
        sessionManager.refreshSession()
    }
    
    fun logout() {
        println("DEBUG: SessionViewModel - Logging out user")
        sessionManager.logout()
    }
    
    fun isLoggedIn(): Boolean {
        val loggedIn = sessionManager.isLoggedIn()
        println("DEBUG: SessionViewModel - isLoggedIn(): $loggedIn")
        return loggedIn
    }
    
    fun setLoggedIn(userId: String? = null) {
        println("DEBUG: SessionViewModel - Setting user as logged in")
        sessionManager.setLoggedIn(userId)
    }
}
