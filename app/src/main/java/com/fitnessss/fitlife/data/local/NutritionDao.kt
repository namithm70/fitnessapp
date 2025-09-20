package com.fitnessss.fitlife.data.local

import androidx.room.*
import com.fitnessss.fitlife.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface NutritionDao {
    // Food operations
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    fun searchFoods(query: String): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE barcode = :barcode")
    suspend fun getFoodByBarcode(barcode: String): Food?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: Food)

    @Update
    suspend fun updateFood(food: Food)

    @Delete
    suspend fun deleteFood(food: Food)

    // Meal operations
    @Query("SELECT * FROM meals ORDER BY createdAt DESC")
    fun getAllMeals(): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: String): Meal?

    @Query("SELECT * FROM meals WHERE tags LIKE '%' || :tag || '%'")
    fun getMealsByTag(tag: String): Flow<List<Meal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)

    @Update
    suspend fun updateMeal(meal: Meal)

    @Delete
    suspend fun deleteMeal(meal: Meal)

    // Nutrition log operations
    @Query("SELECT * FROM nutrition_logs WHERE userId = :userId AND date = :date ORDER BY loggedAt DESC")
    fun getNutritionLogsByDate(userId: String, date: LocalDate): Flow<List<NutritionLog>>

    @Query("SELECT * FROM nutrition_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getNutritionLogsByDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<NutritionLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionLog(log: NutritionLog)

    @Update
    suspend fun updateNutritionLog(log: NutritionLog)

    @Delete
    suspend fun deleteNutritionLog(log: NutritionLog)

    // Water log operations
    @Query("SELECT * FROM water_logs WHERE userId = :userId AND date = :date ORDER BY loggedAt DESC")
    fun getWaterLogsByDate(userId: String, date: LocalDate): Flow<List<WaterLog>>

    @Query("SELECT SUM(amount) FROM water_logs WHERE userId = :userId AND date = :date")
    suspend fun getTotalWaterIntake(userId: String, date: LocalDate): Float?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWaterLog(log: WaterLog)

    @Update
    suspend fun updateWaterLog(log: WaterLog)

    @Delete
    suspend fun deleteWaterLog(log: WaterLog)

    // Meal plan operations
    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND weekStartDate = :weekStartDate")
    suspend fun getMealPlanByWeek(userId: String, weekStartDate: LocalDate): MealPlan?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan)

    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)

    @Delete
    suspend fun deleteMealPlan(mealPlan: MealPlan)

    // Nutrition goals operations
    @Query("SELECT * FROM nutrition_goals WHERE userId = :userId")
    suspend fun getNutritionGoals(userId: String): NutritionGoals?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNutritionGoals(goals: NutritionGoals)

    @Update
    suspend fun updateNutritionGoals(goals: NutritionGoals)

    // Statistics
    @Query("SELECT SUM(totalCalories) FROM nutrition_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalCaloriesInRange(userId: String, startDate: LocalDate, endDate: LocalDate): Float?

    @Query("SELECT AVG(totalCalories) FROM nutrition_logs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageCaloriesInRange(userId: String, startDate: LocalDate, endDate: LocalDate): Float?
}
