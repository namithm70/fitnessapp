package com.fitnessss.fitlife.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.navigation.Screen
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.UserProgressManager
import com.fitnessss.fitlife.data.UserProfileManager
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionManager
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController, onMenuClick: () -> Unit = {}) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PageHeaderWithThemeToggle(
            title = "Dashboard",
            showAIToggle = true,
            onAIToggleClick = {
                navController.navigate(Screen.AIWorkoutGenerator.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome Section
            item {
                WelcomeCard()
            }

            // Stats Overview
            item {
                Text(
                    text = "Today's Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                StatsGrid()
            }

            // Quick Actions
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                QuickActionsGrid(navController, snackbarHostState)
            }

            // Recent Workouts
            item {
                Text(
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                RecentWorkoutsList(navController)
            }

            // Recommendations
            item {
                Text(
                    text = "Recommended for You",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                RecommendationsRow(navController)
            }
        }
    }
}

@Composable
fun WelcomeCard() {
    val userProfile = UserProfileManager.userProfile
    val userName = if (userProfile.name.isNotEmpty()) userProfile.name else "Fitness Warrior"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Welcome back, $userName!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Ready to crush your fitness goals today?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatsGrid() {
    val today = LocalDate.now()
    
    // Use derivedStateOf to automatically recalculate when UserProgressManager changes
    val workoutSessions by remember { derivedStateOf { UserProgressManager.workoutSessions.value } }
    val dailyStats by remember { derivedStateOf { UserProgressManager.dailyStats.value } }
    
    val todayStats = dailyStats.find { it.date == today }
    val totalWorkouts = workoutSessions.size
    val currentStreak = UserProgressManager.getCurrentStreak()
    
    // Debug logging
    LaunchedEffect(todayStats, workoutSessions) {
        println("DEBUG: Dashboard - Today's stats: ${todayStats?.workoutsCompleted ?: 0} workouts, ${todayStats?.caloriesBurned ?: 0} calories")
        println("DEBUG: Dashboard - Total workout sessions: ${workoutSessions.size}")
        println("DEBUG: Dashboard - Today's workout sessions: ${workoutSessions.filter { it.date.toLocalDate() == today }.size}")
    }
    
    // Calculate total calories burned from all workout sessions
    val totalCaloriesBurned = workoutSessions.sumOf { it.caloriesBurned }
    
    // Calculate total calories consumed from nutrition
    val totalCaloriesConsumed = NutritionManager.getTotalNutrition().calories
    
    // Calculate this week's calories (last 7 days)
    val weekStart = today.minusDays(6)
    val thisWeekCalories = workoutSessions
        .filter { it.date.toLocalDate().isAfter(weekStart.minusDays(1)) }
        .sumOf { it.caloriesBurned }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Today's Workouts",
                value = todayStats?.workoutsCompleted?.toString() ?: "0",
                icon = Icons.Filled.FitnessCenter,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = if (todayStats?.caloriesBurned != null && todayStats.caloriesBurned > 0) "Today's Burned" else "This Week's Burned",
                value = if (todayStats?.caloriesBurned != null && todayStats.caloriesBurned > 0) {
                    todayStats.caloriesBurned.toString()
                } else {
                    thisWeekCalories.toString()
                },
                icon = Icons.Filled.LocalFireDepartment,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Workouts",
                value = totalWorkouts.toString(),
                icon = Icons.Filled.Timeline,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Current Streak",
                value = "${currentStreak} days",
                icon = Icons.Filled.LocalFireDepartment,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Calories Consumed",
                value = totalCaloriesConsumed.toString(),
                icon = Icons.Filled.Restaurant,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Net Calories",
                value = (totalCaloriesConsumed - totalCaloriesBurned).toString(),
                icon = Icons.Filled.TrendingUp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Total Burned",
                value = totalCaloriesBurned.toString(),
                icon = Icons.Filled.LocalFireDepartment,
                color = MaterialTheme.colorScheme.onSurface
            )
            StatCard(
                modifier = Modifier.weight(1f),
                title = "Avg Duration",
                value = if (totalWorkouts > 0) {
                    val avgDuration = workoutSessions.map { it.duration }.average().toInt()
                    "${avgDuration} min"
                } else {
                    "0 min"
                },
                icon = Icons.Filled.Timer,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun StatCard(
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
fun QuickActionsGrid(navController: NavController, snackbarHostState: SnackbarHostState) {
    val scope = rememberCoroutineScope()
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Start Workout",
                icon = Icons.Filled.PlayArrow,
                onClick = { 
                    navController.navigate(Screen.WorkoutList.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Log Meal",
                icon = Icons.Filled.Restaurant,
                onClick = { 
                    navController.navigate(Screen.FoodSearch.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "Track Progress",
                icon = Icons.Filled.TrendingUp,
                onClick = { 
                    navController.navigate(Screen.Progress.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
            QuickActionButton(
                modifier = Modifier.weight(1f),
                title = "History",
                icon = Icons.Filled.History,
                onClick = { 
                    navController.navigate(Screen.History.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color(0xFFFCE4EC) // Light pink background
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = androidx.compose.ui.graphics.Color(0xFFC2185B), // Dark pink for icons
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = androidx.compose.ui.graphics.Color(0xFF880E4F) // Darker pink for text
            )
        }
    }
}

@Composable
fun RecentWorkoutsList(navController: NavController) {
    // Use derivedStateOf to automatically recalculate when UserProgressManager changes
    val workoutSessions by remember { derivedStateOf { UserProgressManager.workoutSessions.value } }
    
    val recentWorkouts = workoutSessions
        .sortedByDescending { it.date }
        .take(3)
        .map { session ->
            val daysAgo = when {
                session.date.toLocalDate() == LocalDate.now() -> "Today"
                session.date.toLocalDate() == LocalDate.now().minusDays(1) -> "Yesterday"
                else -> {
                    val days = LocalDate.now().toEpochDay() - session.date.toLocalDate().toEpochDay()
                    "${days} days ago"
                }
            }
            WorkoutItem(session.workoutName, "${session.duration} min", daysAgo)
        }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (recentWorkouts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = "No workouts",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No workouts yet",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Start your first workout to see it here",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            recentWorkouts.forEach { workout ->
                WorkoutListItem(
                    workout = workout,
                    onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${workout.name.replace(" ", "_").lowercase()}") }
                )
            }
        }
    }
}

@Composable
fun WorkoutListItem(
    workout: WorkoutItem,
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
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = "Workout",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${workout.duration} • ${workout.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "View workout",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun RecommendationsRow(navController: NavController) {
    val recommendations = listOf(
        RecommendationItem("Morning Yoga", "15 min", Icons.Filled.SelfImprovement),
        RecommendationItem("Quick Cardio", "20 min", Icons.Filled.DirectionsRun),
        RecommendationItem("Strength Circuit", "35 min", Icons.Filled.FitnessCenter)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(recommendations) { recommendation ->
            RecommendationCard(
                recommendation = recommendation,
                onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${recommendation.name.replace(" ", "_").lowercase()}") }
            )
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: RecommendationItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(160.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = recommendation.icon,
                contentDescription = recommendation.name,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recommendation.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = recommendation.duration,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Data classes
data class WorkoutItem(
    val name: String,
    val duration: String,
    val date: String
)

data class RecommendationItem(
    val name: String,
    val duration: String,
    val icon: ImageVector
)