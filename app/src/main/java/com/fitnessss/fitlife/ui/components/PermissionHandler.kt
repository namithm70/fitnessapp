package com.fitnessss.fitlife.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
fun rememberCameraPermissionState(): CameraPermissionState {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkCameraPermission(context)) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    
    return remember {
        CameraPermissionState(
            hasPermission = hasPermission,
            requestPermission = { launcher.launch(Manifest.permission.CAMERA) }
        )
    }
}

@Composable
fun rememberAudioPermissionState(): AudioPermissionState {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(checkAudioPermission(context)) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }
    
    return remember {
        AudioPermissionState(
            hasPermission = hasPermission,
            requestPermission = { launcher.launch(Manifest.permission.RECORD_AUDIO) }
        )
    }
}

@Composable
fun rememberCallPermissionsState(): CallPermissionsState {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(checkCameraPermission(context)) }
    var hasAudioPermission by remember { mutableStateOf(checkAudioPermission(context)) }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }
    
    return remember {
        CallPermissionsState(
            hasCameraPermission = hasCameraPermission,
            hasAudioPermission = hasAudioPermission,
            requestCameraPermission = { cameraLauncher.launch(Manifest.permission.CAMERA) },
            requestAudioPermission = { audioLauncher.launch(Manifest.permission.RECORD_AUDIO) }
        )
    }
}

private fun checkCameraPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun checkAudioPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED
}

class CameraPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

class AudioPermissionState(
    val hasPermission: Boolean,
    val requestPermission: () -> Unit
)

class CallPermissionsState(
    val hasCameraPermission: Boolean,
    val hasAudioPermission: Boolean,
    val requestCameraPermission: () -> Unit,
    val requestAudioPermission: () -> Unit
) {
    val hasAllPermissions: Boolean
        get() = hasCameraPermission && hasAudioPermission
}
