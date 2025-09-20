package com.fitnessss.fitlife.data.service

import com.fitnessss.fitlife.data.*
import com.fitnessss.fitlife.data.model.ActivityHistory
import com.fitnessss.fitlife.data.model.LoggedFood
import com.fitnessss.fitlife.data.model.MealType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.datetime.LocalDate as KLocalDate
import kotlinx.datetime.LocalDateTime as KLocalDateTime
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDate
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    
    private val currentUserId: String?
        get() = auth.currentUser?.uid
    
    // User Profile Management
    suspend fun syncUserProfile(userProfile: UserProfile) {
        currentUserId?.let { userId ->
                            val userData = mapOf(
                    "name" to userProfile.name,
                    "email" to userProfile.email,
                    "age" to userProfile.age,
                    "weight" to userProfile.weight,
                    "height" to userProfile.height,
                    "fitnessGoal" to userProfile.fitnessGoal.name,
                    "fitnessLevel" to userProfile.fitnessLevel.name,
                    "gender" to userProfile.gender.name,
                    "bio" to userProfile.bio,
                    "location" to userProfile.location,
                    "joinDate" to userProfile.joinDate.toString(),
                    "lastUpdated" to LocalDateTime.now().toString()
                )
            
            firestore.collection("users").document(userId)
                .collection("profile").document("data")
                .set(userData)
                .await()
        }
    }
    
    suspend fun getUserProfile(): UserProfile? {
        return try {
            currentUserId?.let { userId ->
                val document = firestore.collection("users").document(userId)
                    .collection("profile").document("data")
                    .get()
                    .await()
                
                if (document.exists()) {
                    UserProfile(
                        name = document.getString("name") ?: "",
                        email = document.getString("email") ?: "",
                        age = document.getLong("age")?.toInt() ?: 25,
                        weight = document.getDouble("weight") ?: 70.0,
                        height = document.getDouble("height") ?: 170.0,
                        fitnessGoal = FitnessGoal.valueOf(document.getString("fitnessGoal") ?: "LOSE_WEIGHT"),
                        fitnessLevel = FitnessLevel.valueOf(document.getString("fitnessLevel") ?: "BEGINNER"),
                        gender = Gender.valueOf(document.getString("gender") ?: "OTHER"),
                        bio = document.getString("bio") ?: "",
                        location = document.getString("location") ?: "",
                        joinDate = LocalDate.parse(document.getString("joinDate") ?: LocalDate.now().toString())
                    )
                } else null
            }
        } catch (e: Exception) {
            println("Error getting user profile: ${e.message}")
            null
        }
    }
    
    // Workout Sessions Management
    suspend fun syncWorkoutSessions(workoutSessions: List<WorkoutSession>) {
        currentUserId?.let { userId ->
            val batch = firestore.batch()
            
            // Clear existing workout sessions
            val existingDocs = firestore.collection("users").document(userId)
                .collection("workoutSessions")
                .get()
                .await()
            
            existingDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Add new workout sessions
            workoutSessions.forEach { session ->
                val sessionData = mapOf(
                    "id" to session.id,
                    "workoutType" to session.workoutType,
                    "workoutName" to session.workoutName,
                    "duration" to session.duration,
                    "exercisesCompleted" to session.exercisesCompleted,
                    "setsCompleted" to session.setsCompleted,
                    "caloriesBurned" to session.caloriesBurned,
                    "date" to session.date.toString(),
                    "exercises" to session.exercises.map { exercise ->
                        mapOf(
                            "name" to exercise.name,
                            "sets" to exercise.sets,
                            "reps" to exercise.reps
                        )
                    }
                )
                
                val docRef = firestore.collection("users").document(userId)
                    .collection("workoutSessions")
                    .document(session.id)
                
                batch.set(docRef, sessionData)
            }
            
            batch.commit().await()
        }
    }
    
    fun getWorkoutSessionsFlow(): Flow<List<WorkoutSession>> = flow {
        try {
            currentUserId?.let { userId ->
                val snapshot = firestore.collection("users").document(userId)
                    .collection("workoutSessions")
                    .get()
                    .await()
                
                val sessions = snapshot.documents.mapNotNull { doc ->
                    try {
                        WorkoutSession(
                            id = doc.getString("id") ?: "",
                            workoutType = doc.getString("workoutType") ?: "General",
                            workoutName = doc.getString("workoutName") ?: "Workout",
                            duration = doc.getLong("duration")?.toInt() ?: 0,
                            exercisesCompleted = doc.getLong("exercisesCompleted")?.toInt() ?: 0,
                            setsCompleted = doc.getLong("setsCompleted")?.toInt() ?: 0,
                            caloriesBurned = doc.getLong("caloriesBurned")?.toInt() ?: 0,
                            date = LocalDateTime.parse(doc.getString("date") ?: LocalDateTime.now().toString()),
                            exercises = (doc.get("exercises") as? List<Map<String, Any>>)?.map { exerciseData ->
                                CompletedExercise(
                                    name = exerciseData["name"] as? String ?: "",
                                    sets = (exerciseData["sets"] as? Number)?.toInt() ?: 0,
                                    reps = (exerciseData["reps"] as? List<Number>)?.map { it.toInt() } ?: emptyList(),
                                    weight = (exerciseData["weight"] as? List<Number>)?.map { it.toDouble() } ?: emptyList(),
                                    duration = (exerciseData["duration"] as? Number)?.toInt() ?: 0
                                )
                            } ?: emptyList()
                        )
                    } catch (e: Exception) {
                        println("Error parsing workout session: ${e.message}")
                        null
                    }
                }
                
                emit(sessions)
                println("Loaded ${sessions.size} workout sessions from Firebase")
            } ?: emit(emptyList())
        } catch (e: Exception) {
            println("Error loading workout sessions: ${e.message}")
            emit(emptyList())
        }
    }
    
    // Weight Entries Management
    suspend fun syncWeightEntries(weightEntries: List<WeightEntry>) {
        currentUserId?.let { userId ->
            val batch = firestore.batch()
            
            // Clear existing weight entries
            val existingDocs = firestore.collection("users").document(userId)
                .collection("weightEntries")
                .get()
                .await()
            
            existingDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Add new weight entries
            weightEntries.forEach { entry ->
                val entryData = mapOf(
                    "id" to entry.id,
                    "weight" to entry.weight,
                    "date" to entry.date.toString(),
                    "notes" to entry.notes
                )
                
                val docRef = firestore.collection("users").document(userId)
                    .collection("weightEntries")
                    .document(entry.id)
                
                batch.set(docRef, entryData)
            }
            
            batch.commit().await()
        }
    }
    
    fun getWeightEntriesFlow(): Flow<List<WeightEntry>> = flow {
        try {
            currentUserId?.let { userId ->
                val snapshot = firestore.collection("users").document(userId)
                    .collection("weightEntries")
                    .get()
                    .await()
                
                val entries = snapshot.documents.mapNotNull { doc ->
                    try {
                        WeightEntry(
                            id = doc.getString("id") ?: "",
                            weight = doc.getDouble("weight") ?: 0.0,
                            date = LocalDate.parse(doc.getString("date") ?: LocalDate.now().toString()),
                            notes = doc.getString("notes") ?: ""
                        )
                    } catch (e: Exception) {
                        println("Error parsing weight entry: ${e.message}")
                        null
                    }
                }
                
                emit(entries)
                println("Loaded ${entries.size} weight entries from Firebase")
            } ?: emit(emptyList())
        } catch (e: Exception) {
            println("Error loading weight entries: ${e.message}")
            emit(emptyList())
        }
    }
    
    // Body Measurements Management
    suspend fun syncBodyMeasurements(measurements: List<BodyMeasurement>) {
        currentUserId?.let { userId ->
            val batch = firestore.batch()
            
            // Clear existing measurements
            val existingDocs = firestore.collection("users").document(userId)
                .collection("bodyMeasurements")
                .get()
                .await()
            
            existingDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Add new measurements
            measurements.forEach { measurement ->
                val measurementData = mapOf(
                    "id" to measurement.id,
                    "chest" to measurement.chest,
                    "waist" to measurement.waist,
                    "hips" to measurement.hips,
                    "biceps" to measurement.biceps,
                    "thighs" to measurement.thighs,
                    "date" to measurement.date.toString()
                )
                
                val docRef = firestore.collection("users").document(userId)
                    .collection("bodyMeasurements")
                    .document(measurement.id)
                
                batch.set(docRef, measurementData)
            }
            
            batch.commit().await()
        }
    }
    
    fun getBodyMeasurementsFlow(): Flow<List<BodyMeasurement>> = flow {
        try {
            currentUserId?.let { userId ->
                val snapshot = firestore.collection("users").document(userId)
                    .collection("bodyMeasurements")
                    .get()
                    .await()
                
                val measurements = snapshot.documents.mapNotNull { doc ->
                    try {
                        BodyMeasurement(
                            id = doc.getString("id") ?: "",
                            chest = doc.getDouble("chest") ?: 0.0,
                            waist = doc.getDouble("waist") ?: 0.0,
                            hips = doc.getDouble("hips") ?: 0.0,
                            biceps = doc.getDouble("biceps") ?: 0.0,
                            thighs = doc.getDouble("thighs") ?: 0.0,
                            date = LocalDate.parse(doc.getString("date") ?: LocalDate.now().toString())
                        )
                    } catch (e: Exception) {
                        println("Error parsing body measurement: ${e.message}")
                        null
                    }
                }
                
                emit(measurements)
                println("Loaded ${measurements.size} body measurements from Firebase")
            } ?: emit(emptyList())
        } catch (e: Exception) {
            println("Error loading body measurements: ${e.message}")
            emit(emptyList())
        }
    }
    
    // Activity History Management
    suspend fun syncActivityHistory(activities: List<ActivityHistory>) {
        currentUserId?.let { userId ->
            val batch = firestore.batch()
            
            // Clear existing activities
            val existingDocs = firestore.collection("users").document(userId)
                .collection("activityHistory")
                .get()
                .await()
            
            existingDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Add new activities
            activities.forEach { activity ->
                val activityData = mapOf(
                    "id" to activity.id,
                    "userId" to activity.userId,
                    "title" to activity.title,
                    "description" to activity.description,
                    "activityType" to activity.activityType.name,
                    "date" to activity.date.toString(),
                    "duration" to activity.duration,
                    "caloriesBurned" to activity.calories,
                    "notes" to activity.notes
                )
                
                val docRef = firestore.collection("users").document(userId)
                    .collection("activityHistory")
                    .document(activity.id)
                
                batch.set(docRef, activityData)
            }
            
            batch.commit().await()
        }
    }
    
    fun getActivityHistoryFlow(): Flow<List<ActivityHistory>> = flow {
        // For now, return empty list to avoid Flow suspension issues
        // TODO: Implement proper real-time listener
        emit(emptyList())
    }
    
    // Nutrition Data Management
    suspend fun syncNutritionData(loggedFoods: List<com.fitnessss.fitlife.ui.screens.nutrition.LoggedFood>, waterGlasses: Int) {
        currentUserId?.let { userId ->
            val batch = firestore.batch()
            
            // Clear existing nutrition data
            val existingDocs = firestore.collection("users").document(userId)
                .collection("nutrition")
                .get()
                .await()
            
            existingDocs.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            
            // Add logged foods
            loggedFoods.forEach { food ->
                val foodData = mapOf(
                    "id" to food.foodItem.id,
                    "name" to food.foodItem.name,
                    "calories" to food.foodItem.calories,
                    "protein" to food.foodItem.protein,
                    "carbs" to food.foodItem.carbs,
                    "fat" to food.foodItem.fat,
                    "portionGrams" to food.portionGrams,
                    "mealType" to food.mealType.name,
                    "date" to LocalDateTime.now().toString()
                )
                
                val docRef = firestore.collection("users").document(userId)
                    .collection("nutrition")
                    .document("food_${food.foodItem.id}_${System.currentTimeMillis()}")
                
                batch.set(docRef, foodData)
            }
            
            // Add water intake
            val waterData = mapOf(
                "waterGlasses" to waterGlasses,
                "date" to LocalDate.now().toString()
            )
            
            val waterDocRef = firestore.collection("users").document(userId)
                .collection("nutrition")
                .document("water_${LocalDate.now()}")
            
            batch.set(waterDocRef, waterData)
            
            batch.commit().await()
        }
    }
    
    // Sync all data
    suspend fun syncAllData() {
        try {
            println("Starting full data sync to Firebase...")
            
            // Sync user profile
            UserProfileManager.userProfile?.let { profile ->
                syncUserProfile(profile)
                println("Synced user profile")
            }
            
            // Sync workout sessions
            syncWorkoutSessions(UserProgressManager.workoutSessions.value)
            println("Synced ${UserProgressManager.workoutSessions.value.size} workout sessions")
            
            // Sync weight entries
            syncWeightEntries(UserProgressManager.weightEntries.value)
            println("Synced ${UserProgressManager.weightEntries.value.size} weight entries")
            
            // Sync body measurements
            syncBodyMeasurements(UserProgressManager.bodyMeasurements.value)
            println("Synced ${UserProgressManager.bodyMeasurements.value.size} body measurements")
            
            // Sync nutrition data
            val nutritionManager = com.fitnessss.fitlife.ui.screens.nutrition.NutritionManager
            syncNutritionData(nutritionManager.loggedFoods, nutritionManager.waterGlasses)
            println("Synced nutrition data")
            
            println("Full data sync completed successfully!")
            
        } catch (e: Exception) {
            println("Error during full data sync: ${e.message}")
            e.printStackTrace()
        }
    }
    
    // Load all data from Firebase
    suspend fun loadAllData() {
        try {
            println("Starting full data load from Firebase...")
            
            // Load user profile
            getUserProfile()?.let { profile ->
                UserProfileManager.updateProfile(profile)
                println("Loaded user profile")
            }
            
            // Note: For real-time data, we'll use the Flow methods instead of one-time loads
            // The Flow methods will automatically update the local data when Firebase changes
            
            println("Full data load completed!")
            
        } catch (e: Exception) {
            println("Error during full data load: ${e.message}")
            e.printStackTrace()
        }
    }
}
