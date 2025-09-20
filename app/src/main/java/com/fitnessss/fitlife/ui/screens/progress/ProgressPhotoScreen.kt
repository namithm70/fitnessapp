package com.fitnessss.fitlife.ui.screens.progress

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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotoScreen(navController: NavController) {
    var selectedPhotoType by remember { mutableStateOf("Front") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress Photos") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Take new photo */ }) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo")
                    }
                    IconButton(onClick = { /* Compare photos */ }) {
                        Icon(Icons.Filled.Analytics, contentDescription = "Compare")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Take new photo */ }
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Take Photo")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Photo type selector
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Photo Types",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            photoTypes.forEach { type ->
                                FilterChip(
                                    onClick = { selectedPhotoType = type },
                                    label = { Text(type) },
                                    selected = selectedPhotoType == type,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Progress summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Progress Summary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProgressSummaryItem(
                                label = "Total Photos",
                                value = "24",
                                icon = Icons.Filled.PhotoCamera
                            )
                            ProgressSummaryItem(
                                label = "Days Tracked",
                                value = "45",
                                icon = Icons.Filled.Schedule
                            )
                            ProgressSummaryItem(
                                label = "Last Photo",
                                value = "2 days ago",
                                icon = Icons.Filled.Schedule
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                Text(
                    text = "$selectedPhotoType View - Timeline",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            items(sampleProgressPhotos) { photo ->
                ProgressPhotoCard(
                    photo = photo,
                    onView = { /* View full photo */ },
                    onDelete = { /* Delete photo */ }
                )
            }
            
            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun ProgressSummaryItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
fun ProgressPhotoCard(
    photo: SampleProgressPhoto,
    onView: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Photo header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = photo.date,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${photo.daysAgo} days ago",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onView,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Visibility,
                            contentDescription = "View",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Photo placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = photo.photoType,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Photo details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PhotoDetailItem(
                    label = "Weight",
                    value = photo.weight
                )
                PhotoDetailItem(
                    label = "Body Fat %",
                    value = photo.bodyFat
                )
                PhotoDetailItem(
                    label = "Notes",
                    value = photo.notes
                )
            }
        }
    }
}

@Composable
fun PhotoDetailItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

data class SampleProgressPhoto(
    val date: String,
    val daysAgo: Int,
    val photoType: String,
    val weight: String,
    val bodyFat: String,
    val notes: String
)

private val photoTypes = listOf("Front", "Back", "Side", "Flexed")

private val sampleProgressPhotos = listOf(
    SampleProgressPhoto(
        date = "Dec 15, 2024",
        daysAgo = 2,
        photoType = "Front View",
        weight = "165 lbs",
        bodyFat = "12%",
        notes = "Feeling great!"
    ),
    SampleProgressPhoto(
        date = "Dec 8, 2024",
        daysAgo = 9,
        photoType = "Front View",
        weight = "167 lbs",
        bodyFat = "13%",
        notes = "Good progress"
    ),
    SampleProgressPhoto(
        date = "Dec 1, 2024",
        daysAgo = 16,
        photoType = "Front View",
        weight = "169 lbs",
        bodyFat = "14%",
        notes = "Starting to see changes"
    ),
    SampleProgressPhoto(
        date = "Nov 24, 2024",
        daysAgo = 23,
        photoType = "Front View",
        weight = "171 lbs",
        bodyFat = "15%",
        notes = "Baseline photo"
    )
)
