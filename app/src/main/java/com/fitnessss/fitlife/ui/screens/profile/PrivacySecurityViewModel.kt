package com.fitnessss.fitlife.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.service.BiometricAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacySecurityViewModel @Inject constructor(
    val biometricAuthManager: BiometricAuthManager
) : ViewModel() {
    
    private val _biometricEnabled = MutableStateFlow(biometricAuthManager.isBiometricEnabled())
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled
    
    private val _biometricStatus = MutableStateFlow(biometricAuthManager.getBiometricStatus())
    val biometricStatus: StateFlow<String> = _biometricStatus
    
    fun setBiometricEnabled(enabled: Boolean) {
        println("DEBUG: PrivacySecurityViewModel - Setting biometric enabled to: $enabled")
        biometricAuthManager.setBiometricEnabled(enabled)
        _biometricEnabled.value = enabled
        val status = biometricAuthManager.getBiometricStatus()
        println("DEBUG: PrivacySecurityViewModel - Updated biometric status: $status")
        _biometricStatus.value = status
    }
    
    fun refreshBiometricStatus() {
        val status = biometricAuthManager.getBiometricStatus()
        println("DEBUG: PrivacySecurityViewModel - Refreshing biometric status: $status")
        _biometricStatus.value = status
    }
}
