package com.fitnessss.fitlife.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.fitnessss.fitlife.PageHeaderWithThemeToggle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    navController: NavController
) {
    var darkModeEnabled by remember { mutableStateOf(false) }
    var autoSyncEnabled by remember { mutableStateOf(true) }
    var workoutRemindersEnabled by remember { mutableStateOf(true) }
    var nutritionRemindersEnabled by remember { mutableStateOf(true) }
    var progressRemindersEnabled by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var unitsMetric by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            PageHeaderWithThemeToggle(title = "App Preferences")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display Settings
            PreferenceSection(
                title = "Display",
                icon = Icons.Filled.DisplaySettings
            ) {
                PreferenceToggleItem(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    icon = Icons.Filled.DarkMode,
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
            
            // Sync Settings
            PreferenceSection(
                title = "Data Sync",
                icon = Icons.Filled.Sync
            ) {
                PreferenceToggleItem(
                    title = "Auto Sync",
                    subtitle = "Automatically sync data to cloud",
                    icon = Icons.Filled.CloudSync,
                    checked = autoSyncEnabled,
                    onCheckedChange = { autoSyncEnabled = it }
                )
            }
            
            // Notification Settings
            PreferenceSection(
                title = "Notifications",
                icon = Icons.Filled.Notifications
            ) {
                PreferenceToggleItem(
                    title = "Workout Reminders",
                    subtitle = "Get reminded about your workouts",
                    icon = Icons.Filled.FitnessCenter,
                    checked = workoutRemindersEnabled,
                    onCheckedChange = { workoutRemindersEnabled = it }
                )
                
                PreferenceToggleItem(
                    title = "Nutrition Reminders",
                    subtitle = "Get reminded to log your meals",
                    icon = Icons.Filled.Restaurant,
                    checked = nutritionRemindersEnabled,
                    onCheckedChange = { nutritionRemindersEnabled = it }
                )
                
                PreferenceToggleItem(
                    title = "Progress Reminders",
                    subtitle = "Get reminded to check your progress",
                    icon = Icons.Filled.TrendingUp,
                    checked = progressRemindersEnabled,
                    onCheckedChange = { progressRemindersEnabled = it }
                )
            }
            
            // Sound & Vibration
            PreferenceSection(
                title = "Sound & Vibration",
                icon = Icons.Filled.VolumeUp
            ) {
                PreferenceToggleItem(
                    title = "Sound",
                    subtitle = "Play sounds for notifications",
                    icon = Icons.Filled.VolumeUp,
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
                
                PreferenceToggleItem(
                    title = "Vibration",
                    subtitle = "Vibrate for notifications",
                    icon = Icons.Filled.Vibration,
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }
            
            // Units
            PreferenceSection(
                title = "Units",
                icon = Icons.Filled.Straighten
            ) {
                PreferenceToggleItem(
                    title = "Metric Units",
                    subtitle = "Use kg, cm, km (Imperial: lbs, ft, mi)",
                    icon = Icons.Filled.Straighten,
                    checked = unitsMetric,
                    onCheckedChange = { unitsMetric = it }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PreferenceSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun PreferenceToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
