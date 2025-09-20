package com.fitnessss.fitlife.ui.screens.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessss.fitlife.data.model.CallStatus
import com.fitnessss.fitlife.data.model.CallType

@Composable
fun OutgoingCallScreen(
    callSessionId: String,
    receiverName: String,
    onEndCall: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callSession by viewModel.callSession.collectAsStateWithLifecycle()
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()
    val isAudioEnabled by viewModel.isAudioEnabled.collectAsStateWithLifecycle()
    val isSpeakerEnabled by viewModel.isSpeakerEnabled.collectAsStateWithLifecycle()
    
    LaunchedEffect(callSessionId) {
        viewModel.initializeCall(callSessionId)
    }
    
    // Auto-end call if it's ended remotely
    LaunchedEffect(callSession?.status) {
        if (callSession?.status in listOf(CallStatus.ENDED.name, CallStatus.DECLINED.name, CallStatus.MISSED.name)) {
            onEndCall()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section - Call status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 64.dp)
            ) {
                Text(
                    text = when (callSession?.status) {
                        CallStatus.INITIATING.name -> "Calling..."
                        CallStatus.RINGING.name -> "Ringing..."
                        CallStatus.CONNECTING.name -> "Connecting..."
                        CallStatus.CONNECTED.name -> "Connected"
                        else -> "Calling..."
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = receiverName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (callSession?.status == CallStatus.CONNECTED.name) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CallTimer(startTime = callSession?.startTime)
                }
            }
            
            // Middle section - Avatar placeholder
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Bottom section - Call controls
            CallControls(
                isAudioEnabled = isAudioEnabled,
                isSpeakerEnabled = isSpeakerEnabled,
                onToggleAudio = { viewModel.toggleAudio() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onEndCall = {
                    viewModel.endCall()
                    onEndCall()
                }
            )
        }
    }
}

@Composable
fun IncomingCallScreen(
    callSessionId: String,
    callerName: String,
    onAnswerCall: () -> Unit,
    onDeclineCall: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    LaunchedEffect(callSessionId) {
        viewModel.initializeCall(callSessionId)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 64.dp)
            ) {
                Text(
                    text = "Incoming call",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = callerName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Middle section - Avatar
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Bottom section - Answer/Decline buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Decline button
                FloatingActionButton(
                    onClick = {
                        viewModel.declineCall()
                        onDeclineCall()
                    },
                    modifier = Modifier.size(72.dp),
                    containerColor = Color.Red
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                
                // Answer button
                FloatingActionButton(
                    onClick = {
                        onAnswerCall()
                    },
                    modifier = Modifier.size(72.dp),
                    containerColor = Color.Green
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Answer",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveCallScreen(
    callSessionId: String,
    otherUserName: String,
    onEndCall: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callSession by viewModel.callSession.collectAsStateWithLifecycle()
    val isAudioEnabled by viewModel.isAudioEnabled.collectAsStateWithLifecycle()
    val isSpeakerEnabled by viewModel.isSpeakerEnabled.collectAsStateWithLifecycle()
    
    LaunchedEffect(callSessionId) {
        viewModel.initializeCall(callSessionId)
    }
    
    // Auto-end call if it's ended remotely
    LaunchedEffect(callSession?.status) {
        if (callSession?.status == CallStatus.ENDED.name) {
            onEndCall()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 64.dp)
            ) {
                Text(
                    text = otherUserName,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                CallTimer(startTime = callSession?.startTime)
            }
            
            // Middle section - Avatar
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Bottom section - Call controls
            CallControls(
                isAudioEnabled = isAudioEnabled,
                isSpeakerEnabled = isSpeakerEnabled,
                onToggleAudio = { viewModel.toggleAudio() },
                onToggleSpeaker = { viewModel.toggleSpeaker() },
                onEndCall = {
                    viewModel.endCall()
                    onEndCall()
                }
            )
        }
    }
}

@Composable
fun CallControls(
    isAudioEnabled: Boolean,
    isSpeakerEnabled: Boolean,
    onToggleAudio: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onEndCall: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute/Unmute button
        FloatingActionButton(
            onClick = onToggleAudio,
            modifier = Modifier.size(56.dp),
            containerColor = if (isAudioEnabled) 
                MaterialTheme.colorScheme.surfaceVariant 
            else 
                Color.Red.copy(alpha = 0.8f)
        ) {
            Icon(
                imageVector = if (isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isAudioEnabled) "Mute" else "Unmute",
                tint = if (isAudioEnabled) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    Color.White
            )
        }
        
        // Speaker button
        FloatingActionButton(
            onClick = onToggleSpeaker,
            modifier = Modifier.size(56.dp),
            containerColor = if (isSpeakerEnabled) 
                MaterialTheme.colorScheme.primary 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = if (isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = if (isSpeakerEnabled) "Speaker On" else "Speaker Off",
                tint = if (isSpeakerEnabled) 
                    MaterialTheme.colorScheme.onPrimary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // End call button
        FloatingActionButton(
            onClick = onEndCall,
            modifier = Modifier.size(72.dp),
            containerColor = Color.Red
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                modifier = Modifier.size(32.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun CallTimer(startTime: com.google.firebase.Timestamp?) {
    var duration by remember { mutableStateOf("00:00") }
    
    LaunchedEffect(startTime) {
        if (startTime != null) {
            while (true) {
                val currentTime = System.currentTimeMillis()
                val callStartTime = startTime.toDate().time
                val durationMs = currentTime - callStartTime
                
                val minutes = (durationMs / 60000).toInt()
                val seconds = ((durationMs % 60000) / 1000).toInt()
                
                duration = String.format("%02d:%02d", minutes, seconds)
                
                kotlinx.coroutines.delay(1000)
            }
        }
    }
    
    Text(
        text = duration,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}
