package com.fitnessss.fitlife.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.service.FirebaseAuthService
import com.fitnessss.fitlife.data.service.DataSyncManager
import com.fitnessss.fitlife.data.UserProfileManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuthService: FirebaseAuthService,
    private val dataSyncManager: DataSyncManager,
    private val sessionManager: com.fitnessss.fitlife.data.service.SessionManager
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState
    
    fun signUp(email: String, password: String, name: String) {
        viewModelScope.launch {
            println("DEBUG: AuthViewModel - Starting sign up process")
            _authState.value = AuthState.Loading
            
            val result = firebaseAuthService.signUpWithEmail(email, password, name)
            
            if (result.isSuccess) {
                println("DEBUG: AuthViewModel - Sign up successful")
                // Save session
                val userId = firebaseAuthService.getCurrentUserId()
                println("DEBUG: AuthViewModel - Saving session for user: $userId")
                sessionManager.setLoggedIn(userId)
                // Initialize data sync after successful signup
                dataSyncManager.initialize()
                _authState.value = AuthState.Success
            } else {
                println("DEBUG: AuthViewModel - Sign up failed: ${result.exceptionOrNull()?.message}")
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Sign up failed"
                )
            }
        }
    }
    
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            println("DEBUG: AuthViewModel - Starting sign in process")
            _authState.value = AuthState.Loading
            
            val result = firebaseAuthService.signInWithEmail(email, password)
            
            if (result.isSuccess) {
                println("DEBUG: AuthViewModel - Sign in successful")
                // Save session
                val userId = firebaseAuthService.getCurrentUserId()
                println("DEBUG: AuthViewModel - Saving session for user: $userId")
                sessionManager.setLoggedIn(userId)
                // Initialize data sync after successful signin
                dataSyncManager.initialize()
                _authState.value = AuthState.Success
            } else {
                println("DEBUG: AuthViewModel - Sign in failed: ${result.exceptionOrNull()?.message}")
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Sign in failed"
                )
            }
        }
    }
    
    fun signInAnonymously() {
        viewModelScope.launch {
            println("DEBUG: AuthViewModel - Starting anonymous sign in process")
            _authState.value = AuthState.Loading
            
            val result = firebaseAuthService.signInAnonymously()
            
            if (result.isSuccess) {
                println("DEBUG: AuthViewModel - Anonymous sign in successful")
                // Save session
                val userId = firebaseAuthService.getCurrentUserId()
                println("DEBUG: AuthViewModel - Saving session for user: $userId")
                sessionManager.setLoggedIn(userId)
                // Initialize data sync after successful anonymous signin
                dataSyncManager.initialize()
                _authState.value = AuthState.Success
            } else {
                println("DEBUG: AuthViewModel - Anonymous sign in failed: ${result.exceptionOrNull()?.message}")
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Anonymous sign in failed"
                )
            }
        }
    }
    
    fun signInWithGoogle() {
        viewModelScope.launch {
            println("DEBUG: AuthViewModel - Starting Google sign in process")
            _authState.value = AuthState.Loading
            
            // For now, use anonymous sign in as fallback
            // The actual Google Sign-In will be implemented with ActivityResultLauncher
            val result = firebaseAuthService.signInAnonymously()
            
            if (result.isSuccess) {
                println("DEBUG: AuthViewModel - Google sign in successful")
                // Save session
                val userId = firebaseAuthService.getCurrentUserId()
                println("DEBUG: AuthViewModel - Saving session for user: $userId")
                sessionManager.setLoggedIn(userId)
                // Initialize data sync after successful Google signin
                dataSyncManager.initialize()
                _authState.value = AuthState.Success
            } else {
                println("DEBUG: AuthViewModel - Google sign in failed: ${result.exceptionOrNull()?.message}")
                _authState.value = AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Google sign in failed"
                )
            }
        }
    }
    
    fun handleGoogleSignInResult(data: android.content.Intent?) {
        viewModelScope.launch {
            println("DEBUG: AuthViewModel - Handling Google sign in result")
            _authState.value = AuthState.Loading
            
            val result = firebaseAuthService.handleGoogleSignInResult(data)
            
            if (result.isSuccess) {
                println("DEBUG: AuthViewModel - Google sign in result successful")
                
                // Save session
                val userId = firebaseAuthService.getCurrentUserId()
                println("DEBUG: AuthViewModel - Saving session for user: $userId")
                sessionManager.setLoggedIn(userId)
                
                // Sync user profile with Firebase user data
                result.getOrNull()?.let { firebaseUser ->
                    println("DEBUG: AuthViewModel - Syncing user profile with Firebase user: ${firebaseUser.displayName}")
                    UserProfileManager.syncWithFirebaseUser(firebaseUser)
                    println("DEBUG: AuthViewModel - User profile synced successfully")
                }
                
                // Initialize data sync after successful Google signin
                dataSyncManager.initialize()
                println("DEBUG: AuthViewModel - Data sync manager initialized")
                
                // Sync user profile to Firebase
                dataSyncManager.syncUserProfile()
                println("DEBUG: AuthViewModel - User profile synced to Firebase")
                
                _authState.value = AuthState.Success
                println("DEBUG: AuthViewModel - Auth state set to Success")
            } else {
                println("DEBUG: AuthViewModel - Google sign in result failed: ${result.exceptionOrNull()?.message}")
                println("DEBUG: AuthViewModel - Falling back to anonymous login")
                
                // Fallback to anonymous login
                val anonymousResult = firebaseAuthService.signInAnonymously()
                if (anonymousResult.isSuccess) {
                    println("DEBUG: AuthViewModel - Anonymous login successful as fallback")
                    // Save session
                    val userId = firebaseAuthService.getCurrentUserId()
                    println("DEBUG: AuthViewModel - Saving session for user: $userId")
                    sessionManager.setLoggedIn(userId)
                    dataSyncManager.initialize()
                    _authState.value = AuthState.Success
                } else {
                    println("DEBUG: AuthViewModel - Anonymous login also failed: ${anonymousResult.exceptionOrNull()?.message}")
                    _authState.value = AuthState.Error(
                        "Google sign in failed. Please try anonymous login instead."
                    )
                }
            }
        }
    }
    
    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
    
    fun getGoogleSignInIntent(context: Context): android.content.Intent? {
        return firebaseAuthService.getGoogleSignInIntent(context)
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
