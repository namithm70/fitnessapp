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
fun NotificationsScreen(
    navController: NavController
) {
    var workoutReminders by remember { mutableStateOf(true) }
    var nutritionReminders by remember { mutableStateOf(true) }
    var progressReminders by remember { mutableStateOf(false) }
    var achievementNotifications by remember { mutableStateOf(true) }
    var weeklyReports by remember { mutableStateOf(true) }
    var motivationalMessages by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var quietHoursEnabled by remember { mutableStateOf(false) }
    var quietHoursStart by remember { mutableStateOf("22:00") }
    var quietHoursEnd by remember { mutableStateOf("08:00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
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
            PageHeaderWithThemeToggle(title = "Notification Settings")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Workout Notifications
            NotificationSection(
                title = "Workout Notifications",
                icon = Icons.Filled.FitnessCenter
            ) {
                NotificationToggleItem(
                    title = "Workout Reminders",
                    subtitle = "Get reminded about scheduled workouts",
                    icon = Icons.Filled.Schedule,
                    checked = workoutReminders,
                    onCheckedChange = { workoutReminders = it }
                )
                
                NotificationToggleItem(
                    title = "Workout Completion",
                    subtitle = "Celebrate when you finish a workout",
                    icon = Icons.Filled.CheckCircle,
                    checked = true,
                    onCheckedChange = { }
                )
                
                NotificationToggleItem(
                    title = "Rest Day Reminders",
                    subtitle = "Get reminded to take rest days",
                    icon = Icons.Filled.Bedtime,
                    checked = false,
                    onCheckedChange = { }
                )
            }
            
            // Nutrition Notifications
            NotificationSection(
                title = "Nutrition Notifications",
                icon = Icons.Filled.Restaurant
            ) {
                NotificationToggleItem(
                    title = "Meal Logging Reminders",
                    subtitle = "Get reminded to log your meals",
                    icon = Icons.Filled.Restaurant,
                    checked = nutritionReminders,
                    onCheckedChange = { nutritionReminders = it }
                )
                
                NotificationToggleItem(
                    title = "Water Intake Reminders",
                    subtitle = "Get reminded to drink water",
                    icon = Icons.Filled.WaterDrop,
                    checked = true,
                    onCheckedChange = { }
                )
                
                NotificationToggleItem(
                    title = "Calorie Goal Alerts",
                    subtitle = "Get notified about calorie goals",
                    icon = Icons.Filled.LocalFireDepartment,
                    checked = false,
                    onCheckedChange = { }
                )
            }
            
            // Progress Notifications
            NotificationSection(
                title = "Progress & Achievements",
                icon = Icons.Filled.TrendingUp
            ) {
                NotificationToggleItem(
                    title = "Progress Reminders",
                    subtitle = "Get reminded to check your progress",
                    icon = Icons.Filled.TrendingUp,
                    checked = progressReminders,
                    onCheckedChange = { progressReminders = it }
                )
                
                NotificationToggleItem(
                    title = "Achievement Notifications",
                    subtitle = "Celebrate your fitness achievements",
                    icon = Icons.Filled.EmojiEvents,
                    checked = achievementNotifications,
                    onCheckedChange = { achievementNotifications = it }
                )
                
                NotificationToggleItem(
                    title = "Weekly Reports",
                    subtitle = "Get weekly fitness summaries",
                    icon = Icons.Filled.Assessment,
                    checked = weeklyReports,
                    onCheckedChange = { weeklyReports = it }
                )
                
                NotificationToggleItem(
                    title = "Motivational Messages",
                    subtitle = "Receive motivational fitness tips",
                    icon = Icons.Filled.Psychology,
                    checked = motivationalMessages,
                    onCheckedChange = { motivationalMessages = it }
                )
            }
            
            // Notification Behavior
            NotificationSection(
                title = "Notification Behavior",
                icon = Icons.Filled.Settings
            ) {
                NotificationToggleItem(
                    title = "Sound",
                    subtitle = "Play sounds for notifications",
                    icon = Icons.Filled.VolumeUp,
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )
                
                NotificationToggleItem(
                    title = "Vibration",
                    subtitle = "Vibrate for notifications",
                    icon = Icons.Filled.Vibration,
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
                
                NotificationToggleItem(
                    title = "Quiet Hours",
                    subtitle = "Silence notifications during quiet hours",
                    icon = Icons.Filled.Bedtime,
                    checked = quietHoursEnabled,
                    onCheckedChange = { quietHoursEnabled = it }
                )
            }
            
            // Quiet Hours Settings (if enabled)
            if (quietHoursEnabled) {
                NotificationSection(
                    title = "Quiet Hours",
                    icon = Icons.Filled.Bedtime
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = "Quiet Hours",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Quiet Hours",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "From $quietHoursStart to $quietHoursEnd",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        TextButton(
                            onClick = { /* TODO: Show time picker */ }
                        ) {
                            Text("Change")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun NotificationSection(
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
fun NotificationToggleItem(
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
