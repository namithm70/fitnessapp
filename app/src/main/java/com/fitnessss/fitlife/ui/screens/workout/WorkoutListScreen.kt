package com.fitnessss.fitlife.ui.screens.workout

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
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabPosition

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.navigation.Screen

import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.UserProgressManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun WorkoutListScreen(navController: NavController, onMenuClick: () -> Unit = {}) {
    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PageHeaderWithThemeToggle(title = "Workouts")
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Workouts") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Exercises") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("My Routines") }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> WorkoutsSection(navController, snackbarHostState)
                1 -> ExercisesSection(navController, snackbarHostState)
                2 -> MyRoutinesSection(navController, snackbarHostState)
            }
        }
    }
}

@Composable
fun WorkoutsSection(navController: NavController, snackbarHostState: SnackbarHostState) {

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Start
        item {
            QuickStartSection(navController, snackbarHostState)
        }

        // Workout Categories
        item {
                Text(

                text = "Workout Categories",
                    style = MaterialTheme.typography.titleLarge,

                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            WorkoutCategoriesGrid(navController, snackbarHostState)
            }
            
            // Featured Workouts
        item {
            Text(
                text = "Featured Workouts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            FeaturedWorkoutsList(navController, snackbarHostState)
        }

        // Quick Workouts
        item {
            Text(
                text = "Quick Workouts (15-30 min)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            QuickWorkoutsList(navController, snackbarHostState)
        }
    }
}

@Composable
fun ExercisesSection(navController: NavController, snackbarHostState: SnackbarHostState) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedMuscleGroup by remember { mutableStateOf("All") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },

                label = { Text("Search exercises...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
        }

        // Muscle Group Filters
        item {
            Text(
                text = "Muscle Groups",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            MuscleGroupFilters(selectedMuscleGroup) { selectedMuscleGroup = it }
        }

        // Exercise Categories
        item {
            Text(
                text = "Exercise Categories",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ExerciseCategoriesGrid(navController, snackbarHostState)
        }

        // Exercise Database
        item {
            Text(
                text = "Exercise Database",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            ExercisesList(navController, snackbarHostState, selectedMuscleGroup, searchQuery)
        }
    }
}

@Composable
fun MyRoutinesSection(navController: NavController, snackbarHostState: SnackbarHostState) {
            LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // My Routines
        item {
            Text(
                text = "My Custom Routines",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            CustomRoutinesList(navController, snackbarHostState)
        }

        // Recent Workouts
        item {
            Text(

                text = "Recent Workouts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            RecentWorkoutsList(navController, snackbarHostState)
        }
    }
}

@Composable
fun QuickStartSection(navController: NavController, snackbarHostState: SnackbarHostState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ready to Workout?",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
            Text(
                        text = "Start your fitness journey now!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = "Fitness",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=quick_start") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Quick Start")
                }
                OutlinedButton(
                    onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=custom") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Build, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Custom")
                }
            }
        }
    }
}

@Composable

fun WorkoutCategoriesGrid(navController: NavController, snackbarHostState: SnackbarHostState) {
    val categories = listOf(
        WorkoutCategory("Strength", Icons.Filled.FitnessCenter),
        WorkoutCategory("Cardio", Icons.Filled.DirectionsRun),
        WorkoutCategory("HIIT", Icons.Filled.LocalFireDepartment),
        WorkoutCategory("Yoga", Icons.Filled.SelfImprovement),
        WorkoutCategory("Pilates", Icons.Filled.Accessibility),
        WorkoutCategory("CrossFit", Icons.Filled.Sports),
        WorkoutCategory("Calisthenics", Icons.Filled.SportsGymnastics),
        WorkoutCategory("Stretching", Icons.Filled.Accessible)
    )
    
    categories.chunked(4).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { category ->
                WorkoutCategoryCard(
                    modifier = Modifier.weight(1f),
                    category = category,
                    onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${category.name.lowercase()}") }
                )
            }
            repeat(4 - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
fun WorkoutCategoryCard(
    modifier: Modifier = Modifier,
    category: WorkoutCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f), // Square aspect ratio for consistency
            onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FeaturedWorkoutsList(navController: NavController, snackbarHostState: SnackbarHostState) {
    val featuredWorkouts = listOf(
        WorkoutPlan("Full Body Blast", "45 min", "Intermediate", Icons.Filled.FitnessCenter, "Complete workout targeting all muscle groups"),
        WorkoutPlan("Morning Cardio", "30 min", "Beginner", Icons.Filled.DirectionsRun, "Start your day with energy-boosting cardio"),
        WorkoutPlan("HIIT Burn", "20 min", "Advanced", Icons.Filled.LocalFireDepartment, "High-intensity interval training for fat loss"),
        WorkoutPlan("Yoga Flow", "60 min", "All Levels", Icons.Filled.SelfImprovement, "Relaxing yoga sequence for flexibility")
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        featuredWorkouts.forEach { workout ->
            FeaturedWorkoutCard(
                workout = workout,
                onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${workout.name.replace(" ", "_").lowercase()}") }
            )
        }
    }
}

@Composable
fun FeaturedWorkoutCard(
    workout: WorkoutPlan,
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
                        imageVector = workout.icon,
                        contentDescription = workout.name,

                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    WorkoutInfoChip(Icons.Filled.Timer, workout.duration)
                    WorkoutInfoChip(Icons.Filled.TrendingUp, workout.difficulty)
                }
            }
                Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Start workout",
                tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
}

