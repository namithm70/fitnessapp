package com.fitnessss.fitlife.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.fitnessss.fitlife.navigation.Screen
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.UserProfileManager
import com.fitnessss.fitlife.data.UserProgressManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onSignOut: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showClearDataDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PageHeaderWithThemeToggle(title = "Profile")
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header
            item {
                ProfileHeader()
            }

            // Stats Overview
            item {
                Text(
                    text = "Fitness Stats",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                FitnessStatsGrid()
            }

            // Goals Section
            item {
                Text(
                    text = "Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                GoalsSection()
            }

            // Account Settings
            item {
                Text(
                    text = "Account & Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                AccountSettingsSection(navController, viewModel, showClearDataDialog, onSignOut) { showClearDataDialog = it }
            }

            // Progress Section (moved from bottom nav)
            item {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                // Navigate to detailed progress when tapped
                ElevatedButton(onClick = { navController.navigate(com.fitnessss.fitlife.navigation.Screen.Progress.route) }) {
                    Text("Open Progress")
                }
            }

            // History Section (moved from bottom nav)
            item {
                Text(
                    text = "History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                ElevatedButton(onClick = { navController.navigate(com.fitnessss.fitlife.navigation.Screen.History.route) }) {
                    Text("Open History")
                }
            }

            // Support & About
            item {
                Text(
                    text = "Support & About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                SupportSection()
            }
        }
        
        // Clear Data Confirmation Dialog
        if (showClearDataDialog) {
            ClearDataConfirmationDialog(
                onConfirm = {
                    viewModel.clearAllUserData(
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("All data cleared successfully")
                            }
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Error: $error")
                            }
                        }
                    )
                    showClearDataDialog = false
                },
                onDismiss = { showClearDataDialog = false }
            )
        }
    }
    
    // Snackbar for showing messages
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun ProfileHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Picture
            Card(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile Picture",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (UserProfileManager.userProfile.name.isNotEmpty()) UserProfileManager.userProfile.name else "Set Your Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = UserProfileManager.getFitnessLevelDisplay(),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = UserProfileManager.getMemberSinceDisplay(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FitnessStatsGrid() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
                         FitnessStatCard(
                 modifier = Modifier.weight(1f),
                 title = "Weight",
                 value = UserProfileManager.getWeightDisplay(),
                 icon = Icons.Filled.MonitorWeight,
                 color = MaterialTheme.colorScheme.onSurface
             )
             FitnessStatCard(
                 modifier = Modifier.weight(1f),
                 title = "Height",
                 value = UserProfileManager.getHeightDisplay(),
                 icon = Icons.Filled.Height,
                 color = MaterialTheme.colorScheme.onSurface
             )
         }
         Row(
             modifier = Modifier.fillMaxWidth(),
             horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
         ) {
             FitnessStatCard(
                 modifier = Modifier.weight(1f),
                 title = "Workouts",
                 value = UserProgressManager.workoutSessions.value.size.toString(),
                 icon = Icons.Filled.FitnessCenter,
                 color = MaterialTheme.colorScheme.onSurface
             )
             FitnessStatCard(
                 modifier = Modifier.weight(1f),
                 title = "Streak",
                 value = "${UserProgressManager.getCurrentStreak()} days",
                 icon = Icons.Filled.LocalFireDepartment,
                 color = MaterialTheme.colorScheme.onSurface
             )
        }
    }
}

