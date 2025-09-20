package com.fitnessss.fitlife

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.fitnessss.fitlife.navigation.FitLifeNavGraph
import com.fitnessss.fitlife.navigation.Screen
import com.fitnessss.fitlife.ui.screens.auth.AuthScreen
import com.fitnessss.fitlife.ui.screens.auth.UserPreferencesScreen
import com.fitnessss.fitlife.ui.screens.auth.BiometricAuthScreen
import com.fitnessss.fitlife.ui.screens.auth.BiometricAuthViewModel
import com.fitnessss.fitlife.data.service.FirebaseAuthService
import com.fitnessss.fitlife.data.service.BiometricAuthManager
import com.fitnessss.fitlife.data.service.AIService
import com.fitnessss.fitlife.ui.components.AIChatBubble
import com.fitnessss.fitlife.ui.viewmodels.AIViewModel
import com.fitnessss.fitlife.ui.screens.chat.ChatListScreen
import com.fitnessss.fitlife.ui.screens.chat.ChatDetailScreen
import com.fitnessss.fitlife.ui.screens.call.OutgoingCallScreen
import com.fitnessss.fitlife.ui.screens.call.IncomingCallScreen
import com.fitnessss.fitlife.ui.screens.call.ActiveCallScreen
import com.fitnessss.fitlife.data.service.CallListenerService
import com.fitnessss.fitlife.data.service.CallNotificationService
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitnessss.fitlife.ui.screens.dashboard.DashboardScreen
import com.fitnessss.fitlife.ui.screens.workout.WorkoutListScreen
import com.fitnessss.fitlife.ui.screens.workout.WorkoutSessionScreen
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionScreen
import com.fitnessss.fitlife.ui.screens.nutrition.FoodSearchScreen
import com.fitnessss.fitlife.ui.screens.nutrition.MealPlanScreen
import com.fitnessss.fitlife.ui.screens.progress.ProgressScreen
import com.fitnessss.fitlife.ui.screens.history.HistoryScreen
import com.fitnessss.fitlife.ui.screens.history.HistoryDetailScreen

