package com.fitnessss.fitlife.ui.screens.progress

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitnessss.fitlife.data.*
import com.fitnessss.fitlife.navigation.Screen
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController) {
    // Sample data initialization removed - only real user data will be shown
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PageHeaderWithThemeToggle(title = "Progress")
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Stats Overview
            PersonalizedStatsOverview()
            
            // Current Streak
            CurrentStreakCard()
            
            // Recent Activity
            RecentActivityCard()
            
            // Weight Progress (if user has weight data)
            WeightProgressCard()
            
            // Body Measurements Progress
            BodyMeasurementsProgressCard()
            
            // Personal Records
            PersonalRecordsCard()
            
            // Achievements
            EarnedAchievementsCard()
            
            // Weekly Analytics
            WeeklyAnalyticsCard()
        }
    }
}

@Composable
fun PersonalizedStatsOverview() {
    val workoutFrequency = UserProgressManager.getWorkoutFrequency(30)
    val totalCalories = UserProgressManager.getTotalCaloriesBurned(30)
    val avgDuration = UserProgressManager.getAverageWorkoutDuration(30)
    val totalWorkouts = UserProgressManager.workoutSessions.value.size
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                    text = "Your Fitness Summary",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatsCard(
                    value = totalWorkouts.toString(),
                    label = "Total\nWorkouts",
                    icon = Icons.Filled.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    value = String.format("%.1f", workoutFrequency),
                    label = "Weekly\nAverage",
                    icon = Icons.Filled.CalendarToday,
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    value = totalCalories.toString(),
                    label = "Calories\nBurned",
                    icon = Icons.Filled.LocalFireDepartment,
                    modifier = Modifier.weight(1f)
                )
                
                StatsCard(
                    value = "${avgDuration.toInt()}min",
                    label = "Avg\nDuration",
                    icon = Icons.Filled.Timer,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatsCard(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(4.dp),
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
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp,
                maxLines = 2
            )
        }
    }
}

@Composable
fun CurrentStreakCard() {
    val currentStreak = UserProgressManager.getCurrentStreak()
    
    AnimatedVisibility(visible = currentStreak > 0) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (currentStreak >= 7) 
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                else 
                    MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.LocalFireDepartment,
                    contentDescription = "Streak",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$currentStreak Day Streak!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (currentStreak >= 7) "Amazing consistency! Keep it up!" else "Great start! Keep building that habit!",
                        style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
                }
                if (currentStreak >= 7) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Achievement",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivityCard() {
    val recentWorkouts = UserProgressManager.workoutSessions.value
        .sortedByDescending { it.date }
        .take(5)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Text(
                    text = "Recent Workouts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recentWorkouts.size} this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (recentWorkouts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.DirectionsRun,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No workouts yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Start your first workout to see progress!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentWorkouts.forEach { workout ->
                        RecentWorkoutItem(workout)
                    }
                }
            }
        }
    }
}

