package com.fitnessss.fitlife.ui.screens.call

import android.os.Bundle
import android.view.WindowManager
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import com.fitnessss.fitlife.ui.theme.FitLifeTheme
import com.fitnessss.fitlife.ui.screens.call.CallViewModel
import com.fitnessss.fitlife.ui.screens.call.ActiveCallScreen
import com.fitnessss.fitlife.data.service.CallNotificationService.Companion.ACTION_ANSWER_CALL
import com.fitnessss.fitlife.data.service.CallNotificationService.Companion.ACTION_DECLINE_CALL
import com.fitnessss.fitlife.data.service.CallNotificationService.Companion.ACTION_SHOW_INCOMING_CALL
import com.fitnessss.fitlife.data.service.CallNotificationService
import javax.inject.Inject

@AndroidEntryPoint
class IncomingCallActivity : ComponentActivity() {
    private val callViewModel: CallViewModel by viewModels()
    private var pendingAction: (() -> Unit)? = null
    private var hasAnswered = false
    private var onCallEnded: (() -> Unit)? = null
    
    @Inject
    lateinit var callNotificationService: CallNotificationService

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pendingAction?.invoke()
        } else {
            finish()
        }
        pendingAction = null
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        val callSessionId = intent?.getStringExtra("callSessionId") ?: ""
        if (callSessionId.isBlank()) {
            finish()
            return
        }
        val callerName = intent?.getStringExtra("callerName") ?: ""

        // Reset the answered flag for new calls
        hasAnswered = false

        // Dismiss any incoming call notifications since we're showing the full-screen UI
        callNotificationService.dismissIncomingCallNotification()

        callViewModel.initializeCall(callSessionId)
        handleActionIfPresent(callSessionId)

        setContent {
            FitLifeTheme {
                IncomingCallActivityContent(
                    callSessionId = callSessionId,
                    callerName = callerName,
                    callViewModel = callViewModel,
                    onAnswer = {
                        if (!hasAnswered) {
                            hasAnswered = true
                            ensureMicPermission {
                                callViewModel.answerCall()
                                // Keep activity visible until call transitions state
                            }
                        }
                    },
                    onDecline = {
                        callViewModel.declineCall()
                        finish()
                    },
                    onCallEnded = {
                        // Navigate back to MainActivity to keep app in foreground after call ends
                        val intent = android.content.Intent(this@IncomingCallActivity, com.fitnessss.fitlife.MainActivity::class.java).apply {
                            addFlags(android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val callSessionId = intent?.getStringExtra("callSessionId") ?: ""
        if (callSessionId.isBlank()) {
            finish()
            return
        }
        handleActionIfPresent(callSessionId)
    }

    private fun handleActionIfPresent(callSessionId: String) {
        when (intent?.action) {
            ACTION_ANSWER_CALL -> {
                callViewModel.initializeCall(callSessionId)
                ensureMicPermission {
                    callViewModel.answerCall()
                    // Keep activity visible until call transitions state
                }
            }
            ACTION_DECLINE_CALL -> {
                callViewModel.initializeCall(callSessionId)
                callViewModel.declineCall()
                finish()
            }
            ACTION_SHOW_INCOMING_CALL -> {
                // Just show the incoming call screen, don't auto-answer
                callViewModel.initializeCall(callSessionId)
            }
        }
    }

    private fun ensureMicPermission(onGranted: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onGranted()
        } else {
            pendingAction = onGranted
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
private fun IncomingCallActivityContent(
    callSessionId: String,
    callerName: String,
    callViewModel: CallViewModel,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
    onCallEnded: () -> Unit
) {
    val callSession by callViewModel.callSession.collectAsStateWithLifecycle()
    // Stabilize call type to avoid UI flashing while session updates propagate
    val stableIsVideoCall = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Boolean?>(null) }
    
    // Auto-finish activity when call ends
    androidx.compose.runtime.LaunchedEffect(callSession?.status) {
        if (callSession?.status in listOf(
            com.fitnessss.fitlife.data.model.CallStatus.ENDED.name,
            com.fitnessss.fitlife.data.model.CallStatus.DECLINED.name,
            com.fitnessss.fitlife.data.model.CallStatus.MISSED.name
        )) {
            onCallEnded()
        }
    }
    
    // Capture call type once when first known to prevent toggling between AUDIO/VIDEO during early updates
    androidx.compose.runtime.LaunchedEffect(callSession?.callType) {
        if (stableIsVideoCall.value == null) {
            callSession?.callType?.let { typeStr ->
                val isVideo = typeStr == com.fitnessss.fitlife.data.model.CallType.VIDEO.name
                stableIsVideoCall.value = isVideo
                callViewModel.setVideoMode(isVideo)
                println("DEBUG: IncomingCallActivity - stabilized isVideoCall=$isVideo from callType='$typeStr'")
            }
        }
    }
    
    // Show different screens based on call status and type
    val isVideoCall = stableIsVideoCall.value ?: (callSession?.callType == com.fitnessss.fitlife.data.model.CallType.VIDEO.name)

    // Ensure ViewModel's isVideoCall flag stays in sync with session
    androidx.compose.runtime.LaunchedEffect(isVideoCall) {
        // Sync only when stabilized or actually changed
        callViewModel.setVideoMode(isVideoCall)
        println("DEBUG: IncomingCallActivity - syncing isVideoCall to ViewModel: $isVideoCall")
    }
    when (callSession?.status) {
        com.fitnessss.fitlife.data.model.CallStatus.CONNECTED.name -> {
            if (isVideoCall) {
                VideoCallScreen(
                    callSessionId = callSessionId,
                    otherUserName = callerName,
                    onEndCall = {
                        callViewModel.endCall()
                        // Return to main instead of closing app
                        // Do not finish immediately; let status observer close via onCallEnded
                    },
                    viewModel = callViewModel
                )
            } else {
                ActiveCallScreen(
                    callSessionId = callSessionId,
                    otherUserName = callerName,
                    onEndCall = {
                        callViewModel.endCall()
                        // Do not finish immediately; let status observer close via onCallEnded
                    },
                    viewModel = callViewModel
                )
            }
        }
        else -> {
            // Show stable incoming UI without flashing during RINGING/CONNECTING
            if (isVideoCall) {
                VideoIncomingCallScreen(
                    callSessionId = callSessionId,
                    callerName = callerName,
                    onAnswerCall = onAnswer,
                    onDeclineCall = onDecline
                )
            } else {
                IncomingCallScreen(
                    callSessionId = callSessionId,
                    callerName = callerName,
                    onAnswerCall = onAnswer,
                    onDeclineCall = onDecline
                )
            }
        }
    }
}


