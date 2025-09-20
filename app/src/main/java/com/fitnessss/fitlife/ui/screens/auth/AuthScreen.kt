package com.fitnessss.fitlife.ui.screens.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.service.FirebaseAuthService
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    navController: NavController,
    onAuthSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Google Sign-In launcher
    val context = LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            println("DEBUG: AuthScreen - Google Sign-In result OK")
            viewModel.handleGoogleSignInResult(result.data)
        } else {
            println("DEBUG: AuthScreen - Google Sign-In result failed: ${result.resultCode}")
            scope.launch {
                snackbarHostState.showSnackbar("Google Sign-In cancelled. Trying anonymous login...")
            }
            // Automatically try anonymous login as fallback
            viewModel.signInAnonymously()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.authState.collect { authState ->
            println("DEBUG: AuthScreen - Auth state changed: $authState")
            println("DEBUG: AuthScreen - Auth state type: ${authState.javaClass.simpleName}")
            when (authState) {
                is AuthState.Success -> {
                    println("DEBUG: AuthScreen - Auth success, calling onAuthSuccess")
                    // Call the success callback
                    onAuthSuccess()
                    println("DEBUG: AuthScreen - onAuthSuccess callback completed")
                }
                is AuthState.Error -> {
                    println("DEBUG: AuthScreen - Auth error: ${authState.message}")
                    errorMessage = authState.message
                    scope.launch {
                        snackbarHostState.showSnackbar(errorMessage)
                    }
                }
                is AuthState.Loading -> {
                    println("DEBUG: AuthScreen - Auth loading")
                    isLoading = true
                }
                else -> {
                    println("DEBUG: AuthScreen - Auth idle")
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageHeaderWithThemeToggle(title = if (isSignUp) "Sign Up" else "Sign In")
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = "Fitness Icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = if (isSignUp) "Create Account" else "Welcome Back",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = if (isSignUp) "Join FitLife and start your fitness journey" else "Sign in to continue your progress",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Filled.Person, "Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        leadingIcon = { Icon(Icons.Filled.Email, "Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Filled.Lock, "Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            println("DEBUG: AuthScreen - Button clicked: ${if (isSignUp) "Sign Up" else "Sign In"}")
                            if (isSignUp) {
                                viewModel.signUp(email, password, name)
                            } else {
                                viewModel.signIn(email, password)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() && (!isSignUp || name.isNotEmpty()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(if (isSignUp) "Sign Up" else "Sign In")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = "OR",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                                                                                  // Google Sign In Button
                     OutlinedButton(
                         onClick = { 
                             println("DEBUG: AuthScreen - Google sign in button clicked")
                             // Launch Google Sign-In intent
                             val signInIntent = viewModel.getGoogleSignInIntent(context)
                             println("DEBUG: AuthScreen - Google sign in intent: $signInIntent")
                             if (signInIntent != null) {
                                 println("DEBUG: AuthScreen - Launching Google sign in intent")
                                 googleSignInLauncher.launch(signInIntent)
                             } else {
                                 println("DEBUG: AuthScreen - Google sign in intent is null")
                                 scope.launch {
                                     snackbarHostState.showSnackbar("Google Sign-In not available. Trying anonymous login...")
                                 }
                                 // Fallback to anonymous login
                                 viewModel.signInAnonymously()
                             }
                         },
                          modifier = Modifier.fillMaxWidth(),
                          enabled = !isLoading,
                          colors = ButtonDefaults.outlinedButtonColors(
                              contentColor = MaterialTheme.colorScheme.onSurface
                          )
                      ) {
                          Icon(
                              imageVector = Icons.Filled.AccountCircle,
                              contentDescription = "Google",
                              modifier = Modifier.size(20.dp)
                          )
                          Spacer(modifier = Modifier.width(8.dp))
                          Text("Continue with Google")
                      }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Anonymous Login Button
                    OutlinedButton(
                        onClick = { 
                            println("DEBUG: AuthScreen - Anonymous login button clicked")
                            viewModel.signInAnonymously() 
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Guest",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue as Guest (Local Only)")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { isSignUp = !isSignUp },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            if (isSignUp) "Already have an account? Sign In" else "Don't have an account? Sign Up"
                        )
                    }
                }
            }
        }
    }
}
