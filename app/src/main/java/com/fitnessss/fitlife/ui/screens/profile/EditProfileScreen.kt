package com.fitnessss.fitlife.ui.screens.profile

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
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.data.UserProfileManager
import com.fitnessss.fitlife.data.FitnessGoal
import com.fitnessss.fitlife.data.FitnessLevel
import com.fitnessss.fitlife.data.Gender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavController) {
    val userProfile = UserProfileManager.userProfile
    
    var name by remember { mutableStateOf(userProfile.name) }
    var email by remember { mutableStateOf(userProfile.email) }
    var age by remember { mutableStateOf(if (userProfile.age > 0) userProfile.age.toString() else "") }
    var height by remember { mutableStateOf(if (userProfile.height > 0) userProfile.height.toString() else "") }
    var weight by remember { mutableStateOf(if (userProfile.weight > 0) userProfile.weight.toString() else "") }
    var fitnessGoal by remember { mutableStateOf(userProfile.fitnessGoal) }
    var fitnessLevel by remember { mutableStateOf(userProfile.fitnessLevel) }
    var workoutDays by remember { mutableStateOf(userProfile.workoutDaysPerWeek.toString()) }
    var bio by remember { mutableStateOf(userProfile.bio) }
    var location by remember { mutableStateOf(userProfile.location) }
    var gender by remember { mutableStateOf(userProfile.gender) }
    
    var showSaveSuccess by remember { mutableStateOf(false) }
    var fitnessGoalExpanded by remember { mutableStateOf(false) }
    var fitnessLevelExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        // Save profile data
                        UserProfileManager.updateName(name)
                        UserProfileManager.updateEmail(email)
                        UserProfileManager.updateAge(age.toIntOrNull() ?: 0)
                        UserProfileManager.updateHeight(height.toDoubleOrNull() ?: 0.0)
                        UserProfileManager.updateWeight(weight.toDoubleOrNull() ?: 0.0)
                        UserProfileManager.updateFitnessGoal(fitnessGoal)
                        UserProfileManager.updateFitnessLevel(fitnessLevel)
                        UserProfileManager.updateWorkoutDays(workoutDays.toIntOrNull() ?: 3)
                        UserProfileManager.updateBio(bio)
                        UserProfileManager.updateLocation(location)
                        UserProfileManager.updateGender(gender)
                        
                        // Show success message
                        scope.launch {
                            snackbarHostState.showSnackbar("Profile saved successfully!")
                        }
                        
                        // Navigate back after a short delay
                        scope.launch {
                            kotlinx.coroutines.delay(1000)
                            navController.navigateUp()
                        }
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Profile photo section
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile photo placeholder
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.large
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { /* Take photo */ }
                            ) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Take Photo")
                            }
                            OutlinedButton(
                                onClick = { /* Choose from gallery */ }
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Gallery")
                            }
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                // Basic info form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Filled.Person, contentDescription = null)
                            }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Filled.Email, contentDescription = null)
                            }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            OutlinedTextField(
                                value = age,
                                onValueChange = { age = it },
                                label = { Text("Age") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Filled.Person, contentDescription = null)
                                }
                            )
                            
                            OutlinedTextField(
                                value = height,
                                onValueChange = { height = it },
                                label = { Text("Height") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = {
                                    Icon(Icons.Filled.Straighten, contentDescription = null)
                                }
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = weight,
                            onValueChange = { weight = it },
                            label = { Text("Weight (lbs)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Filled.Scale, contentDescription = null)
                            }
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                Text(
                    text = "Fitness Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                // Fitness preferences form
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Fitness goal dropdown
                        ExposedDropdownMenuBox(
                            expanded = fitnessGoalExpanded,
                            onExpandedChange = { fitnessGoalExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = fitnessGoal.name.replace("_", " ").capitalize(),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Fitness Goal") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fitnessGoalExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                leadingIcon = {
                                    Icon(Icons.Filled.Flag, contentDescription = null)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = fitnessGoalExpanded,
                                onDismissRequest = { fitnessGoalExpanded = false }
                            ) {
                                FitnessGoal.values().forEach { goal ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(goal.name.replace("_", " ").capitalize()) },
                                        onClick = {
                                            fitnessGoal = goal
                                            fitnessGoalExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Fitness level dropdown
                        ExposedDropdownMenuBox(
                            expanded = fitnessLevelExpanded,
                            onExpandedChange = { fitnessLevelExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = fitnessLevel.name.capitalize(),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Fitness Level") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fitnessLevelExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                leadingIcon = {
                                    Icon(Icons.Filled.FitnessCenter, contentDescription = null)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = fitnessLevelExpanded,
                                onDismissRequest = { fitnessLevelExpanded = false }
                            ) {
                                FitnessLevel.values().forEach { level ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(level.name.capitalize()) },
                                        onClick = {
                                            fitnessLevel = level
                                            fitnessLevelExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Workout days per week
                        OutlinedTextField(
                            value = workoutDays,
                            onValueChange = { workoutDays = it },
                            label = { Text("Workout Days per Week") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Filled.Schedule, contentDescription = null)
                            }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Bio
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Filled.Description, contentDescription = null)
                            },
                            minLines = 3,
                            maxLines = 5
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Location
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            label = { Text("Location") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Filled.LocationOn, contentDescription = null)
                            }
                        )
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Gender dropdown
                        ExposedDropdownMenuBox(
                            expanded = genderExpanded,
                            onExpandedChange = { genderExpanded = it },
                        ) {
                            OutlinedTextField(
                                value = gender.name.capitalize(),
                                onValueChange = { },
                                readOnly = true,
                                label = { Text("Gender") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                leadingIcon = {
                                    Icon(Icons.Filled.Person, contentDescription = null)
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = genderExpanded,
                                onDismissRequest = { genderExpanded = false }
                            ) {
                                Gender.values().forEach { genderOption ->
                                    androidx.compose.material3.DropdownMenuItem(
                                        text = { Text(genderOption.name.capitalize()) },
                                        onClick = {
                                            gender = genderOption
                                            genderExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                Text(
                    text = "Privacy Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            item {
                // Privacy settings
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Public Profile",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Allow others to see your profile",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = { }
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Progress Sharing",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Share progress with community",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = false,
                                onCheckedChange = { }
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Push Notifications",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Receive workout reminders",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = true,
                                onCheckedChange = { }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                // Action buttons
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = { /* Save changes */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Changes")
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { /* Reset to defaults */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset to Defaults")
                    }
                }
            }
            
            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}
