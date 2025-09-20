package com.fitnessss.fitlife.data.service

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("biometric_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    }
    
    /**
     * Check if biometric authentication is available on this device
     */
    fun isBiometricAvailable(): Boolean {
        println("DEBUG: BiometricAuthManager - Checking biometric availability")
        
        try {
            val biometricManager = BiometricManager.from(context)
            
            // Try different authenticator types for better MIUI compatibility
            val authenticators = listOf(
                BiometricManager.Authenticators.BIOMETRIC_WEAK,
                BiometricManager.Authenticators.BIOMETRIC_STRONG,
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            
            for (authenticator in authenticators) {
                try {
                    val canAuthenticate = biometricManager.canAuthenticate(authenticator)
                    println("DEBUG: BiometricAuthManager - Testing authenticator: $authenticator, result: $canAuthenticate")
                    
                    when (canAuthenticate) {
                        BiometricManager.BIOMETRIC_SUCCESS -> {
                            println("DEBUG: BiometricAuthManager - Biometric is available with authenticator: $authenticator")
                            return true
                        }
                        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                            println("DEBUG: BiometricAuthManager - No biometric hardware available")
                        }
                        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                            println("DEBUG: BiometricAuthManager - Biometric hardware is unavailable")
                        }
                        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                            println("DEBUG: BiometricAuthManager - No biometric credentials enrolled")
                        }
                        else -> {
                            println("DEBUG: BiometricAuthManager - Unknown biometric status: $canAuthenticate")
                        }
                    }
                } catch (e: Exception) {
                    println("DEBUG: BiometricAuthManager - Exception testing authenticator $authenticator: ${e.message}")
                }
            }
            
            println("DEBUG: BiometricAuthManager - No biometric authenticators available")
            return false
            
        } catch (e: Exception) {
            println("DEBUG: BiometricAuthManager - Exception checking biometric availability: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Check if biometric authentication is enabled by user
     */
    fun isBiometricEnabled(): Boolean {
        val enabled = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        println("DEBUG: BiometricAuthManager - isBiometricEnabled: $enabled")
        return enabled
    }
    
    /**
     * Enable or disable biometric authentication
     */
    fun setBiometricEnabled(enabled: Boolean) {
        println("DEBUG: BiometricAuthManager - Setting biometric enabled to: $enabled")
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        println("DEBUG: BiometricAuthManager - Biometric enabled setting saved")
    }
    
    /**
     * Get biometric authentication status message
     */
    fun getBiometricStatus(): String {
        println("DEBUG: BiometricAuthManager - Getting biometric status")
        
        if (!isBiometricEnabled()) {
            println("DEBUG: BiometricAuthManager - Biometric is disabled by user")
            return "Biometric authentication is disabled"
        }
        
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
        
        val status = when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                println("DEBUG: BiometricAuthManager - Status: Biometric authentication is available")
                "Biometric authentication is available"
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                println("DEBUG: BiometricAuthManager - Status: No biometric hardware available")
                "No biometric hardware available"
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                println("DEBUG: BiometricAuthManager - Status: Biometric hardware is unavailable")
                "Biometric hardware is unavailable"
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                println("DEBUG: BiometricAuthManager - Status: No biometric credentials enrolled")
                "No biometric credentials enrolled"
            }
            else -> {
                println("DEBUG: BiometricAuthManager - Status: Biometric authentication is not available")
                "Biometric authentication is not available"
            }
        }
        
        println("DEBUG: BiometricAuthManager - Final status: $status")
        return status
    }
    
    /**
     * Authenticate user with biometric credentials
     */
    suspend fun authenticateUser(activity: FragmentActivity): BiometricResult {
        println("DEBUG: BiometricAuthManager - Starting biometric authentication")
        
        return suspendCancellableCoroutine { continuation ->
            println("DEBUG: BiometricAuthManager - Inside suspendCancellableCoroutine")
            
            try {
                if (!isBiometricEnabled()) {
                    println("DEBUG: BiometricAuthManager - Biometric is disabled, returning Disabled")
                    continuation.resume(BiometricResult.Disabled)
                    return@suspendCancellableCoroutine
                }
                
                if (!isBiometricAvailable()) {
                    println("DEBUG: BiometricAuthManager - Biometric is not available, returning NotAvailable")
                    continuation.resume(BiometricResult.NotAvailable)
                    return@suspendCancellableCoroutine
                }
                
                println("DEBUG: BiometricAuthManager - Creating biometric prompt")
                
                // Try different prompt configurations for MIUI compatibility
                val promptInfo = try {
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock FitLife")
                        .setSubtitle("Use your biometric credentials to unlock the app")
                        .setNegativeButtonText("Cancel")
                        .build()
                } catch (e: Exception) {
                    println("DEBUG: BiometricAuthManager - Error creating prompt info: ${e.message}")
                    continuation.resume(BiometricResult.Error("Failed to create biometric prompt: ${e.message}"))
                    return@suspendCancellableCoroutine
                }
            
            println("DEBUG: BiometricAuthManager - Creating BiometricPrompt")
            
            val biometricPrompt = try {
                BiometricPrompt(activity, ContextCompat.getMainExecutor(context),
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            println("DEBUG: BiometricAuthManager - Authentication error: $errorCode - $errString")
                            
                            val result = when (errorCode) {
                                BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                                    println("DEBUG: BiometricAuthManager - Hardware not present")
                                    BiometricResult.NotAvailable
                                }
                                BiometricPrompt.ERROR_HW_UNAVAILABLE -> {
                                    println("DEBUG: BiometricAuthManager - Hardware unavailable")
                                    BiometricResult.NotAvailable
                                }
                                BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                                    println("DEBUG: BiometricAuthManager - No biometrics enrolled")
                                    BiometricResult.NotEnrolled
                                }
                                BiometricPrompt.ERROR_USER_CANCELED -> {
                                    println("DEBUG: BiometricAuthManager - User cancelled")
                                    BiometricResult.Cancelled
                                }
                                BiometricPrompt.ERROR_LOCKOUT -> {
                                    println("DEBUG: BiometricAuthManager - Locked out")
                                    BiometricResult.LockedOut
                                }
                                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                                    println("DEBUG: BiometricAuthManager - Permanently locked out")
                                    BiometricResult.LockedOutPermanent
                                }
                                else -> {
                                    println("DEBUG: BiometricAuthManager - Unknown error: $errString")
                                    BiometricResult.Error(errString.toString())
                                }
                            }
                            
                            println("DEBUG: BiometricAuthManager - Resuming with error result: $result")
                            continuation.resume(result)
                        }
                        
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            println("DEBUG: BiometricAuthManager - Authentication succeeded!")
                            continuation.resume(BiometricResult.Success)
                        }
                        
                        override fun onAuthenticationFailed() {
                            super.onAuthenticationFailed()
                            println("DEBUG: BiometricAuthManager - Authentication failed")
                            continuation.resume(BiometricResult.Failed)
                        }
                    })
            } catch (e: Exception) {
                println("DEBUG: BiometricAuthManager - Error creating BiometricPrompt: ${e.message}")
                e.printStackTrace()
                continuation.resume(BiometricResult.Error("Failed to create biometric prompt: ${e.message}"))
                return@suspendCancellableCoroutine
            }
            
            println("DEBUG: BiometricAuthManager - Starting biometric authentication prompt")
            try {
                biometricPrompt.authenticate(promptInfo)
            } catch (e: Exception) {
                println("DEBUG: BiometricAuthManager - Error starting authentication: ${e.message}")
                e.printStackTrace()
                continuation.resume(BiometricResult.Error("Failed to start biometric authentication: ${e.message}"))
            }
        } catch (e: Exception) {
            println("DEBUG: BiometricAuthManager - General error in authenticateUser: ${e.message}")
            e.printStackTrace()
            continuation.resume(BiometricResult.Error("Biometric authentication error: ${e.message}"))
        }
        }
    }
    
    /**
     * Check if app should require biometric authentication
     */
    fun shouldRequireBiometric(): Boolean {
        val enabled = isBiometricEnabled()
        val available = isBiometricAvailable()
        val shouldRequire = enabled && available
        println("DEBUG: BiometricAuthManager - shouldRequireBiometric: enabled=$enabled, available=$available, shouldRequire=$shouldRequire")
        return shouldRequire
    }
}

sealed class BiometricResult {
    object Success : BiometricResult()
    object Failed : BiometricResult()
    object Cancelled : BiometricResult()
    object Disabled : BiometricResult()
    object NotAvailable : BiometricResult()
    object NotEnrolled : BiometricResult()
    object LockedOut : BiometricResult()
    object LockedOutPermanent : BiometricResult()
    data class Error(val message: String) : BiometricResult()
}
