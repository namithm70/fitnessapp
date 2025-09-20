package com.fitnessss.fitlife.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.FitnessGoal
import com.fitnessss.fitlife.data.FitnessLevel
import com.fitnessss.fitlife.data.Gender
import com.fitnessss.fitlife.data.UserProfileManager
import kotlinx.coroutines.launch

private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserPreferencesScreen(
    navController: NavController,
    onPreferencesComplete: () -> Unit = {},
    viewModel: UserPreferencesViewModel = hiltViewModel()
) {
    println("DEBUG: UserPreferencesScreen - Composable called")
    
    LaunchedEffect(Unit) {
        println("DEBUG: UserPreferencesScreen - LaunchedEffect triggered")
    }
    var fitnessGoal by remember { mutableStateOf(FitnessGoal.GENERAL_FITNESS) }
    var fitnessLevel by remember { mutableStateOf(FitnessLevel.BEGINNER) }
    var workoutsPerWeek by remember { mutableStateOf("3") }
    var gender by remember { mutableStateOf(Gender.OTHER) }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    
    // Dropdown states
    var fitnessGoalExpanded by remember { mutableStateOf(false) }
    var fitnessLevelExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var workoutsPerWeekExpanded by remember { mutableStateOf(false) }
    var ageExpanded by remember { mutableStateOf(false) }
    var heightExpanded by remember { mutableStateOf(false) }
    var weightExpanded by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        viewModel.preferencesState.collect { state ->
            when (state) {
                is PreferencesState.Success -> {
                    onPreferencesComplete()
                }
                is PreferencesState.Error -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(state.message)
                    }
                }
                is PreferencesState.Loading -> {
                    isLoading = true
                }
                else -> {
                    isLoading = false
                }
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PageHeaderWithThemeToggle(title = "Setup Your Profile")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = "Setup Profile",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Let's Personalize Your Experience",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Help us customize your fitness journey by providing some basic information",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Fitness Goal
                    Text(
                        text = "What's your primary fitness goal?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = fitnessGoalExpanded,
                        onExpandedChange = { fitnessGoalExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = fitnessGoal.name.replace("_", " ").capitalize(),
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fitnessGoalExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Fitness Goal") }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = fitnessGoalExpanded,
                            onDismissRequest = { fitnessGoalExpanded = false }
                        ) {
                            FitnessGoal.values().forEach { goal ->
                                DropdownMenuItem(
                                    text = { Text(goal.name.replace("_", " ").capitalize()) },
                                    onClick = { 
                                        fitnessGoal = goal
                                        fitnessGoalExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Fitness Level
                    Text(
                        text = "What's your current fitness level?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = fitnessLevelExpanded,
                        onExpandedChange = { fitnessLevelExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = fitnessLevel.name.capitalize(),
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fitnessLevelExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Fitness Level") }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = fitnessLevelExpanded,
                            onDismissRequest = { fitnessLevelExpanded = false }
                        ) {
                            FitnessLevel.values().forEach { level ->
                                DropdownMenuItem(
                                    text = { Text(level.name.capitalize()) },
                                    onClick = { 
                                        fitnessLevel = level
                                        fitnessLevelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Workouts per week
                    Text(
                        text = "How many workouts per week?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = workoutsPerWeekExpanded,
                        onExpandedChange = { workoutsPerWeekExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = "$workoutsPerWeek days",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workoutsPerWeekExpanded) },
                            leadingIcon = { Icon(Icons.Filled.FitnessCenter, "Workouts") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Workouts per Week") }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = workoutsPerWeekExpanded,
                            onDismissRequest = { workoutsPerWeekExpanded = false }
                        ) {
                            (1..7).forEach { days ->
                                DropdownMenuItem(
                                    text = { Text("$days days") },
                                    onClick = { 
                                        workoutsPerWeek = days.toString()
                                        workoutsPerWeekExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Personal Information Section
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Gender
                    Text(
                        text = "Gender",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = gender.name.capitalize(),
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Gender") }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            Gender.values().forEach { genderOption ->
                                DropdownMenuItem(
                                    text = { Text(genderOption.name.capitalize()) },
                                    onClick = { 
                                        gender = genderOption
                                        genderExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Age
                    Text(
                        text = "Age",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = ageExpanded,
                        onExpandedChange = { ageExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = if (age.isEmpty()) "Select Age" else "$age years",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ageExpanded) },
                            leadingIcon = { Icon(Icons.Filled.Person, "Age") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Age") }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = ageExpanded,
                            onDismissRequest = { ageExpanded = false }
                        ) {
                            (18..70).forEach { ageValue ->
                                DropdownMenuItem(
                                    text = { Text("$ageValue years") },
                                    onClick = { 
                                        age = ageValue.toString()
                                        ageExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Height
                    Text(
                        text = "Height",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = heightExpanded,
                        onExpandedChange = { heightExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = if (height.isEmpty()) "Select Height" else "${height}cm",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = heightExpanded) },
                            leadingIcon = { Icon(Icons.Filled.Height, "Height") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Height (cm)") }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = heightExpanded,
                            onDismissRequest = { heightExpanded = false }
                        ) {
                            (145..200).forEach { heightValue ->
                                DropdownMenuItem(
                                    text = { Text("${heightValue}cm") },
                                    onClick = { 
                                        height = heightValue.toString()
                                        heightExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Weight
                    Text(
                        text = "Weight",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = weightExpanded,
                        onExpandedChange = { weightExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = if (weight.isEmpty()) "Select Weight" else "${weight}kg",
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weightExpanded) },
                            leadingIcon = { Icon(Icons.Filled.MonitorWeight, "Weight") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            label = { Text("Weight (kg)") }
                        )
                        
                        ExposedDropdownMenu(
                            expanded = weightExpanded,
                            onDismissRequest = { weightExpanded = false }
                        ) {
                            (40..120).forEach { weightValue ->
                                DropdownMenuItem(
                                    text = { Text("${weightValue}kg") },
                                    onClick = { 
                                        weight = weightValue.toString()
                                        weightExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Save Button
                    Button(
                        onClick = {
                            viewModel.savePreferences(
                                fitnessGoal = fitnessGoal,
                                fitnessLevel = fitnessLevel,
                                workoutsPerWeek = workoutsPerWeek.toIntOrNull() ?: 3,
                                gender = gender,
                                age = age.toIntOrNull() ?: 25,
                                height = height.toDoubleOrNull() ?: 170.0,
                                weight = weight.toDoubleOrNull() ?: 70.0
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && age.isNotEmpty() && height.isNotEmpty() && weight.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Save Preferences & Continue")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Skip Button
                    TextButton(
                        onClick = { onPreferencesComplete() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Skip for now")
                    }
                }
            }
        }
    }
}
