package com.fitnessss.fitlife.ui.screens.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.model.CallSession
import com.fitnessss.fitlife.data.model.CallStatus
import com.fitnessss.fitlife.data.model.CallType
import com.fitnessss.fitlife.data.repository.CallRepository
import com.fitnessss.fitlife.data.service.WebRTCService
import com.fitnessss.fitlife.data.service.CallNotificationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import javax.inject.Inject

@HiltViewModel
class CallViewModel @Inject constructor(
    private val callRepository: CallRepository,
    private val webRTCService: WebRTCService,
    private val callNotificationService: CallNotificationService
) : ViewModel() {
    
    private val _callSession = MutableStateFlow<CallSession?>(null)
    val callSession: StateFlow<CallSession?> = _callSession.asStateFlow()
    
    private val _connectionState = MutableStateFlow(PeerConnection.PeerConnectionState.NEW)
    val connectionState: StateFlow<PeerConnection.PeerConnectionState> = _connectionState.asStateFlow()
    
    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled.asStateFlow()
    
    private val _isSpeakerEnabled = MutableStateFlow(false)
    val isSpeakerEnabled: StateFlow<Boolean> = _isSpeakerEnabled.asStateFlow()
    
    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled.asStateFlow()
    
    private val _isVideoCall = MutableStateFlow(false)
    val isVideoCall: StateFlow<Boolean> = _isVideoCall.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private var currentCallSessionId: String? = null
    private var listenJob: kotlinx.coroutines.Job? = null
    private val iceCandidates = mutableListOf<String>()
    private var hasProcessedCallerSdp = false
    private var hasProcessedReceiverSdp = false
    
    init {
        // Observe WebRTC service states
        viewModelScope.launch {
            webRTCService.connectionState.collect { state ->
                println("DEBUG: CallViewModel - connectionState: $state")
                _connectionState.value = state
                
                // Update call status based on connection state
                when (state) {
                    PeerConnection.PeerConnectionState.CONNECTED -> {
                        currentCallSessionId?.let { sessionId ->
                            // Update status to CONNECTED which will set startTime
                            callRepository.updateCallStatus(sessionId, CallStatus.CONNECTED)
                        }
                        // Ensure audio track is enabled when connection is established
                        webRTCService.ensureAudioTrackEnabled()
                        webRTCService.onCallConnected()
                    }
                    PeerConnection.PeerConnectionState.DISCONNECTED,
                    PeerConnection.PeerConnectionState.FAILED -> {
                        currentCallSessionId?.let { sessionId ->
                            callRepository.updateCallStatus(sessionId, CallStatus.ENDED)
                        }
                    }
                    else -> {}
                }
            }
        }
        
        viewModelScope.launch {
            webRTCService.isAudioEnabled.collect { enabled ->
                _isAudioEnabled.value = enabled
            }
        }
        
        viewModelScope.launch {
            webRTCService.isVideoEnabled.collect { enabled ->
                _isVideoEnabled.value = enabled
            }
        }
        
        viewModelScope.launch {
            webRTCService.isVideoCall.collect { isVideo ->
                _isVideoCall.value = isVideo
            }
        }
        
        // Set up WebRTC callbacks
        webRTCService.onIceCandidateFound = { candidate ->
            println("DEBUG: CallViewModel - onIceCandidateFound ${candidate.sdpMid}:${candidate.sdpMLineIndex}")
            handleIceCandidate(candidate)
        }
        
        webRTCService.onConnectionStateChanged = { state ->
            println("DEBUG: CallViewModel - onConnectionStateChanged $state")
            _connectionState.value = state
        }
        
        // Set up ICE connection callback to trigger CONNECTED status
        webRTCService.setOnCallConnectedCallback {
            currentCallSessionId?.let { sessionId ->
                println("DEBUG: CallViewModel - ICE connected callback triggered, updating status to CONNECTED")
                viewModelScope.launch {
                    callRepository.updateCallStatus(sessionId, CallStatus.CONNECTED)
                }
            }
        }
    }

    /**
     * Explicitly set video-call mode based on current UI/session context.
     * Useful to keep UI (e.g., IncomingCallActivity) and ViewModel flags in sync.
     */
    fun setVideoMode(isVideo: Boolean) {
        _isVideoCall.value = isVideo
    }
    
    fun initializeCall(callSessionId: String) {
        // Prevent duplicate listeners for the same session (which cause flashing)
        if (currentCallSessionId == callSessionId && listenJob?.isActive == true) {
            return
        }
        currentCallSessionId = callSessionId
        listenJob?.cancel()
        // Listen for call session updates
        listenJob = viewModelScope.launch {
            callRepository.listenForCallSession(callSessionId).collect { session ->
                println("DEBUG: CallViewModel - listenForCallSession update: ${session?.status}")
                _callSession.value = session
                session?.let { handleCallSessionUpdate(it) }
            }
        }
    }
    
    fun initiateCall(receiverId: String, receiverName: String, chatRoomId: String, callType: CallType = CallType.AUDIO) {
        println("DEBUG: CallViewModel - initiateCall() called for receiver: $receiverName, type: $callType")
        // Reset flags for new call
        hasProcessedCallerSdp = false
        hasProcessedReceiverSdp = false
        _isVideoCall.value = (callType == CallType.VIDEO)
        
        viewModelScope.launch {
            callRepository.initiateCall(receiverId, receiverName, chatRoomId, callType)
                .onSuccess { callSession ->
                    println("DEBUG: CallViewModel - initiateCall repository success, sessionId: ${callSession.id}")
                    _callSession.value = callSession
                    currentCallSessionId = callSession.id
                    
                    // Initialize WebRTC and create offer
                    if (webRTCService.createPeerConnection(callType == CallType.VIDEO, isCaller = true)) {
                        println("DEBUG: CallViewModel - createPeerConnection success, creating offer")
                        launch {
                            val offer = webRTCService.createOffer()
                            println("DEBUG: CallViewModel - offer created: type=${offer?.type} length=${offer?.description?.length}")
                            offer?.let { sdp ->
                                callRepository.updateSignalingData(
                                    callSessionId = callSession.id,
                                    callerSdp = "${sdp.type}:${sdp.description}"
                                )
                                callRepository.updateCallStatus(callSession.id, CallStatus.RINGING)
                            }
                        }
                    } else {
                        println("DEBUG: CallViewModel - createPeerConnection failed")
                    }
                }
                .onFailure { exception ->
                    println("DEBUG: CallViewModel - initiateCall failure: ${exception.message}")
                    _error.value = exception.message
                }
        }
    }
    
    fun answerCall() {
        currentCallSessionId?.let { sessionId ->
            // Reset flags for new call
            hasProcessedCallerSdp = false
            hasProcessedReceiverSdp = false
            
            // Determine if this is a video call
            val isVideoCall = _callSession.value?.callType == CallType.VIDEO.name
            _isVideoCall.value = isVideoCall
            
            viewModelScope.launch {
                println("DEBUG: CallViewModel - answerCall() called for session: $sessionId, isVideoCall: $isVideoCall")
                // Stop ringing notification immediately on answer
                callNotificationService.dismissIncomingCallNotification()
                callRepository.answerCall(sessionId)
                    .onSuccess {
                        println("DEBUG: CallViewModel - answerCall repository success, status updated to CONNECTING")
                        // Initialize WebRTC for answering
                        if (webRTCService.createPeerConnection(isVideoCall, isCaller = false)) {
                            println("DEBUG: CallViewModel - answerCall: peer created, processing callerSdp")
                            // Process the callerSdp immediately after creating peer connection
                            _callSession.value?.let { callSession ->
                                if (callSession.callerSdp != null && !hasProcessedCallerSdp) {
                                    println("DEBUG: CallViewModel - answerCall: processing existing callerSdp")
                                    hasProcessedCallerSdp = true
                                    val parts = callSession.callerSdp.split(":", limit = 2)
                                    if (parts.size == 2) {
                                        val sdpType = SessionDescription.Type.valueOf(parts[0])
                                        val sdpDescription = parts[1]
                                        val callerSdp = SessionDescription(sdpType, sdpDescription)
                                        
                                        webRTCService.setRemoteDescription(callerSdp)
                                        // Prepare local video after remote OFFER is set (callee side)
                                        webRTCService.prepareLocalVideoAfterRemoteSet()
                                        // Create answer
                                        val answer = webRTCService.createAnswer()
                                        println("DEBUG: CallViewModel - answerCall: answer created: type=${answer?.type} length=${answer?.description?.length}")
                                        answer?.let { sdp ->
                                            callRepository.updateSignalingData(
                                                callSessionId = sessionId,
                                                receiverSdp = "${sdp.type}:${sdp.description}"
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            println("DEBUG: CallViewModel - failed to create peer connection")
                        }
                    }
                    .onFailure { exception ->
                        println("DEBUG: CallViewModel - answerCall failure: ${exception.message}")
                        _error.value = exception.message
                    }
            }
        } ?: run {
            println("DEBUG: CallViewModel - answerCall() called but no currentCallSessionId")
        }
    }
    
    fun declineCall() {
        currentCallSessionId?.let { sessionId ->
            viewModelScope.launch {
                callRepository.declineCall(sessionId)
                webRTCService.disconnect()
                // Stop any ringing/vibration on this device immediately
                callNotificationService.dismissIncomingCallNotification()
                callNotificationService.dismissOngoingCallNotification()
            }
        }
    }
    
    fun endCall() {
        currentCallSessionId?.let { sessionId ->
            viewModelScope.launch {
                callRepository.endCall(sessionId)
                webRTCService.disconnect()
                // Ensure local notifications and vibration are stopped
                callNotificationService.dismissIncomingCallNotification()
                callNotificationService.dismissOngoingCallNotification()
            }
        }
    }
    
    fun toggleAudio() {
        webRTCService.toggleAudio()
    }
    
    fun toggleSpeaker() {
        val newState = !_isSpeakerEnabled.value
        _isSpeakerEnabled.value = newState
        webRTCService.enableSpeaker(newState)
    }
    
    fun toggleVideo() {
        webRTCService.toggleVideo()
    }
    
    fun switchCamera() {
        webRTCService.switchCamera()
    }
    
    fun setupLocalSurfaceView(surfaceView: org.webrtc.SurfaceViewRenderer) {
        webRTCService.setupLocalSurfaceView(surfaceView)
    }
    
    fun setupRemoteSurfaceView(surfaceView: org.webrtc.SurfaceViewRenderer) {
        webRTCService.setupRemoteSurfaceView(surfaceView)
    }
    
    private fun handleCallSessionUpdate(callSession: CallSession) {
        println("DEBUG: CallViewModel - handleCallSessionUpdate: status=${callSession.status}, callerSdp=${callSession.callerSdp != null}, receiverSdp=${callSession.receiverSdp != null}")
        when (callSession.status) {
            CallStatus.CONNECTING.name -> {
                // SDP processing is now handled in answerCall() method
                println("DEBUG: CallViewModel - handleCallSessionUpdate: CONNECTING status, SDP processing handled in answerCall()")
                
                // Handle receiverSdp received (for caller)
                if (callSession.receiverSdp != null && !hasProcessedReceiverSdp) {
                    println("DEBUG: CallViewModel - handleCallSessionUpdate: receiverSdp received")
                    hasProcessedReceiverSdp = true
                    val parts = callSession.receiverSdp.split(":", limit = 2)
                    if (parts.size == 2) {
                        val sdpType = SessionDescription.Type.valueOf(parts[0])
                        val sdpDescription = parts[1]
                        val receiverSdp = SessionDescription(sdpType, sdpDescription)
                        
                        webRTCService.setRemoteDescription(receiverSdp)
                        // Do not force CONNECTED here; wait for actual WebRTC connection state
                    }
                }
            }
            CallStatus.CONNECTED.name -> {
                println("DEBUG: CallViewModel - handleCallSessionUpdate: call connected successfully")
                // Call is connected, ensure audio track is enabled and force recording
                webRTCService.ensureAudioTrackEnabled()
                webRTCService.onCallConnected()
                // Ensure notification state reflects active call
                callNotificationService.dismissIncomingCallNotification()
                callNotificationService.showOngoingCallNotification(callSession)
            }
            CallStatus.ENDED.name, CallStatus.DECLINED.name, CallStatus.MISSED.name -> {
                println("DEBUG: CallViewModel - handleCallSessionUpdate: call finished with ${callSession.status}")
                webRTCService.disconnect()
                // Ensure all call notifications are cleared
                callNotificationService.dismissIncomingCallNotification()
                callNotificationService.dismissOngoingCallNotification()
            }
        }
        
        // Handle ICE candidates
        val newCandidates = callSession.iceCandidates - iceCandidates
        if (newCandidates.isNotEmpty()) println("DEBUG: CallViewModel - new ICE candidates: ${newCandidates.size}")
        newCandidates.forEach { candidateString ->
            try {
                val parts = candidateString.split("|")
                if (parts.size >= 3) {
                    val candidate = IceCandidate(parts[0], parts[1].toInt(), parts[2])
                    webRTCService.addIceCandidate(candidate)
                }
            } catch (e: Exception) {
                println("DEBUG: CallViewModel - handleIceCandidate parse error: ${e.message}")
                e.printStackTrace()
            }
        }
        iceCandidates.addAll(newCandidates)
    }
    
    private fun handleIceCandidate(candidate: IceCandidate) {
        val candidateString = "${candidate.sdpMid}|${candidate.sdpMLineIndex}|${candidate.sdp}"
        val updatedCandidates = iceCandidates + candidateString
        
        currentCallSessionId?.let { sessionId ->
            viewModelScope.launch {
                callRepository.updateSignalingData(
                    callSessionId = sessionId,
                    iceCandidates = updatedCandidates
                )
                println("DEBUG: CallViewModel - pushed ICE candidates: ${updatedCandidates.size}")
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    override fun onCleared() {
        super.onCleared()
        webRTCService.disconnect()
    }
}
