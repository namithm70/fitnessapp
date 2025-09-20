package com.fitnessss.fitlife.data.service

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isUserLoggedIn: Boolean
        get() = auth.currentUser != null
    
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
    
    // Google Sign In Client (will be initialized when needed)
    private var googleSignInClient: GoogleSignInClient? = null
    
    fun getAuthStateFlow(): Flow<FirebaseUser?> = flow {
        // For now, return null to avoid Flow suspension issues
        // TODO: Implement proper auth state listener
        emit(null)
    }
    
    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                // Update user profile with name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                
                user.updateProfile(profileUpdates).await()
                
                // Add user to Firestore users collection for search functionality
                val userData = mapOf(
                    "displayName" to name,
                    "email" to email,
                    "isOnline" to true,
                    "lastSeen" to com.google.firebase.Timestamp.now()
                )
                
                firestore.collection("users").document(user.uid).set(userData).await()
            }
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signInAnonymously(): Result<FirebaseUser> {
        return try {
            println("DEBUG: FirebaseAuthService - Starting anonymous sign in")
            println("DEBUG: FirebaseAuthService - Current user before: ${auth.currentUser}")
            println("DEBUG: FirebaseAuthService - Auth instance: $auth")
            
            // Check if we already have a user
            if (auth.currentUser != null) {
                println("DEBUG: FirebaseAuthService - User already exists, returning current user")
                return Result.success(auth.currentUser!!)
            }
            
            // Try to create an anonymous user
            println("DEBUG: FirebaseAuthService - Attempting to create anonymous user")
            val result = auth.signInAnonymously().await()
            println("DEBUG: FirebaseAuthService - Anonymous sign in result: $result")
            println("DEBUG: FirebaseAuthService - User after sign in: ${result.user}")
            
            Result.success(result.user!!)
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthService - Anonymous sign in exception: ${e.message}")
            println("DEBUG: FirebaseAuthService - Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            
            // If Firebase fails, create a temporary local user for testing
            println("DEBUG: FirebaseAuthService - Creating temporary local user for testing")
            return Result.success(createTemporaryUser())
        }
    }
    
    suspend fun signInWithGoogle(): Result<FirebaseUser> {
        return try {
            println("DEBUG: FirebaseAuthService - Starting Google sign in")
            println("DEBUG: FirebaseAuthService - Current user before: ${auth.currentUser}")
            
            // Check if we already have a user
            if (auth.currentUser != null) {
                println("DEBUG: FirebaseAuthService - User already exists, returning current user")
                return Result.success(auth.currentUser!!)
            }
            
            // For now, create an anonymous user as a fallback
            // The actual Google Sign-In will be handled by the UI layer
            println("DEBUG: FirebaseAuthService - Creating anonymous user as fallback")
            val result = auth.signInAnonymously().await()
            println("DEBUG: FirebaseAuthService - Google sign in result: $result")
            println("DEBUG: FirebaseAuthService - User after sign in: ${result.user}")
            
            Result.success(result.user!!)
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthService - Google sign in exception: ${e.message}")
            println("DEBUG: FirebaseAuthService - Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            
            // If Firebase fails, create a temporary local user for testing
            println("DEBUG: FirebaseAuthService - Creating temporary local user for testing")
            return Result.success(createTemporaryUser())
        }
    }
    
    fun getGoogleSignInIntent(context: Context): Intent? {
        try {
            println("DEBUG: FirebaseAuthService - Creating Google Sign-In intent")
            
            // Use the Web client ID from google-services.json (client_type: 3)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("496670957515-2omn2fkij67c6i79p0ho9hnghtiuuomu.apps.googleusercontent.com") // Web client ID
                .requestEmail()
                .requestProfile()
                .build()
            
            println("DEBUG: FirebaseAuthService - GoogleSignInOptions built successfully")
            
            // Clear any cached account to force account selection
            val tempClient = GoogleSignIn.getClient(context, gso)
            tempClient.signOut().addOnCompleteListener {
                println("DEBUG: FirebaseAuthService - Previous Google account signed out")
            }
            
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            println("DEBUG: FirebaseAuthService - GoogleSignInClient created: $googleSignInClient")
            
            val intent = googleSignInClient?.signInIntent
            println("DEBUG: FirebaseAuthService - Sign-In intent created: $intent")
            
            return intent
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthService - Error creating Google Sign-In intent: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    // Method to explicitly sign out and force account picker
    fun signOutGoogleAccount(context: Context) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("496670957515-2omn2fkij67c6i79p0ho9hnghtiuuomu.apps.googleusercontent.com")
                .requestEmail()
                .requestProfile()
                .build()
            
            GoogleSignIn.getClient(context, gso).signOut().addOnCompleteListener {
                println("DEBUG: FirebaseAuthService - Google account signed out successfully")
            }
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthService - Error signing out Google account: ${e.message}")
        }
    }
    
    suspend fun handleGoogleSignInResult(data: Intent?): Result<FirebaseUser> {
        return try {
            println("DEBUG: FirebaseAuthService - Handling Google Sign-In result")
            println("DEBUG: FirebaseAuthService - Intent data: $data")
            
            if (data == null) {
                println("DEBUG: FirebaseAuthService - Intent data is null")
                return Result.failure(Exception("No sign-in data received"))
            }
            
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
            println("DEBUG: FirebaseAuthService - Google account: ${account.displayName}, ${account.email}")
            println("DEBUG: FirebaseAuthService - Google account ID: ${account.id}")
            println("DEBUG: FirebaseAuthService - Google account photo: ${account.photoUrl}")
            println("DEBUG: FirebaseAuthService - Google account ID token: ${account.idToken?.take(20)}...")
            
            if (account.idToken == null) {
                println("DEBUG: FirebaseAuthService - ID token is null")
                return Result.failure(Exception("Google Sign-In failed: No ID token received"))
            }
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            println("DEBUG: FirebaseAuthService - Created credential with ID token")
            
            val result = auth.signInWithCredential(credential).await()
            println("DEBUG: FirebaseAuthService - Firebase sign-in successful: ${result.user?.uid}")
            
            // Update the user's profile with Google account information
            result.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(account.displayName)
                    .setPhotoUri(account.photoUrl)
                    .build()
                
                user.updateProfile(profileUpdates).await()
                println("DEBUG: FirebaseAuthService - Updated user profile with Google data")
                println("DEBUG: FirebaseAuthService - User display name: ${user.displayName}")
                println("DEBUG: FirebaseAuthService - User email: ${user.email}")
                
                // Add user to Firestore users collection for search functionality
                val userData = mapOf(
                    "displayName" to (account.displayName ?: "Unknown User"),
                    "email" to (account.email ?: ""),
                    "isOnline" to true,
                    "lastSeen" to com.google.firebase.Timestamp.now(),
                    "photoUrl" to account.photoUrl?.toString()
                )
                
                firestore.collection("users").document(user.uid).set(
                    userData, 
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
                println("DEBUG: FirebaseAuthService - Added user to Firestore users collection")

                // Save current FCM token for push notifications
                try {
                    val token = FirebaseMessaging.getInstance().token.await()
                    firestore.collection("users").document(user.uid).set(
                        mapOf("fcmToken" to token),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()
                    println("DEBUG: FirebaseAuthService - Saved FCM token")
                } catch (te: Exception) {
                    println("DEBUG: FirebaseAuthService - Failed to fetch/save FCM token: ${te.message}")
                }
            }
            
            Result.success(result.user!!)
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthService - Google Sign-In result error: ${e.message}")
            println("DEBUG: FirebaseAuthService - Exception type: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    fun signOut() {
        auth.signOut()
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUserProfile(displayName: String? = null, photoUrl: String? = null): Result<Unit> {
        return try {
            currentUser?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder().apply {
                    displayName?.let { setDisplayName(it) }
                    photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
                }.build()
                
                user.updateProfile(profileUpdates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAccount(): Result<Unit> {
        return try {
            currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun createTemporaryUser(): FirebaseUser {
        // Create a temporary user for testing when Firebase is not available
        // This is a workaround to allow testing the app functionality
        println("DEBUG: FirebaseAuthService - Creating temporary user for local testing")
        
        // For now, we'll create a simple anonymous user using Firebase
        // If this also fails, we'll need to implement a local user system
        return try {
            val result = auth.signInAnonymously().await()
            result.user!!
        } catch (e: Exception) {
            println("DEBUG: FirebaseAuthService - Failed to create even temporary user: ${e.message}")
            throw e
        }
    }
}