@Composable
fun FitnessStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun GoalsSection() {
    val userProfile = UserProfileManager.userProfile
    val goals = listOf(
        GoalItem(
            "Fitness Goal", 
            "Target: ${UserProfileManager.getFitnessGoalDisplay()}", 
            "Set your goal", 
            0.0f
        ),
        GoalItem(
            "Workout Frequency", 
            "Target: ${UserProfileManager.getWorkoutDaysDisplay()}", 
            "Set your frequency", 
            0.0f
        ),
        GoalItem(
            "Current Level", 
            "Target: ${UserProfileManager.getFitnessLevelDisplay()}", 
            "Set your level", 
            0.0f
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        goals.forEach { goal ->
            GoalCard(goal = goal)
        }
    }
}

@Composable
fun GoalCard(goal: GoalItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = goal.target,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                                 Text(
                     text = goal.progress,
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurface,
                     fontWeight = FontWeight.Medium
                 )
             }
             
             Spacer(modifier = Modifier.height(8.dp))
             
             LinearProgressIndicator(
                 progress = { goal.progressValue },
                 modifier = Modifier.fillMaxWidth(),
                 color = MaterialTheme.colorScheme.onSurface,
             )
        }
    }
}

@Composable
fun AccountSettingsSection(
    navController: NavController,
    viewModel: ProfileViewModel,
    showClearDataDialog: Boolean,
    onSignOut: () -> Unit,
    onShowClearDataDialog: (Boolean) -> Unit
) {
    val settingsItems = listOf(
        SettingItem("Edit Profile", "Update your profile details", Icons.Filled.Person),
        SettingItem("Preferences", "App settings and preferences", Icons.Filled.Settings),
        SettingItem("Notifications", "Manage your notifications", Icons.Filled.Notifications),
        SettingItem("Privacy & Security", "Privacy settings and security", Icons.Filled.Security),
        SettingItem("Data & Storage", "Manage your data", Icons.Filled.Storage),
        SettingItem("Clear All Data", "Delete all workout, nutrition, and history data", Icons.Filled.DeleteForever),
        SettingItem("Sign Out", "Sign out of your account", Icons.Filled.Logout)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        settingsItems.forEach { item ->
            SettingCard(
                item = item,
                onClick = { 
                    when (item.title) {
                        "Edit Profile" -> navController.navigate(Screen.EditProfile.route)
                        "Preferences" -> navController.navigate(Screen.Preferences.route)
                        "Notifications" -> navController.navigate(Screen.Notifications.route)
                        "Privacy & Security" -> navController.navigate(Screen.PrivacySecurity.route)
                        "Data & Storage" -> navController.navigate(Screen.DataStorage.route)
                        "Clear All Data" -> onShowClearDataDialog(true)
                        "Sign Out" -> {
                            viewModel.signOut()
                            onSignOut()
                        }
                        else -> { /* Navigate to specific setting */ }
                    }
                }
            )
        }
    }
}

@Composable
fun SupportSection() {
    val supportItems = listOf(
        SettingItem("Help Center", "FAQs and guides", Icons.Filled.Help),
        SettingItem("Contact Support", "Get help from our team", Icons.Filled.ContactSupport),
        SettingItem("Rate App", "Rate us on the app store", Icons.Filled.Star),
        SettingItem("About", "App version and info", Icons.Filled.Info),
        SettingItem("Logout", "Sign out of your account", Icons.Filled.Logout)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        supportItems.forEach { item ->
            SettingCard(
                item = item,
                onClick = { 
                    if (item.title == "Logout") {
                        // Handle logout
                    }
                }
            )
        }
    }
}

@Composable
fun SettingCard(
    item: SettingItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                         Icon(
                 imageVector = item.icon,
                 contentDescription = item.title,
                 tint = MaterialTheme.colorScheme.onSurface,
                 modifier = Modifier.size(24.dp)
             )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Go to ${item.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ClearDataConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Clear All Data",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "This action will permanently delete all your:\n\n" +
                       "• Workout history and progress\n" +
                       "• Nutrition data and logged foods\n" +
                       "• Activity history\n" +
                       "• Weight entries and measurements\n" +
                       "• Personal records and achievements\n\n" +
                       "This action cannot be undone. Are you sure you want to continue?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Clear All Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

// Data classes
data class GoalItem(
    val title: String,
    val target: String,
    val progress: String,
    val progressValue: Float
)

data class SettingItem(
    val title: String,
    val description: String,
    val icon: ImageVector
)