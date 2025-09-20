package com.fitnessss.fitlife.ui.components

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AudioPermissionRequest(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val audioPermissionState = rememberPermissionState(
        permission = Manifest.permission.RECORD_AUDIO
    )
    
    LaunchedEffect(audioPermissionState.status) {
        when (audioPermissionState.status) {
            is PermissionStatus.Granted -> {
                onPermissionGranted()
            }
            is PermissionStatus.Denied -> {
                if ((audioPermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                    // Show rationale
                } else {
                    onPermissionDenied()
                }
            }
        }
    }
    
    when (audioPermissionState.status) {
        is PermissionStatus.Denied -> {
            AudioPermissionDialog(
                onRequestPermission = { audioPermissionState.launchPermissionRequest() },
                onDismiss = onPermissionDenied,
                shouldShowRationale = (audioPermissionState.status as PermissionStatus.Denied).shouldShowRationale
            )
        }
        else -> {
            // Permission granted or checking
        }
    }
}

@Composable
fun AudioPermissionDialog(
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    shouldShowRationale: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (shouldShowRationale) Icons.Default.Warning else Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (shouldShowRationale) "Microphone Permission Required" else "Allow Microphone Access",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = if (shouldShowRationale) {
                    "FitLife needs microphone access to enable voice calls with other users. Without this permission, you won't be able to make or receive calls."
                } else {
                    "To make voice calls, FitLife needs access to your microphone. This allows you to communicate with other users during calls."
                },
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onRequestPermission) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CallPermissionsHandler(
    onAllPermissionsGranted: () -> Unit,
    onPermissionsDenied: () -> Unit,
    content: @Composable () -> Unit = {}
) {
    val permissions = listOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    )
    
    val multiplePermissionsState = rememberMultiplePermissionsState(permissions)
    
    LaunchedEffect(multiplePermissionsState.allPermissionsGranted) {
        if (multiplePermissionsState.allPermissionsGranted) {
            onAllPermissionsGranted()
        }
    }
    
    when {
        multiplePermissionsState.allPermissionsGranted -> {
            content()
        }
        multiplePermissionsState.shouldShowRationale -> {
            CallPermissionRationaleDialog(
                onRequestPermissions = { multiplePermissionsState.launchMultiplePermissionRequest() },
                onDismiss = onPermissionsDenied
            )
        }
        else -> {
            LaunchedEffect(Unit) {
                multiplePermissionsState.launchMultiplePermissionRequest()
            }
        }
    }
}

@Composable
fun CallPermissionRationaleDialog(
    onRequestPermissions: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = "Audio Permissions Required",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "FitLife needs the following permissions to enable voice calls:",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "• Microphone access: To transmit your voice during calls",
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Audio settings: To manage call audio quality and routing",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Without these permissions, you won't be able to make or receive voice calls.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onRequestPermissions) {
                Text("Allow Permissions")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
