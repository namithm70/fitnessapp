package com.fitnessss.fitlife.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "foods")
data class Food(
    @PrimaryKey
    val id: String,
    val name: String,
    val brand: String? = null,
    val barcode: String? = null,
    val servingSize: Float,
    val servingUnit: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f,
    val imageUrl: String? = null,
    val isVerified: Boolean = false
)

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val nutritionInfo: NutritionInfo,
    val prepTime: Int, // minutes
    val cookTime: Int, // minutes
    val servings: Int,
    val difficulty: Difficulty,
    val tags: List<String>,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isCustom: Boolean = false,
    val createdBy: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class RecipeStep(
    val stepNumber: Int,
    val instruction: String,
    val duration: Int? = null // seconds
)

data class MealIngredient(
    val foodId: String,
    val amount: Float,
    val unit: String
)

data class NutritionInfo(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f
)

@Entity(tableName = "nutrition_logs")
data class NutritionLog(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: LocalDate,
    val mealType: MealType,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val notes: String? = null,
    val loggedAt: Long = System.currentTimeMillis()
)

data class LoggedFood(
    val foodId: String,
    val amount: Float,
    val unit: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

@Entity(tableName = "water_logs")
data class WaterLog(
    @PrimaryKey
    val id: String,
    val userId: String,
    val date: LocalDate,
    val amount: Float, // ml
    val loggedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey
    val id: String,
    val userId: String,
    val weekStartDate: LocalDate,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val createdAt: Long = System.currentTimeMillis()
)

data class PlannedMeal(
    val mealId: String,
    val dayOfWeek: Int, // 1-7
    val mealType: MealType,
    val servings: Int = 1
)

enum class MealType {
    BREAKFAST, LUNCH, DINNER, SNACK
}

@Entity(tableName = "nutrition_goals")
data class NutritionGoals(
    @PrimaryKey
    val userId: String,
    val dailyCalories: Float,
    val dailyProtein: Float,
    val dailyCarbs: Float,
    val dailyFat: Float,
    val dailyWater: Float, // ml
    val updatedAt: Long = System.currentTimeMillis()
)
