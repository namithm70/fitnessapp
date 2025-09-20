package com.fitnessss.fitlife.ui.screens.nutrition

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionManager
import com.fitnessss.fitlife.ui.screens.nutrition.FoodItem
import com.fitnessss.fitlife.ui.screens.nutrition.LoggedFood
import com.fitnessss.fitlife.ui.screens.nutrition.MealType2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    navController: NavController,
    mealType: String? = null
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var currentMealType by remember { mutableStateOf(mealType) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Food Search")
                        if (currentMealType != null) {
                            Text(
                                text = "Adding to ${currentMealType?.replace("_", " ")?.lowercase()?.capitalize()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Scan barcode */ }) {
                        Icon(Icons.Filled.QrCode, contentDescription = "Scan Barcode")
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
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search for food...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true
                )
            }
            
            item {
                // Category filters
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(com.fitnessss.fitlife.ui.screens.nutrition.foodCategories) { category ->
                        FilterChip(
                            onClick = { selectedCategory = category.name },
                            label = { Text(category.name) },
                            selected = selectedCategory == category.name
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            
            item {
                // Quick add section
                if (searchQuery.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Quick Add",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickAddButton(
                                    text = "Scan Barcode",
                                    icon = Icons.Filled.QrCode,
                                    onClick = { /* Scan barcode */ },
                                    modifier = Modifier.weight(1f)
                                )
                                QuickAddButton(
                                    text = "Recent Foods",
                                    icon = Icons.Filled.Schedule,
                                    onClick = { /* Show recent */ },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            
            // Search results or popular foods
            if (searchQuery.isNotEmpty()) {
                items(filteredFoods(searchQuery)) { food ->
                    FoodItemCard(
                        food = food,
                        onAdd = { 
                            // Add food to nutrition log
                            addFoodToLog(food, currentMealType)
                            navController.navigateUp()
                        }
                    )
                }
            } else {
                item {
                    Text(
                        text = "Popular Foods",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                items(popularFoods) { food ->
                    FoodItemCard(
                        food = food,
                        onAdd = { 
                            // Add food to nutrition log
                            addFoodToLog(food, currentMealType)
                            navController.navigateUp()
                        }
                    )
                }
            }
            
            item {
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemCard(
    food: SampleFood,
    onAdd: () -> Unit
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
            // Food image placeholder
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
            
            Spacer(Modifier.width(16.dp))
            
            // Food details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${food.servingSize}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                
                // Nutrition info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    NutritionInfo(label = "Cal", value = "${food.calories}")
                    NutritionInfo(label = "Protein", value = "${food.protein}g")
                    NutritionInfo(label = "Carbs", value = "${food.carbs}g")
                    NutritionInfo(label = "Fat", value = "${food.fat}g")
                }
            }
            
            // Add button
            IconButton(
                onClick = onAdd
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add to log",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun NutritionInfo(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

data class SampleFood(
    val name: String,
    val servingSize: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

// Removed duplicate foodCategories - using the one from NutritionScreen

private val popularFoods = listOf(
    // Popular Indian Staples
    SampleFood("Roti (Whole Wheat)", "1 piece (30g)", 85, 3, 15, 1),
    SampleFood("Chapati", "1 piece (30g)", 85, 3, 15, 1),
    SampleFood("Basmati Rice", "1 cup cooked (195g)", 205, 4, 45, 0),
    SampleFood("Toor Dal", "1 cup cooked (197g)", 198, 11, 37, 1),
    SampleFood("Moong Dal", "1 cup cooked (202g)", 212, 14, 38, 1),
    SampleFood("Paneer", "100g", 265, 18, 4, 20),
    SampleFood("Curd (Dahi)", "1 cup (245g)", 137, 14, 8, 7),
    SampleFood("Ghee", "1 tbsp (14g)", 112, 0, 0, 13),
    
    // Popular Indian Vegetables
    SampleFood("Okra (Bhindi)", "1 cup (100g)", 33, 2, 7, 0),
    SampleFood("Spinach (Palak)", "1 cup (100g)", 23, 3, 4, 0),
    SampleFood("Potato", "1 medium (100g)", 77, 2, 17, 0),
    SampleFood("Onion", "1 medium (110g)", 44, 1, 10, 0),
    SampleFood("Tomato", "1 medium (123g)", 22, 1, 5, 0),
    SampleFood("Bitter Gourd (Karela)", "1 cup (100g)", 17, 1, 3, 0),
    SampleFood("Bottle Gourd (Lauki)", "1 cup (100g)", 15, 1, 3, 0),
    SampleFood("Drumstick (Moringa)", "1 cup (100g)", 37, 2, 8, 0),
    
    // Popular Indian Fruits
    SampleFood("Mango", "1 cup (165g)", 99, 1, 25, 0),
    SampleFood("Banana", "1 medium (118g)", 105, 1, 27, 0),
    SampleFood("Apple", "1 medium (182g)", 95, 0, 25, 0),
    SampleFood("Amla (Indian Gooseberry)", "1 medium (15g)", 13, 0, 3, 0),
    SampleFood("Jamun (Black Plum)", "1 cup (100g)", 62, 0, 15, 0),
    SampleFood("Guava", "1 medium (55g)", 37, 1, 8, 0),
    SampleFood("Pomegranate", "1/2 cup (87g)", 72, 1, 16, 1),
    SampleFood("Custard Apple", "1 medium (100g)", 94, 2, 24, 0),
    SampleFood("Sapota (Chikoo)", "1 medium (100g)", 83, 0, 20, 0),
    
    // Popular Indian Breads
    SampleFood("Naan", "1 piece (90g)", 262, 6, 45, 4),
    SampleFood("Paratha", "1 piece (50g)", 180, 4, 25, 6),
    SampleFood("Poori", "1 piece (25g)", 120, 3, 18, 4),
    SampleFood("Dosa", "1 piece (100g)", 133, 4, 25, 2),
    SampleFood("Idli", "2 pieces (100g)", 106, 4, 20, 1),
    SampleFood("Uttapam", "1 piece (100g)", 150, 5, 28, 2),
    
    // Popular Indian Lentils
    SampleFood("Masoor Dal", "1 cup cooked (198g)", 230, 18, 40, 1),
    SampleFood("Urad Dal", "1 cup cooked (202g)", 189, 13, 34, 1),
    SampleFood("Chana Dal", "1 cup cooked (164g)", 269, 14, 45, 4),
    SampleFood("Rajma (Kidney Beans)", "1 cup cooked (177g)", 225, 15, 40, 1),
    SampleFood("Chickpeas (Kabuli)", "1 cup cooked (164g)", 269, 14, 45, 4),
    SampleFood("Black Chickpeas (Kala Chana)", "1 cup cooked (164g)", 269, 14, 45, 4),
    
    // Popular Indian Dairy & Beverages
    SampleFood("Buttermilk (Chaas)", "1 cup (245g)", 99, 8, 12, 2),
    SampleFood("Lassi", "1 cup (245g)", 150, 8, 20, 5),
    SampleFood("Rasgulla", "1 piece (50g)", 186, 3, 35, 2),
    SampleFood("Gulab Jamun", "1 piece (40g)", 150, 2, 25, 4),
    SampleFood("Kheer", "1 cup (200g)", 300, 8, 45, 8),
    SampleFood("Rasmalai", "1 piece (60g)", 200, 4, 30, 6),
    
    // Popular Indian Nuts & Seeds
    SampleFood("Almonds (Badam)", "1/4 cup (28g)", 164, 6, 6, 14),
    SampleFood("Cashews (Kaju)", "1/4 cup (28g)", 157, 5, 9, 12),
    SampleFood("Pistachios (Pista)", "1/4 cup (28g)", 159, 6, 8, 13),
    SampleFood("Walnuts (Akhrot)", "1/4 cup (28g)", 185, 4, 4, 18),
    SampleFood("Pumpkin Seeds", "1/4 cup (28g)", 151, 7, 5, 13),
    SampleFood("Sesame Seeds (Til)", "1/4 cup (28g)", 160, 5, 6, 14),
    
    // Popular Indian Snacks
    SampleFood("Samosa", "1 piece (50g)", 200, 4, 25, 10),
    SampleFood("Pakora", "1 piece (30g)", 120, 3, 15, 6),
    SampleFood("Bhel Puri", "1 cup (100g)", 180, 4, 25, 7),
    SampleFood("Pani Puri", "1 piece (15g)", 60, 1, 8, 2),
    SampleFood("Vada Pav", "1 piece (150g)", 350, 8, 45, 12),
    SampleFood("Pav Bhaji", "1 cup (200g)", 320, 8, 40, 12),
    SampleFood("Chaat", "1 cup (150g)", 250, 6, 35, 8),
    
    // Popular Indian Main Dishes
    SampleFood("Butter Chicken", "1 cup (200g)", 350, 25, 15, 20),
    SampleFood("Chicken Tikka", "1 cup (150g)", 280, 35, 8, 12),
    SampleFood("Paneer Tikka", "1 cup (150g)", 320, 18, 12, 22),
    SampleFood("Dal Makhani", "1 cup (200g)", 280, 12, 35, 8),
    SampleFood("Rajma Masala", "1 cup (200g)", 320, 14, 45, 6),
    SampleFood("Chana Masala", "1 cup (200g)", 280, 12, 40, 6),
    SampleFood("Palak Paneer", "1 cup (200g)", 280, 16, 15, 18),
    
    // Popular Indian Rice Dishes
    SampleFood("Biryani", "1 cup (200g)", 350, 12, 45, 10),
    SampleFood("Pulao", "1 cup (200g)", 280, 6, 50, 4),
    SampleFood("Jeera Rice", "1 cup (200g)", 260, 4, 48, 4),
    SampleFood("Lemon Rice", "1 cup (200g)", 280, 5, 50, 4),
    SampleFood("Coconut Rice", "1 cup (200g)", 320, 6, 52, 8),
    SampleFood("Curd Rice", "1 cup (200g)", 240, 6, 42, 4),
    
    // Popular Indian Grains
    SampleFood("Brown Basmati Rice", "1 cup cooked (195g)", 216, 5, 45, 2),
    SampleFood("Millet (Bajra)", "1 cup cooked (174g)", 207, 6, 41, 2),
    SampleFood("Finger Millet (Ragi)", "1 cup cooked (100g)", 119, 3, 25, 1),
    SampleFood("Sorghum (Jowar)", "1 cup cooked (192g)", 198, 6, 43, 2),
    SampleFood("Amaranth (Rajgira)", "1 cup cooked (246g)", 251, 9, 46, 4),
    SampleFood("Buckwheat (Kuttu)", "1 cup cooked (168g)", 155, 6, 33, 1),
    
    // Popular Indian Greens
    SampleFood("Mustard Greens (Sarson)", "1 cup (100g)", 27, 3, 5, 0),
    SampleFood("Amaranth Leaves (Chaulai)", "1 cup (100g)", 23, 3, 4, 0),
    SampleFood("Fenugreek Leaves (Methi)", "1 cup (100g)", 49, 4, 6, 0),
    SampleFood("Mint Leaves (Pudina)", "1 cup (100g)", 44, 4, 8, 0),
    SampleFood("Coriander Leaves (Dhania)", "1 cup (100g)", 23, 2, 4, 0),
    SampleFood("Basil Leaves (Tulsi)", "1 cup (100g)", 22, 3, 4, 0),
    
    // Popular Indian Spices
    SampleFood("Turmeric Powder", "1 tsp (2g)", 8, 0, 1, 0),
    SampleFood("Cumin Seeds", "1 tsp (2g)", 8, 0, 1, 0),
    SampleFood("Cardamom", "1 tsp (2g)", 6, 0, 1, 0),
    SampleFood("Cinnamon", "1 tsp (2g)", 6, 0, 2, 0),
    SampleFood("Black Pepper", "1 tsp (2g)", 6, 0, 1, 0),
    SampleFood("Ginger", "1 tbsp (6g)", 5, 0, 1, 0),
    SampleFood("Garlic", "1 clove (3g)", 4, 0, 1, 0),
    
    // Popular International Foods (keeping some for variety)
    SampleFood("Chicken Breast", "1 breast (174g)", 284, 53, 0, 6),
    SampleFood("Salmon", "3 oz (85g)", 175, 22, 0, 10),
    SampleFood("Greek Yogurt", "1 cup (245g)", 130, 22, 9, 4),
    SampleFood("Eggs", "2 large (100g)", 143, 13, 1, 10),
    SampleFood("Avocado", "1/2 (100g)", 160, 2, 9, 15),
    SampleFood("Orange", "1 medium (131g)", 62, 1, 15, 0),
    SampleFood("Pineapple", "1 cup (165g)", 82, 1, 22, 0),
    SampleFood("Strawberries", "1 cup (152g)", 49, 1, 12, 0),
    SampleFood("Grapes", "1 cup (151g)", 104, 1, 27, 0),
    SampleFood("Watermelon", "1 cup (154g)", 46, 1, 12, 0),
    SampleFood("Papaya", "1 cup (145g)", 62, 1, 16, 0),
    SampleFood("Lychee", "1 cup (190g)", 125, 1, 31, 1),
    SampleFood("Jackfruit", "1 cup (165g)", 155, 3, 40, 1),
    SampleFood("Dragon Fruit", "1 cup (227g)", 136, 3, 29, 0),
    SampleFood("Star Fruit", "1 medium (91g)", 28, 1, 6, 0),
    SampleFood("Bael (Wood Apple)", "1 medium (100g)", 137, 2, 32, 0),
    SampleFood("Ber (Indian Jujube)", "1 cup (100g)", 79, 1, 20, 0),
    SampleFood("Karonda", "1 cup (100g)", 44, 1, 10, 0),
    SampleFood("Phalsa", "1 cup (100g)", 47, 1, 11, 0),
    SampleFood("Ridge Gourd (Turai)", "1 cup (100g)", 20, 1, 4, 0),
    SampleFood("Snake Gourd (Chichinda)", "1 cup (100g)", 18, 1, 4, 0),
    SampleFood("Pointed Gourd (Parwal)", "1 cup (100g)", 24, 1, 5, 0),
    SampleFood("Ivy Gourd (Tindora)", "1 cup (100g)", 18, 1, 4, 0),
    SampleFood("Raw Banana", "1 medium (100g)", 89, 1, 23, 0),
    SampleFood("Plantain", "1 medium (100g)", 122, 1, 32, 0),
    SampleFood("Colocasia (Arbi)", "1 cup (100g)", 112, 2, 26, 0),
    SampleFood("Yam (Suran)", "1 cup (100g)", 118, 2, 28, 0),
    SampleFood("Sweet Potato (Shakarkandi)", "1 medium (100g)", 86, 2, 20, 0),
    SampleFood("Taro Root (Arbi)", "1 cup (100g)", 112, 2, 26, 0),
    SampleFood("Elephant Foot Yam", "1 cup (100g)", 97, 2, 23, 0),
    SampleFood("Curry Leaves", "1 cup (100g)", 108, 16, 18, 1),
    SampleFood("Jasmine Rice", "1 cup cooked (195g)", 205, 4, 45, 0),
    SampleFood("Red Rice", "1 cup cooked (195g)", 216, 5, 45, 2),
    SampleFood("Black Rice", "1 cup cooked (195g)", 216, 5, 45, 2),
    SampleFood("Pearl Millet", "1 cup cooked (174g)", 207, 6, 41, 2),
    SampleFood("Bhatura", "1 piece (80g)", 280, 6, 45, 8),
    SampleFood("Appam", "1 piece (80g)", 120, 3, 22, 2),
    SampleFood("Green Peas", "1 cup (145g)", 118, 8, 21, 0),
    SampleFood("Soybeans", "1 cup cooked (172g)", 298, 29, 17, 15),
    SampleFood("Moth Beans", "1 cup cooked (177g)", 187, 13, 34, 1),
    SampleFood("Horse Gram", "1 cup cooked (177g)", 187, 13, 34, 1),
    SampleFood("Jalebi", "1 piece (30g)", 120, 1, 22, 3),
    SampleFood("Coriander Seeds", "1 tsp (2g)", 5, 0, 1, 0),
    SampleFood("Green Chilies", "1 medium (5g)", 2, 0, 0, 0),
    SampleFood("Red Chilies", "1 medium (5g)", 2, 0, 0, 0),
    SampleFood("Pine Nuts (Chilgoza)", "1/4 cup (28g)", 191, 4, 4, 19),
    SampleFood("Sunflower Seeds", "1/4 cup (28g)", 164, 6, 6, 14),
    SampleFood("Flax Seeds", "1/4 cup (28g)", 150, 5, 8, 12),
    SampleFood("Chia Seeds", "1/4 cup (28g)", 137, 5, 12, 9),
    SampleFood("Dahi Puri", "1 cup (100g)", 200, 5, 28, 8),
    SampleFood("Sev Puri", "1 cup (100g)", 220, 4, 30, 8),
    SampleFood("Ragda Pattice", "1 cup (150g)", 280, 8, 35, 10),
    SampleFood("Papdi Chaat", "1 cup (100g)", 180, 4, 25, 6),
    SampleFood("Aloo Gobi", "1 cup (200g)", 180, 6, 25, 6),
    SampleFood("Baingan Bharta", "1 cup (200g)", 160, 4, 20, 6),
    SampleFood("Mushroom Masala", "1 cup (200g)", 180, 8, 20, 6),
    SampleFood("Mixed Vegetable Curry", "1 cup (200g)", 160, 6, 22, 4),
    SampleFood("Tomato Rice", "1 cup (200g)", 260, 4, 48, 4),
    SampleFood("Fried Rice", "1 cup (200g)", 300, 8, 45, 8)
)

private fun filteredFoods(query: String): List<SampleFood> {
    return popularFoods.filter { food ->
        food.name.contains(query, ignoreCase = true)
    }
}

private fun addFoodToLog(food: SampleFood, mealType: String?) {
    // Convert SampleFood to FoodItem and add to NutritionManager
    val foodItem = FoodItem(
        id = food.name.lowercase().replace(" ", "_"),
        name = food.name,
        category = "General",
        calories = food.calories,
        protein = food.protein.toDouble(),
        carbs = food.carbs.toDouble(),
        fat = food.fat.toDouble()
    )
    
    val mealTypeEnum = when (mealType) {
        "BREAKFAST" -> MealType2.BREAKFAST
        "LUNCH" -> MealType2.LUNCH
        "DINNER" -> MealType2.DINNER
        "SNACK" -> MealType2.SNACK
        else -> MealType2.SNACK
    }
    
    val loggedFood = LoggedFood(
        foodItem = foodItem,
        portionGrams = 100, // Default 100g portion
        mealType = mealTypeEnum
    )
    
    NutritionManager.addFood(loggedFood)
}

private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
}
