package com.fitnessss.fitlife.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementsScreen(navController: NavController) {
    var selectedMeasurement by remember { mutableStateOf("Weight") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Body Measurements") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add measurement */ }) {
                        Icon(Icons.Filled.Add, contentDescription = "Add")
                    }
                    IconButton(onClick = { /* Export data */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Export")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add new measurement */ }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Measurement")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Measurement type selector
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Measurement Types",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(measurementTypes) { type ->
                                FilterChip(
                                    onClick = { selectedMeasurement = type },
                                    label = { Text(type) },
                                    selected = selectedMeasurement == type
                                )
                            }
                        }
                    }
                }
            }
            
            item {
                // Current measurement summary
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Current $selectedMeasurement",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            MeasurementSummaryItem(
                                label = "Current",
                                value = getCurrentValue(selectedMeasurement),
                                icon = Icons.AutoMirrored.Filled.TrendingUp
                            )
                            MeasurementSummaryItem(
                                label = "Change",
                                value = getChangeValue(selectedMeasurement),
                                icon = Icons.AutoMirrored.Filled.TrendingUp
                            )
                            MeasurementSummaryItem(
                                label = "Goal",
                                value = getGoalValue(selectedMeasurement),
                                icon = Icons.Filled.Flag
                            )
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        // Progress bar placeholder
                        LinearProgressIndicator(
                            progress = 0.7f,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "70% to goal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                // Chart placeholder
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Analytics,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$selectedMeasurement Chart",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Chart will be implemented with Vico library",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                Text(
                    text = "Measurement History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            items(getMeasurementHistory(selectedMeasurement)) { measurement ->
                MeasurementHistoryCard(
                    measurement = measurement,
                    onEdit = { /* Edit measurement */ },
                    onDelete = { /* Delete measurement */ }
                )
            }
            
            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun MeasurementSummaryItem(
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
fun MeasurementHistoryCard(
    measurement: SampleMeasurement,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date and measurement info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = measurement.date,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${measurement.value} ${measurement.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (measurement.change.isNotEmpty()) {
                    Text(
                        text = measurement.change,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (measurement.change.startsWith("+")) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit",
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
    }
}

data class SampleMeasurement(
    val date: String,
    val value: String,
    val unit: String,
    val change: String
)

private val measurementTypes = listOf(
    "Weight", "Body Fat %", "Chest", "Waist", "Hips", "Arms", "Thighs", "Calves"
)

private fun getCurrentValue(measurement: String): String {
    return when (measurement) {
        "Weight" -> "165 lbs"
        "Body Fat %" -> "12%"
        "Chest" -> "42\""
        "Waist" -> "32\""
        "Hips" -> "38\""
        "Arms" -> "15\""
        "Thighs" -> "24\""
        "Calves" -> "16\""
        else -> "0"
    }
}

private fun getChangeValue(measurement: String): String {
    return when (measurement) {
        "Weight" -> "-2 lbs"
        "Body Fat %" -> "-1%"
        "Chest" -> "+1\""
        "Waist" -> "-1\""
        "Hips" -> "0\""
        "Arms" -> "+0.5\""
        "Thighs" -> "+0.5\""
        "Calves" -> "+0.25\""
        else -> "0"
    }
}

private fun getGoalValue(measurement: String): String {
    return when (measurement) {
        "Weight" -> "160 lbs"
        "Body Fat %" -> "10%"
        "Chest" -> "44\""
        "Waist" -> "30\""
        "Hips" -> "36\""
        "Arms" -> "16\""
        "Thighs" -> "25\""
        "Calves" -> "16.5\""
        else -> "0"
    }
}

private fun getMeasurementHistory(measurement: String): List<SampleMeasurement> {
    return when (measurement) {
        "Weight" -> listOf(
            SampleMeasurement("Dec 15, 2024", "165", "lbs", "-1 lb"),
            SampleMeasurement("Dec 8, 2024", "166", "lbs", "-1 lb"),
            SampleMeasurement("Dec 1, 2024", "167", "lbs", "-2 lbs"),
            SampleMeasurement("Nov 24, 2024", "169", "lbs", "-1 lb"),
            SampleMeasurement("Nov 17, 2024", "170", "lbs", "Baseline")
        )
        "Body Fat %" -> listOf(
            SampleMeasurement("Dec 15, 2024", "12", "%", "-0.5%"),
            SampleMeasurement("Dec 8, 2024", "12.5", "%", "-0.5%"),
            SampleMeasurement("Dec 1, 2024", "13", "%", "-1%"),
            SampleMeasurement("Nov 24, 2024", "14", "%", "-0.5%"),
            SampleMeasurement("Nov 17, 2024", "14.5", "%", "Baseline")
        )
        else -> listOf(
            SampleMeasurement("Dec 15, 2024", "42", "\"", "+0.5\""),
            SampleMeasurement("Dec 8, 2024", "41.5", "\"", "+0.5\""),
            SampleMeasurement("Dec 1, 2024", "41", "\"", "0\""),
            SampleMeasurement("Nov 24, 2024", "41", "\"", "-0.5\""),
            SampleMeasurement("Nov 17, 2024", "41.5", "\"", "Baseline")
        )
    }
}
