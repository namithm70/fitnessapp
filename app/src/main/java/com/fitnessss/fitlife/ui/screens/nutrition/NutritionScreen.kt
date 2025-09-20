package com.fitnessss.fitlife.ui.screens.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
// Removed LazyVerticalGrid imports as we're using regular layouts now
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fitnessss.fitlife.navigation.Screen
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.service.HistoryLoggingService
import com.fitnessss.fitlife.data.model.MealType
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

// Enhanced data classes for nutrition tracking
data class NutritionGoals(
    val calories: Int = 2000,
    val protein: Int = 120,
    val carbs: Int = 250,
    val fat: Int = 65,
    val fiber: Int = 25,
    val sugar: Int = 50,
    val sodium: Int = 2300
)

data class FoodItem(
    val id: String,
    val name: String,
    val category: String,
    val calories: Int, // per 100g
    val protein: Double, // grams
    val carbs: Double, // grams
    val fat: Double, // grams
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0, // mg
    val icon: ImageVector = Icons.Filled.Restaurant,
    val popularPortions: List<Portion> = emptyList()
)

data class Portion(
    val name: String,
    val grams: Int
)

data class LoggedFood(
    val foodItem: FoodItem,
    val portionGrams: Int,
    val mealType: MealType2,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MealType2(val displayName: String, val icon: ImageVector) {
    BREAKFAST("Breakfast", Icons.Filled.WbSunny),
    LUNCH("Lunch", Icons.Filled.LightMode),
    DINNER("Dinner", Icons.Filled.Nightlight),
    SNACK("Snack", Icons.Filled.Fastfood)
}

// Nutrition Manager for state management
object NutritionManager {
    private val _loggedFoods = mutableStateListOf<LoggedFood>()
    private val _goals = mutableStateOf(NutritionGoals())
    private val _waterGlasses = mutableStateOf(0)
    
    // History logging service - will be injected
    private var historyLoggingService: HistoryLoggingService? = null
    
    val loggedFoods: List<LoggedFood> get() = _loggedFoods
    val goals: NutritionGoals get() = _goals.value
    val waterGlasses: Int get() = _waterGlasses.value
    
    // Set history logging service
    fun setHistoryLoggingService(service: HistoryLoggingService) {
        historyLoggingService = service
    }
    
    // Initialize with history logging service (global function)
    fun initializeWithHistoryService(service: HistoryLoggingService) {
        historyLoggingService = service
    }
    
    fun addFood(loggedFood: LoggedFood) {
        _loggedFoods.add(loggedFood)
        
        // Log to history
        historyLoggingService?.let { service ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val totalCalories = (loggedFood.foodItem.calories * loggedFood.portionGrams / 100.0).toFloat()
                    val mealType = when (loggedFood.mealType) {
                        MealType2.BREAKFAST -> MealType.BREAKFAST
                        MealType2.LUNCH -> MealType.LUNCH
                        MealType2.DINNER -> MealType.DINNER
                        MealType2.SNACK -> MealType.SNACK
                    }
                    
                    // Convert to history LoggedFood format
                    val historyLoggedFood = com.fitnessss.fitlife.data.model.LoggedFood(
                        foodId = loggedFood.foodItem.id,
                        amount = loggedFood.portionGrams.toFloat(),
                        unit = "g",
                        calories = totalCalories,
                        protein = (loggedFood.foodItem.protein * loggedFood.portionGrams / 100.0).toFloat(),
                        carbs = (loggedFood.foodItem.carbs * loggedFood.portionGrams / 100.0).toFloat(),
                        fat = (loggedFood.foodItem.fat * loggedFood.portionGrams / 100.0).toFloat()
                    )
                    
                    service.logNutritionEntry(
                        userId = "current_user",
                        nutritionLogId = loggedFood.foodItem.id,
                        mealType = mealType,
                        totalCalories = totalCalories,
                        foodItems = listOf(historyLoggedFood),
                        foodNames = listOf(loggedFood.foodItem.name),
                        notes = "Added ${loggedFood.foodItem.name} (${loggedFood.portionGrams}g)"
                    )
                } catch (e: Exception) {
                    // Log error but don't fail the food addition
                    e.printStackTrace()
                }
            }
        }
    }
    
    fun removeFood(loggedFood: LoggedFood) {
        _loggedFoods.remove(loggedFood)
    }
    
    fun addWaterGlass() {
        if (_waterGlasses.value < 12) {
            _waterGlasses.value++
            
            // Log to history
            historyLoggingService?.let { service ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        service.logWaterIntake(
                            userId = "current_user",
                            amount = 250f, // Standard glass size
                            notes = "Added water glass"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    fun removeWaterGlass() {
        if (_waterGlasses.value > 0) {
            _waterGlasses.value--
            
            // Log to history
            historyLoggingService?.let { service ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        service.logWaterIntake(
                            userId = "current_user",
                            amount = -250f, // Negative to indicate removal
                            notes = "Removed water glass"
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    fun getTotalNutrition(): NutritionGoals {
        val totalCalories = _loggedFoods.sumOf { 
            (it.foodItem.calories * it.portionGrams / 100.0).toInt() 
        }
        val totalProtein = _loggedFoods.sumOf { 
            it.foodItem.protein * it.portionGrams / 100.0 
        }.toInt()
        val totalCarbs = _loggedFoods.sumOf { 
            it.foodItem.carbs * it.portionGrams / 100.0 
        }.toInt()
        val totalFat = _loggedFoods.sumOf { 
            it.foodItem.fat * it.portionGrams / 100.0 
        }.toInt()
        val totalFiber = _loggedFoods.sumOf { 
            it.foodItem.fiber * it.portionGrams / 100.0 
        }.toInt()
        val totalSugar = _loggedFoods.sumOf { 
            it.foodItem.sugar * it.portionGrams / 100.0 
        }.toInt()
        val totalSodium = _loggedFoods.sumOf { 
            it.foodItem.sodium * it.portionGrams / 100.0 
        }.toInt()
        
        return NutritionGoals(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat,
            fiber = totalFiber,
            sugar = totalSugar,
            sodium = totalSodium
        )
    }
    
    /**
     * Clears all nutrition data
     */
    fun clearAllData() {
        _loggedFoods.clear()
        _waterGlasses.value = 0
        _goals.value = NutritionGoals() // Reset to default goals
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionScreen(
    navController: NavController,
    viewModel: NutritionViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PageHeaderWithThemeToggle(title = "Nutrition")
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Enhanced Tab Row with better styling
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),                contentColor = MaterialTheme.colorScheme.onSurface,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { 
                Text(
                            "Today",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        Text(
                            "Food Database",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        ) 
                    },
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> NutritionDashboard(navController, snackbarHostState, viewModel)
                1 -> FoodDatabaseSection(navController, snackbarHostState, viewModel)
            }
        }
    }
}

@Composable
fun NutritionDashboard(navController: NavController, snackbarHostState: SnackbarHostState, viewModel: NutritionViewModel) {
    val scope = rememberCoroutineScope()
    val totalNutrition = NutritionManager.getTotalNutrition()
    val goals = NutritionManager.goals
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Daily Overview Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
        Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Progress",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            Icons.Filled.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Calories Progress Circle
                    CaloriesProgressCircle(
                        current = totalNutrition.calories,
                        goal = goals.calories
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Macro breakdown
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MacroCard(
                            name = "Protein",
                            current = totalNutrition.protein,
                            goal = goals.protein,
                            unit = "g",
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            name = "Carbs",
                            current = totalNutrition.carbs,
                            goal = goals.carbs,
                            unit = "g",
                            color = Color(0xFF2196F3),
                            modifier = Modifier.weight(1f)
                        )
                        MacroCard(
                            name = "Fat",
                            current = totalNutrition.fat,
                            goal = goals.fat,
                            unit = "g",
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Water Intake Section
        item {
            WaterIntakeCard(
                glasses = NutritionManager.waterGlasses,
                onAddGlass = { viewModel.addWaterWithHistory() },
                onRemoveGlass = { viewModel.removeWaterWithHistory() }
            )
        }
        
        // Quick Add Meals Section
        item {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
                    modifier = Modifier.padding(20.dp)
        ) {
            Text(
                        text = "Quick Add Meal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        MealType2.values().forEach { mealType ->
                            OutlinedCard(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        // Navigate to food search screen with meal type parameter
                                        navController.navigate("${Screen.FoodSearch.route}?mealType=${mealType.name}")
                                    },
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = mealType.icon,
                                        contentDescription = mealType.displayName,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = mealType.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Recent Meals
        item {
            RecentMealsSection(
                meals = NutritionManager.loggedFoods.takeLast(5),
                onRemoveFood = { viewModel.removeFood(it) }
            )
        }
        
        // Micronutrients Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
) {
    Column(
                    modifier = Modifier.padding(20.dp)
    ) {
        Text(
                        text = "Micronutrients",
            style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MicronutrientRow("Fiber", totalNutrition.fiber, goals.fiber, "g")
                    Spacer(modifier = Modifier.height(8.dp))
                    MicronutrientRow("Sugar", totalNutrition.sugar, goals.sugar, "g")
                    Spacer(modifier = Modifier.height(8.dp))
                    MicronutrientRow("Sodium", totalNutrition.sodium, goals.sodium, "mg")
                }
            }
        }
        
        // Quick Actions
        item {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
                    modifier = Modifier.padding(20.dp)
        ) {
            Text(
                        text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate(Screen.FoodSearch.route) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Search Food")
                        }
                        
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.MealPlan.route) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Meal Plan")
                        }
                    }
                }
            }
        }
        
        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun CaloriesProgressCircle(
    current: Int,
    goal: Int
) {
    val progress = if (goal > 0) (current.toFloat() / goal.toFloat()).coerceAtMost(1f) else 0f
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        CircularProgressIndicator(
            progress = progress,
            modifier = Modifier.size(120.dp),
            strokeWidth = 8.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = current.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "/ $goal cal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MacroCard(
    name: String,
    current: Int,
    goal: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (current.toFloat() / goal.toFloat()).coerceAtMost(1f) else 0f
    
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "$current$unit",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "/ $goal$unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        
            Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
        }
    }
}

@Composable
fun WaterIntakeCard(
    glasses: Int,
    onAddGlass: () -> Unit,
    onRemoveGlass: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Water Intake",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$glasses / 8 glasses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Water glasses visualization
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(8) { index ->
                    Box(
                modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                if (index < glasses) 
                                    Color(0xFF2196F3) 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { 
                                if (index < glasses) onRemoveGlass() else onAddGlass() 
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.WaterDrop,
                            contentDescription = "Water glass ${index + 1}",
                            tint = if (index < glasses) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRemoveGlass,
                    enabled = glasses > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Remove")
                }
                
                Button(
                    onClick = onAddGlass,
                    enabled = glasses < 8,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Glass")
                }
            }
        }
    }
}

@Composable
fun MicronutrientRow(
    name: String,
    current: Int,
    goal: Int,
    unit: String
) {
    val progress = if (goal > 0) (current.toFloat() / goal.toFloat()).coerceAtMost(1f) else 0f
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "$current / $goal $unit",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun RecentMealsSection(
    meals: List<LoggedFood>,
    onRemoveFood: (LoggedFood) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Recent Meals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (meals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.RestaurantMenu,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No meals logged yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Add your first meal to start tracking!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    meals.forEach { meal ->
                        RecentMealItem(
                            meal = meal,
                            onRemove = { onRemoveFood(meal) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RecentMealItem(
    meal: LoggedFood,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = meal.foodItem.icon,
            contentDescription = meal.foodItem.name,
            modifier = Modifier.size(32.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meal.foodItem.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            val calories = (meal.foodItem.calories * meal.portionGrams / 100.0).toInt()
            Text(
                text = "${meal.portionGrams}g • $calories cal • ${meal.mealType.displayName}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = "Remove meal",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun FoodDatabaseSection(navController: NavController, snackbarHostState: SnackbarHostState, viewModel: NutritionViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search foods...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )
        }
        
        // Categories
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
                    modifier = Modifier.padding(20.dp)
                ) {
            Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Categories grid using regular Column and Row
                    val chunkedCategories = foodCategories.chunked(3)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        chunkedCategories.forEach { rowCategories ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowCategories.forEach { category ->
                                    CategoryCard(
                                        category = category,
                                        isSelected = selectedCategory == category.name,
                                        onClick = { 
                                            selectedCategory = if (selectedCategory == category.name) "All" else category.name
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Fill remaining space if row is not complete
                                repeat(3 - rowCategories.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Popular Foods
        item {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
                    modifier = Modifier.padding(20.dp)
        ) {
            Text(
                        text = "Popular Foods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val filteredFoods = getAllFoodItems().filter { food ->
                            (selectedCategory == "All" || food.category == selectedCategory) &&
                            (searchQuery.isEmpty() || food.name.contains(searchQuery, ignoreCase = true))
                        }.take(20)
                        
                        filteredFoods.forEach { food ->
                            FoodItemCard(
                                food = food,
                                onClick = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("${food.name} details coming soon!")
                                    }
                                },
                                onQuickAdd = {
                                    // Quick add 100g portion
                                    val loggedFood = LoggedFood(
                                        foodItem = food,
                                        portionGrams = 100,
                                        mealType = MealType2.SNACK
                                    )
                                    viewModel.addFoodWithHistory(loggedFood)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added ${food.name} to your diary!")
                                    }
                                }
                            )
                        }
                        
                        if (filteredFoods.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No foods found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionAnalytics(navController: NavController, snackbarHostState: SnackbarHostState) {
    // Simple analytics placeholder
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
        modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Analytics,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
            Text(
                        text = "Analytics Coming Soon",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
            Text(
                        text = "Advanced nutrition analytics and insights will be available here",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
                }
            }
        }
    }
}

// Helper composables and data
@Composable
fun CategoryCard(
    category: FoodCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.surfaceVariant
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.onSurface) 
        else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.name,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun FoodItemCard(
    food: FoodItem,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
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
                imageVector = food.icon,
                contentDescription = food.name,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
            Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${food.calories} cal • ${food.protein}g protein • ${food.carbs}g carbs • ${food.fat}g fat",
                style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                
                Text(
                    text = "per 100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            
            IconButton(
                onClick = onQuickAdd,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Quick add",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Data classes and helper functions
data class FoodCategory(
    val name: String,
    val icon: ImageVector
)

val foodCategories = listOf(
    FoodCategory("Fruits", Icons.Filled.Eco),
    FoodCategory("Indian Fruits", Icons.Filled.Eco),
    FoodCategory("Vegetables", Icons.Filled.Agriculture),
    FoodCategory("Indian Vegetables", Icons.Filled.Agriculture),
    FoodCategory("Indian Greens", Icons.Filled.Agriculture),
    FoodCategory("Proteins", Icons.Filled.LocalDining),
    FoodCategory("Indian Grains", Icons.Filled.LocalFlorist),
    FoodCategory("Indian Breads", Icons.Filled.LocalFlorist),
    FoodCategory("Indian Lentils", Icons.Filled.Eco),
    FoodCategory("Dairy", Icons.Filled.EmojiFoodBeverage),
    FoodCategory("Indian Dairy", Icons.Filled.EmojiFoodBeverage),
    FoodCategory("Nuts & Seeds", Icons.Filled.LocalFlorist),
    FoodCategory("Indian Nuts", Icons.Filled.LocalFlorist),
    FoodCategory("Indian Spices", Icons.Filled.LocalDining),
    FoodCategory("Beverages", Icons.Filled.LocalCafe),
    FoodCategory("Indian Snacks", Icons.Filled.Fastfood),
    FoodCategory("Indian Main Dishes", Icons.Filled.LocalDining),
    FoodCategory("Indian Rice Dishes", Icons.Filled.LocalDining),
    FoodCategory("Snacks", Icons.Filled.Fastfood),
    FoodCategory("Desserts", Icons.Filled.Cake),
    FoodCategory("Indian Desserts", Icons.Filled.Cake),
    FoodCategory("Seafood", Icons.Filled.SetMeal),
    FoodCategory("Legumes", Icons.Filled.Eco),
    FoodCategory("Oils & Fats", Icons.Filled.LocalDining)
)

fun getAllFoodItems(): List<FoodItem> {
    // Simplified food list without emojis
    return listOf(
        FoodItem("apple", "Apple", "Fruits", 52, 0.3, 14.0, 0.2, 2.4, 10.4, 1.0),
        FoodItem("banana", "Banana", "Fruits", 89, 1.1, 23.0, 0.3, 2.6, 12.2, 1.0),
        FoodItem("orange", "Orange", "Fruits", 47, 0.9, 12.0, 0.1, 2.4, 9.4, 0.0),
        FoodItem("broccoli", "Broccoli", "Vegetables", 34, 2.8, 7.0, 0.4, 2.6, 1.5, 33.0),
        FoodItem("carrot", "Carrot", "Vegetables", 41, 0.9, 10.0, 0.2, 2.8, 4.7, 69.0),
        FoodItem("chicken_breast", "Chicken Breast", "Proteins", 165, 31.0, 0.0, 3.6, 0.0, 0.0, 74.0),
        FoodItem("salmon", "Salmon", "Proteins", 208, 20.0, 0.0, 13.0, 0.0, 0.0, 59.0),
        FoodItem("brown_rice", "Brown Rice", "Grains", 111, 2.6, 23.0, 0.9, 1.8, 0.4, 5.0),
        FoodItem("oats", "Oats", "Grains", 389, 16.9, 66.0, 6.9, 10.6, 0.9, 2.0),
        FoodItem("milk", "Whole Milk", "Dairy", 61, 3.2, 4.8, 3.3, 0.0, 4.8, 40.0),
        FoodItem("almonds", "Almonds", "Nuts & Seeds", 579, 21.0, 22.0, 50.0, 12.5, 4.4, 1.0),
        FoodItem("water", "Water", "Beverages", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
    )
}

fun getAllFoodItemsOLD(): List<FoodItem> {
    // Old function disabled to avoid emoji conflicts
    return emptyList()
    /*
    return listOf(
        // Sample food items without emojis
        // Fruits (30 items)
        FoodItem("apple", "Apple", "Fruits", 52, 0.3, 14.0, 0.2, 2.4, 10.4, 1.0),
        FoodItem("banana", "Banana", "Fruits", 89, 1.1, 23.0, 0.3, 2.6, 12.2, 1.0),
        FoodItem("orange", "Orange", "Fruits", 47, 0.9, 12.0, 0.1, 2.4, 9.4, 0.0),
        FoodItem("strawberry", "Strawberry", "Fruits", 32, 0.7, 8.0, 0.3, 2.0, 4.9, 1.0),
        FoodItem("blueberry", "Blueberry", "Fruits", 57, 0.7, 14.0, 0.3, 2.4, 10.0, 1.0),
        FoodItem("mango", "Mango", "Fruits", 60, 0.8, 15.0, 0.4, 1.6, 13.7, 1.0),
        FoodItem("pineapple", "Pineapple", "Fruits", 50, 0.5, 13.0, 0.1, 1.4, 9.9, 1.0, "🍍"),
        FoodItem("grape", "Grapes", "Fruits", 62, 0.6, 16.0, 0.2, 0.9, 15.5, 2.0, "🍇"),
        FoodItem("watermelon", "Watermelon", "Fruits", 30, 0.6, 8.0, 0.2, 0.4, 6.2, 1.0, "🍉"),
        FoodItem("avocado", "Avocado", "Fruits", 160, 2.0, 9.0, 15.0, 7.0, 0.7, 7.0, "🥑"),
        FoodItem("kiwi", "Kiwi", "Fruits", 61, 1.1, 15.0, 0.5, 3.0, 9.0, 3.0, "🥝"),
        FoodItem("peach", "Peach", "Fruits", 39, 0.9, 10.0, 0.3, 1.5, 8.4, 0.0, "🍑"),
        FoodItem("pear", "Pear", "Fruits", 57, 0.4, 15.0, 0.1, 3.1, 9.8, 1.0, "🍐"),
        FoodItem("cherry", "Cherry", "Fruits", 63, 1.1, 16.0, 0.2, 2.1, 12.8, 0.0, "🍒"),
        FoodItem("plum", "Plum", "Fruits", 46, 0.7, 11.0, 0.3, 1.4, 9.9, 0.0, "🍇"),
        FoodItem("apricot", "Apricot", "Fruits", 48, 1.4, 11.0, 0.4, 2.0, 9.2, 1.0),
        FoodItem("coconut", "Coconut", "Fruits", 354, 3.3, 15.0, 33.0, 9.0, 6.2, 20.0, "🥥"),
        FoodItem("lemon", "Lemon", "Fruits", 29, 1.1, 9.0, 0.3, 2.8, 1.5, 2.0, "🍋"),
        FoodItem("lime", "Lime", "Fruits", 30, 0.7, 11.0, 0.2, 2.8, 1.7, 2.0, "🍋"),
        FoodItem("grapefruit", "Grapefruit", "Fruits", 42, 0.8, 11.0, 0.1, 1.6, 6.9, 0.0),
        FoodItem("cantaloupe", "Cantaloupe", "Fruits", 34, 0.8, 8.0, 0.2, 0.9, 7.9, 16.0, "🍈"),
        FoodItem("honeydew", "Honeydew", "Fruits", 36, 0.5, 9.0, 0.1, 0.8, 8.1, 18.0, "🍈"),
        FoodItem("papaya", "Papaya", "Fruits", 43, 0.5, 11.0, 0.3, 1.7, 7.8, 8.0),
        FoodItem("dragon_fruit", "Dragon Fruit", "Fruits", 60, 1.2, 13.0, 0.4, 3.0, 7.7, 0.0, "🐉"),
        FoodItem("passion_fruit", "Passion Fruit", "Fruits", 97, 2.2, 23.0, 0.7, 10.4, 11.2, 28.0, "🥥"),
        FoodItem("pomegranate", "Pomegranate", "Fruits", 83, 1.7, 19.0, 1.2, 4.0, 13.7, 3.0),
        FoodItem("fig", "Fig", "Fruits", 74, 0.8, 19.0, 0.3, 2.9, 16.3, 1.0, "🍇"),
        FoodItem("date", "Date", "Fruits", 277, 1.8, 75.0, 0.2, 6.7, 66.5, 1.0, "🌰"),
        FoodItem("cranberry", "Cranberry", "Fruits", 46, 0.4, 12.0, 0.1, 4.6, 4.0, 2.0),
        FoodItem("blackberry", "Blackberry", "Fruits", 43, 1.4, 10.0, 0.5, 5.3, 4.9, 1.0),
        
        // Vegetables (25 items)
        FoodItem("broccoli", "Broccoli", "Vegetables", 34, 2.8, 7.0, 0.4, 2.6, 1.5, 33.0, "🥦"),
        FoodItem("carrot", "Carrot", "Vegetables", 41, 0.9, 10.0, 0.2, 2.8, 4.7, 69.0, "🥕"),
        FoodItem("spinach", "Spinach", "Vegetables", 23, 2.9, 4.0, 0.4, 2.2, 0.4, 79.0, "🥬"),
        FoodItem("tomato", "Tomato", "Vegetables", 18, 0.9, 4.0, 0.2, 1.2, 2.6, 5.0, "🍅"),
        FoodItem("cucumber", "Cucumber", "Vegetables", 16, 0.7, 4.0, 0.1, 0.5, 1.7, 2.0, "🥒"),
        FoodItem("bell_pepper", "Bell Pepper", "Vegetables", 31, 1.0, 7.0, 0.3, 2.5, 4.2, 4.0, "🫑"),
        FoodItem("onion", "Onion", "Vegetables", 40, 1.1, 9.0, 0.1, 1.7, 4.2, 4.0, "🧅"),
        FoodItem("garlic", "Garlic", "Vegetables", 149, 6.4, 33.0, 0.5, 2.1, 1.0, 17.0, "🧄"),
        FoodItem("sweet_potato", "Sweet Potato", "Vegetables", 86, 1.6, 20.0, 0.1, 3.0, 4.2, 54.0, "🍠"),
        FoodItem("mushroom", "Mushroom", "Vegetables", 22, 3.1, 3.0, 0.3, 1.0, 2.0, 5.0, "🍄"),
        FoodItem("cauliflower", "Cauliflower", "Vegetables", 25, 1.9, 5.0, 0.3, 2.0, 1.9, 30.0, "🥦"),
        FoodItem("zucchini", "Zucchini", "Vegetables", 17, 1.2, 3.0, 0.3, 1.0, 2.5, 8.0, "🥒"),
        FoodItem("eggplant", "Eggplant", "Vegetables", 25, 1.0, 6.0, 0.2, 3.0, 3.5, 2.0, "🍆"),
        FoodItem("celery", "Celery", "Vegetables", 14, 0.7, 3.0, 0.2, 1.6, 1.3, 80.0, "🥬"),
        FoodItem("lettuce", "Lettuce", "Vegetables", 15, 1.4, 3.0, 0.2, 1.3, 0.8, 28.0, "🥬"),
        FoodItem("cabbage", "Cabbage", "Vegetables", 25, 1.3, 6.0, 0.1, 2.5, 3.2, 18.0, "🥬"),
        FoodItem("kale", "Kale", "Vegetables", 49, 4.3, 9.0, 0.9, 3.6, 2.3, 38.0, "🥬"),
        FoodItem("asparagus", "Asparagus", "Vegetables", 20, 2.2, 4.0, 0.1, 2.1, 1.9, 2.0, "🥒"),
        FoodItem("artichoke", "Artichoke", "Vegetables", 47, 3.3, 11.0, 0.2, 8.6, 1.0, 94.0, "🌿"),
        FoodItem("beet", "Beet", "Vegetables", 43, 1.6, 10.0, 0.2, 2.8, 6.8, 78.0, "🥕"),
        FoodItem("radish", "Radish", "Vegetables", 16, 0.7, 4.0, 0.1, 1.6, 1.9, 39.0, "🥕"),
        FoodItem("turnip", "Turnip", "Vegetables", 28, 0.9, 6.0, 0.1, 1.8, 3.8, 67.0, "🥕"),
        FoodItem("potato", "Potato", "Vegetables", 77, 2.0, 17.0, 0.1, 2.2, 0.8, 6.0, "🥔"),
        FoodItem("corn", "Corn", "Vegetables", 86, 3.3, 19.0, 1.4, 2.7, 3.2, 35.0, "🌽"),
        FoodItem("pumpkin", "Pumpkin", "Vegetables", 26, 1.0, 7.0, 0.1, 0.5, 2.8, 1.0, "🎃"),
        
        // Proteins (20 items)
        FoodItem("chicken_breast", "Chicken Breast", "Proteins", 165, 31.0, 0.0, 3.6, 0.0, 0.0, 74.0, "🍗"),
        FoodItem("salmon", "Salmon", "Proteins", 208, 20.0, 0.0, 13.0, 0.0, 0.0, 59.0, "🐟"),
        FoodItem("beef", "Lean Beef", "Proteins", 250, 26.0, 0.0, 15.0, 0.0, 0.0, 72.0, "🥩"),
        FoodItem("eggs", "Eggs", "Proteins", 155, 13.0, 1.0, 11.0, 0.0, 0.6, 124.0, "🥚"),
        FoodItem("tofu", "Tofu", "Proteins", 76, 8.0, 2.0, 4.8, 0.4, 0.7, 7.0, "🥢"),
        FoodItem("greek_yogurt", "Greek Yogurt", "Proteins", 59, 10.0, 4.0, 0.4, 0.0, 4.0, 36.0, "🥛"),
        FoodItem("tuna", "Tuna", "Proteins", 144, 30.0, 0.0, 1.0, 0.0, 0.0, 39.0, "🐟"),
        FoodItem("shrimp", "Shrimp", "Proteins", 85, 18.0, 1.0, 1.0, 0.0, 0.0, 111.0, "🦐"),
        FoodItem("turkey", "Turkey Breast", "Proteins", 135, 30.0, 0.0, 1.0, 0.0, 0.0, 70.0, "🦃"),
        FoodItem("cottage_cheese", "Cottage Cheese", "Proteins", 98, 11.0, 3.4, 4.3, 0.0, 2.7, 364.0, "🧀"),
        FoodItem("pork", "Lean Pork", "Proteins", 242, 27.0, 0.0, 14.0, 0.0, 0.0, 62.0, "🥩"),
        FoodItem("lamb", "Lean Lamb", "Proteins", 294, 25.0, 0.0, 21.0, 0.0, 0.0, 72.0, "🥩"),
        FoodItem("chicken_thigh", "Chicken Thigh", "Proteins", 209, 26.0, 0.0, 11.0, 0.0, 0.0, 82.0, "🍗"),
        FoodItem("cod", "Cod", "Proteins", 82, 18.0, 0.0, 0.7, 0.0, 0.0, 54.0, "🐟"),
        FoodItem("sardines", "Sardines", "Proteins", 208, 25.0, 0.0, 11.0, 0.0, 0.0, 307.0, "🐟"),
        FoodItem("duck", "Duck Breast", "Proteins", 337, 19.0, 0.0, 28.0, 0.0, 0.0, 74.0, "🦆"),
        FoodItem("rabbit", "Rabbit", "Proteins", 173, 33.0, 0.0, 3.5, 0.0, 0.0, 47.0, "🐰"),
        FoodItem("venison", "Venison", "Proteins", 158, 30.0, 0.0, 3.2, 0.0, 0.0, 51.0, "🦌"),
        FoodItem("tempeh", "Tempeh", "Proteins", 192, 19.0, 9.0, 11.0, 9.0, 0.0, 9.0, "🥢"),
        FoodItem("seitan", "Seitan", "Proteins", 370, 75.0, 14.0, 1.9, 0.6, 0.0, 2119.0, "🥢"),
        
        // Grains (15 items)
        FoodItem("brown_rice", "Brown Rice", "Grains", 111, 2.6, 23.0, 0.9, 1.8, 0.4, 5.0, "🍚"),
        FoodItem("quinoa", "Quinoa", "Grains", 120, 4.4, 22.0, 1.9, 2.8, 0.9, 7.0, "🌾"),
        FoodItem("oats", "Oats", "Grains", 389, 16.9, 66.0, 6.9, 10.6, 0.9, 2.0, "🥣"),
        FoodItem("whole_wheat_bread", "Whole Wheat Bread", "Grains", 247, 13.0, 41.0, 4.2, 7.0, 6.0, 681.0, "🍞"),
        FoodItem("pasta", "Whole Wheat Pasta", "Grains", 124, 5.0, 25.0, 1.1, 3.2, 1.0, 6.0, "🍝"),
        FoodItem("barley", "Barley", "Grains", 123, 2.3, 28.0, 0.4, 3.8, 0.8, 9.0, "🌾"),
        FoodItem("buckwheat", "Buckwheat", "Grains", 343, 13.0, 71.0, 3.4, 10.0, 1.5, 1.0, "🌾"),
        FoodItem("millet", "Millet", "Grains", 378, 11.0, 73.0, 4.2, 8.5, 1.7, 5.0, "🌾"),
        FoodItem("amaranth", "Amaranth", "Grains", 371, 14.0, 65.0, 7.0, 6.7, 1.7, 4.0, "🌾"),
        FoodItem("wild_rice", "Wild Rice", "Grains", 101, 4.0, 21.0, 0.3, 1.8, 0.7, 3.0, "🍚"),
        FoodItem("bulgur", "Bulgur", "Grains", 83, 3.1, 19.0, 0.2, 4.5, 0.1, 5.0, "🌾"),
        FoodItem("couscous", "Couscous", "Grains", 112, 3.8, 23.0, 0.2, 1.4, 0.1, 5.0, "🌾"),
        FoodItem("spelt", "Spelt", "Grains", 338, 14.6, 70.0, 2.4, 10.7, 7.9, 8.0, "🌾"),
        FoodItem("rye", "Rye", "Grains", 338, 10.3, 76.0, 1.6, 15.1, 1.0, 2.0, "🌾"),
        FoodItem("teff", "Teff", "Grains", 367, 13.3, 73.0, 2.4, 8.0, 1.8, 12.0, "🌾"),
        
        // Seafood (15 items)
        FoodItem("lobster", "Lobster", "Seafood", 89, 19.0, 0.0, 0.9, 0.0, 0.0, 296.0, "🦞"),
        FoodItem("crab", "Crab", "Seafood", 97, 19.0, 0.0, 1.5, 0.0, 0.0, 293.0, "🦀"),
        FoodItem("scallops", "Scallops", "Seafood", 88, 17.0, 2.0, 0.8, 0.0, 0.0, 392.0, "🐚"),
        FoodItem("mussels", "Mussels", "Seafood", 86, 12.0, 4.0, 2.2, 0.0, 0.0, 286.0, "🦪"),
        FoodItem("oysters", "Oysters", "Seafood", 81, 9.0, 5.0, 2.3, 0.0, 2.7, 90.0, "🦪"),
        FoodItem("clams", "Clams", "Seafood", 86, 15.0, 3.0, 1.0, 0.0, 0.0, 601.0, "🦪"),
        FoodItem("octopus", "Octopus", "Seafood", 82, 15.0, 2.0, 1.0, 0.0, 0.0, 230.0, "🐙"),
        FoodItem("squid", "Squid", "Seafood", 92, 16.0, 3.0, 1.4, 0.0, 0.0, 44.0, "🦑"),
        FoodItem("halibut", "Halibut", "Seafood", 111, 23.0, 0.0, 2.3, 0.0, 0.0, 54.0, "🐟"),
        FoodItem("sea_bass", "Sea Bass", "Seafood", 124, 23.0, 0.0, 2.6, 0.0, 0.0, 68.0, "🐟"),
        FoodItem("mackerel", "Mackerel", "Seafood", 205, 19.0, 0.0, 14.0, 0.0, 0.0, 83.0, "🐟"),
        FoodItem("anchovy", "Anchovy", "Seafood", 131, 20.0, 0.0, 4.8, 0.0, 0.0, 104.0, "🐟"),
        FoodItem("herring", "Herring", "Seafood", 158, 18.0, 0.0, 9.0, 0.0, 0.0, 90.0, "🐟"),
        FoodItem("trout", "Trout", "Seafood", 148, 21.0, 0.0, 6.6, 0.0, 0.0, 59.0, "🐟"),
        FoodItem("sea_bream", "Sea Bream", "Seafood", 96, 19.0, 0.0, 1.2, 0.0, 0.0, 64.0, "🐟"),
        
        // Legumes (10 items)
        FoodItem("black_beans", "Black Beans", "Legumes", 132, 8.9, 24.0, 0.5, 8.7, 0.3, 2.0, "🫘"),
        FoodItem("chickpeas", "Chickpeas", "Legumes", 164, 8.9, 27.0, 2.6, 7.6, 4.8, 7.0, "🫘"),
        FoodItem("lentils", "Lentils", "Legumes", 116, 9.0, 20.0, 0.4, 7.9, 1.8, 2.0, "🫘"),
        FoodItem("kidney_beans", "Kidney Beans", "Legumes", 127, 8.7, 23.0, 0.5, 6.4, 0.3, 2.0, "🫘"),
        FoodItem("navy_beans", "Navy Beans", "Legumes", 140, 8.2, 26.0, 0.6, 10.5, 0.3, 2.0, "🫘"),
        FoodItem("pinto_beans", "Pinto Beans", "Legumes", 143, 9.0, 26.0, 0.7, 9.0, 0.3, 1.0, "🫘"),
        FoodItem("lima_beans", "Lima Beans", "Legumes", 115, 7.8, 21.0, 0.4, 7.0, 2.9, 2.0, "🫘"),
        FoodItem("green_peas", "Green Peas", "Legumes", 81, 5.4, 14.0, 0.4, 5.7, 5.7, 5.0, "🟢"),
        FoodItem("split_peas", "Split Peas", "Legumes", 118, 8.3, 21.0, 0.4, 8.3, 2.9, 2.0, "🟢"),
        FoodItem("edamame", "Edamame", "Legumes", 121, 11.0, 10.0, 5.2, 5.2, 2.2, 6.0, "🫛"),
        
        // Nuts & Seeds (15 items)
        FoodItem("almonds", "Almonds", "Nuts & Seeds", 579, 21.0, 22.0, 50.0, 12.5, 4.4, 1.0, "🥜"),
        FoodItem("walnuts", "Walnuts", "Nuts & Seeds", 654, 15.0, 14.0, 65.0, 6.7, 2.6, 2.0, "🥜"),
        FoodItem("peanuts", "Peanuts", "Nuts & Seeds", 567, 26.0, 16.0, 49.0, 8.5, 4.7, 18.0, "🥜"),
        FoodItem("chia_seeds", "Chia Seeds", "Nuts & Seeds", 486, 17.0, 42.0, 31.0, 34.4, 0.0, 16.0, "🌿"),
        FoodItem("flax_seeds", "Flax Seeds", "Nuts & Seeds", 534, 18.0, 29.0, 42.0, 27.3, 1.6, 30.0, "🌿"),
        FoodItem("sunflower_seeds", "Sunflower Seeds", "Nuts & Seeds", 584, 21.0, 20.0, 51.0, 8.6, 2.6, 9.0, "🌻"),
        FoodItem("pumpkin_seeds", "Pumpkin Seeds", "Nuts & Seeds", 559, 19.0, 54.0, 49.0, 6.0, 1.4, 7.0, "🎃"),
        FoodItem("cashews", "Cashews", "Nuts & Seeds", 553, 18.0, 30.0, 44.0, 3.3, 5.9, 12.0, "🥜"),
        FoodItem("pistachios", "Pistachios", "Nuts & Seeds", 560, 20.0, 28.0, 45.0, 10.6, 7.7, 1.0, "🥜"),
        FoodItem("brazil_nuts", "Brazil Nuts", "Nuts & Seeds", 659, 14.0, 12.0, 67.0, 7.5, 2.3, 3.0, "🥜"),
        FoodItem("pecans", "Pecans", "Nuts & Seeds", 691, 9.2, 14.0, 72.0, 9.6, 4.0, 0.0, "🥜"),
        FoodItem("macadamia", "Macadamia Nuts", "Nuts & Seeds", 718, 7.9, 14.0, 76.0, 8.6, 4.6, 5.0, "🥜"),
        FoodItem("pine_nuts", "Pine Nuts", "Nuts & Seeds", 673, 14.0, 13.0, 68.0, 3.7, 3.6, 2.0, "🌿"),
        FoodItem("hemp_seeds", "Hemp Seeds", "Nuts & Seeds", 553, 31.0, 8.7, 49.0, 4.0, 1.5, 5.0, "🌿"),
        FoodItem("sesame_seeds", "Sesame Seeds", "Nuts & Seeds", 573, 18.0, 23.0, 50.0, 11.8, 0.3, 11.0, "🌿"),
        
        // Dairy (10 items)
        FoodItem("milk", "Milk (2%)", "Dairy", 50, 3.4, 5.0, 2.0, 0.0, 5.0, 44.0, "🥛"),
        FoodItem("cheese_cheddar", "Cheddar Cheese", "Dairy", 403, 25.0, 1.0, 33.0, 0.0, 0.5, 621.0, "🧀"),
        FoodItem("yogurt", "Plain Yogurt", "Dairy", 61, 3.5, 4.7, 3.3, 0.0, 4.7, 46.0, "🥛"),
        FoodItem("butter", "Butter", "Dairy", 717, 0.9, 0.1, 81.0, 0.0, 0.1, 11.0, "🧈"),
        FoodItem("cream_cheese", "Cream Cheese", "Dairy", 342, 6.2, 4.1, 34.0, 0.0, 3.2, 321.0, "🧀"),
        FoodItem("mozzarella", "Mozzarella", "Dairy", 280, 28.0, 2.2, 17.0, 0.0, 1.0, 627.0, "🧀"),
        FoodItem("parmesan", "Parmesan", "Dairy", 431, 38.0, 4.1, 29.0, 0.0, 0.9, 1529.0, "🧀"),
        FoodItem("ricotta", "Ricotta", "Dairy", 174, 11.0, 3.0, 13.0, 0.0, 0.3, 84.0, "🧀"),
        FoodItem("feta", "Feta Cheese", "Dairy", 264, 14.0, 4.1, 21.0, 0.0, 4.1, 1116.0, "🧀"),
        FoodItem("heavy_cream", "Heavy Cream", "Dairy", 345, 2.1, 2.8, 37.0, 0.0, 2.8, 38.0, "🥛"),
        
        // Beverages (8 items)
        FoodItem("water", "Water", "Beverages", 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "💧"),
        FoodItem("green_tea", "Green Tea", "Beverages", 1, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, "🍵"),
        FoodItem("coffee", "Black Coffee", "Beverages", 2, 0.3, 0.0, 0.0, 0.0, 0.0, 5.0, "☕"),
        FoodItem("orange_juice", "Orange Juice", "Beverages", 45, 0.7, 10.0, 0.2, 0.2, 8.4, 1.0, "🧃"),
        FoodItem("almond_milk", "Almond Milk", "Beverages", 17, 0.6, 1.5, 1.1, 0.5, 1.0, 63.0, "🥛"),
        FoodItem("soy_milk", "Soy Milk", "Beverages", 33, 2.9, 1.8, 1.6, 0.4, 1.0, 51.0, "🥛"),
        FoodItem("coconut_water", "Coconut Water", "Beverages", 19, 0.7, 3.7, 0.2, 1.1, 2.6, 105.0, "🥥"),
        FoodItem("kombucha", "Kombucha", "Beverages", 29, 0.0, 7.0, 0.0, 0.0, 2.0, 4.0, "🍵"),
        
        // Oils & Fats (8 items)
        FoodItem("olive_oil", "Olive Oil", "Oils & Fats", 884, 0.0, 0.0, 100.0, 0.0, 0.0, 2.0, "🫒"),
        FoodItem("coconut_oil", "Coconut Oil", "Oils & Fats", 862, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, "🥥"),
        FoodItem("avocado_oil", "Avocado Oil", "Oils & Fats", 884, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, "🥑"),
        FoodItem("sesame_oil", "Sesame Oil", "Oils & Fats", 884, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, "🌿"),
        FoodItem("flaxseed_oil", "Flaxseed Oil", "Oils & Fats", 884, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, "🌿"),
        FoodItem("walnut_oil", "Walnut Oil", "Oils & Fats", 884, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, "🥜"),
        FoodItem("sunflower_oil", "Sunflower Oil", "Oils & Fats", 884, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, "🌻"),
        FoodItem("canola_oil", "Canola Oil", "Oils & Fats", 884, 0.0, 0.0, 100.0, 0.0, 0.0, 0.0, "🌾"),
        
        // Snacks (10 items)
        FoodItem("popcorn", "Air-Popped Popcorn", "Snacks", 387, 13.0, 78.0, 4.5, 14.5, 0.9, 8.0, "🍿"),
        FoodItem("dark_chocolate", "Dark Chocolate (70%)", "Snacks", 598, 8.0, 46.0, 43.0, 11.0, 24.0, 6.0, "🍫"),
        FoodItem("rice_cakes", "Rice Cakes", "Snacks", 387, 8.0, 82.0, 2.8, 1.4, 1.4, 5.0, "🍘"),
        FoodItem("protein_bar", "Protein Bar", "Snacks", 406, 25.0, 44.0, 14.0, 6.0, 20.0, 200.0, "🍫"),
        FoodItem("granola_bar", "Granola Bar", "Snacks", 471, 10.0, 64.0, 20.0, 6.0, 29.0, 79.0, "🍫"),
        FoodItem("trail_mix", "Trail Mix", "Snacks", 462, 13.0, 45.0, 29.0, 7.0, 24.0, 343.0, "🥜"),
        FoodItem("beef_jerky", "Beef Jerky", "Snacks", 410, 33.0, 11.0, 25.0, 2.0, 6.0, 1590.0, "🥩"),
        FoodItem("apple_chips", "Apple Chips", "Snacks", 243, 2.2, 66.0, 0.3, 5.4, 57.0, 3.0),
        FoodItem("kale_chips", "Kale Chips", "Snacks", 540, 35.0, 44.0, 32.0, 15.0, 0.0, 316.0, "🥬"),
        FoodItem("seaweed_snack", "Seaweed Snack", "Snacks", 264, 56.0, 23.0, 2.9, 58.0, 0.0, 312.0, "🌿"),
        
        // Desserts (8 items)
        FoodItem("ice_cream", "Vanilla Ice Cream", "Desserts", 207, 3.5, 24.0, 11.0, 0.7, 21.0, 80.0, "🍦"),
        FoodItem("cake", "Chocolate Cake", "Desserts", 352, 5.0, 56.0, 14.0, 3.0, 38.0, 469.0, "🍰"),
        FoodItem("cookies", "Chocolate Chip Cookies", "Desserts", 502, 5.0, 66.0, 25.0, 2.0, 36.0, 400.0, "🍪"),
        FoodItem("donut", "Glazed Donut", "Desserts", 452, 4.9, 51.0, 25.0, 1.4, 23.0, 373.0, "🍩"),
        FoodItem("cheesecake", "Cheesecake", "Desserts", 321, 5.5, 26.0, 23.0, 0.8, 20.0, 438.0, "🍰"),
        FoodItem("brownie", "Brownie", "Desserts", 466, 6.0, 63.0, 22.0, 3.0, 48.0, 295.0, "🍫"),
        FoodItem("muffin", "Blueberry Muffin", "Desserts", 277, 4.7, 39.0, 12.0, 1.5, 18.0, 255.0, "🧁"),
        FoodItem("pie", "Apple Pie", "Desserts", 237, 2.4, 34.0, 11.0, 1.6, 16.0, 327.0, "🥧")
    )
    */
}



