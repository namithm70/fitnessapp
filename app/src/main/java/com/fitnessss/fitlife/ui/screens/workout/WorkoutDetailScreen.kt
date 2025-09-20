package com.fitnessss.fitlife.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(navController: NavController, workoutId: String) {
    var isWorkoutStarted by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upper Body Strength") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Share workout */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { /* Add to favorites */ }) {
                        Icon(Icons.Filled.FavoriteBorder, contentDescription = "Favorite")
                    }
                }
            )
        },
        bottomBar = {
            if (!isWorkoutStarted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Preview workout */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Preview")
                        }
                        Button(
                            onClick = { isWorkoutStarted = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.SportsGymnastics, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Start Workout")
                        }
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Workout header info
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Upper Body Strength",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WorkoutInfoItem(
                                icon = Icons.Filled.Schedule,
                                label = "Duration",
                                value = "45 min"
                            )
                            WorkoutInfoItem(
                                icon = Icons.Filled.Restaurant,
                                label = "Calories",
                                value = "320"
                            )
                            WorkoutInfoItem(
                                icon = Icons.Filled.SportsGymnastics,
                                label = "Difficulty",
                                value = "Intermediate"
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Text(
                            text = "Target muscle groups: Chest, Shoulders, Triceps, Biceps",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            item {
                Text(
                    text = "Exercises (${sampleExercises.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            items(sampleExercises) { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    isWorkoutStarted = isWorkoutStarted
                )
            }
            
            item {
                Spacer(Modifier.height(100.dp)) // Bottom padding for FAB
            }
        }
    }
}

@Composable
fun WorkoutInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseCard(
    exercise: SampleExercise,
    isWorkoutStarted: Boolean
) {
    var isCompleted by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                MaterialTheme.colorScheme.primaryContainer
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise number or completion status
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isCompleted) 
                            MaterialTheme.colorScheme.primary
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = exercise.number.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Exercise details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${exercise.sets} sets × ${exercise.reps} reps",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (exercise.rest.isNotEmpty()) {
                    Text(
                        text = "Rest: ${exercise.rest}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            if (isWorkoutStarted) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { /* Show exercise demo */ }
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Demo")
                    }
                    IconButton(
                        onClick = { isCompleted = !isCompleted }
                    ) {
                        Icon(
                            if (isCompleted) Icons.Filled.Undo else Icons.Filled.Check,
                            contentDescription = if (isCompleted) "Undo" else "Complete"
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = { /* Show exercise details */ }
                ) {
                    Icon(Icons.Filled.Info, contentDescription = "Details")
                }
            }
        }
    }
}

data class SampleExercise(
    val number: Int,
    val name: String,
    val sets: Int,
    val reps: String,
    val rest: String
)

private val sampleExercises = listOf(
    SampleExercise(1, "Push-ups", 3, "10-12", "60s"),
    SampleExercise(2, "Dumbbell Bench Press", 3, "8-10", "90s"),
    SampleExercise(3, "Overhead Press", 3, "8-10", "90s"),
    SampleExercise(4, "Dumbbell Rows", 3, "10-12", "60s"),
    SampleExercise(5, "Tricep Dips", 3, "8-10", "60s"),
    SampleExercise(6, "Bicep Curls", 3, "10-12", "60s")
)
