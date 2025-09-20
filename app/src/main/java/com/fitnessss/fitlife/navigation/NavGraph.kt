package com.fitnessss.fitlife.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fitnessss.fitlife.ui.screens.auth.LoginScreen
import com.fitnessss.fitlife.ui.screens.auth.RegisterScreen
import com.fitnessss.fitlife.ui.screens.auth.OnboardingScreen
import com.fitnessss.fitlife.ui.screens.dashboard.DashboardScreen
import com.fitnessss.fitlife.ui.screens.workout.WorkoutListScreen
import com.fitnessss.fitlife.ui.screens.workout.WorkoutDetailScreen
import com.fitnessss.fitlife.ui.screens.workout.WorkoutSessionScreen
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionScreen
import com.fitnessss.fitlife.ui.screens.nutrition.FoodSearchScreen
import com.fitnessss.fitlife.ui.screens.nutrition.MealPlanScreen
import com.fitnessss.fitlife.ui.screens.progress.ProgressScreen
import com.fitnessss.fitlife.ui.screens.progress.ProgressPhotoScreen
import com.fitnessss.fitlife.ui.screens.progress.MeasurementsScreen

import com.fitnessss.fitlife.ui.screens.community.CommunityScreen
import com.fitnessss.fitlife.ui.screens.community.ForumScreen
import com.fitnessss.fitlife.ui.screens.community.ChallengesScreen
import com.fitnessss.fitlife.ui.screens.gym.GymLocatorScreen
import com.fitnessss.fitlife.ui.screens.gym.GymDetailScreen
import com.fitnessss.fitlife.ui.screens.profile.ProfileScreen
import com.fitnessss.fitlife.ui.screens.profile.EditProfileScreen
import com.fitnessss.fitlife.ui.screens.camera.CameraScreen

@Composable
fun FitLifeNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screens
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        
        composable(Screen.Onboarding.route) {
            OnboardingScreen(navController = navController)
        }
        
        // Main App Screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(navController = navController)
        }
        
        // Workout Screens
        composable(Screen.WorkoutList.route) {
            WorkoutListScreen(navController = navController)
        }
        
        composable(
            route = Screen.WorkoutDetail.route + "/{workoutId}"
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId")
            WorkoutDetailScreen(
                navController = navController,
                workoutId = workoutId ?: ""
            )
        }
        
        composable(
            route = Screen.WorkoutSession.route + "/{workoutId}"
        ) { backStackEntry ->
            val workoutId = backStackEntry.arguments?.getString("workoutId")
            WorkoutSessionScreen(
                navController = navController,
                workoutId = workoutId ?: ""
            )
        }
        
        // Nutrition Screens
        composable(Screen.Nutrition.route) {
            NutritionScreen(navController = navController)
        }
        
        composable(
            route = Screen.FoodSearch.route + "?mealType={mealType}"
        ) { backStackEntry ->
            val mealType = backStackEntry.arguments?.getString("mealType")
            FoodSearchScreen(
                navController = navController,
                mealType = mealType
            )
        }
        composable(Screen.FoodSearch.route) {
            FoodSearchScreen(navController = navController)
        }
        
        composable(Screen.MealPlan.route) {
            MealPlanScreen(navController = navController)
        }
        
        // Progress Screens
        composable(Screen.Progress.route) {
            ProgressScreen(navController = navController)
        }
        
        composable(Screen.ProgressPhoto.route) {
            ProgressPhotoScreen(navController = navController)
        }
        
        composable(Screen.Measurements.route) {
            MeasurementsScreen(navController = navController)
        }
        
        
        
        // Community Screens
        composable(Screen.Community.route) {
            CommunityScreen(navController = navController)
        }
        
        composable(
            route = Screen.Forum.route + "/{forumId}"
        ) { backStackEntry ->
            val forumId = backStackEntry.arguments?.getString("forumId")
            ForumScreen(
                navController = navController,
                forumId = forumId ?: ""
            )
        }
        
        composable(Screen.Challenges.route) {
            ChallengesScreen(navController = navController)
        }
        
        // Gym Screens
        composable(Screen.GymLocator.route) {
            GymLocatorScreen(navController = navController)
        }
        
        composable(
            route = Screen.GymDetail.route + "/{gymId}"
        ) { backStackEntry ->
            val gymId = backStackEntry.arguments?.getString("gymId")
            GymDetailScreen(
                navController = navController,
                gymId = gymId ?: ""
            )
        }
        
        // Profile Screens
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        
        // Camera Screen
        composable(Screen.Camera.route) {
            CameraScreen(navController = navController)
        }
    }
}
