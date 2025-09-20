package com.fitnessss.fitlife.ui.screens.nutrition

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(navController: NavController) {
    var selectedDay by remember { mutableStateOf("Monday") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meal Planner") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Generate grocery list */ }) {
                        Icon(Icons.Filled.ShoppingCart, contentDescription = "Grocery List")
                    }
                    IconButton(onClick = { /* Save meal plan */ }) {
                        Icon(Icons.Filled.Save, contentDescription = "Save")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* Add meal to plan */ }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Meal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                // Weekly overview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "This Week's Plan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(weekDays) { day ->
                                DayChip(
                                    day = day,
                                    isSelected = selectedDay == day,
                                    onClick = { selectedDay = day }
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NutritionSummaryItem(
                                label = "Total Calories",
                                value = "2,450",
                                icon = Icons.Filled.Restaurant
                            )
                            NutritionSummaryItem(
                                label = "Protein",
                                value = "180g",
                                icon = Icons.Filled.SportsGymnastics
                            )
                            NutritionSummaryItem(
                                label = "Carbs",
                                value = "250g",
                                icon = Icons.Filled.Restaurant
                            )
                        }
                    }
                }
            }
            
            item {
                Text(
                    text = "Meals for $selectedDay",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            items(mealTypes) { mealType ->
                MealTypeSection(
                    mealType = mealType,
                    selectedDay = selectedDay
                )
            }
            
            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayChip(
    day: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { Text(day) },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            { Icon(Icons.Filled.Check, contentDescription = null) }
        } else null
    )
}

@Composable
fun NutritionSummaryItem(
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
fun MealTypeSection(
    mealType: MealType,
    selectedDay: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
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
                        imageVector = mealType.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = mealType.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = "${mealType.targetCalories} cal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Sample meal for this type
            val sampleMeal = getSampleMealForType(mealType)
            if (sampleMeal != null) {
                PlannedMealCard(meal = sampleMeal)
            } else {
                // Empty state
                OutlinedButton(
                    onClick = { /* Add meal */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add ${mealType.name}")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannedMealCard(meal: SampleMeal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal image placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Meal details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = meal.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${meal.calories} cal • ${meal.protein}g protein",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (meal.prepTime.isNotEmpty()) {
                    Text(
                        text = "Prep: ${meal.prepTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { /* View recipe */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = "Recipe",
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    onClick = { /* Remove meal */ },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Remove",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

data class MealType(
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val targetCalories: Int
)

data class SampleMeal(
    val name: String,
    val calories: Int,
    val protein: Int,
    val prepTime: String
)

private val weekDays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

private val mealTypes = listOf(
    MealType("Breakfast", Icons.Filled.WbSunny, 450),
    MealType("Lunch", Icons.Filled.Restaurant, 650),
    MealType("Dinner", Icons.Filled.DinnerDining, 750),
    MealType("Snacks", Icons.Filled.LocalCafe, 200)
)

private fun getSampleMealForType(mealType: MealType): SampleMeal? {
    return when (mealType.name) {
        "Breakfast" -> SampleMeal("Oatmeal with Berries", 320, 12, "5 min")
        "Lunch" -> SampleMeal("Grilled Chicken Salad", 420, 35, "15 min")
        "Dinner" -> SampleMeal("Salmon with Vegetables", 480, 42, "25 min")
        "Snacks" -> SampleMeal("Greek Yogurt with Nuts", 180, 15, "2 min")
        else -> null
    }
}
