package com.fitnessss.fitlife.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.service.BackupManager
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageScreen(
    navController: NavController,
    viewModel: DataStorageViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var message by remember { mutableStateOf<String?>(null) }
    
    // Backup settings state
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val wifiOnlyBackup by viewModel.wifiOnlyBackup.collectAsState()
    val autoCleanupEnabled by viewModel.autoCleanupEnabled.collectAsState()
    
    // Backup status state
    val lastBackupTime by viewModel.lastBackupTime.collectAsState()
    val backupInProgress by viewModel.backupInProgress.collectAsState()
    val restoreInProgress by viewModel.restoreInProgress.collectAsState()
    
    // Data sizes (calculated)
    var cacheSize by remember { mutableStateOf("Calculating...") }
    var totalDataSize by remember { mutableStateOf("Calculating...") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data & Storage") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            PageHeaderWithThemeToggle(title = "Data & Storage Management")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Backup Status Card
            if (lastBackupTime != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Backup,
                                contentDescription = "Last Backup",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Last Backup",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Last backup: ${lastBackupTime!!.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' HH:mm"))}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Storage Overview
            DataSection(
                title = "Storage Overview",
                icon = Icons.Filled.Storage
            ) {
                StorageInfoCard(
                    title = "Total Data",
                    value = totalDataSize,
                    icon = Icons.Filled.Storage,
                    color = MaterialTheme.colorScheme.primary
                )
                
                StorageInfoCard(
                    title = "Cache Size",
                    value = cacheSize,
                    icon = Icons.Filled.Cached,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            
            // Backup Settings
            DataSection(
                title = "Backup Settings",
                icon = Icons.Filled.Backup
            ) {
                DataToggleItem(
                    title = "Auto Backup",
                    subtitle = "Automatically backup data to cloud",
                    icon = Icons.Filled.CloudUpload,
                    checked = autoBackupEnabled,
                    onCheckedChange = { viewModel.setAutoBackupEnabled(it) }
                )
                
                DataToggleItem(
                    title = "WiFi Only",
                    subtitle = "Only backup when connected to WiFi",
                    icon = Icons.Filled.Wifi,
                    checked = wifiOnlyBackup,
                    onCheckedChange = { viewModel.setWifiOnlyBackup(it) }
                )
                
                DataActionItem(
                    title = "Manual Backup",
                    subtitle = if (backupInProgress) "Backing up..." else "Create a backup now",
                    icon = Icons.Filled.Backup,
                    onClick = {
                        if (!backupInProgress) {
                            scope.launch {
                                val result = viewModel.performManualBackup()
                                when (result) {
                                    is BackupManager.BackupResult.Success -> {
                                        message = "Backup completed successfully!"
                                    }
                                    is BackupManager.BackupResult.Error -> {
                                        message = "Backup failed: ${result.message}"
                                    }
                                }
                            }
                        }
                    }
                )
                
                DataActionItem(
                    title = "Restore Data",
                    subtitle = if (restoreInProgress) "Restoring..." else "Restore from a previous backup",
                    icon = Icons.Filled.Restore,
                    onClick = {
                        if (!restoreInProgress) {
                            scope.launch {
                                val result = viewModel.performRestore()
                                when (result) {
                                    is BackupManager.RestoreResult.Success -> {
                                        message = "Data restored successfully!"
                                    }
                                    is BackupManager.RestoreResult.Error -> {
                                        message = "Restore failed: ${result.message}"
                                    }
                                }
                            }
                        }
                    }
                )
            }
            
            // Data Management
            DataSection(
                title = "Data Management",
                icon = Icons.Filled.ManageAccounts
            ) {
                DataToggleItem(
                    title = "Auto Cleanup",
                    subtitle = "Automatically clean old data",
                    icon = Icons.Filled.CleaningServices,
                    checked = autoCleanupEnabled,
                    onCheckedChange = { viewModel.setAutoCleanupEnabled(it) }
                )
                
                DataActionItem(
                    title = "Clear Cache",
                    subtitle = "Free up space by clearing cache",
                    icon = Icons.Filled.ClearAll,
                    onClick = {
                        scope.launch {
                            val result = viewModel.clearCache()
                            when (result) {
                                is BackupManager.ClearCacheResult.Success -> {
                                    val sizeMB = result.clearedSize / (1024 * 1024)
                                    message = "Cache cleared! Freed ${sizeMB}MB"
                                }
                                is BackupManager.ClearCacheResult.Error -> {
                                    message = "Failed to clear cache: ${result.message}"
                                }
                            }
                        }
                    }
                )
                
                DataActionItem(
                    title = "Export Data",
                    subtitle = "Download all your data as backup",
                    icon = Icons.Filled.Download,
                    onClick = {
                        scope.launch {
                            val result = viewModel.exportData()
                            when (result) {
                                is BackupManager.ExportResult.Success -> {
                                    message = "Data exported successfully!"
                                    // TODO: Save to file or share
                                }
                                is BackupManager.ExportResult.Error -> {
                                    message = "Export failed: ${result.message}"
                                }
                            }
                        }
                    }
                )
                
                DataActionItem(
                    title = "Import Data",
                    subtitle = "Import data from another device",
                    icon = Icons.Filled.Upload,
                    onClick = {
                        // TODO: Implement file picker and import
                        message = "Import feature coming soon!"
                    }
                )
            }
            
            // Data Usage
            DataSection(
                title = "Data Usage",
                icon = Icons.Filled.DataUsage
            ) {
                DataUsageItem(
                    title = "Workout Data",
                    size = "45.2 MB",
                    percentage = 0.35f,
                    icon = Icons.Filled.FitnessCenter
                )
                
                DataUsageItem(
                    title = "Nutrition Data",
                    size = "32.1 MB",
                    percentage = 0.25f,
                    icon = Icons.Filled.Restaurant
                )
                
                DataUsageItem(
                    title = "Progress Data",
                    size = "28.4 MB",
                    percentage = 0.22f,
                    icon = Icons.Filled.TrendingUp
                )
                
                DataUsageItem(
                    title = "History Data",
                    size = "23.0 MB",
                    percentage = 0.18f,
                    icon = Icons.Filled.History
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Message display
            message?.let { msg ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Message",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(
                            onClick = { message = null }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Dismiss",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DataSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun StorageInfoCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun DataToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}

@Composable
fun DataActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Go to $title",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DataUsageItem(
    title: String,
    size: String,
    percentage: Float,
    icon: ImageVector
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Text(
                text = size,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class DataStorageViewModel @javax.inject.Inject constructor(
    private val backupManager: BackupManager
) : androidx.lifecycle.ViewModel() {
    
    val autoBackupEnabled = backupManager.autoBackupEnabled
    val wifiOnlyBackup = backupManager.wifiOnlyBackup
    val autoCleanupEnabled = backupManager.autoCleanupEnabled
    val lastBackupTime = backupManager.lastBackupTime
    val backupInProgress = backupManager.backupInProgress
    val restoreInProgress = backupManager.restoreInProgress
    
    fun setAutoBackupEnabled(enabled: Boolean) {
        backupManager.setAutoBackupEnabled(enabled)
    }
    
    fun setWifiOnlyBackup(enabled: Boolean) {
        backupManager.setWifiOnlyBackup(enabled)
    }
    
    fun setAutoCleanupEnabled(enabled: Boolean) {
        backupManager.setAutoCleanupEnabled(enabled)
    }
    
    suspend fun performManualBackup(): BackupManager.BackupResult {
        return backupManager.performManualBackup()
    }
    
    suspend fun performRestore(): BackupManager.RestoreResult {
        return backupManager.performRestore()
    }
    
    suspend fun clearCache(): BackupManager.ClearCacheResult {
        return backupManager.clearCache()
    }
    
    suspend fun exportData(): BackupManager.ExportResult {
        return backupManager.exportData()
    }
}
