package com.fitnessss.fitlife.navigation

sealed class Screen(val route: String) {
    // Splash Screen
    object Splash : Screen("splash")
    
    // Auth Screens
    object Auth : Screen("auth")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object UserPreferences : Screen("user_preferences")
    object BiometricAuth : Screen("biometric_auth")
    object Main : Screen("main")
    
    // Main App Screens
    object Dashboard : Screen("dashboard")
    object Chat : Screen("chat")
    
    // Workout Screens
    object WorkoutList : Screen("workout_list")
    object WorkoutDetail : Screen("workout_detail")
    object WorkoutSession : Screen("workout_session")
    object AIWorkoutSession : Screen("ai_workout_session")
    object AIWorkoutGenerator : Screen("ai_workout_generator")
    
    // Nutrition Screens
    object Nutrition : Screen("nutrition")
    object FoodSearch : Screen("food_search")
    object MealPlan : Screen("meal_plan")
    
    // Progress Screens
    object Progress : Screen("progress")
    object ProgressPhoto : Screen("progress_photo")
    object Measurements : Screen("measurements")
    
    // History Screens
    object History : Screen("history")
    object HistoryDetail : Screen("history_detail")
    object HistoryAnalytics : Screen("history_analytics")
    
    
    
    // Community Screens
    object Community : Screen("community")
    object Forum : Screen("forum")
    object Challenges : Screen("challenges")
    
    // Gym Screens
    object GymLocator : Screen("gym_locator")
    object GymDetail : Screen("gym_detail")
    
    // Profile Screens
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Preferences : Screen("preferences")
    object Notifications : Screen("notifications")
    object PrivacySecurity : Screen("privacy_security")
    object DataStorage : Screen("data_storage")
    
    // Chat Screens
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{chatRoomId}") {
        fun createRoute(chatRoomId: String) = "chat_detail/$chatRoomId"
    }
    
    // Call screens
    object OutgoingCall : Screen("outgoing_call/{callSessionId}/{receiverName}") {
        fun createRoute(callSessionId: String, receiverName: String) = "outgoing_call/$callSessionId/$receiverName"
    }
    
    object IncomingCall : Screen("incoming_call/{callSessionId}/{callerName}") {
        fun createRoute(callSessionId: String, callerName: String) = "incoming_call/$callSessionId/$callerName"
    }
    
    object ActiveCall : Screen("active_call/{callSessionId}/{otherUserName}") {
        fun createRoute(callSessionId: String, otherUserName: String) = "active_call/$callSessionId/$otherUserName"
    }
    
    // Video call screens
    object VideoOutgoingCall : Screen("video_outgoing_call/{callSessionId}/{receiverName}") {
        fun createRoute(callSessionId: String, receiverName: String) = "video_outgoing_call/$callSessionId/$receiverName"
    }
    
    object VideoIncomingCall : Screen("video_incoming_call/{callSessionId}/{callerName}") {
        fun createRoute(callSessionId: String, callerName: String) = "video_incoming_call/$callSessionId/$callerName"
    }
    
    object VideoCall : Screen("video_call/{callSessionId}/{otherUserName}") {
        fun createRoute(callSessionId: String, otherUserName: String) = "video_call/$callSessionId/$otherUserName"
    }
    
    // Camera Screen
    object Camera : Screen("camera")
}
