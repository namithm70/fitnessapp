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
import com.fitnessss.fitlife.data.model.GeneratedExercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIWorkoutSessionScreen(
    navController: NavController,
    workoutName: String,
    workoutType: String,
    duration: Int,
    calories: Int,
    exercises: String,
    viewModel: WorkoutSessionViewModel = hiltViewModel()
) {
    // Parse exercises string back to list
    val exercisesList = remember(exercises) {
        parseExercisesString(exercises)
    }

    var currentExerciseIndex by remember { mutableStateOf(0) }
    var isRunning by remember { mutableStateOf(false) }
    var isResting by remember { mutableStateOf(false) }
    var elapsedSeconds by remember { mutableStateOf(0) }
    var restSecondsRemaining by remember { mutableStateOf(0) }
    val totalSets = remember(exercisesList) { exercisesList.sumOf { it.sets } }
    var completedSets by remember { mutableStateOf(0) }
    var completedSetsThisExercise by remember { mutableStateOf(0) }

    // Timer for workout
    LaunchedEffect(isRunning) {
        while (isRunning) {
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
                            text = workoutName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = workoutType,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Progress",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${completedSets}/${totalSets} sets",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = formatTime(elapsedSeconds),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Elapsed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column {
                                Text(
                                    text = "${duration} min",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Target",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column {
                                Text(
                                    text = "$calories cal",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Estimated",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Current Exercise Section
            if (exercisesList.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Current Exercise",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (currentExerciseIndex < exercisesList.size) {
                                val currentExercise = exercisesList[currentExerciseIndex]
                                Text(
                                    text = currentExercise.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "${currentExercise.sets} sets × ${currentExercise.reps} reps",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            completedSetsThisExercise++
                                            completedSets++
                                            if (completedSetsThisExercise >= currentExercise.sets) {
                                                currentExerciseIndex++
                                                completedSetsThisExercise = 0
                                                if (currentExerciseIndex < exercisesList.size) {
                                                    isResting = true
                                                    restSecondsRemaining = 60
                                                    isRunning = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(Icons.Filled.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Complete Set")
                                    }
                                    
                                    OutlinedButton(
                                        onClick = {
                                            if (!isRunning && !isResting) {
                                                isRunning = true
                                            } else {
                                                isRunning = false
                                            }
                                        },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            if (isRunning) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                            contentDescription = null
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(if (isRunning) "Pause" else "Start")
                                    }
                                }
                            } else {
                                Text(
                                    text = "Workout Complete!",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Rest Timer
            if (isResting) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Rest Time",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = formatTime(restSecondsRemaining),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Next: ${if (currentExerciseIndex < exercisesList.size) exercisesList[currentExerciseIndex].name else "Workout Complete"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Exercise List
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Workout Plan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        exercisesList.forEachIndexed { index, exercise ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (index < currentExerciseIndex) Icons.Filled.CheckCircle else Icons.Filled.Circle,
                                    contentDescription = null,
                                    tint = if (index < currentExerciseIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = exercise.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${exercise.sets} sets × ${exercise.reps} reps",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (index == currentExerciseIndex && !isResting) {
                                    Icon(
                                        imageVector = Icons.Filled.PlayArrow,
                                        contentDescription = "Current",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            if (index < exercisesList.size - 1) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            }
                        }
                    }
                }
            }

            // Finish Session Button
            item {
                FilledTonalButton(
                    onClick = {
                        // Save workout session data using ViewModel
                        viewModel.saveWorkoutSession(
                            workoutId = "ai_${workoutName.replace(" ", "_").lowercase()}",
                            exercises = exercisesList.map { 
                                SessionExercise(
                                    id = it.name,
                                    name = it.name,
                                    sets = it.sets,
                                    target = "${it.reps} reps"
                                )
                            },
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
}

// Helper function to parse exercises string back to list
private fun parseExercisesString(exercisesString: String): List<GeneratedExercise> {
    return if (exercisesString.isNotEmpty()) {
        try {
            exercisesString.split("|").map { exerciseStr ->
                val parts = exerciseStr.split(",")
                if (parts.size >= 3) {
                    GeneratedExercise(
                        name = parts[0],
                        sets = parts[1].toIntOrNull() ?: 3,
                        reps = parts[2].toIntOrNull() ?: 10
                    )
                } else {
                    GeneratedExercise("Unknown Exercise", 3, 10)
                }
            }
        } catch (e: Exception) {
            listOf(
                GeneratedExercise("Push-ups", 3, 10),
                GeneratedExercise("Squats", 3, 15),
                GeneratedExercise("Lunges", 3, 12),
                GeneratedExercise("Plank", 3, 45)
            )
        }
    } else {
        listOf(
            GeneratedExercise("Push-ups", 3, 10),
            GeneratedExercise("Squats", 3, 15),
            GeneratedExercise("Lunges", 3, 12),
            GeneratedExercise("Plank", 3, 45)
        )
    }
}

// SessionExercise is defined in WorkoutSessionViewModel
