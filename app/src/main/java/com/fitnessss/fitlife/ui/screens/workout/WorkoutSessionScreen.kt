package com.fitnessss.fitlife.ui.screens.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    navController: NavController, 
    workoutId: String,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    val exercises = remember(workoutId) {
        when (workoutId) {
            "quick_start" -> listOf(
                SessionExercise("1", "Jumping Jacks", 3, "30 seconds"),
                SessionExercise("2", "Push-ups", 3, "10-15 reps"),
                SessionExercise("3", "Squats", 3, "15-20 reps"),
                SessionExercise("4", "Plank", 3, "30-45 seconds")
            )
            "custom" -> listOf(
                SessionExercise("1", "Burpees", 3, "8-10 reps"),
                SessionExercise("2", "Mountain Climbers", 3, "20 reps"),
                SessionExercise("3", "High Knees", 3, "30 seconds")
            )
            "strength" -> listOf(
                SessionExercise("1", "Push-ups", 4, "8-12 reps"),
                SessionExercise("2", "Pull-ups", 4, "5-8 reps"),
                SessionExercise("3", "Squats", 4, "12-15 reps"),
                SessionExercise("4", "Deadlifts", 4, "6-8 reps"),
                SessionExercise("5", "Lunges", 4, "10-12 per leg"),
                SessionExercise("6", "Pike Push-ups", 4, "6-10 reps")
            )
            "cardio" -> listOf(
                SessionExercise("1", "Running in Place", 4, "60 seconds"),
                SessionExercise("2", "Jumping Jacks", 4, "45 seconds"),
                SessionExercise("3", "Burpees", 4, "10 reps"),
                SessionExercise("4", "High Knees", 4, "30 seconds")
            )
            "hiit" -> listOf(
                SessionExercise("1", "Burpees", 3, "20 seconds"),
                SessionExercise("2", "Mountain Climbers", 3, "20 seconds"),
                SessionExercise("3", "Jump Squats", 3, "20 seconds"),
                SessionExercise("4", "Plank Jacks", 3, "20 seconds")
            )
            "yoga" -> listOf(
                SessionExercise("1", "Downward Dog", 3, "60 seconds"),
                SessionExercise("2", "Warrior I", 3, "45 seconds each side"),
                SessionExercise("3", "Warrior II", 3, "45 seconds each side"),
                SessionExercise("4", "Tree Pose", 3, "30 seconds each"),
                SessionExercise("5", "Triangle Pose", 3, "30 seconds each side"),
                SessionExercise("6", "Child's Pose", 3, "60 seconds")
            )
            "full_body_blast" -> listOf(
                SessionExercise("1", "Warm-up Jacks", 1, "2 minutes"),
                SessionExercise("2", "Push-ups", 3, "12-15 reps"),
                SessionExercise("3", "Squats", 3, "15-20 reps"),
                SessionExercise("4", "Lunges", 3, "10 per leg"),
                SessionExercise("5", "Plank", 3, "45-60 seconds"),
                SessionExercise("6", "Burpees", 3, "8-10 reps")
            )
            "morning_cardio" -> listOf(
                SessionExercise("1", "Light Jog", 1, "5 minutes"),
                SessionExercise("2", "Jumping Jacks", 3, "45 seconds"),
                SessionExercise("3", "High Knees", 3, "30 seconds"),
                SessionExercise("4", "Butt Kicks", 3, "30 seconds"),
                SessionExercise("5", "Cool Down Walk", 1, "5 minutes")
            )
            "hiit_burn" -> listOf(
                SessionExercise("1", "Burpees", 4, "20 seconds max effort"),
                SessionExercise("2", "Mountain Climbers", 4, "20 seconds"),
                SessionExercise("3", "Jump Squats", 4, "20 seconds"),
                SessionExercise("4", "Plank to Push-up", 4, "15 seconds")
            )
            "7-minute_workout" -> listOf(
                SessionExercise("1", "Jumping Jacks", 1, "30 seconds"),
                SessionExercise("2", "Wall Sit", 1, "30 seconds"),
                SessionExercise("3", "Push-ups", 1, "30 seconds"),
                SessionExercise("4", "Crunches", 1, "30 seconds"),
                SessionExercise("5", "Step-ups", 1, "30 seconds"),
                SessionExercise("6", "Squats", 1, "30 seconds"),
                SessionExercise("7", "Tricep Dips", 1, "30 seconds"),
                SessionExercise("8", "Plank", 1, "30 seconds"),
                SessionExercise("9", "High Knees", 1, "30 seconds"),
                SessionExercise("10", "Lunges", 1, "30 seconds"),
                SessionExercise("11", "Push-up Rotation", 1, "30 seconds"),
                SessionExercise("12", "Side Plank", 1, "30 seconds each side")
            )
            else -> listOf(
                SessionExercise("1", "Bodyweight Squats", 3, "15 reps"),
                SessionExercise("2", "Push-ups", 3, "10 reps"),
                SessionExercise("3", "Lunges", 3, "12 reps each"),
                SessionExercise("4", "Plank", 3, "45 seconds")
            )
        }
    }

    var currentExerciseIndex by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var isResting by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var restSecondsRemaining by remember { mutableStateOf(0) }
    val totalSets = remember(exercises) { exercises.sumOf { it.sets } }
    var completedSets by remember { mutableStateOf(0) }
    var completedSetsThisExercise by remember { mutableStateOf(0) }

    // Session timer
    LaunchedEffect(isRunning, isResting) {
        while (isRunning && !isResting) {
            delay(1000)
            elapsedSeconds++
        }
    }

    // Rest countdown
    LaunchedEffect(isResting) {
        while (isResting && restSecondsRemaining > 0) {
            delay(1000)
            restSecondsRemaining--
        }
        if (isResting && restSecondsRemaining <= 0) {
            isResting = false
            isRunning = true
        }
    }

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%d:%02d".format(m, s)
    }

    val progress = remember(completedSets, totalSets) {
        if (totalSets == 0) 0f else completedSets.toFloat() / totalSets.toFloat()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (workoutId) {
                                "quick_start" -> "Quick Start Workout"
                                "custom" -> "Custom Workout"
                                "strength" -> "Strength Training"
                                "cardio" -> "Cardio Workout"
                                "hiit" -> "HIIT Training"
                                "yoga" -> "Yoga Session"
                                "full_body_blast" -> "Full Body Blast"
                                "morning_cardio" -> "Morning Cardio"
                                "hiit_burn" -> "HIIT Burn"
                                "7-minute_workout" -> "7-Minute Workout"
                                else -> workoutId.replace("_", " ").split(" ").joinToString(" ") { 
                                    it.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase() else char.toString() } 
                                }
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${exercises.size} exercises • ${totalSets} sets",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Text(
                        text = formatTime(elapsedSeconds),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 0.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Start/Pause Button
                    OutlinedButton(
                        onClick = {
                            if (isResting) return@OutlinedButton
                            isRunning = !isRunning
                        },
                        enabled = !isResting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            if (isRunning && !isResting) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (isResting) "Resting..." else if (isRunning) "Pause" else "Start",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    // Complete Set Button
                    Button(
                        onClick = {
                            // Complete current set logic
                            if (currentExerciseIndex < exercises.size && completedSetsThisExercise < exercises[currentExerciseIndex].sets) {
                                completedSetsThisExercise++
                                completedSets++
                                
                                // Check if current exercise is completed
                                if (completedSetsThisExercise >= exercises[currentExerciseIndex].sets) {
                                    // Move to next exercise if available
                                    if (currentExerciseIndex < exercises.lastIndex) {
                                        currentExerciseIndex++
                                        completedSetsThisExercise = 0
                                        isResting = false
                                        isRunning = true
                                    } else {
                                        // Workout completed
                                        isRunning = false
                                        isResting = false
                                    }
                                } else {
                                    // Start rest period between sets
                                    isRunning = false
                                    isResting = true
                                    restSecondsRemaining = 60
                                }
                            }
                        },
                        enabled = currentExerciseIndex < exercises.size && completedSetsThisExercise < exercises[currentExerciseIndex].sets,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(
                            Icons.Filled.Check, 
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (completedSets >= totalSets) "Workout Completed!" else "Complete Set",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }

                    // Finish Session Button
                    FilledTonalButton(
                        onClick = {
                            // Save workout session data using ViewModel
                            viewModel.saveWorkoutSession(
                                workoutId = workoutId,
                                exercises = exercises,
                                completedSets = completedSets,
                                elapsedSeconds = elapsedSeconds
                            )
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            Icons.Filled.Flag, 
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Finish Workout",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
        modifier = Modifier
            .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Session Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
    ) {
        Text(
                                text = "${completedSets} / ${totalSets} sets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = if (isResting) "Rest ${restSecondsRemaining}s" else if (isRunning) "On going" else "Paused",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            item {
                val current = exercises[currentExerciseIndex]
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            text = "Current Exercise",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = current.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Target: ${current.target}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(current.sets) { idx ->
                                val done = idx < completedSetsThisExercise
                                val isCurrent = idx == completedSetsThisExercise
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when {
                                            done -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    ),
                                    modifier = Modifier.animateContentSize()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            when {
                                                done -> Icons.Filled.Check
                                                isCurrent -> Icons.Filled.PlayArrow
                                                else -> Icons.Filled.SportsGymnastics
                                            },
                                            contentDescription = null,
                                            tint = when {
                                                done -> MaterialTheme.colorScheme.primary
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(6.dp))
        Text(
                                            text = "Set ${idx + 1}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = if (done || isCurrent) FontWeight.Medium else FontWeight.Normal,
                                            color = when {
                                                done -> MaterialTheme.colorScheme.primary
                                                isCurrent -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (currentExerciseIndex < exercises.lastIndex) {
                item {
                    HorizontalDivider()
                    val next = exercises[currentExerciseIndex + 1]
        Text(
                        text = "Up Next",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(next.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                                Text("${next.sets} sets • ${next.target}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            OutlinedButton(onClick = {
                                currentExerciseIndex++
                                completedSetsThisExercise = 0
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Skip")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
            
            items(emptyList<String>()) { }
        }
    }
}


