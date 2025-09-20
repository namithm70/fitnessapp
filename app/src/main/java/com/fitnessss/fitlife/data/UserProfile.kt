package com.fitnessss.fitlife.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate

// User Profile Data Class
data class UserProfile(
    val id: String = "current_user",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val height: Double = 0.0, // in cm
    val weight: Double = 0.0, // in kg
    val fitnessGoal: FitnessGoal = FitnessGoal.GENERAL_FITNESS,
    val fitnessLevel: FitnessLevel = FitnessLevel.BEGINNER,
    val workoutDaysPerWeek: Int = 3,
    val joinDate: LocalDate = LocalDate.now(),
    val profilePictureUrl: String? = null,
    val bio: String = "",
    val location: String = "",
    val gender: Gender = Gender.OTHER
)

enum class FitnessGoal {
    WEIGHT_LOSS,
    BUILD_MUSCLE,
    GENERAL_FITNESS,
    ENDURANCE,
    STRENGTH,
    FLEXIBILITY
}

enum class FitnessLevel {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

enum class Gender {
    MALE,
    FEMALE,
    OTHER
}

// User Profile Manager Singleton with persistent storage
object UserProfileManager {
    // Current user profile state
    private val _userProfile = mutableStateOf(UserProfile())
    val userProfile: UserProfile get() = _userProfile.value
    
    // Observable state for UI
    val userProfileState: MutableState<UserProfile> = _userProfile
    
    // SharedPreferences for persistent storage
    private var prefs: SharedPreferences? = null
    
    // SharedPreferences keys
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_AGE = "user_age"
    private const val KEY_USER_HEIGHT = "user_height"
    private const val KEY_USER_WEIGHT = "user_weight"
    private const val KEY_FITNESS_GOAL = "fitness_goal"
    private const val KEY_FITNESS_LEVEL = "fitness_level"
    private const val KEY_WORKOUT_DAYS = "workout_days"
    private const val KEY_JOIN_DATE = "join_date"
    private const val KEY_PROFILE_PICTURE_URL = "profile_picture_url"
    private const val KEY_BIO = "bio"
    private const val KEY_LOCATION = "location"
    private const val KEY_GENDER = "gender"
    
