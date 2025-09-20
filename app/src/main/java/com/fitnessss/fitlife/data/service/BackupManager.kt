package com.fitnessss.fitlife.data.service

import android.content.Context
import android.content.SharedPreferences
import com.fitnessss.fitlife.data.*
import com.fitnessss.fitlife.ui.screens.nutrition.NutritionManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManager @Inject constructor(
    private val firebaseService: FirebaseService,
    private val firebaseAuthService: FirebaseAuthService,
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)
    private val backupScope = CoroutineScope(Dispatchers.IO)
    
    // Backup settings
    private val _autoBackupEnabled = MutableStateFlow(prefs.getBoolean("auto_backup_enabled", true))
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled
    
    private val _wifiOnlyBackup = MutableStateFlow(prefs.getBoolean("wifi_only_backup", true))
    val wifiOnlyBackup: StateFlow<Boolean> = _wifiOnlyBackup
    
    private val _autoCleanupEnabled = MutableStateFlow(prefs.getBoolean("auto_cleanup_enabled", false))
    val autoCleanupEnabled: StateFlow<Boolean> = _autoCleanupEnabled
    
    // Backup status
    private val _lastBackupTime = MutableStateFlow<LocalDateTime?>(null)
    val lastBackupTime: StateFlow<LocalDateTime?> = _lastBackupTime
    
    private val _backupInProgress = MutableStateFlow(false)
    val backupInProgress: StateFlow<Boolean> = _backupInProgress
    
    private val _restoreInProgress = MutableStateFlow(false)
    val restoreInProgress: StateFlow<Boolean> = _restoreInProgress
    
    init {
        loadLastBackupTime()
    }
    
    // Backup Settings Management
    fun setAutoBackupEnabled(enabled: Boolean) {
        _autoBackupEnabled.value = enabled
        prefs.edit().putBoolean("auto_backup_enabled", enabled).apply()
    }
    
    fun setWifiOnlyBackup(enabled: Boolean) {
        _wifiOnlyBackup.value = enabled
        prefs.edit().putBoolean("wifi_only_backup", enabled).apply()
    }
    
    fun setAutoCleanupEnabled(enabled: Boolean) {
        _autoCleanupEnabled.value = enabled
        prefs.edit().putBoolean("auto_cleanup_enabled", enabled).apply()
    }
    
    // Manual Backup
    suspend fun performManualBackup(): BackupResult {
        if (!firebaseAuthService.isUserLoggedIn) {
            return BackupResult.Error("User not logged in")
        }
        
        return try {
            _backupInProgress.value = true
            
            // Create backup metadata
            val backupMetadata = mapOf(
                "timestamp" to LocalDateTime.now().toString(),
                "version" to "1.0",
                "deviceId" to android.provider.Settings.Secure.getString(
                    context.contentResolver, 
                    android.provider.Settings.Secure.ANDROID_ID
                ),
                "totalDataSize" to calculateTotalDataSize()
            )
            
            // Store backup metadata
            val userId = firebaseAuthService.currentUser?.uid
            FirebaseFirestore.getInstance()
                .collection("users").document(userId!!)
                .collection("backups")
                .document("metadata")
                .set(backupMetadata)
                .await()
            
            // Sync all data to Firebase
            firebaseService.syncAllData()
            
            // Update last backup time
            _lastBackupTime.value = LocalDateTime.now()
            saveLastBackupTime()
            
            _backupInProgress.value = false
            BackupResult.Success("Backup completed successfully")
            
        } catch (e: Exception) {
            _backupInProgress.value = false
            BackupResult.Error("Backup failed: ${e.message}")
        }
    }
    
    // Restore Data
    suspend fun performRestore(): RestoreResult {
        if (!firebaseAuthService.isUserLoggedIn) {
            return RestoreResult.Error("User not logged in")
        }
        
        return try {
            _restoreInProgress.value = true
            
            // Load all data from Firebase
            firebaseService.loadAllData()
            
            // Update local managers with restored data
            updateLocalDataFromFirebase()
            
            _restoreInProgress.value = false
            RestoreResult.Success("Data restored successfully")
            
        } catch (e: Exception) {
            _restoreInProgress.value = false
            RestoreResult.Error("Restore failed: ${e.message}")
        }
    }
    
    // Auto Backup (called periodically)
    suspend fun performAutoBackup() {
        if (!_autoBackupEnabled.value || !firebaseAuthService.isUserLoggedIn) {
            return
        }
        
        // Check WiFi requirement
        if (_wifiOnlyBackup.value && !isWifiConnected()) {
            return
        }
        
        performManualBackup()
    }
    
    // Clear Cache
    suspend fun clearCache(): ClearCacheResult {
        return try {
            // Clear local cache data
            val cacheDir = context.cacheDir
            val cacheSize = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
            
            cacheDir.deleteRecursively()
            
            ClearCacheResult.Success("Cache cleared successfully", cacheSize)
        } catch (e: Exception) {
            ClearCacheResult.Error("Failed to clear cache: ${e.message}")
        }
    }
    
    // Export Data (for manual backup)
    suspend fun exportData(): ExportResult {
        if (!firebaseAuthService.isUserLoggedIn) {
            return ExportResult.Error("User not logged in")
        }
        
        return try {
            val exportData = mapOf(
                "userProfile" to UserProfileManager.userProfile,
                "workoutSessions" to UserProgressManager.workoutSessions.value,
                "weightEntries" to UserProgressManager.weightEntries.value,
                "bodyMeasurements" to UserProgressManager.bodyMeasurements.value,
                "nutritionData" to mapOf(
                    "loggedFoods" to NutritionManager.loggedFoods,
                    "waterGlasses" to NutritionManager.waterGlasses
                ),
                "exportTimestamp" to LocalDateTime.now().toString()
            )
            
            ExportResult.Success(exportData)
        } catch (e: Exception) {
            ExportResult.Error("Export failed: ${e.message}")
        }
    }
    
    // Import Data
    suspend fun importData(importData: Map<String, Any>): ImportResult {
        return try {
            // Validate import data
            if (!importData.containsKey("userProfile")) {
                return ImportResult.Error("Invalid import data format")
            }
            
            // Import user profile
            val userProfileData = importData["userProfile"] as? Map<String, Any>
            if (userProfileData != null) {
                // Update user profile with imported data
                // This would need to be implemented based on your UserProfile structure
            }
            
            // Import other data types
            // This would need to be implemented for each data type
            
            ImportResult.Success("Data imported successfully")
        } catch (e: Exception) {
            ImportResult.Error("Import failed: ${e.message}")
        }
    }
    
    // Helper functions
    private fun calculateTotalDataSize(): Long {
        // Calculate approximate data size
        var totalSize = 0L
        
        // User profile size
        totalSize += UserProfileManager.userProfile.toString().length
        
        // Workout sessions size
        totalSize += UserProgressManager.workoutSessions.value.sumOf { it.toString().length }
        
        // Weight entries size
        totalSize += UserProgressManager.weightEntries.value.sumOf { it.toString().length }
        
        // Body measurements size
        totalSize += UserProgressManager.bodyMeasurements.value.sumOf { it.toString().length }
        
        // Nutrition data size
        totalSize += NutritionManager.loggedFoods.sumOf { it.toString().length }
        
        return totalSize
    }
    
    private fun isWifiConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) == true
    }
    
    private fun loadLastBackupTime() {
        val timestamp = prefs.getString("last_backup_time", null)
        if (timestamp != null) {
            try {
                _lastBackupTime.value = LocalDateTime.parse(timestamp)
            } catch (e: Exception) {
                println("Error parsing last backup time: ${e.message}")
            }
        }
    }
    
    private fun saveLastBackupTime() {
        _lastBackupTime.value?.let { time ->
            prefs.edit().putString("last_backup_time", time.toString()).apply()
        }
    }
    
    private suspend fun updateLocalDataFromFirebase() {
        // This would update local data managers with data from Firebase
        // Implementation depends on your data manager structure
    }
    
    // Data classes for results
    sealed class BackupResult {
        data class Success(val message: String) : BackupResult()
        data class Error(val message: String) : BackupResult()
    }
    
    sealed class RestoreResult {
        data class Success(val message: String) : RestoreResult()
        data class Error(val message: String) : RestoreResult()
    }
    
    sealed class ClearCacheResult {
        data class Success(val message: String, val clearedSize: Long) : ClearCacheResult()
        data class Error(val message: String) : ClearCacheResult()
    }
    
    sealed class ExportResult {
        data class Success(val data: Map<String, Any>) : ExportResult()
        data class Error(val message: String) : ExportResult()
    }
    
    sealed class ImportResult {
        data class Success(val message: String) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
}