import com.fitnessss.fitlife.ui.screens.profile.ProfileScreen
import com.fitnessss.fitlife.ui.screens.profile.EditProfileScreen
import com.fitnessss.fitlife.ui.screens.profile.PreferencesScreen
import com.fitnessss.fitlife.ui.screens.profile.NotificationsScreen
import com.fitnessss.fitlife.ui.screens.profile.PrivacySecurityScreen
import com.fitnessss.fitlife.ui.screens.profile.DataStorageScreen
import com.fitnessss.fitlife.ui.screens.workout.AIWorkoutGeneratorScreen
import com.fitnessss.fitlife.ui.screens.workout.AIWorkoutSessionScreen
import com.fitnessss.fitlife.ui.theme.FitLifeTheme
import com.fitnessss.fitlife.ui.viewmodels.SessionViewModel
import com.fitnessss.fitlife.ui.theme.ThemeManager
import com.fitnessss.fitlife.ui.theme.LocalThemeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    companion object {
        // Holds a pending incoming call to navigate to when composables are ready
        var pendingIncomingCall: Pair<String, String>? = null // (sessionId, callerName)
    }
    
    // Permission request launcher for microphone access
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            println("DEBUG: MainActivity - Microphone permission granted")
        } else {
            println("DEBUG: MainActivity - Microphone permission denied")
        }
    }
    
    private fun checkAndRequestMicrophonePermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                println("DEBUG: MainActivity - Microphone permission already granted")
            }
            else -> {
                println("DEBUG: MainActivity - Requesting microphone permission")
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun handlePermissionRequestIntent(intent: Intent) {
        if (intent.action == "REQUEST_MICROPHONE_PERMISSION") {
            println("DEBUG: MainActivity - Received microphone permission request from ${intent.getStringExtra("source")}")
            checkAndRequestMicrophonePermission()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize UserProfileManager with context for persistent storage
        com.fitnessss.fitlife.data.UserProfileManager.initialize(this)
        // Initialize UserProgressManager with context for persistent storage
        com.fitnessss.fitlife.data.UserProgressManager.initialize(this)
        
        // Request microphone permission for voice calls
        checkAndRequestMicrophonePermission()
        
        // Handle incoming call intents to navigate correctly when launched from full-screen notification
        handleIncomingCallIntent(intent)
        
        // Handle permission request intents from WebRTC service
        handlePermissionRequestIntent(intent)

        setContent {
            val themeManager = remember { ThemeManager(this@MainActivity) }
            
            CompositionLocalProvider(LocalThemeManager provides themeManager) {
                // Observe theme changes and update the UI accordingly
                val currentTheme by remember { derivedStateOf { themeManager.isDarkMode } }
                
                FitLifeTheme(darkTheme = currentTheme) { // Use current theme state
                    FitLifeApp()
                }
            }
        }
    }


    private fun handleIncomingCallIntent(intent: android.content.Intent) {
        when (intent.action) {
            CallNotificationService.ACTION_SHOW_INCOMING_CALL -> {
                val callSessionId = intent.getStringExtra(CallNotificationService.EXTRA_CALL_SESSION_ID) ?: return
                val callerName = intent.getStringExtra(CallNotificationService.EXTRA_CALLER_NAME) ?: ""
                // Save for navigation once NavController is available
                pendingIncomingCall = callSessionId to callerName
                println("DEBUG: MainActivity - Incoming call intent received for session=$callSessionId from $callerName (pending navigation)")
            }
            CallNotificationService.ACTION_ANSWER_CALL -> {
                // For simplicity, main UI IncomingCallScreen handles answer; here we just bring app to foreground
            }
            CallNotificationService.ACTION_DECLINE_CALL -> {
                // Bring app to foreground if needed; decline is handled inside IncomingCallScreen via repository
            }
        }
    }
    
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { 
            handleIncomingCallIntent(it)
            handlePermissionRequestIntent(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun FitLifeApp() {
    val navController = rememberNavController()
    val isLoggedIn = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(true) }
    val sessionViewModel = androidx.hilt.navigation.compose.hiltViewModel<SessionViewModel>()
    val biometricAuthManager = androidx.hilt.navigation.compose.hiltViewModel<BiometricAuthViewModel>()
    
    // Check authentication state on app start
    LaunchedEffect(Unit) {
        println("DEBUG: MainActivity - App starting, checking session state")
        val loggedIn = sessionViewModel.isLoggedIn()
        println("DEBUG: MainActivity - SessionViewModel.isLoggedIn(): $loggedIn")
        isLoggedIn.value = loggedIn
        
        if (loggedIn) {
            println("DEBUG: MainActivity - User is logged in, checking biometric requirement")
            // Check if biometric authentication is required
            val shouldRequireBiometric = biometricAuthManager.shouldRequireBiometric()
            println("DEBUG: MainActivity - Should require biometric: $shouldRequireBiometric")
            
            if (shouldRequireBiometric) {
                println("DEBUG: MainActivity - Biometric required, navigating to biometric auth")
                navController.navigate(Screen.BiometricAuth.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            } else {
                println("DEBUG: MainActivity - Biometric not required, navigating to main")
                navController.navigate(Screen.Main.route) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
        } else {
            println("DEBUG: MainActivity - User is not logged in, navigating to auth")
            navController.navigate(Screen.Auth.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
        
        // Set loading to false after navigation is determined
        isLoading.value = false
    }
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // Splash screen
        composable(Screen.Splash.route) {
            SplashScreen()
        }
        // Auth flow
        composable(Screen.Auth.route) {
            AuthScreen(
                navController = navController,
                onAuthSuccess = {
                    println("DEBUG: MainActivity - Auth success, navigating to user preferences")
                    println("DEBUG: MainActivity - Current route: ${navController.currentDestination?.route}")
                    println("DEBUG: MainActivity - Setting isLoggedIn to true")
                    isLoggedIn.value = true
                    println("DEBUG: MainActivity - isLoggedIn is now: ${isLoggedIn.value}")
                    println("DEBUG: MainActivity - Navigating to: ${Screen.UserPreferences.route}")
                    navController.navigate(Screen.UserPreferences.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                    println("DEBUG: MainActivity - Navigation command sent")
                }
            )
        }
        
        // User Preferences flow
                        composable(Screen.UserPreferences.route) {
                    UserPreferencesScreen(
                        navController = navController,
                        onPreferencesComplete = {
                            println("DEBUG: MainActivity - Preferences complete, navigating to main")
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.UserPreferences.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.BiometricAuth.route) {
                    BiometricAuthScreen(
                        navController = navController,
                        onAuthSuccess = {
                            println("DEBUG: MainActivity - Biometric auth success, navigating to main")
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.BiometricAuth.route) { inclusive = true }
                            }
                        }
                    )
                }
        
        // Main app with bottom navigation
        composable(Screen.Main.route + "?startTab={startTab}") { backStackEntry ->
            val startTab = backStackEntry.arguments?.getString("startTab")
            println("DEBUG: MainActivity - MainAppWithBottomNavigation composable called with startTab=$startTab")
            MainAppWithBottomNavigation(
                navController = navController,
                onSignOut = {
                    println("DEBUG: MainActivity - Sign out requested")
                    sessionViewModel.logout()
                    isLoggedIn.value = false
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                    println("DEBUG: MainActivity - Sign out completed")
                },
                startTab = startTab
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MainAppWithBottomNavigation(
    navController: androidx.navigation.NavHostController,
    onSignOut: () -> Unit = {},
    startTab: String? = null
) {
    println("DEBUG: MainAppWithBottomNavigation - Function called")
    val themeManager = LocalThemeManager.current
    val sessionViewModel = androidx.hilt.navigation.compose.hiltViewModel<SessionViewModel>()
    val aiViewModel = androidx.hilt.navigation.compose.hiltViewModel<AIViewModel>()
    val context = LocalContext.current
    
    // Create a separate nav controller for the main app navigation
    val mainNavController = rememberNavController()
    println("DEBUG: MainAppWithBottomNavigation - Main nav controller created")
    
    // Refresh session periodically to keep it alive
    LaunchedEffect(Unit) {
        println("DEBUG: MainAppWithBottomNavigation - Starting session refresh")
        while (true) {
            kotlinx.coroutines.delay(5 * 60 * 1000L) // Refresh every 5 minutes
            println("DEBUG: MainAppWithBottomNavigation - Refreshing session")
            sessionViewModel.refreshSession()
        }
    }
    
    val items = listOf(
        BottomNavItem(
            route = Screen.Dashboard.route,
            title = "Dashboard",
            icon = Icons.Filled.Home
        ),
        BottomNavItem(
            route = Screen.ChatList.route,
            title = "Chat",
            icon = Icons.Filled.Chat
        ),
        BottomNavItem(
            route = Screen.WorkoutList.route,
            title = "Workouts",
            icon = Icons.Filled.FitnessCenter
        ),
        BottomNavItem(
            route = Screen.Nutrition.route,
            title = "Nutrition",
            icon = Icons.Filled.Restaurant
        ),
        BottomNavItem(
            route = Screen.Profile.route,
            title = "Profile",
            icon = Icons.Filled.Person
        )
    )


    
    LaunchedEffect(startTab) {
        when (startTab) {
            "chat" -> mainNavController.navigate(Screen.ChatList.route)
        }
    }

    // Ensure call listener service is running for incoming calls
    LaunchedEffect(Unit) {
        try {
            val intent = android.content.Intent(context, CallListenerService::class.java)
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                CleanBottomNavigationBar(
                    navController = mainNavController,
                    items = items
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = mainNavController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(300)) },
                exitTransition = { fadeOut(animationSpec = tween(300)) }
            ) {
                composable(Screen.Dashboard.route) {
                    DashboardScreen(navController = mainNavController)
                }
                composable(Screen.ChatList.route) {
                    ChatListScreen(navController = mainNavController)
                }
                composable(Screen.WorkoutList.route) {
                    WorkoutListScreen(navController = mainNavController)
                }
                composable(Screen.AIWorkoutGenerator.route) {
                    AIWorkoutGeneratorScreen(navController = mainNavController)
                }
                composable(
                    route = Screen.AIWorkoutSession.route + "?workoutName={workoutName}&workoutType={workoutType}&duration={duration}&calories={calories}&exercises={exercises}"
                ) { backStackEntry ->
                    val workoutName = backStackEntry.arguments?.getString("workoutName") ?: ""
                    val workoutType = backStackEntry.arguments?.getString("workoutType") ?: ""
                    val duration = backStackEntry.arguments?.getString("duration") ?: "0"
                    val calories = backStackEntry.arguments?.getString("calories") ?: "0"
                    val exercises = backStackEntry.arguments?.getString("exercises") ?: ""
                    AIWorkoutSessionScreen(
                        navController = mainNavController,
                        workoutName = workoutName,
                        workoutType = workoutType,
                        duration = duration.toIntOrNull() ?: 0,
                        calories = calories.toIntOrNull() ?: 0,
                        exercises = exercises
                    )
                }
                composable(
                    route = Screen.WorkoutSession.route + "?workoutId={workoutId}"
                ) { backStackEntry ->
                    val workoutId = backStackEntry.arguments?.getString("workoutId") ?: "default"
                    WorkoutSessionScreen(
                        navController = mainNavController,
                        workoutId = workoutId
                    )
                }
                composable(Screen.Nutrition.route) {
                    NutritionScreen(navController = mainNavController)
                }
                composable(
                    route = Screen.FoodSearch.route + "?mealType={mealType}"
                ) { backStackEntry ->
                    val mealType = backStackEntry.arguments?.getString("mealType")
                    FoodSearchScreen(
                        navController = mainNavController,
                        mealType = mealType
                    )
                }
                composable(Screen.FoodSearch.route) {
                    FoodSearchScreen(navController = mainNavController)
                }
                composable(Screen.MealPlan.route) {
                    MealPlanScreen(navController = mainNavController)
                }
                composable(Screen.Progress.route) {
                    ProgressScreen(navController = mainNavController)
                }
                composable(Screen.History.route) {
                    HistoryScreen(navController = mainNavController)
                }
                composable(
                    route = Screen.HistoryDetail.route + "/{activityId}"
                ) { backStackEntry ->
                    val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
                    HistoryDetailScreen(
                        navController = mainNavController,
                        activityId = activityId
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        navController = mainNavController,
                        onSignOut = onSignOut
                    )
                }
                composable(
                    route = Screen.ChatDetail.route
                ) { backStackEntry ->
                    val chatRoomId = backStackEntry.arguments?.getString("chatRoomId") ?: ""
                    ChatDetailScreen(navController = mainNavController, chatRoomId = chatRoomId)
                }
                
                // Call screen routes
                composable(
                    route = Screen.OutgoingCall.route
                ) { backStackEntry ->
                    val callSessionId = backStackEntry.arguments?.getString("callSessionId") ?: ""
                    val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
                    OutgoingCallScreen(
                        callSessionId = callSessionId,
                        receiverName = receiverName,
                        onEndCall = {
                            mainNavController.popBackStack()
                        }
                    )
                }
                
                composable(
                    route = Screen.IncomingCall.route
                ) { backStackEntry ->
                    val callSessionId = backStackEntry.arguments?.getString("callSessionId") ?: ""
                    val callerName = backStackEntry.arguments?.getString("callerName") ?: ""
                    IncomingCallScreen(
                        callSessionId = callSessionId,
                        callerName = callerName,
                        onAnswerCall = {
                            mainNavController.navigate(Screen.ActiveCall.createRoute(callSessionId, callerName))
                        },
                        onDeclineCall = {
                            mainNavController.popBackStack()
                        }
                    )
                }
                
                composable(
                    route = Screen.ActiveCall.route
                ) { backStackEntry ->
                    val callSessionId = backStackEntry.arguments?.getString("callSessionId") ?: ""
                    val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
                    ActiveCallScreen(
                        callSessionId = callSessionId,
                        otherUserName = otherUserName,
                        onEndCall = {
mainNavController.popBackStack()
                        }
                    )
                }
                
                // Video call screen routes
                composable(
                    route = Screen.VideoOutgoingCall.route
                ) { backStackEntry ->
                    val callSessionId = backStackEntry.arguments?.getString("callSessionId") ?: ""
                    val receiverName = backStackEntry.arguments?.getString("receiverName") ?: ""
                    com.fitnessss.fitlife.ui.screens.call.VideoOutgoingCallScreen(
                        callSessionId = callSessionId,
                        receiverName = receiverName,
                        onEndCall = {
                            mainNavController.popBackStack()
                        }
                    )
                }
                
                composable(
                    route = Screen.VideoIncomingCall.route
                ) { backStackEntry ->
                    val callSessionId = backStackEntry.arguments?.getString("callSessionId") ?: ""
                    val callerName = backStackEntry.arguments?.getString("callerName") ?: ""
                    com.fitnessss.fitlife.ui.screens.call.VideoIncomingCallScreen(
                        callSessionId = callSessionId,
                        callerName = callerName,
                        onAnswerCall = {
                            mainNavController.navigate(Screen.VideoCall.createRoute(callSessionId, callerName))
                        },
                        onDeclineCall = {
                            mainNavController.popBackStack()
                        }
                    )
                }
                
                composable(
                    route = Screen.VideoCall.route
                ) { backStackEntry ->
                    val callSessionId = backStackEntry.arguments?.getString("callSessionId") ?: ""
                    val otherUserName = backStackEntry.arguments?.getString("otherUserName") ?: ""
                    com.fitnessss.fitlife.ui.screens.call.VideoCallScreen(
                        callSessionId = callSessionId,
                        otherUserName = otherUserName,
                        onEndCall = {
                            mainNavController.popBackStack()
                        }
                    )
                }
                composable(Screen.EditProfile.route) {
                    EditProfileScreen(navController = mainNavController)
                }
                composable(Screen.Preferences.route) {
                    PreferencesScreen(navController = mainNavController)
                }
                composable(Screen.Notifications.route) {
                    NotificationsScreen(navController = mainNavController)
                }
                composable(Screen.PrivacySecurity.route) {
                    PrivacySecurityScreen(navController = mainNavController)
                }
                composable(Screen.DataStorage.route) {
                    DataStorageScreen(navController = mainNavController)
                }
                composable(Screen.BiometricAuth.route) {
                    BiometricAuthScreen(
                        navController = mainNavController,
                        onAuthSuccess = {
                            println("DEBUG: MainActivity - Biometric auth success from main nav, navigating back")
                            mainNavController.popBackStack()
                        }
                    )
                }
            }
        }

        // If app was launched from incoming call pending intent, navigate now
        LaunchedEffect(Unit) {
            MainActivity.pendingIncomingCall?.let { (sessionId, callerName) ->
                try {
                    mainNavController.navigate(Screen.IncomingCall.createRoute(sessionId, callerName))
                } finally {
                    MainActivity.pendingIncomingCall = null
                }
            }
        }
        
        // AI Chat Bubble - Visible on all screens except chat and call screens
        val currentRoute = mainNavController.currentBackStackEntryAsState().value?.destination?.route
        val isChatScreen = currentRoute == Screen.ChatList.route || 
                          currentRoute?.startsWith(Screen.ChatDetail.route.split("/")[0]) == true
        val isCallScreen = currentRoute?.startsWith("outgoing_call") == true ||
                          currentRoute?.startsWith("incoming_call") == true ||
                          currentRoute?.startsWith("active_call") == true
        
        if (!isChatScreen && !isCallScreen) {
            AIChatBubble(
                aiService = aiViewModel.getAIService(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 16.dp)
            )
        }
    }
}

@Composable
fun PageHeaderWithThemeToggle(
    title: String,
    modifier: Modifier = Modifier,
    showAIToggle: Boolean = false,
    onAIToggleClick: (() -> Unit)? = null
) {
    val themeManager = LocalThemeManager.current
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AI Toggle Button (only show when requested)
            if (showAIToggle && onAIToggleClick != null) {
                IconButton(
                    onClick = onAIToggleClick,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SmartToy,
                        contentDescription = "AI Workout Generator",
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Theme Toggle Button
            IconButton(
                onClick = { 
                    themeManager.toggleTheme()
                },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = if (themeManager.isDarkMode) 
                        Icons.Filled.LightMode 
                    else 
                        Icons.Filled.DarkMode,
                    contentDescription = if (themeManager.isDarkMode) 
                        "Switch to Light Mode" 
                    else 
                        "Switch to Dark Mode",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun CleanBottomNavigationBar(
    navController: androidx.navigation.NavHostController,
    items: List<BottomNavItem>
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(28.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the root of this tab if currently on a nested screen under it
                        popUpTo(item.route) { inclusive = false; saveState = true }
                        // Also ensure we pop to the graph start when switching tabs, preserving state
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

// Data classes
data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon or logo
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = "FitLife Logo",
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // App name
            Text(
                text = "FitLife",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Your Personal Fitness Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading indicator
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