    // Initialize with context for persistent storage
    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("user_profile_prefs", Context.MODE_PRIVATE)
            _userProfile.value = loadUserProfile()
        }
    }
    
    // Load user profile from SharedPreferences
    private fun loadUserProfile(): UserProfile {
        val prefs = prefs ?: return UserProfile()
        
        val id = prefs.getString(KEY_USER_ID, "current_user") ?: "current_user"
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        val age = prefs.getInt(KEY_USER_AGE, 0)
        val height = prefs.getFloat(KEY_USER_HEIGHT, 0f).toDouble()
        val weight = prefs.getFloat(KEY_USER_WEIGHT, 0f).toDouble()
        val fitnessGoal = FitnessGoal.valueOf(prefs.getString(KEY_FITNESS_GOAL, FitnessGoal.GENERAL_FITNESS.name) ?: FitnessGoal.GENERAL_FITNESS.name)
        val fitnessLevel = FitnessLevel.valueOf(prefs.getString(KEY_FITNESS_LEVEL, FitnessLevel.BEGINNER.name) ?: FitnessLevel.BEGINNER.name)
        val workoutDays = prefs.getInt(KEY_WORKOUT_DAYS, 3)
        val joinDateStr = prefs.getString(KEY_JOIN_DATE, LocalDate.now().toString()) ?: LocalDate.now().toString()
        val joinDate = LocalDate.parse(joinDateStr)
        val profilePictureUrl = prefs.getString(KEY_PROFILE_PICTURE_URL, null)
        val bio = prefs.getString(KEY_BIO, "") ?: ""
        val location = prefs.getString(KEY_LOCATION, "") ?: ""
        val gender = Gender.valueOf(prefs.getString(KEY_GENDER, Gender.OTHER.name) ?: Gender.OTHER.name)
        
        return UserProfile(
            id = id,
            name = name,
            email = email,
            age = age,
            height = height,
            weight = weight,
            fitnessGoal = fitnessGoal,
            fitnessLevel = fitnessLevel,
            workoutDaysPerWeek = workoutDays,
            joinDate = joinDate,
            profilePictureUrl = profilePictureUrl,
            bio = bio,
            location = location,
            gender = gender
        )
    }
    
    // Save user profile to SharedPreferences
    private fun saveUserProfile(profile: UserProfile) {
        val prefs = prefs ?: return
        
        prefs.edit()
            .putString(KEY_USER_ID, profile.id)
            .putString(KEY_USER_NAME, profile.name)
            .putString(KEY_USER_EMAIL, profile.email)
            .putInt(KEY_USER_AGE, profile.age)
            .putFloat(KEY_USER_HEIGHT, profile.height.toFloat())
            .putFloat(KEY_USER_WEIGHT, profile.weight.toFloat())
            .putString(KEY_FITNESS_GOAL, profile.fitnessGoal.name)
            .putString(KEY_FITNESS_LEVEL, profile.fitnessLevel.name)
            .putInt(KEY_WORKOUT_DAYS, profile.workoutDaysPerWeek)
            .putString(KEY_JOIN_DATE, profile.joinDate.toString())
            .putString(KEY_PROFILE_PICTURE_URL, profile.profilePictureUrl)
            .putString(KEY_BIO, profile.bio)
            .putString(KEY_LOCATION, profile.location)
            .putString(KEY_GENDER, profile.gender.name)
            .apply()
    }
    
    // Update user profile
    fun updateProfile(updatedProfile: UserProfile) {
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    // Update specific fields
    fun updateName(name: String) {
        val updatedProfile = _userProfile.value.copy(name = name)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateEmail(email: String) {
        val updatedProfile = _userProfile.value.copy(email = email)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateAge(age: Int) {
        val updatedProfile = _userProfile.value.copy(age = age)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateHeight(height: Double) {
        val updatedProfile = _userProfile.value.copy(height = height)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateWeight(weight: Double) {
        val updatedProfile = _userProfile.value.copy(weight = weight)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateFitnessGoal(goal: FitnessGoal) {
        val updatedProfile = _userProfile.value.copy(fitnessGoal = goal)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateFitnessLevel(level: FitnessLevel) {
        val updatedProfile = _userProfile.value.copy(fitnessLevel = level)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateWorkoutDays(days: Int) {
        val updatedProfile = _userProfile.value.copy(workoutDaysPerWeek = days)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateBio(bio: String) {
        val updatedProfile = _userProfile.value.copy(bio = bio)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateLocation(location: String) {
        val updatedProfile = _userProfile.value.copy(location = location)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    fun updateGender(gender: Gender) {
        val updatedProfile = _userProfile.value.copy(gender = gender)
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    // Update multiple profile fields at once
    fun updateProfile(
        fitnessGoal: FitnessGoal? = null,
        fitnessLevel: FitnessLevel? = null,
        gender: Gender? = null,
        age: Int? = null,
        height: Double? = null,
        weight: Double? = null,
        notes: String? = null
    ) {
        val updatedProfile = _userProfile.value.copy(
            fitnessGoal = fitnessGoal ?: _userProfile.value.fitnessGoal,
            fitnessLevel = fitnessLevel ?: _userProfile.value.fitnessLevel,
            gender = gender ?: _userProfile.value.gender,
            age = age ?: _userProfile.value.age,
            height = height ?: _userProfile.value.height,
            weight = weight ?: _userProfile.value.weight,
            bio = notes ?: _userProfile.value.bio
        )
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
    }
    
    // Sync with Firebase user data
    fun syncWithFirebaseUser(firebaseUser: com.google.firebase.auth.FirebaseUser) {
        println("DEBUG: UserProfileManager - Syncing with Firebase user: ${firebaseUser.displayName}, ${firebaseUser.email}")
        val updatedProfile = _userProfile.value.copy(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            profilePictureUrl = firebaseUser.photoUrl?.toString()
        )
        _userProfile.value = updatedProfile
        saveUserProfile(updatedProfile)
        println("DEBUG: UserProfileManager - Updated profile: ${_userProfile.value}")
    }
    
    // Get display values
    fun getHeightDisplay(): String {
        return if (_userProfile.value.height > 0) {
            "${_userProfile.value.height.toInt()} cm"
        } else {
            "Not set"
        }
    }
    
    fun getWeightDisplay(): String {
        return if (_userProfile.value.weight > 0) {
            "${_userProfile.value.weight.toInt()} kg"
        } else {
            "Not set"
        }
    }
    
    fun getAgeDisplay(): String {
        return if (_userProfile.value.age > 0) {
            "${_userProfile.value.age} years"
        } else {
            "Not set"
        }
    }
    
    fun getWorkoutDaysDisplay(): String {
        return "${_userProfile.value.workoutDaysPerWeek} days/week"
    }
    
    fun getMemberSinceDisplay(): String {
        val joinDate = _userProfile.value.joinDate
        val month = joinDate.month.toString().lowercase().capitalize()
        val year = joinDate.year
        return "Member since $month $year"
    }
    
    fun getFitnessGoalDisplay(): String {
        return _userProfile.value.fitnessGoal.name.replace("_", " ").capitalize()
    }
    
    fun getFitnessLevelDisplay(): String {
        return _userProfile.value.fitnessLevel.name.capitalize()
    }
    
    /**
     * Resets user profile to default values (keeps basic profile structure)
     */
    fun resetToDefaults() {
        val resetProfile = UserProfile(
            id = "current_user",
            name = "",
            email = "",
            age = 0,
            height = 0.0,
            weight = 0.0,
            fitnessGoal = FitnessGoal.GENERAL_FITNESS,
            fitnessLevel = FitnessLevel.BEGINNER,
            workoutDaysPerWeek = 3,
            joinDate = _userProfile.value.joinDate, // Keep the original join date
            profilePictureUrl = null,
            bio = "",
            location = "",
            gender = Gender.OTHER
        )
        _userProfile.value = resetProfile
        saveUserProfile(resetProfile)
    }
    
    /**
     * Clear all user profile data
     */
    fun clearAllData() {
        prefs?.edit()?.clear()?.apply()
        _userProfile.value = UserProfile()
    }
}

private fun String.capitalize(): String {
    return if (isNotEmpty()) {
        this[0].uppercase() + substring(1)
    } else {
        this
    }
}
