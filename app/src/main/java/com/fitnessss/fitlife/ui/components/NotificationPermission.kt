package com.fitnessss.fitlife.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionRequest(
    onPermissionResult: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS
        )
        
        LaunchedEffect(notificationPermissionState.status) {
            when (notificationPermissionState.status) {
                is PermissionStatus.Granted -> {
                    onPermissionResult(true)
                }
                is PermissionStatus.Denied -> {
                    onPermissionResult(false)
                }
            }
        }
        
        when (notificationPermissionState.status) {
            is PermissionStatus.Denied -> {
                NotificationPermissionDialog(
                    onRequestPermission = { notificationPermissionState.launchPermissionRequest() },
                    onDismiss = { onPermissionResult(false) },
                    shouldShowRationale = (notificationPermissionState.status as PermissionStatus.Denied).shouldShowRationale
                )
            }
            else -> {
                // Permission granted or checking
            }
        }
    } else {
        // No permission needed for Android 12 and below
        LaunchedEffect(Unit) {
            onPermissionResult(true)
        }
    }
}

@Composable
fun NotificationPermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    shouldShowRationale: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (shouldShowRationale) "Notification Permission Required" else "Enable Notifications",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (shouldShowRationale) {
                    "FitLife needs notification access to alert you about new messages and incoming calls. Without this permission, you might miss important communications."
                } else {
                    "To receive notifications for new messages and calls, FitLife needs notification access. This helps you stay connected even when the app is closed."
                },
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Allow Notifications")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}
