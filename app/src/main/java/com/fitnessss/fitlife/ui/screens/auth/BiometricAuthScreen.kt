package com.fitnessss.fitlife.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.service.BiometricAuthManager
import com.fitnessss.fitlife.data.service.BiometricResult
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricAuthScreen(
    navController: NavController,
    onAuthSuccess: () -> Unit,
    viewModel: BiometricAuthViewModel = hiltViewModel()
) {
    val biometricAuthManager = viewModel.biometricAuthManager
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    
    LaunchedEffect(Unit) {
        println("DEBUG: BiometricAuthScreen - LaunchedEffect triggered")
        // Automatically trigger biometric authentication when screen loads
        val shouldRequire = biometricAuthManager.shouldRequireBiometric()
        println("DEBUG: BiometricAuthScreen - shouldRequireBiometric: $shouldRequire")
        
        if (shouldRequire) {
            println("DEBUG: BiometricAuthScreen - Starting biometric authentication")
            viewModel.authenticateUser(context as androidx.fragment.app.FragmentActivity)
        } else {
            println("DEBUG: BiometricAuthScreen - Biometric not required, showing manual options")
        }
    }
    
    LaunchedEffect(authState) {
        println("DEBUG: BiometricAuthScreen - Auth state changed: $authState")
        when (authState) {
            is BiometricAuthState.Success -> {
                println("DEBUG: BiometricAuthScreen - Authentication successful, calling onAuthSuccess")
                onAuthSuccess()
            }
            is BiometricAuthState.Error -> {
                println("DEBUG: BiometricAuthScreen - Authentication error: ${(authState as BiometricAuthState.Error).message}")
                // Handle error - show message but don't navigate away
            }
            is BiometricAuthState.Loading -> {
                println("DEBUG: BiometricAuthScreen - Authentication loading")
            }
            is BiometricAuthState.Idle -> {
                println("DEBUG: BiometricAuthScreen - Authentication idle")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with theme toggle
        PageHeaderWithThemeToggle(
            title = "Unlock FitLife",
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Spacer(modifier = Modifier.weight(0.1f))
        
        // Main content area
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // App Icon
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = "FitLife",
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            Text(
                text = "Welcome Back!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Subtitle
            Text(
                text = "Use your biometric credentials to unlock the app",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Status Message
            when (authState) {
                is BiometricAuthState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Authenticating...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                is BiometricAuthState.Error -> {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = when ((authState as BiometricAuthState.Error).message) {
                            "Not available" -> "Biometric authentication is not available on this device"
                            "Not enrolled" -> "No biometric credentials are enrolled"
                            "Locked out" -> "Too many failed attempts. Try again later"
                            "Cancelled" -> "Authentication was cancelled"
                            else -> "Authentication failed. Please try again"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
                else -> {
                    if (biometricAuthManager.shouldRequireBiometric()) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Biometric",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Touch the sensor to unlock",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Info",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Biometric authentication is not available or disabled",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Retry Button or Try Biometric Button
                if (authState is BiometricAuthState.Error) {
                    Button(
                        onClick = { viewModel.authenticateUser(context as androidx.fragment.app.FragmentActivity) },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Retry",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Again")
                    }
                } else if (biometricAuthManager.shouldRequireBiometric() && authState !is BiometricAuthState.Loading) {
                    Button(
                        onClick = { viewModel.authenticateUser(context as androidx.fragment.app.FragmentActivity) },
                        modifier = Modifier.width(200.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "Try Biometric",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Biometric")
                    }
                }
                
                // Manual Unlock Button (for testing)
                OutlinedButton(
                    onClick = { onAuthSuccess() },
                    modifier = Modifier.width(200.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.LockOpen,
                        contentDescription = "Manual Unlock",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Manual Unlock")
                }
                
                // Disable Biometric Button
                TextButton(
                    onClick = { 
                        biometricAuthManager.setBiometricEnabled(false)
                        onAuthSuccess()
                    }
                ) {
                    Text("Disable Biometric Authentication")
                }
            }
            
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

@HiltViewModel
class BiometricAuthViewModel @Inject constructor(
    val biometricAuthManager: BiometricAuthManager
) : ViewModel() {
    
    private val _authState = MutableStateFlow<BiometricAuthState>(BiometricAuthState.Idle)
    val authState: StateFlow<BiometricAuthState> = _authState
    
    fun shouldRequireBiometric(): Boolean {
        return biometricAuthManager.shouldRequireBiometric()
    }
    
    fun authenticateUser(activity: androidx.fragment.app.FragmentActivity) {
        println("DEBUG: BiometricAuthViewModel - Starting authentication")
        viewModelScope.launch {
            println("DEBUG: BiometricAuthViewModel - Setting state to Loading")
            _authState.value = BiometricAuthState.Loading
            
            try {
                println("DEBUG: BiometricAuthViewModel - Calling biometricAuthManager.authenticateUser")
                val result = biometricAuthManager.authenticateUser(activity)
                println("DEBUG: BiometricAuthViewModel - Authentication result: $result")
                
                when (result) {
                    is BiometricResult.Success -> {
                        println("DEBUG: BiometricAuthViewModel - Authentication successful")
                        _authState.value = BiometricAuthState.Success
                    }
                    is BiometricResult.Failed -> {
                        println("DEBUG: BiometricAuthViewModel - Authentication failed")
                        _authState.value = BiometricAuthState.Error("Authentication failed")
                    }
                    is BiometricResult.Cancelled -> {
                        println("DEBUG: BiometricAuthViewModel - Authentication cancelled")
                        _authState.value = BiometricAuthState.Error("Cancelled")
                    }
                    is BiometricResult.NotAvailable -> {
                        println("DEBUG: BiometricAuthViewModel - Biometric not available")
                        _authState.value = BiometricAuthState.Error("Not available")
                    }
                    is BiometricResult.NotEnrolled -> {
                        println("DEBUG: BiometricAuthViewModel - Biometric not enrolled")
                        _authState.value = BiometricAuthState.Error("Not enrolled")
                    }
                    is BiometricResult.LockedOut -> {
                        println("DEBUG: BiometricAuthViewModel - Biometric locked out")
                        _authState.value = BiometricAuthState.Error("Locked out")
                    }
                    is BiometricResult.LockedOutPermanent -> {
                        println("DEBUG: BiometricAuthViewModel - Biometric permanently locked out")
                        _authState.value = BiometricAuthState.Error("Locked out permanent")
                    }
                    is BiometricResult.Error -> {
                        println("DEBUG: BiometricAuthViewModel - Biometric error: ${result.message}")
                        _authState.value = BiometricAuthState.Error(result.message)
                    }
                    is BiometricResult.Disabled -> {
                        println("DEBUG: BiometricAuthViewModel - Biometric disabled")
                        _authState.value = BiometricAuthState.Error("Disabled")
                    }
                }
            } catch (e: Exception) {
                println("DEBUG: BiometricAuthViewModel - Exception during authentication: ${e.message}")
                _authState.value = BiometricAuthState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class BiometricAuthState {
    object Idle : BiometricAuthState()
    object Loading : BiometricAuthState()
    object Success : BiometricAuthState()
    data class Error(val message: String) : BiometricAuthState()
}