@Composable
fun WorkoutInfoChip(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun QuickWorkoutsList(navController: NavController, snackbarHostState: SnackbarHostState) {
    val quickWorkouts = listOf(
        WorkoutPlan("7-Minute Workout", "7 min", "Beginner", Icons.Filled.Timer, "Quick total body workout"),
        WorkoutPlan("Abs Blast", "15 min", "Intermediate", Icons.Filled.LocalFireDepartment, "Core strengthening routine"),
        WorkoutPlan("Desk Stretches", "10 min", "All Levels", Icons.Filled.SelfImprovement, "Perfect for office breaks"),
        WorkoutPlan("Power Cardio", "20 min", "Advanced", Icons.Filled.DirectionsRun, "High-energy cardio session")
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(quickWorkouts) { workout ->
            QuickWorkoutCard(
                workout = workout,
                onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${workout.name.replace(" ", "_").lowercase()}") }
            )
        }
    }
}

@Composable
fun QuickWorkoutCard(
    workout: WorkoutPlan,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .height(160.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header with icon and play button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = workout.icon,
                    contentDescription = workout.name,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start workout",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Content section
            Column {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = workout.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            // Bottom chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AssistChip(
                    onClick = { onClick() },
                    label = { 
                        Text(
                            workout.duration, 
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        ) 
                    },
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { onClick() },
                    label = { 
                        Text(
                            workout.difficulty, 
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        ) 
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun MuscleGroupFilters(selectedGroup: String, onGroupSelected: (String) -> Unit) {
    val muscleGroups = listOf(
        "All", "Chest", "Back", "Shoulders", "Arms", "Legs", "Core", "Glutes", "Cardio"
    )
    
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(muscleGroups) { group ->
            FilterChip(
                onClick = { onGroupSelected(group) },
                label = { Text(group) },
                selected = selectedGroup == group
            )
        }
    }
}

@Composable
fun ExerciseCategoriesGrid(navController: NavController, snackbarHostState: SnackbarHostState) {
    val categories = listOf(
        ExerciseCategory("Chest", Icons.Filled.FitnessCenter),
        ExerciseCategory("Back", Icons.Filled.SportsGymnastics),
        ExerciseCategory("Shoulders", Icons.Filled.Accessibility),
        ExerciseCategory("Arms", Icons.Filled.SportsHandball),
        ExerciseCategory("Legs", Icons.Filled.DirectionsRun),
        ExerciseCategory("Core", Icons.Filled.CenterFocusWeak),
        ExerciseCategory("Glutes", Icons.Filled.SportsGymnastics),
        ExerciseCategory("Cardio", Icons.Filled.MonitorHeart)
    )
    
    categories.chunked(4).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            row.forEach { category ->
                ExerciseCategoryCard(
                    modifier = Modifier.weight(1f),
                    category = category,
                    onClick = { /* Filter exercises by category */ }
                )
            }
            repeat(4 - row.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
                Spacer(modifier = Modifier.height(8.dp))
                
    }
}

@Composable
fun ExerciseCategoryCard(
    modifier: Modifier = Modifier,
    category: ExerciseCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1f), // Square aspect ratio for consistency
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ExercisesList(navController: NavController, snackbarHostState: SnackbarHostState, selectedMuscleGroup: String, searchQuery: String = "") {
    var showTimer by remember { mutableStateOf(false) }
    var timerDuration by remember { mutableStateOf(30) }
    var isTimerRunning by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(timerDuration) }
    val scope = rememberCoroutineScope()
    
    // Timer effect
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
            }
            isTimerRunning = false
            showTimer = false
            scope.launch {
                snackbarHostState.showSnackbar("Timer finished!")
            }
        }
    }
    val exercises = listOf(
        // Upper Body - Bodyweight
        Exercise("Push-ups", "Chest, Shoulders, Triceps", "Bodyweight", Icons.Filled.FitnessCenter, "3 sets x 10-15 reps"),
        Exercise("Pull-ups", "Back, Biceps", "Bodyweight", Icons.Filled.SportsGymnastics, "3 sets x 5-10 reps"),
        Exercise("Diamond Push-ups", "Triceps, Chest", "Bodyweight", Icons.Filled.FitnessCenter, "3 sets x 8-12 reps"),
        Exercise("Pike Push-ups", "Shoulders, Triceps", "Bodyweight", Icons.Filled.SportsGymnastics, "3 sets x 6-10 reps"),
        Exercise("Tricep Dips", "Triceps, Shoulders", "Bodyweight", Icons.Filled.FitnessCenter, "3 sets x 10-15 reps"),
        Exercise("Wide Push-ups", "Chest, Shoulders", "Bodyweight", Icons.Filled.FitnessCenter, "3 sets x 10-15 reps"),
        Exercise("Close Push-ups", "Triceps, Chest", "Bodyweight", Icons.Filled.FitnessCenter, "3 sets x 8-12 reps"),
        Exercise("Decline Push-ups", "Upper Chest, Shoulders", "Bodyweight", Icons.Filled.FitnessCenter, "3 sets x 8-12 reps"),
        Exercise("Incline Push-ups", "Lower Chest, Triceps", "Bodyweight", Icons.Filled.FitnessCenter, "3 sets x 12-15 reps"),
        Exercise("Chin-ups", "Back, Biceps", "Bodyweight", Icons.Filled.SportsGymnastics, "3 sets x 5-10 reps"),
        Exercise("Negative Pull-ups", "Back, Biceps", "Bodyweight", Icons.Filled.SportsGymnastics, "3 sets x 5-8 reps"),
        Exercise("Assisted Pull-ups", "Back, Biceps", "Bodyweight", Icons.Filled.SportsGymnastics, "3 sets x 8-12 reps"),
        Exercise("Pike Handstand Push-ups", "Shoulders, Triceps", "Bodyweight", Icons.Filled.SportsGymnastics, "3 sets x 3-8 reps"),
        Exercise("Wall Handstand", "Shoulders, Core", "Bodyweight", Icons.Filled.SportsGymnastics, "Hold 30-60 seconds"),
        Exercise("L-Sit", "Core, Shoulders", "Bodyweight", Icons.Filled.SportsGymnastics, "Hold 10-30 seconds"),
        Exercise("Tuck Planche", "Shoulders, Core", "Bodyweight", Icons.Filled.SportsGymnastics, "Hold 5-15 seconds"),
        
        // Lower Body - Bodyweight  
        Exercise("Squats", "Legs, Glutes", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 15-20 reps"),
        Exercise("Lunges", "Legs, Glutes", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 10-15 per leg"),
        Exercise("Jump Squats", "Legs, Glutes, Power", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 12-15 reps"),
        Exercise("Single Leg Squats", "Legs, Balance", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 5-8 per leg"),
        Exercise("Calf Raises", "Calves", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 15-20 reps"),
        Exercise("Glute Bridges", "Glutes, Core", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 15-20 reps"),
        Exercise("Pistol Squats", "Legs, Balance", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 3-8 per leg"),
        Exercise("Bulgarian Split Squats", "Legs, Glutes", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 8-12 per leg"),
        Exercise("Step-ups", "Legs, Glutes", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 10-15 per leg"),
        Exercise("Wall Sit", "Legs, Glutes", "Bodyweight", Icons.Filled.DirectionsRun, "Hold 30-60 seconds"),
        Exercise("Single Leg Calf Raises", "Calves", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 10-15 per leg"),
        Exercise("Donkey Kicks", "Glutes, Hamstrings", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 15-20 per leg"),
        Exercise("Fire Hydrants", "Glutes, Hips", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 15-20 per leg"),
        Exercise("Clamshells", "Glutes, Hips", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 15-20 per leg"),
        Exercise("Hip Thrusts", "Glutes, Hamstrings", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 12-15 reps"),
        Exercise("Single Leg Glute Bridge", "Glutes, Core", "Bodyweight", Icons.Filled.DirectionsRun, "3 sets x 10-15 per leg"),
        
        // Core
        Exercise("Plank", "Core", "Bodyweight", Icons.Filled.Timer, "3 sets x 30-60 seconds"),
        Exercise("Side Plank", "Core, Obliques", "Bodyweight", Icons.Filled.Timer, "3 sets x 20-30 seconds each"),
        Exercise("Russian Twists", "Core, Obliques", "Bodyweight", Icons.Filled.Timer, "3 sets x 20-30 reps"),
        Exercise("Bicycle Crunches", "Core, Obliques", "Bodyweight", Icons.Filled.Timer, "3 sets x 20-30 reps"),
        Exercise("Dead Bug", "Core, Stability", "Bodyweight", Icons.Filled.Timer, "3 sets x 10-12 per side"),
        Exercise("Bird Dog", "Core, Back", "Bodyweight", Icons.Filled.Timer, "3 sets x 10-12 per side"),
        Exercise("Mountain Climbers", "Core, Cardio", "Bodyweight", Icons.Filled.Timer, "3 sets x 20-30 reps"),
        Exercise("Plank Jacks", "Core, Cardio", "Bodyweight", Icons.Filled.Timer, "3 sets x 15-20 reps"),
        Exercise("Spider Plank", "Core, Obliques", "Bodyweight", Icons.Filled.Timer, "3 sets x 10-15 per side"),
        Exercise("Plank to Downward Dog", "Core, Shoulders", "Bodyweight", Icons.Filled.Timer, "3 sets x 8-12 reps"),
        Exercise("Reverse Crunches", "Core", "Bodyweight", Icons.Filled.Timer, "3 sets x 15-20 reps"),
        Exercise("Leg Raises", "Core, Hip Flexors", "Bodyweight", Icons.Filled.Timer, "3 sets x 10-15 reps"),
        Exercise("Scissor Kicks", "Core, Hip Flexors", "Bodyweight", Icons.Filled.Timer, "3 sets x 20-30 reps"),
        Exercise("Flutter Kicks", "Core, Hip Flexors", "Bodyweight", Icons.Filled.Timer, "3 sets x 20-30 reps"),
        Exercise("Hollow Hold", "Core", "Bodyweight", Icons.Filled.Timer, "Hold 20-45 seconds"),
        Exercise("Superman Hold", "Core, Back", "Bodyweight", Icons.Filled.Timer, "Hold 20-45 seconds"),
        Exercise("V-Ups", "Core", "Bodyweight", Icons.Filled.Timer, "3 sets x 10-15 reps"),
        Exercise("Toe Touches", "Core, Hamstrings", "Bodyweight", Icons.Filled.Timer, "3 sets x 15-20 reps"),
        
        // Cardio/HIIT
        Exercise("Burpees", "Full Body", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 5-10 reps"),
        Exercise("Mountain Climbers", "Core, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 20-30 reps"),
        Exercise("Jumping Jacks", "Full Body, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 30-45 seconds"),
        Exercise("High Knees", "Cardio, Legs", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 30 seconds"),
        Exercise("Butt Kicks", "Cardio, Hamstrings", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 30 seconds"),
        Exercise("Plank Jacks", "Core, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 15-20 reps"),
        Exercise("Jump Rope", "Full Body, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 30-60 seconds"),
        Exercise("Mountain Climbers", "Core, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 20-30 reps"),
        Exercise("Sprint in Place", "Cardio, Legs", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 30 seconds"),
        Exercise("Skater Jumps", "Cardio, Legs", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 15-20 reps"),
        Exercise("Tuck Jumps", "Cardio, Legs", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 10-15 reps"),
        Exercise("Star Jumps", "Full Body, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 15-20 reps"),
        Exercise("Burpee Pull-ups", "Full Body, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 5-8 reps"),
        Exercise("Man Makers", "Full Body, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 8-12 reps"),
        Exercise("Thrusters", "Full Body, Cardio", "Cardio", Icons.Filled.LocalFireDepartment, "3 sets x 10-15 reps"),
        
        // Weight Training
        Exercise("Bench Press", "Chest, Shoulders, Triceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Deadlift", "Back, Legs, Core", "Weight", Icons.Filled.MonitorWeight, "3 sets x 5-8 reps"),
        Exercise("Shoulder Press", "Shoulders, Triceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Barbell Rows", "Back, Biceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Lat Pulldowns", "Back, Biceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Leg Press", "Legs, Glutes", "Weight", Icons.Filled.MonitorWeight, "3 sets x 12-15 reps"),
        Exercise("Leg Curls", "Hamstrings", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Calf Raises (Weighted)", "Calves", "Weight", Icons.Filled.MonitorWeight, "3 sets x 15-20 reps"),
        Exercise("Squats (Barbell)", "Legs, Glutes", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Romanian Deadlift", "Hamstrings, Glutes", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Bicep Curls", "Biceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Tricep Extensions", "Triceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Lateral Raises", "Shoulders", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Front Raises", "Shoulders", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Upright Rows", "Shoulders, Traps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Shrugs", "Traps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 12-15 reps"),
        Exercise("Dumbbell Flyes", "Chest", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Incline Bench Press", "Upper Chest", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Decline Bench Press", "Lower Chest", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Dumbbell Rows", "Back", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("T-Bar Rows", "Back", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Seated Cable Rows", "Back", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Leg Extensions", "Quadriceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 12-15 reps"),
        Exercise("Leg Press (Single Leg)", "Legs", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 per leg"),
        Exercise("Hip Thrusts (Weighted)", "Glutes", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Good Mornings", "Hamstrings, Lower Back", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Overhead Press", "Shoulders", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Arnold Press", "Shoulders", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        Exercise("Hammer Curls", "Biceps, Forearms", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Preacher Curls", "Biceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Skull Crushers", "Triceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 10-12 reps"),
        Exercise("Diamond Push-ups (Weighted)", "Triceps", "Weight", Icons.Filled.MonitorWeight, "3 sets x 8-12 reps"),
        
        // Flexibility/Yoga
        Exercise("Downward Dog", "Full Body Stretch", "Yoga", Icons.Filled.SelfImprovement, "Hold 30-60 seconds"),
        Exercise("Warrior I", "Legs, Hip Flexors", "Yoga", Icons.Filled.SelfImprovement, "Hold 30 seconds each"),
        Exercise("Child's Pose", "Back, Shoulders", "Yoga", Icons.Filled.SelfImprovement, "Hold 60 seconds"),
        Exercise("Cat-Cow Stretch", "Spine, Core", "Yoga", Icons.Filled.SelfImprovement, "10-15 reps"),
        Exercise("Pigeon Pose", "Hips, Glutes", "Yoga", Icons.Filled.SelfImprovement, "Hold 30 seconds each"),
        Exercise("Cobra Pose", "Back, Chest", "Yoga", Icons.Filled.SelfImprovement, "Hold 30-60 seconds"),
        Exercise("Bridge Pose", "Back, Glutes", "Yoga", Icons.Filled.SelfImprovement, "Hold 30-60 seconds"),
        Exercise("Tree Pose", "Balance, Legs", "Yoga", Icons.Filled.SelfImprovement, "Hold 30 seconds each"),
        Exercise("Triangle Pose", "Legs, Hips", "Yoga", Icons.Filled.SelfImprovement, "Hold 30 seconds each"),
        Exercise("Warrior II", "Legs, Hips", "Yoga", Icons.Filled.SelfImprovement, "Hold 30 seconds each"),
        Exercise("Warrior III", "Balance, Legs", "Yoga", Icons.Filled.SelfImprovement, "Hold 15-30 seconds each"),
        Exercise("Half Moon Pose", "Balance, Legs", "Yoga", Icons.Filled.SelfImprovement, "Hold 15-30 seconds each"),
        Exercise("Crow Pose", "Arms, Core", "Yoga", Icons.Filled.SelfImprovement, "Hold 10-30 seconds"),
        Exercise("Headstand", "Arms, Core", "Yoga", Icons.Filled.SelfImprovement, "Hold 30-60 seconds"),
        Exercise("Handstand", "Arms, Core", "Yoga", Icons.Filled.SelfImprovement, "Hold 30-60 seconds"),
        Exercise("Sun Salutation", "Full Body", "Yoga", Icons.Filled.SelfImprovement, "5-10 rounds"),
        Exercise("Moon Salutation", "Full Body", "Yoga", Icons.Filled.SelfImprovement, "5-10 rounds"),
        Exercise("Seated Forward Bend", "Hamstrings, Back", "Yoga", Icons.Filled.SelfImprovement, "Hold 60 seconds"),
        Exercise("Butterfly Stretch", "Hips, Groin", "Yoga", Icons.Filled.SelfImprovement, "Hold 60 seconds"),
        Exercise("Happy Baby Pose", "Hips, Groin", "Yoga", Icons.Filled.SelfImprovement, "Hold 60 seconds"),
        Exercise("Corpse Pose", "Relaxation", "Yoga", Icons.Filled.SelfImprovement, "5-10 minutes"),
        
        // Functional Training
        Exercise("Turkish Get-ups", "Full Body, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 3-5 per side"),
        Exercise("Farmers Walk", "Grip, Core, Legs", "Functional", Icons.Filled.Accessibility, "3 sets x 30-60 seconds"),
        Exercise("Bear Crawl", "Full Body, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 20-30 steps"),
        Exercise("Box Jumps", "Legs, Power", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 reps"),
        Exercise("Battle Ropes", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 30 seconds"),
        Exercise("Kettlebell Swings", "Full Body, Power", "Functional", Icons.Filled.Accessibility, "3 sets x 15-20 reps"),
        Exercise("Kettlebell Snatch", "Full Body, Power", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 reps"),
        Exercise("Kettlebell Clean", "Full Body, Power", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 reps"),
        Exercise("Kettlebell Press", "Shoulders, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 reps"),
        Exercise("Kettlebell Squat", "Legs, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 10-15 reps"),
        Exercise("Kettlebell Deadlift", "Legs, Back", "Functional", Icons.Filled.Accessibility, "3 sets x 10-15 reps"),
        Exercise("Kettlebell Row", "Back, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 10-12 reps"),
        Exercise("Medicine Ball Slams", "Full Body, Power", "Functional", Icons.Filled.Accessibility, "3 sets x 10-15 reps"),
        Exercise("Medicine Ball Throws", "Full Body, Power", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 reps"),
        Exercise("Medicine Ball Squats", "Legs, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Rows", "Back, Biceps", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Press", "Chest, Shoulders", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Squats", "Legs, Glutes", "Functional", Icons.Filled.Accessibility, "3 sets x 15-20 reps"),
        Exercise("Resistance Band Deadlifts", "Legs, Back", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Lateral Walks", "Legs, Glutes", "Functional", Icons.Filled.Accessibility, "3 sets x 20 steps each"),
        Exercise("Resistance Band Clamshells", "Glutes, Hips", "Functional", Icons.Filled.Accessibility, "3 sets x 15-20 per leg"),
        Exercise("Resistance Band Glute Bridges", "Glutes, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 15-20 reps"),
        Exercise("Resistance Band Pull-aparts", "Shoulders, Upper Back", "Functional", Icons.Filled.Accessibility, "3 sets x 15-20 reps"),
        Exercise("Resistance Band Face Pulls", "Shoulders, Upper Back", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Bicep Curls", "Biceps", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Tricep Extensions", "Triceps", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Overhead Press", "Shoulders", "Functional", Icons.Filled.Accessibility, "3 sets x 10-12 reps"),
        Exercise("Resistance Band Chest Press", "Chest", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band Woodchops", "Core, Obliques", "Functional", Icons.Filled.Accessibility, "3 sets x 10-12 per side"),
        Exercise("Resistance Band Pallof Press", "Core, Anti-rotation", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 per side"),
        Exercise("Resistance Band Dead Bug", "Core, Stability", "Functional", Icons.Filled.Accessibility, "3 sets x 10-12 per side"),
        Exercise("Resistance Band Bird Dog", "Core, Back", "Functional", Icons.Filled.Accessibility, "3 sets x 10-12 per side"),
        Exercise("Resistance Band Plank", "Core", "Functional", Icons.Filled.Accessibility, "Hold 30-60 seconds"),
        Exercise("Resistance Band Side Plank", "Core, Obliques", "Functional", Icons.Filled.Accessibility, "Hold 20-30 seconds each"),
        Exercise("Resistance Band Mountain Climbers", "Core, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 20-30 reps"),
        Exercise("Resistance Band Burpees", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 5-10 reps"),
        Exercise("Resistance Band Jump Squats", "Legs, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 12-15 reps"),
        Exercise("Resistance Band High Knees", "Cardio, Legs", "Functional", Icons.Filled.Accessibility, "3 sets x 30 seconds"),
        Exercise("Resistance Band Butt Kicks", "Cardio, Hamstrings", "Functional", Icons.Filled.Accessibility, "3 sets x 30 seconds"),
        Exercise("Resistance Band Jumping Jacks", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 20-30 reps"),
        Exercise("Resistance Band Skater Jumps", "Cardio, Legs", "Functional", Icons.Filled.Accessibility, "3 sets x 15-20 reps"),
        Exercise("Resistance Band Tuck Jumps", "Cardio, Legs", "Functional", Icons.Filled.Accessibility, "3 sets x 10-15 reps"),
        Exercise("Resistance Band Star Jumps", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 15-20 reps"),
        Exercise("Resistance Band Burpee Pull-ups", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 5-8 reps"),
        Exercise("Resistance Band Man Makers", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 reps"),
        Exercise("Resistance Band Thrusters", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 10-15 reps"),
        Exercise("Resistance Band Turkish Get-ups", "Full Body, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 3-5 per side"),
        Exercise("Resistance Band Farmers Walk", "Grip, Core, Legs", "Functional", Icons.Filled.Accessibility, "3 sets x 30-60 seconds"),
        Exercise("Resistance Band Bear Crawl", "Full Body, Core", "Functional", Icons.Filled.Accessibility, "3 sets x 20-30 steps"),
        Exercise("Resistance Band Box Jumps", "Legs, Power", "Functional", Icons.Filled.Accessibility, "3 sets x 8-12 reps"),
        Exercise("Resistance Band Battle Ropes", "Full Body, Cardio", "Functional", Icons.Filled.Accessibility, "3 sets x 30 seconds")
    )
    
    // Filter exercises based on search query and muscle group
    val filteredExercises = exercises.filter { exercise ->
        val matchesSearch = searchQuery.isEmpty() || 
                           exercise.name.contains(searchQuery, ignoreCase = true) ||
                           exercise.muscles.contains(searchQuery, ignoreCase = true) ||
                           exercise.type.contains(searchQuery, ignoreCase = true)
        
        val matchesMuscleGroup = selectedMuscleGroup == "All" || 
                                exercise.muscles.contains(selectedMuscleGroup, ignoreCase = true) ||
                                exercise.type.contains(selectedMuscleGroup, ignoreCase = true)
        
        matchesSearch && matchesMuscleGroup
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Timer Card
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Timer,
                            contentDescription = "Timer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Exercise Timer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { showTimer = !showTimer }
                    ) {
                        Icon(
                            imageVector = if (showTimer) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (showTimer) "Hide timer" else "Show timer"
                        )
                    }
                }
                
                if (showTimer) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Timer Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isTimerRunning) {
                                    val minutes = timeRemaining / 60
                                    val seconds = timeRemaining % 60
                                    String.format("%02d:%02d", minutes, seconds)
                                } else {
                                    val minutes = timerDuration / 60
                                    val seconds = timerDuration % 60
                                    String.format("%02d:%02d", minutes, seconds)
                                },
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Timer Controls
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (!isTimerRunning) {
                                    // Duration selector
                                    OutlinedButton(
                                        onClick = { 
                                            timerDuration = when (timerDuration) {
                                                30 -> 60
                                                60 -> 90
                                                90 -> 120
                                                120 -> 180
                                                180 -> 300
                                                else -> 30
                                            }
                                            timeRemaining = timerDuration
                                        }
                                    ) {
                                        Text("${timerDuration}s")
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // Start button
                                    Button(
                                        onClick = { 
                                            isTimerRunning = true
                                            timeRemaining = timerDuration
                                        }
                                    ) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = "Start")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start")
                                    }
                                } else {
                                    // Stop button
                                    Button(
                                        onClick = { 
                                            isTimerRunning = false
                                            timeRemaining = timerDuration
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Filled.Stop, contentDescription = "Stop")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Stop")
                                    }
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    // Reset button
                                    OutlinedButton(
                                        onClick = { 
                                            isTimerRunning = false
                                            timeRemaining = timerDuration
                                        }
                                    ) {
                                        Icon(Icons.Filled.Refresh, contentDescription = "Reset")
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reset")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (filteredExercises.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = if (searchQuery.isNotEmpty()) 
                        "No exercises found for \"$searchQuery\"" 
                    else 
                        "No exercises found for selected filters",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            filteredExercises.forEach { exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${exercise.name.replace(" ", "_").lowercase()}") }
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = exercise.icon,
                contentDescription = exercise.name,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = exercise.muscles,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    text = exercise.recommendation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                AssistChip(
                    onClick = { onClick() },
                    label = { 
                        Text(
                            exercise.type, 
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        ) 
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Start exercise",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable

fun CustomRoutinesList(navController: NavController, snackbarHostState: SnackbarHostState) {
    val routines = listOf(
        WorkoutPlan("My Morning Routine", "30 min", "Custom", Icons.Filled.WbSunny, "Personal morning workout"),
        WorkoutPlan("Evening Strength", "45 min", "Custom", Icons.Filled.Nightlight, "Evening strength training"),
        WorkoutPlan("Quick HIIT", "15 min", "Custom", Icons.Filled.Timer, "Custom HIIT routine")
    )
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        routines.forEach { routine ->
            FeaturedWorkoutCard(
                workout = routine,
                onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${routine.name.replace(" ", "_").lowercase()}") }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedButton(
            onClick = { /* Create new routine */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Create routine")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Routine")
        }
    }
}

@Composable
fun RecentWorkoutsList(navController: NavController, snackbarHostState: SnackbarHostState) {
    // Use derivedStateOf to automatically recalculate when UserProgressManager changes
    val workoutSessions by remember { derivedStateOf { UserProgressManager.workoutSessions.value } }
    
    val recentWorkouts = workoutSessions
        .sortedByDescending { it.date }
        .take(5)
        .map { session ->
            val daysAgo = when {
                session.date.toLocalDate() == LocalDate.now() -> "Today"
                session.date.toLocalDate() == LocalDate.now().minusDays(1) -> "Yesterday"
                else -> {
                    val days = LocalDate.now().toEpochDay() - session.date.toLocalDate().toEpochDay()
                    "${days} days ago"
                }
            }
            WorkoutHistory(session.workoutName, daysAgo, "${session.duration} min", Icons.Filled.CheckCircle)
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
                RecentWorkoutCard(
                    workout = workout,
                    onClick = { navController.navigate(Screen.WorkoutSession.route + "?workoutId=${workout.name.replace(" ", "_").lowercase()}") }
                )
            }
        }
    }
}

@Composable
fun RecentWorkoutCard(
    workout: WorkoutHistory,
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
                imageVector = workout.statusIcon,
                contentDescription = "Workout completed",
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

                    text = "${workout.date} • ${workout.duration}",
            style = MaterialTheme.typography.bodySmall,

                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onClick) {
                Text("Repeat")
            }
        }
    }
}

// Data classes
data class WorkoutCategory(
    val name: String,
    val icon: ImageVector
)

data class ExerciseCategory(
    val name: String,
    val icon: ImageVector
)

data class WorkoutPlan(
    val name: String,

    val duration: String,
    val difficulty: String,

    val icon: ImageVector,
    val description: String
)

data class Exercise(
    val name: String,
    val muscles: String,
    val type: String,
    val icon: ImageVector,
    val recommendation: String
)

data class WorkoutHistory(
    val name: String,
    val date: String,
    val duration: String,
    val statusIcon: ImageVector
)
