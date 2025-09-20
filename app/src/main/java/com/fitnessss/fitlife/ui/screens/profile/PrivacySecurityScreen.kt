package com.fitnessss.fitlife.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.fitnessss.fitlife.PageHeaderWithThemeToggle
import com.fitnessss.fitlife.data.service.BiometricAuthManager
import com.fitnessss.fitlife.navigation.Screen
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySecurityScreen(
    navController: NavController,
    viewModel: PrivacySecurityViewModel = hiltViewModel()
) {
    val biometricEnabled by viewModel.biometricEnabled.collectAsState()
    val biometricStatus by viewModel.biometricStatus.collectAsState()
    var twoFactorEnabled by remember { mutableStateOf(false) }
    var dataSharingEnabled by remember { mutableStateOf(false) }
    var analyticsEnabled by remember { mutableStateOf(true) }
    var locationSharingEnabled by remember { mutableStateOf(false) }
    var autoBackupEnabled by remember { mutableStateOf(true) }
    var dataRetentionDays by remember { mutableStateOf(365) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security") },
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
            PageHeaderWithThemeToggle(title = "Privacy & Security Settings")
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Account Security
            PrivacySection(
                title = "Account Security",
                icon = Icons.Filled.Security
            ) {
                PrivacyToggleItem(
                    title = "Biometric Authentication",
                    subtitle = biometricStatus,
                    icon = Icons.Filled.Fingerprint,
                    checked = biometricEnabled,
                    onCheckedChange = { viewModel.setBiometricEnabled(it) }
                )
                
                // Test Biometric Button
                if (biometricEnabled) {
                    PrivacyActionItem(
                        title = "Test Biometric Authentication",
                        subtitle = "Test the biometric authentication flow",
                        icon = Icons.Filled.Security,
                        onClick = { 
                            println("DEBUG: PrivacySecurityScreen - Test biometric button clicked")
                            navController.navigate(Screen.BiometricAuth.route)
                        }
                    )
                }
                
                PrivacyToggleItem(
                    title = "Two-Factor Authentication",
                    subtitle = "Add an extra layer of security",
                    icon = Icons.Filled.VerifiedUser,
                    checked = twoFactorEnabled,
                    onCheckedChange = { twoFactorEnabled = it }
                )
                
                PrivacyActionItem(
                    title = "Change Password",
                    subtitle = "Update your account password",
                    icon = Icons.Filled.Lock,
                    onClick = { /* TODO: Navigate to change password */ }
                )
                
                PrivacyActionItem(
                    title = "Login History",
                    subtitle = "View recent login activity",
                    icon = Icons.Filled.History,
                    onClick = { /* TODO: Show login history */ }
                )
            }
            
            // Data Privacy
            PrivacySection(
                title = "Data Privacy",
                icon = Icons.Filled.PrivacyTip
            ) {
                PrivacyToggleItem(
                    title = "Data Sharing",
                    subtitle = "Share anonymous data for app improvement",
                    icon = Icons.Filled.Share,
                    checked = dataSharingEnabled,
                    onCheckedChange = { dataSharingEnabled = it }
                )
                
                PrivacyToggleItem(
                    title = "Analytics",
                    subtitle = "Help improve the app with usage analytics",
                    icon = Icons.Filled.Analytics,
                    checked = analyticsEnabled,
                    onCheckedChange = { analyticsEnabled = it }
                )
                
                PrivacyToggleItem(
                    title = "Location Sharing",
                    subtitle = "Share location for gym finder feature",
                    icon = Icons.Filled.LocationOn,
                    checked = locationSharingEnabled,
                    onCheckedChange = { locationSharingEnabled = it }
                )
            }
            
            // Data Management
            PrivacySection(
                title = "Data Management",
                icon = Icons.Filled.Storage
            ) {
                PrivacyToggleItem(
                    title = "Auto Backup",
                    subtitle = "Automatically backup data to cloud",
                    icon = Icons.Filled.Backup,
                    checked = autoBackupEnabled,
                    onCheckedChange = { autoBackupEnabled = it }
                )
                
                PrivacyActionItem(
                    title = "Export Data",
                    subtitle = "Download all your data",
                    icon = Icons.Filled.Download,
                    onClick = { /* TODO: Export data */ }
                )
                
                PrivacyActionItem(
                    title = "Data Retention",
                    subtitle = "Keep data for $dataRetentionDays days",
                    icon = Icons.Filled.Schedule,
                    onClick = { /* TODO: Show retention options */ }
                )
            }
            
            // Privacy Controls
            PrivacySection(
                title = "Privacy Controls",
                icon = Icons.Filled.Settings
            ) {
                PrivacyActionItem(
                    title = "Delete Account",
                    subtitle = "Permanently delete your account and data",
                    icon = Icons.Filled.DeleteForever,
                    onClick = { /* TODO: Show delete account dialog */ }
                )
                
                PrivacyActionItem(
                    title = "Privacy Policy",
                    subtitle = "Read our privacy policy",
                    icon = Icons.Filled.Description,
                    onClick = { /* TODO: Open privacy policy */ }
                )
                
                PrivacyActionItem(
                    title = "Terms of Service",
                    subtitle = "Read our terms of service",
                    icon = Icons.Filled.Description,
                    onClick = { /* TODO: Open terms of service */ }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun PrivacySection(
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
fun PrivacyToggleItem(
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
fun PrivacyActionItem(
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
