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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fitnessss.fitlife.data.model.CallStatus
import com.fitnessss.fitlife.data.model.CallType
import org.webrtc.SurfaceViewRenderer

@Composable
fun VideoCallScreen(
    callSessionId: String,
    otherUserName: String,
    onEndCall: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callSession by viewModel.callSession.collectAsStateWithLifecycle()
    val isAudioEnabled by viewModel.isAudioEnabled.collectAsStateWithLifecycle()
    val isVideoEnabled by viewModel.isVideoEnabled.collectAsStateWithLifecycle()
    val isSpeakerEnabled by viewModel.isSpeakerEnabled.collectAsStateWithLifecycle()
    val isVideoCall by viewModel.isVideoCall.collectAsStateWithLifecycle()
    
    // Do not re-initialize here; activity already initializes to avoid duplicate listeners
    
    // Auto-end call if it's ended/declined/missed remotely
    LaunchedEffect(callSession?.status) {
        if (callSession?.status in listOf(CallStatus.ENDED.name, CallStatus.DECLINED.name, CallStatus.MISSED.name)) {
            onEndCall()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Ensure video mode is enabled while this screen is active
        LaunchedEffect(Unit) {
            viewModel.setVideoMode(true)
            println("DEBUG: VideoCallScreen - forcing isVideoCall=true while screen active")
        }
        // Debug: Log video state
        LaunchedEffect(isVideoCall, isVideoEnabled) {
            println("DEBUG: VideoCallScreen - isVideoCall=$isVideoCall, isVideoEnabled=$isVideoEnabled")
        }
        
        // Remote video (full screen background)
        if (isVideoEnabled) {
            println("DEBUG: VideoCallScreen - Creating remote surface view")
            AndroidView(
                factory = { context ->
                    SurfaceViewRenderer(context).apply {
                        println("DEBUG: VideoCallScreen - Setting up remote surface view")
                        viewModel.setupRemoteSurfaceView(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback background when video is disabled
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = otherUserName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Local video (picture-in-picture)
        if (isVideoEnabled) {
            println("DEBUG: VideoCallScreen - Creating local surface view")
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(120.dp, 160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                AndroidView(
                    factory = { context ->
                        SurfaceViewRenderer(context).apply {
                            println("DEBUG: VideoCallScreen - Setting up local surface view")
                            viewModel.setupLocalSurfaceView(this)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Top overlay with call info
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = otherUserName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (callSession?.status == CallStatus.CONNECTED.name) {
                Spacer(modifier = Modifier.height(4.dp))
                CallTimer(startTime = callSession?.startTime, textColor = Color.White)
            }
        }
        
        // Bottom overlay with controls
        VideoCallControls(
            isAudioEnabled = isAudioEnabled,
            isVideoEnabled = isVideoEnabled,
            isSpeakerEnabled = isSpeakerEnabled,
            onToggleAudio = { viewModel.toggleAudio() },
            onToggleVideo = { viewModel.toggleVideo() },
            onToggleSpeaker = { viewModel.toggleSpeaker() },
            onSwitchCamera = { viewModel.switchCamera() },
            onEndCall = {
                viewModel.endCall()
                onEndCall()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun VideoIncomingCallScreen(
    callSessionId: String,
    callerName: String,
    onAnswerCall: () -> Unit,
    onDeclineCall: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callSession by viewModel.callSession.collectAsStateWithLifecycle()
    val isVideoCall by viewModel.isVideoCall.collectAsStateWithLifecycle()
    
    // Do not re-initialize here to avoid multiple collectors
    
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
                    text = if (isVideoCall) "Incoming video call" else "Incoming call",
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
                
                if (isVideoCall) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video call",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
                    imageVector = if (isVideoCall) Icons.Default.Videocam else Icons.Default.Person,
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
                        imageVector = if (isVideoCall) Icons.Default.Videocam else Icons.Default.Call,
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
fun VideoOutgoingCallScreen(
    callSessionId: String,
    receiverName: String,
    onEndCall: () -> Unit,
    viewModel: CallViewModel = hiltViewModel()
) {
    val callSession by viewModel.callSession.collectAsStateWithLifecycle()
    val isVideoCall by viewModel.isVideoCall.collectAsStateWithLifecycle()
    
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
                    text = run {
                        val pcState = viewModel.connectionState.collectAsStateWithLifecycle().value
                        if (pcState == org.webrtc.PeerConnection.PeerConnectionState.CONNECTED) {
                            "Connected"
                        } else when (callSession?.status) {
                            CallStatus.INITIATING.name -> if (isVideoCall) "Starting video call..." else "Calling..."
                            CallStatus.RINGING.name -> if (isVideoCall) "Video call ringing..." else "Ringing..."
                            CallStatus.CONNECTING.name -> "Connecting..."
                            CallStatus.CONNECTED.name -> "Connected"
                            else -> if (isVideoCall) "Starting video call..." else "Calling..."
                        }
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
                
                if (isVideoCall) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video call",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
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
                    imageVector = if (isVideoCall) Icons.Default.Videocam else Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Bottom section - End call button
            FloatingActionButton(
                onClick = {
                    viewModel.endCall()
                    onEndCall()
                },
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
}

@Composable
fun VideoCallControls(
    isAudioEnabled: Boolean,
    isVideoEnabled: Boolean,
    isSpeakerEnabled: Boolean,
    onToggleAudio: () -> Unit,
    onToggleVideo: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onSwitchCamera: () -> Unit,
    onEndCall: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .background(
                Color.Black.copy(alpha = 0.3f),
                RoundedCornerShape(24.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mute/Unmute button
        FloatingActionButton(
            onClick = onToggleAudio,
            modifier = Modifier.size(56.dp),
            containerColor = if (isAudioEnabled) 
                Color.White.copy(alpha = 0.2f)
            else 
                Color.Red.copy(alpha = 0.8f)
        ) {
            Icon(
                imageVector = if (isAudioEnabled) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = if (isAudioEnabled) "Mute" else "Unmute",
                tint = Color.White
            )
        }
        
        // Video toggle button
        FloatingActionButton(
            onClick = onToggleVideo,
            modifier = Modifier.size(56.dp),
            containerColor = if (isVideoEnabled) 
                Color.White.copy(alpha = 0.2f)
            else 
                Color.Red.copy(alpha = 0.8f)
        ) {
            Icon(
                imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = if (isVideoEnabled) "Turn off video" else "Turn on video",
                tint = Color.White
            )
        }
        
        // Speaker button
        FloatingActionButton(
            onClick = onToggleSpeaker,
            modifier = Modifier.size(56.dp),
            containerColor = if (isSpeakerEnabled) 
                MaterialTheme.colorScheme.primary 
            else 
                Color.White.copy(alpha = 0.2f)
        ) {
            Icon(
                imageVector = if (isSpeakerEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = if (isSpeakerEnabled) "Speaker On" else "Speaker Off",
                tint = Color.White
            )
        }
        
        // Switch camera button
        FloatingActionButton(
            onClick = onSwitchCamera,
            modifier = Modifier.size(56.dp),
            containerColor = Color.White.copy(alpha = 0.2f)
        ) {
            Icon(
                imageVector = Icons.Default.FlipCameraAndroid,
                contentDescription = "Switch Camera",
                tint = Color.White
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
fun CallTimer(
    startTime: com.google.firebase.Timestamp?,
    textColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
) {
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
        color = textColor
    )
}
