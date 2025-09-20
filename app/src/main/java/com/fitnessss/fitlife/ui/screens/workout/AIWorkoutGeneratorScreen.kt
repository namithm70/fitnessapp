package com.fitnessss.fitlife.ui.screens.workout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.navigation.Screen
import com.fitnessss.fitlife.data.model.GeneratedWorkout
import com.fitnessss.fitlife.data.model.GeneratedExercise
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIWorkoutGeneratorScreen(
    navController: NavController
) {
    var generatedWorkouts by remember { mutableStateOf(listOf<GeneratedWorkout>()) }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PageHeaderWithThemeToggle(title = "AI Workout Generator")
        
        // AI Toggle Button (same as theme toggle)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { /* AI toggle functionality */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "AI Workout Generator",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Generate Button
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SmartToy,
                            contentDescription = "AI",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "AI Workout Generator",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Generate personalized workout routines with AI",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                isGenerating = true
                                // Simulate AI generation delay
                                scope.launch {
                                    kotlinx.coroutines.delay(1500) // 1.5 second delay
                                    generatedWorkouts = generateRandomWorkouts()
                                    isGenerating = false
                                }
                            },
                            enabled = !isGenerating,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generating...")
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Refresh,
                                    contentDescription = "Generate",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate Workout")
                            }
                        }
                    }
                }
            }
            
            // Generated Workouts
            items(generatedWorkouts) { workout ->
                GeneratedWorkoutCard(
                    workout = workout,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun GeneratedWorkoutCard(
    workout: GeneratedWorkout,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Workout Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = workout.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = workout.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    imageVector = workout.icon,
                    contentDescription = workout.type,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Workout Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutDetailItem(
                    icon = Icons.Filled.Timer,
                    label = "Duration",
                    value = "${workout.duration} min"
                )
                WorkoutDetailItem(
                    icon = Icons.Filled.FitnessCenter,
                    label = "Exercises",
                    value = "${workout.exercises.size}"
                )
                WorkoutDetailItem(
                    icon = Icons.Filled.LocalFireDepartment,
                    label = "Calories",
                    value = "${workout.estimatedCalories}"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Exercises List
            Text(
                text = "Exercises",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            workout.exercises.forEach { exercise ->
                ExerciseItem(exercise = exercise)
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Start Workout Button
            Button(
                onClick = {
                    // Convert exercises to string format for navigation
                    val exercisesString = workout.exercises.joinToString("|") { exercise ->
                        "${exercise.name},${exercise.sets},${exercise.reps}"
                    }
                    
                    // Navigate to AI workout session
                    navController.navigate(
                        "${Screen.AIWorkoutSession.route}?workoutName=${workout.name}&workoutType=${workout.type}&duration=${workout.duration}&calories=${workout.estimatedCalories}&exercises=${exercisesString}"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Workout")
            }
        }
    }
}

@Composable
fun WorkoutDetailItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ExerciseItem(exercise: GeneratedExercise) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.FitnessCenter,
            contentDescription = "Exercise",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "${exercise.sets} sets × ${exercise.reps} reps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data Classes moved to com.fitnessss.fitlife.data.model.GeneratedWorkout

// Workout Generation Logic
fun generateRandomWorkouts(): List<GeneratedWorkout> {
    val workoutTypes = listOf(
        Triple("Full Body Blast", "Strength", Icons.Filled.FitnessCenter),
        Triple("Cardio Crush", "Cardio", Icons.Filled.DirectionsRun),
        Triple("Upper Power", "Strength", Icons.Filled.SportsGymnastics),
        Triple("Lower Focus", "Strength", Icons.Filled.DirectionsWalk),
        Triple("Core Crusher", "Strength", Icons.Filled.SelfImprovement),
        Triple("HIIT Circuit", "Cardio", Icons.Filled.Timer),
        Triple("Strength Builder", "Strength", Icons.Filled.MonitorWeight),
        Triple("Endurance Boost", "Cardio", Icons.Filled.DirectionsRun),
        Triple("Power Hour", "Strength", Icons.Filled.FitnessCenter),
        Triple("Quick Burn", "Cardio", Icons.Filled.LocalFireDepartment)
    )
    
    val exerciseDatabase = listOf(
        "Push-ups", "Pull-ups", "Squats", "Lunges", "Plank", "Burpees",
        "Mountain Climbers", "Jumping Jacks", "High Knees", "Butterfly Kicks",
        "Diamond Push-ups", "Tricep Dips", "Pike Push-ups", "Wall Sit",
        "Jump Squats", "Lunge Jumps", "Bicycle Crunches", "Russian Twists", 
        "Leg Raises", "Superman", "Bird Dog", "Dead Bug", "Side Plank", 
        "Bear Crawl", "Spider-Man Push-ups", "Decline Push-ups",
        "Incline Push-ups", "Close Grip Push-ups", "Wide Push-ups",
        "Pistol Squats", "Handstand Push-ups", "Muscle-ups", "Dips",
        "Chin-ups", "Negative Pull-ups", "Assisted Pull-ups",
        "Pike Handstand Push-ups", "Wall Handstand", "L-Sit", "Tuck Planche"
    )
    
    // Generate exactly 3-4 unique workouts
    val numWorkouts = 3 + Random.nextInt(2) // 3 or 4 workouts
    val generatedWorkouts = mutableListOf<GeneratedWorkout>()
    val usedWorkoutNames = mutableSetOf<String>()
    
    while (generatedWorkouts.size < numWorkouts) {
        val availableWorkoutTypes = workoutTypes.filter { !usedWorkoutNames.contains(it.first) }
        
        if (availableWorkoutTypes.isEmpty()) {
            // If we run out of unique workout types, add a number to make them unique
            val baseWorkout = workoutTypes.random()
            val workoutNumber = (usedWorkoutNames.count { it.startsWith(baseWorkout.first) } + 1)
            val uniqueName = "${baseWorkout.first} $workoutNumber"
            
            if (!usedWorkoutNames.contains(uniqueName)) {
                val (_, type, icon) = baseWorkout
                val duration = listOf(20, 25, 30, 35, 40, 45).random()
                val estimatedCalories = (duration * 8) + Random.nextInt(50)
                
                val exercises = generateUniqueExercises(exerciseDatabase, type)
                generatedWorkouts.add(GeneratedWorkout(uniqueName, type, icon, duration, estimatedCalories, exercises))
                usedWorkoutNames.add(uniqueName)
            }
        } else {
            val (name, type, icon) = availableWorkoutTypes.random()
            val duration = listOf(20, 25, 30, 35, 40, 45).random()
            val estimatedCalories = (duration * 8) + Random.nextInt(50)
            
            val exercises = generateUniqueExercises(exerciseDatabase, type)
            generatedWorkouts.add(GeneratedWorkout(name, type, icon, duration, estimatedCalories, exercises))
            usedWorkoutNames.add(name)
        }
    }
    
    return generatedWorkouts
}

private fun generateUniqueExercises(exerciseDatabase: List<String>, workoutType: String): List<GeneratedExercise> {
    val numExercises = 4 + Random.nextInt(3) // 4-6 exercises
    val exercises = mutableListOf<GeneratedExercise>()
    val usedExercises = mutableSetOf<String>()
    
    while (exercises.size < numExercises) {
        val availableExercises = exerciseDatabase.filter { !usedExercises.contains(it) }
        
        if (availableExercises.isEmpty()) {
            // If we run out of unique exercises, add a variation
            val baseExercise = exerciseDatabase.random()
            val variation = listOf("Modified", "Advanced", "Variation", "Alternate").random()
            val uniqueExercise = "$baseExercise ($variation)"
            
            if (!usedExercises.contains(uniqueExercise)) {
                val sets = listOf(2, 3, 4).random()
                val reps = when (workoutType) {
                    "Cardio" -> listOf(15, 20, 25, 30).random()
                    else -> listOf(8, 10, 12, 15, 20).random()
                }
                exercises.add(GeneratedExercise(uniqueExercise, sets, reps))
                usedExercises.add(uniqueExercise)
            }
        } else {
            val exerciseName = availableExercises.random()
            val sets = listOf(2, 3, 4).random()
            val reps = when (workoutType) {
                "Cardio" -> listOf(15, 20, 25, 30).random()
                else -> listOf(8, 10, 12, 15, 20).random()
            }
            exercises.add(GeneratedExercise(exerciseName, sets, reps))
            usedExercises.add(exerciseName)
        }
    }
    
    return exercises
}