@Composable
fun RecentWorkoutItem(workout: WorkoutSession) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            when (workout.workoutType.lowercase()) {
                "strength" -> Icons.Filled.FitnessCenter
                "cardio" -> Icons.Filled.DirectionsRun
                "hiit" -> Icons.Filled.LocalFireDepartment
                "yoga" -> Icons.Filled.SelfImprovement
                else -> Icons.Filled.SportsGymnastics
            },
            contentDescription = workout.workoutType,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = workout.workoutName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${workout.duration}min • ${workout.caloriesBurned} cal • ${workout.setsCompleted} sets",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Text(
            text = workout.date.toLocalDate().format(DateTimeFormatter.ofPattern("MMM dd")),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun WeightProgressCard() {
    val weightProgress = UserProgressManager.getWeightProgress()
    val latestWeight = UserProgressManager.weightEntries.value.maxByOrNull { it.date }
    
    AnimatedVisibility(visible = weightProgress != null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                    text = "Weight Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
                Spacer(modifier = Modifier.height(12.dp))
                
                if (weightProgress != null && latestWeight != null) {
                    val (startWeight, currentWeight) = weightProgress
                    val weightChange = currentWeight - startWeight
                    val isLoss = weightChange < 0
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Weight",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${currentWeight} lbs",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                    Text(
                                text = if (isLoss) "Lost" else "Gained",
                                style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isLoss) Icons.Filled.TrendingDown else Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${abs(weightChange)} lbs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    
                    if (latestWeight.bodyFatPercentage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Body Fat: ${latestWeight.bodyFatPercentage}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BodyMeasurementsProgressCard() {
    val measurements = UserProgressManager.bodyMeasurements.value
    val latestMeasurement = measurements.maxByOrNull { it.date }
    val previousMeasurement = measurements
        .filter { it.date != latestMeasurement?.date }
        .maxByOrNull { it.date }
    
    AnimatedVisibility(visible = latestMeasurement != null) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Text(
                    text = "Body Measurements",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                    TextButton(onClick = { /* Navigate to add measurements */ }) {
                    Text("Update")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
                if (latestMeasurement != null) {
                    val measurementData = listOf(
                        "Chest" to latestMeasurement.chest,
                        "Waist" to latestMeasurement.waist,
                        "Hips" to latestMeasurement.hips,
                        "Biceps" to latestMeasurement.biceps,
                        "Thighs" to latestMeasurement.thighs
                    )
                    
                    measurementData.forEach { (label, current) ->
                        val previous = when (label) {
                            "Chest" -> previousMeasurement?.chest
                            "Waist" -> previousMeasurement?.waist
                            "Hips" -> previousMeasurement?.hips
                            "Biceps" -> previousMeasurement?.biceps
                            "Thighs" -> previousMeasurement?.thighs
                            else -> null
                        }
                        
                        val change = if (previous != null) current - previous else 0.0
                        
                        MeasurementProgressItem(
                            label = label,
                            current = "${current}\"",
                            change = if (change != 0.0) String.format("%+.1f\"", change) else "New"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MeasurementProgressItem(
    label: String,
    current: String,
    change: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = current,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = change,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PersonalRecordsCard() {
    val personalRecords = UserProgressManager.personalRecords
        .sortedByDescending { it.date }
        .take(5)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Personal Records",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (personalRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No records yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Complete workouts to set your first records!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    personalRecords.forEach { record ->
                        PersonalRecordItem(record)
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalRecordItem(record: PersonalRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = record.exerciseName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = record.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        Text(
                text = "${record.value.toInt()} ${record.unit}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
        )
        }
    }
}

@Composable
fun EarnedAchievementsCard() {
    val achievements = UserProgressManager.achievements
        .sortedByDescending { it.dateEarned }
        .take(3)
    
    AnimatedVisibility(visible = achievements.isNotEmpty()) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Recent Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    achievements.forEach { achievement ->
                        AchievementItem(achievement)
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementItem(achievement: Achievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            when (achievement.category) {
                AchievementCategory.CONSISTENCY -> Icons.Filled.LocalFireDepartment
                AchievementCategory.STRENGTH -> Icons.Filled.FitnessCenter
                AchievementCategory.ENDURANCE -> Icons.Filled.DirectionsRun
                AchievementCategory.WEIGHT_LOSS -> Icons.Filled.TrendingDown
                AchievementCategory.GENERAL_FITNESS -> Icons.Filled.Star
            },
            contentDescription = achievement.title,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        
        Text(
            text = achievement.dateEarned.format(DateTimeFormatter.ofPattern("MMM dd")),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun WeeklyAnalyticsCard() {
    val last7Days = (0..6).map { LocalDate.now().minusDays(it.toLong()) }.reversed()
    val dailyStats = last7Days.map { date ->
        UserProgressManager.dailyStats.value.find { it.date == date } ?: DailyStats(date)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "This Week's Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                dailyStats.forEach { dayStats ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = dayStats.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (dayStats.workoutsCompleted > 0) 
                                        MaterialTheme.colorScheme.onSurface
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayStats.workoutsCompleted.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (dayStats.workoutsCompleted > 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (dayStats.workoutsCompleted > 0) 
                                MaterialTheme.colorScheme.onSurface
                            else 
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val totalWeekWorkouts = dailyStats.sumOf { it.workoutsCompleted }
            val totalWeekCalories = dailyStats.sumOf { it.caloriesBurned }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalWeekWorkouts.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Workouts",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalWeekCalories.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                        text = "Calories",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
    }
}