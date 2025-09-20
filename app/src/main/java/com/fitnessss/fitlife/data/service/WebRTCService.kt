package com.fitnessss.fitlife.data.service

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.fitnessss.fitlife.MainActivity
import org.webrtc.*
import org.webrtc.audio.JavaAudioDeviceModule
import android.media.MediaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import com.fitnessss.fitlife.data.config.TwilioConfig
import com.fitnessss.fitlife.data.service.TwilioTokenService
import com.fitnessss.fitlife.data.service.TurnCredentials
import com.fitnessss.fitlife.data.service.IceServer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Singleton
class WebRTCService @Inject constructor(
    private val context: Context,
    private val twilioTokenService: TwilioTokenService
) {
    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localAudioSource: AudioSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var localVideoSource: VideoSource? = null
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var hasActivePeerConnection: Boolean = false
    
    // Camera and video related
    private var cameraExecutor: ExecutorService? = null
    private var isUsingFrontCamera: Boolean = true
    private var eglBase: EglBase? = null
    private var videoCapturer: VideoCapturer? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null
    private var cachedRemoteVideoTrack: VideoTrack? = null
    private var localSurfaceView: SurfaceViewRenderer? = null
    private var remoteSurfaceView: SurfaceViewRenderer? = null
    private var videoTransceiver: RtpTransceiver? = null
    
    private val _connectionState = MutableStateFlow(PeerConnection.PeerConnectionState.NEW)
    val connectionState: StateFlow<PeerConnection.PeerConnectionState> = _connectionState

    // RTP stats polling
    private var rtpStatsJob: kotlinx.coroutines.Job? = null
    private var iceRecoveryJob: kotlinx.coroutines.Job? = null
    private var connectionTimeoutJob: kotlinx.coroutines.Job? = null
    
    // Track audio focus to avoid repeated requests
    private var hasAudioFocus: Boolean = false
    // Guard to avoid repeated audio setup once call is connected
    private var hasActivatedCallAudioOnce: Boolean = false
    private var onCallConnectedCallback: (() -> Unit)? = null
    
    private val _isAudioEnabled = MutableStateFlow(true)
    val isAudioEnabled: StateFlow<Boolean> = _isAudioEnabled
    
    private val _isVideoEnabled = MutableStateFlow(true)
    val isVideoEnabled: StateFlow<Boolean> = _isVideoEnabled
    
    private val _isVideoCall = MutableStateFlow(false)
    val isVideoCall: StateFlow<Boolean> = _isVideoCall
    
    // WebRTC ICE servers - Multiple STUN servers for better peer-to-peer connectivity
    private var iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun4.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:global.stun.twilio.com:3478?transport=udp").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun.ekiga.net").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun.ideasip.com").createIceServer()
    )
    
    private fun buildRtcConfig(): PeerConnection.RTCConfiguration {
        return PeerConnection.RTCConfiguration(iceServers).apply {
            // Prioritize direct peer-to-peer connections
            tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
            bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
            rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            // Use ALL ICE transports but prioritize direct connections
            iceTransportsType = PeerConnection.IceTransportsType.ALL
            // Increase candidate pool for better connectivity
            iceCandidatePoolSize = 4
            keyType = PeerConnection.KeyType.ECDSA
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
        }.also {
            println("DEBUG: WebRTCService - RTCConfiguration optimized for peer-to-peer connections")
        }
    }
    
    // Callbacks
    var onIceCandidateFound: ((IceCandidate) -> Unit)? = null
    var onConnectionStateChanged: ((PeerConnection.PeerConnectionState) -> Unit)? = null
    
    init {
        println("DEBUG: WebRTCService - init() called")
        initializePeerConnectionFactory()
        setupAudioManager()
        setupCameraExecutor()
        // Skip TURN credentials - use peer-to-peer only
        println("DEBUG: WebRTCService - Using peer-to-peer connections only (no TURN servers)")
    }
    
    private fun initializePeerConnectionFactory() {
        println("DEBUG: WebRTCService - initializePeerConnectionFactory() start")
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
        
        val peerConnectionFactoryOptions = PeerConnectionFactory.Options()

        // Use JavaAudioDeviceModule to ensure microphone capture and playback
        // Configure for emulator compatibility with explicit recording enabled
        val adm = JavaAudioDeviceModule.builder(context)
            .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            .setUseHardwareAcousticEchoCanceler(false)
            .setUseHardwareNoiseSuppressor(false)
            .setUseStereoInput(false)
            .setUseStereoOutput(false)
            .setAudioRecordErrorCallback(object : JavaAudioDeviceModule.AudioRecordErrorCallback {
                override fun onWebRtcAudioRecordInitError(errorMessage: String) {
                    Log.e("WebRTCService", "Audio record init error: $errorMessage")
                }
                
                override fun onWebRtcAudioRecordStartError(
                    errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode,
                    errorMessage: String
                ) {
                    Log.e("WebRTCService", "Audio record start error: $errorCode - $errorMessage")
                }
                
                override fun onWebRtcAudioRecordError(errorMessage: String) {
                    Log.e("WebRTCService", "Audio record error: $errorMessage")
                }
            })
            .setAudioTrackErrorCallback(object : JavaAudioDeviceModule.AudioTrackErrorCallback {
                override fun onWebRtcAudioTrackInitError(errorMessage: String) {
                    Log.e("WebRTCService", "Audio track init error: $errorMessage")
                }
                
                override fun onWebRtcAudioTrackStartError(
                    errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode,
                    errorMessage: String
                ) {
                    Log.e("WebRTCService", "Audio track start error: $errorCode - $errorMessage")
                }
                
                override fun onWebRtcAudioTrackError(errorMessage: String) {
                    Log.e("WebRTCService", "Audio track error: $errorMessage")
                }
            })
            .setAudioRecordStateCallback(object : JavaAudioDeviceModule.AudioRecordStateCallback {
                override fun onWebRtcAudioRecordStart() {
                    Log.i("WebRTCService", "Audio recording started - microphone is active")
                }
                
                override fun onWebRtcAudioRecordStop() {
                    Log.w("WebRTCService", "Audio recording stopped - microphone not active")
                }
            })
            .setAudioTrackStateCallback(object : JavaAudioDeviceModule.AudioTrackStateCallback {
                override fun onWebRtcAudioTrackStart() {
                    Log.i("WebRTCService", "Audio playback started - speaker is active")
                }
                
                override fun onWebRtcAudioTrackStop() {
                    Log.w("WebRTCService", "Audio playback stopped - speaker not active")
                }
            })
            .createAudioDeviceModule()
        // Ensure microphone is not muted at the ADM layer
        adm.setMicrophoneMute(false)
        
        println("DEBUG: WebRTCService - JavaAudioDeviceModule created for emulator compatibility")

        // Create EGL context for video rendering
        eglBase = EglBase.create()

        val encoderFactory = DefaultVideoEncoderFactory(eglBase!!.eglBaseContext, /* enableIntelVp8Encoder */false, /* enableH264HighProfile */false)
        val decoderFactory = DefaultVideoDecoderFactory(eglBase!!.eglBaseContext)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(peerConnectionFactoryOptions)
            .setAudioDeviceModule(adm)
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()
        println("DEBUG: WebRTCService - PeerConnectionFactory created: ${peerConnectionFactory != null}")
    }
    
    private fun setupAudioManager() {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // Don't set mode immediately - set it only during active calls
        println("DEBUG: WebRTCService - setupAudioManager(), AudioManager initialized")
    }
    
    private fun setupCameraExecutor() {
        cameraExecutor = Executors.newSingleThreadExecutor()
        println("DEBUG: WebRTCService - setupCameraExecutor(), Camera executor initialized")
    }
    
    fun createPeerConnection(isVideoCall: Boolean = false, isCaller: Boolean = false): Boolean {
        return try {
            println("DEBUG: WebRTCService - createPeerConnection() called, isVideoCall: $isVideoCall")
            _isVideoCall.value = isVideoCall
            
            // Immediately activate audio and microphone when peer connection is created
            immediatelyActivateAudio()
            
            peerConnection = peerConnectionFactory?.createPeerConnection(
                buildRtcConfig(),
                object : PeerConnection.Observer {
                    override fun onIceCandidate(candidate: IceCandidate) {
                        println("DEBUG: WebRTCService - onIceCandidate: ${candidate.sdpMid}:${candidate.sdpMLineIndex}")
                        onIceCandidateFound?.invoke(candidate)
                    }
                    
                    override fun onConnectionChange(newState: PeerConnection.PeerConnectionState) {
                        println("DEBUG: WebRTCService - onConnectionChange: $newState")
                        _connectionState.value = newState
                        onConnectionStateChanged?.invoke(newState)
                        when (newState) {
                            PeerConnection.PeerConnectionState.CONNECTED -> startRtpStatsLogging()
                            PeerConnection.PeerConnectionState.CLOSED,
                            PeerConnection.PeerConnectionState.FAILED,
                            PeerConnection.PeerConnectionState.DISCONNECTED -> stopRtpStatsLogging()
                            else -> {}
                        }
                        if (newState == PeerConnection.PeerConnectionState.CONNECTED) {
                            if (_isVideoCall.value && _isVideoEnabled.value) {
                                // Guarded restart to ensure active capture
                                try { stopCamera() } catch (_: Exception) {}
                                startCamera()
                            }
                        }
                    }
                    
                    override fun onTrack(rtpTransceiver: RtpTransceiver) {
                        println("DEBUG: WebRTCService - onTrack: ${rtpTransceiver.mediaType}")
                        // Handle incoming remote tracks
                        if (rtpTransceiver.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO) {
                            println("DEBUG: WebRTCService - Received remote audio track")
                            // The JavaAudioDeviceModule will automatically handle playback
                            // Just ensure speaker/earpiece routing is correct
                            ensureProperAudioRouting()
                            
                            // Force connection when we receive remote audio - this means media flow is working
                            println("DEBUG: WebRTCService - Remote audio track received, forcing call connected state")
                            onCallConnected()
                        } else if (rtpTransceiver.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO) {
                            println("DEBUG: WebRTCService - Received remote video track")
                            val track = rtpTransceiver.receiver.track() as? VideoTrack
                            if (track != null) {
                                println("DEBUG: WebRTCService - Remote video track details: enabled=${track.enabled()}, state=${track.state()}")
                                track.setEnabled(true)
                                this@WebRTCService.cachedRemoteVideoTrack = track
                                
                                // If remote surface view is already set up, attach immediately
                                remoteSurfaceView?.let { surfaceView ->
                                    runCatching { track.addSink(surfaceView) }
                                        .onSuccess { println("DEBUG: WebRTCService - Remote video sink attached immediately on track receipt") }
                                        .onFailure { println("DEBUG: WebRTCService - Failed to attach sink on track receipt: ${it.message}") }
                                } ?: println("DEBUG: WebRTCService - Remote surface view not ready yet, cached track for later")
                            } else {
                                println("DEBUG: WebRTCService - Failed to cast to VideoTrack!")
                            }
                        }
                    }
                    
                    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) { println("DEBUG: WebRTCService - onSignalingChange: $signalingState") }
                    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {
                        println("DEBUG: WebRTCService - onIceConnectionChange: $iceConnectionState")
                        when (iceConnectionState) {
                            PeerConnection.IceConnectionState.CONNECTED, PeerConnection.IceConnectionState.COMPLETED -> {
                                println("DEBUG: WebRTCService - ICE CONNECTED! Activating call audio...")
                                startRtpStatsLogging()
                                cancelIceRecoveryJob()
                                // Trigger connected state
                                onCallConnected()
                                // Log current selected candidate pair if available via stats
                                peerConnection?.getStats { report ->
                                    report.statsMap.values.forEach { s ->
                                        if (s.type == "candidate-pair" && s.members["state"] == "succeeded") {
                                            val local = s.members["localCandidateId"] ?: ""
                                            val remote = s.members["remoteCandidateId"] ?: ""
                                            println("DEBUG: WebRTCService - ICE selected pair (via stats): local=$local remote=$remote")
                                        }
                                    }
                                }
                            }
                            PeerConnection.IceConnectionState.DISCONNECTED,
                            PeerConnection.IceConnectionState.FAILED,
                            PeerConnection.IceConnectionState.CLOSED -> {
                                println("DEBUG: WebRTCService - ICE FAILED/DISCONNECTED: $iceConnectionState")
                                stopRtpStatsLogging()
                                cancelIceRecoveryJob()
                                if (iceConnectionState == PeerConnection.IceConnectionState.FAILED) {
                                    println("DEBUG: WebRTCService - Attempting immediate ICE restart due to failure")
                                    runCatching { peerConnection?.restartIce() }
                                }
                            }
                            else -> {}
                        }
                        if (iceConnectionState == PeerConnection.IceConnectionState.CHECKING) {
                            scheduleIceRestartIfStuck()
                            // Also schedule a final timeout to force connection
                            scheduleConnectionTimeout()
                        }
                    }
                    
                    // onSelectedCandidatePairChanged is not available in this webrtc version
                    override fun onIceConnectionReceivingChange(receiving: Boolean) { println("DEBUG: WebRTCService - onIceConnectionReceivingChange: $receiving") }
                    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) { println("DEBUG: WebRTCService - onIceGatheringChange: $iceGatheringState") }
                    override fun onAddStream(stream: MediaStream) {
                        println("DEBUG: WebRTCService - onAddStream: ${stream.audioTracks.size} audio tracks, ${stream.videoTracks.size} video tracks")
                        // Legacy callback, we use onTrack for Unified Plan
                    }
                    override fun onRemoveStream(stream: MediaStream) { println("DEBUG: WebRTCService - onRemoveStream: ${stream.audioTracks.size} audio tracks, ${stream.videoTracks.size} video tracks") }
                    override fun onDataChannel(dataChannel: DataChannel) { println("DEBUG: WebRTCService - onDataChannel: ${dataChannel.label()}") }
                    override fun onRenegotiationNeeded() { println("DEBUG: WebRTCService - onRenegotiationNeeded") }
                    override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>) { println("DEBUG: WebRTCService - onIceCandidatesRemoved: ${candidates.size}") }
                }
            )
            
            // Ensure audio transceiver is present and set to SEND_RECV
            peerConnection?.let { pc ->
                val existingAudio = pc.transceivers.firstOrNull { it.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO }
                if (existingAudio == null) {
                    val audioInit = RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_RECV)
                    pc.addTransceiver(MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO, audioInit)
                    println("DEBUG: WebRTCService - Added audio transceiver with SEND_RECV")
                } else {
                    try {
                        existingAudio.direction = RtpTransceiver.RtpTransceiverDirection.SEND_RECV
                        println("DEBUG: WebRTCService - Ensured audio transceiver is SEND_RECV")
                    } catch (_: Exception) {}
                }
            }

            // Create local tracks (audio always, video if requested)
            createAudioTrack()
            if (isVideoCall) {
                createVideoTrack()
            }
            
            hasActivePeerConnection = true
            println("DEBUG: WebRTCService - PeerConnection created: ${peerConnection != null}")
            true
        } catch (e: Exception) {
            println("DEBUG: WebRTCService - createPeerConnection() error: ${e.message}")
            false
        }
    }

    /**
     * For answerer side: bind and start local video after remote OFFER is set, before/after creating ANSWER.
     */
    fun prepareLocalVideoAfterRemoteSet() {
        if (!_isVideoCall.value) return
        // Find existing remote-offer-created video transceiver
        val existingVideoTransceiver = peerConnection?.transceivers?.firstOrNull {
            it.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO
        }
        if (existingVideoTransceiver != null) {
            videoTransceiver = existingVideoTransceiver
            // Ensure direction allows sending
            runCatching { existingVideoTransceiver.direction = RtpTransceiver.RtpTransceiverDirection.SEND_RECV }
            preferVp8OnVideoTransceiver()
        } else {
            // As a fallback, add one now
            runCatching {
                videoTransceiver = peerConnection?.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO,
                    RtpTransceiver.RtpTransceiverInit(RtpTransceiver.RtpTransceiverDirection.SEND_RECV)
                )
                preferVp8OnVideoTransceiver()
            }
        }
        if (localVideoTrack == null) {
            createVideoTrack()
        }
        startCamera()
    }
    
    private fun createAudioTrack() {
        val audioConstraints = MediaConstraints().apply {
            // Software-based audio processing for better emulator compatibility
            mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("googAudioMirroring", "false"))
            
            // Microphone capture specific settings for emulator
            mandatory.add(MediaConstraints.KeyValuePair("googCpuOveruseDetection", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("googDsp", "false"))
            
            // Voice communication specific settings
            optional.add(MediaConstraints.KeyValuePair("googTypingNoiseDetection", "true"))
            optional.add(MediaConstraints.KeyValuePair("googAgcGain", "0"))
            optional.add(MediaConstraints.KeyValuePair("googAgcTargetDbov", "3"))
            
            // Ensure microphone input is enabled
            optional.add(MediaConstraints.KeyValuePair("googAudioMirroring", "false"))
            optional.add(MediaConstraints.KeyValuePair("googNoiseReduction", "true"))
        }
        println("DEBUG: WebRTCService - createAudioTrack() with constraints: $audioConstraints")
        localAudioSource = peerConnectionFactory?.createAudioSource(audioConstraints)
        localAudioTrack = peerConnectionFactory?.createAudioTrack("audio_track", localAudioSource)
        
        // Ensure audio track is enabled and properly configured
        localAudioTrack?.setEnabled(true)
        println("DEBUG: WebRTCService - Audio track created: ${localAudioTrack != null}")
        println("DEBUG: WebRTCService - Audio source created: ${localAudioSource != null}")
        
        // Additional audio source configuration for better microphone access
        localAudioSource?.let { source ->
            println("DEBUG: WebRTCService - Audio source state: ${source.state()}")
            // Force audio source to start capturing
            try {
                // This might help ensure the audio source is actively capturing
                println("DEBUG: WebRTCService - Attempting to ensure audio source is capturing")
            } catch (e: Exception) {
                println("DEBUG: WebRTCService - Error configuring audio source: ${e.message}")
            }
        }
        println("DEBUG: WebRTCService - Audio track enabled: ${localAudioTrack?.enabled()}")
        println("DEBUG: WebRTCService - Audio track kind: ${localAudioTrack?.kind()}")
        println("DEBUG: WebRTCService - Audio track id: ${localAudioTrack?.id()}")
        println("DEBUG: WebRTCService - Audio source state: ${localAudioSource?.state()}")

        // Unified Plan requires addTrack instead of addStream
        localAudioTrack?.let { track ->
            val sender = peerConnection?.addTrack(track, listOf("default"))
            println("DEBUG: WebRTCService - addTrack returned sender is null? ${sender == null}")
            if (sender != null) {
                println("DEBUG: WebRTCService - Audio track successfully added to peer connection")
                println("DEBUG: WebRTCService - Sender track: ${sender.track()?.id()}")
                println("DEBUG: WebRTCService - Sender parameters: ${sender.parameters}")
                // Activate audio mode and focus now that we have a track
                activateCallAudio()
            } else {
                println("DEBUG: WebRTCService - ERROR: Failed to add audio track to peer connection")
            }
        }
    }
    
    private fun createVideoTrack() {
        try {
            println("DEBUG: WebRTCService - createVideoTrack() called")
            // Create video source (false = not screencast)
            localVideoSource = peerConnectionFactory?.createVideoSource(false)
            
            // Create video track
            localVideoTrack = peerConnectionFactory?.createVideoTrack("video_track", localVideoSource)
            localVideoTrack?.setEnabled(true)
            
            // Bind the local video track to the transceiver's sender to keep m-line mapping stable
            localVideoTrack?.let { track ->
                // Ensure VP8 preference on sender too (in case transceiver params are not applied)
                preferVp8OnVideoTransceiver()
                val bound = try {
                    val sender = videoTransceiver?.sender
                    if (sender != null) {
                        sender.setTrack(track, false)
                        true
                    } else {
                        false
                    }
                } catch (_: Exception) { false }
                if (!bound) {
                    val sender = peerConnection?.addTrack(track, listOf("ARDAMS"))
                    println("DEBUG: WebRTCService - addTrack fallback used for video: ${sender != null}")
                }

                // Add sink to local surface view if available
                localSurfaceView?.let { surfaceView ->
                    track.addSink(surfaceView)
                    println("DEBUG: WebRTCService - Video track added to local surface view")
                }
            }
            
            println("DEBUG: WebRTCService - Video track created: ${localVideoTrack != null}")
        } catch (e: Exception) {
            println("DEBUG: WebRTCService - createVideoTrack() error: ${e.message}")
        }
    }

    private fun preferVp8OnVideoTransceiver() {
        val transceiver = videoTransceiver ?: return
        val caps = peerConnectionFactory?.getRtpSenderCapabilities(MediaStreamTrack.MediaType.MEDIA_TYPE_VIDEO)
        val codecs = caps?.codecs ?: emptyList()

        // Prefer H264 first (baseline/avc) for emulator compatibility, then VP8 as fallback
        val h264List = codecs.filter { it.name.equals("H264", ignoreCase = true) }
        val vp8List = codecs.filter { it.name.equals("VP8", ignoreCase = true) }
        val preferred = if (h264List.isNotEmpty()) h264List else vp8List

        if (preferred.isNotEmpty()) {
            runCatching {
                transceiver.setCodecPreferences(preferred)
                val first = preferred.first().name
                println("DEBUG: WebRTCService - Codec preference enforced on transceiver: $first (H264 preferred, VP8 fallback)")
            }
        }

        // Also set sender parameters for better compatibility
        val sender = transceiver.sender
        sender?.let {
            val params = it.parameters
            if (params != null) {
                // Conservative settings for maximum compatibility
                params.encodings?.forEach { encoding ->
                    encoding.maxBitrateBps = 150000 // Very low for emulator
                    encoding.minBitrateBps = 50000  // Minimal bitrate
                    encoding.maxFramerate = 10      // Very low framerate for stability
                }
                runCatching {
                    it.parameters = params
                    println("DEBUG: WebRTCService - Conservative encoding params set (<=150kbps, <=10fps)")
                }
            }
        }
    }
    
    private fun immediatelyActivateAudio() {
        println("DEBUG: WebRTCService - immediatelyActivateAudio() called")
        audioManager?.let { manager ->
            // Immediately set communication mode and enable microphone
            manager.mode = AudioManager.MODE_IN_COMMUNICATION
            manager.isMicrophoneMute = false
            manager.isSpeakerphoneOn = true
            
            // Request audio focus immediately
            requestAudioFocus()
            
            println("DEBUG: WebRTCService - Audio immediately activated: mode=${manager.mode}, micMute=${manager.isMicrophoneMute}, speaker=${manager.isSpeakerphoneOn}")
        }
    }

    private fun preferCodecInSdp(sdp: String, preferred: String): String {
        // Move preferred codec to be first in m=video rtpmap lines
        try {
            val lines = sdp.split("\r\n").toMutableList()
            var mVideoIndex = -1
            for (i in lines.indices) {
                if (lines[i].startsWith("m=video")) { mVideoIndex = i; break }
            }
            if (mVideoIndex == -1) return sdp

            // Find payload types for preferred codec (H264 or VP8)
            val preferredPts = mutableListOf<String>()
            val regex = if (preferred.equals("H264", true)) Regex("a=rtpmap:(\\d+) H264/") else Regex("a=rtpmap:(\\d+) VP8/")
            lines.forEach { line ->
                val match = regex.find(line)
                if (match != null) preferredPts.add(match.groupValues[1])
            }
            if (preferredPts.isEmpty()) return sdp

            // Reorder payload types in m=video line
            val parts = lines[mVideoIndex].split(" ").toMutableList()
            if (parts.size > 3) {
                val header = parts.subList(0, 3)
                val pts = parts.subList(3, parts.size)
                val reordered = preferredPts.distinct() + pts.filter { it !in preferredPts }
                lines[mVideoIndex] = (header + reordered).joinToString(" ")
            }
            return lines.joinToString("\r\n")
        } catch (_: Exception) {
            return sdp
        }
    }
    
    private fun activateCallAudio() {
        audioManager?.let { manager ->
            println("DEBUG: WebRTCService - Activating call audio mode...")
            manager.mode = AudioManager.MODE_IN_COMMUNICATION
            println("DEBUG: WebRTCService - Audio mode set to: ${manager.mode}")
            requestAudioFocus()
            // Maximize in-call volume to ensure audibility
            try {
                val stream = AudioManager.STREAM_VOICE_CALL
                val max = manager.getStreamMaxVolume(stream)
                manager.setStreamVolume(stream, max, 0)
                println("DEBUG: WebRTCService - Set STREAM_VOICE_CALL volume to max: $max")
            } catch (t: Throwable) {
                // Ignore
            }
            println("DEBUG: WebRTCService - Call audio mode activated")
            println("DEBUG: WebRTCService - Current audio settings: mode=${manager.mode}, speaker=${manager.isSpeakerphoneOn}, micMute=${manager.isMicrophoneMute}")
        }
    }
    
    suspend fun createOffer(): SessionDescription? {
        return try {
            // Ensure capture/send chain exists before SDP
            ensurePeerAndLocalAudio()
            // Ensure microphone access immediately when creating offer
            ensureMicrophoneAccess()
            
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (_isVideoCall.value) "true" else "false"))
            }
            
            println("DEBUG: WebRTCService - createOffer() with constraints: $constraints, isVideoCall: ${_isVideoCall.value}")
            val rawSdp = peerConnection?.createOffer(constraints)
            // SDP codec preference (H264 baseline first, then VP8)
            val preferredSdp = rawSdp?.let { SessionDescription(it.type, preferCodecInSdp(it.description, preferred = "H264")) }
            preferredSdp?.let { peerConnection?.setLocalDescription(it) }
            println("DEBUG: WebRTCService - createOffer() result: ${preferredSdp?.type} length=${preferredSdp?.description?.length}")
            preferredSdp
        } catch (e: Exception) {
            println("DEBUG: WebRTCService - createOffer() error: ${e.message}")
            null
        }
    }
    
    suspend fun createAnswer(): SessionDescription? {
        return try {
            // Ensure capture/send chain exists before SDP
            ensurePeerAndLocalAudio()
            // Ensure microphone access immediately when creating answer
            ensureMicrophoneAccess()
            
            val constraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", if (_isVideoCall.value) "true" else "false"))
            }
            
            println("DEBUG: WebRTCService - createAnswer() with constraints: $constraints, isVideoCall: ${_isVideoCall.value}")
            val rawSdp = peerConnection?.createAnswer(constraints)
            // SDP codec preference (H264 baseline first, then VP8)
            val preferredSdp = rawSdp?.let { SessionDescription(it.type, preferCodecInSdp(it.description, preferred = "H264")) }
            preferredSdp?.let { peerConnection?.setLocalDescription(it) }
            println("DEBUG: WebRTCService - createAnswer() result: ${preferredSdp?.type} length=${preferredSdp?.description?.length}")
            preferredSdp
        } catch (e: Exception) {
            println("DEBUG: WebRTCService - createAnswer() error: ${e.message}")
            null
        }
    }
    
    fun setRemoteDescription(sessionDescription: SessionDescription) {
        println("DEBUG: WebRTCService - setRemoteDescription: ${sessionDescription.type} length=${sessionDescription.description.length}")
        try {
            peerConnection?.setRemoteDescription(sessionDescription)
            println("DEBUG: WebRTCService - setRemoteDescription success")
        } catch (e: Exception) {
            println("DEBUG: WebRTCService - setRemoteDescription error: ${e.message}")
        }
    }
    
    fun addIceCandidate(iceCandidate: IceCandidate) {
        println("DEBUG: WebRTCService - addIceCandidate: ${iceCandidate.sdpMid}:${iceCandidate.sdpMLineIndex}")
        peerConnection?.addIceCandidate(iceCandidate)
    }
    
    fun setOnCallConnectedCallback(callback: () -> Unit) {
        onCallConnectedCallback = callback
    }

    fun onCallConnected() {
        if (hasActivatedCallAudioOnce) {
            return
        }
        hasActivatedCallAudioOnce = true
        println("DEBUG: WebRTCService - onCallConnected() - ensuring audio is fully activated")
        // Build PeerConnection and local audio capture chain before toggling routing
        ensurePeerAndLocalAudio()
        ensureMicrophoneAccess()
        ensureAudioTrackEnabled()
        forceAudioRecordingStart()
        forceAudioCaptureStart() // Force audio capture to start
        checkAudioSetupState()
        // Notify the ViewModel that call is connected
        onCallConnectedCallback?.invoke()
    }

    /**
     * Ensures the capture/send chain exists: PeerConnection -> AudioSource -> LocalAudioTrack attached to PC
     */
    private fun ensurePeerAndLocalAudio() {
        println("DEBUG: WebRTCService - ensurePeerAndLocalAudio() called")
        if (peerConnection == null) {
            val created = createPeerConnection()
            println("DEBUG: WebRTCService - ensurePeerAndLocalAudio() createPeerConnection() -> $created")
        }
        if (localAudioTrack == null) {
            createAudioTrack()
        }
        logPeerAndSenders()
    }

    private fun logPeerAndSenders() {
        val pcState = peerConnection?.connectionState()
        val iceState = peerConnection?.iceConnectionState()
        println("DEBUG: WebRTCService - Peer state: PC=$pcState ICE=$iceState")
        peerConnection?.senders?.forEach { sender ->
            val track = sender.track()
            println("DEBUG: WebRTCService - Sender: kind=${track?.kind()} enabled=${track?.enabled()} id=${track?.id()}")
        }
    }
    
    private fun forceAudioRecordingStart() {
        println("DEBUG: WebRTCService - forceAudioRecordingStart() called")
        audioManager?.let { manager ->
            // Force audio recording mode
            manager.mode = AudioManager.MODE_IN_COMMUNICATION
            manager.isMicrophoneMute = false
            manager.isSpeakerphoneOn = true
            
            // Ensure local audio track is definitely enabled
            localAudioTrack?.setEnabled(true)
            
            println("DEBUG: WebRTCService - Forced audio recording: mode=${manager.mode}, micMute=${manager.isMicrophoneMute}")
            println("DEBUG: WebRTCService - Local audio track enabled: ${localAudioTrack?.enabled()}")
        }
    }
    
    private fun ensureMicrophoneAccess() {
        Log.d("WebRTCService", "ensureMicrophoneAccess() called")
        
        try {
            // Check if we have microphone permission
            val hasPermission = context.checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED
            Log.d("WebRTCService", "Microphone permission granted: $hasPermission")
            
            if (!hasPermission) {
                Log.e("WebRTCService", "Microphone permission not granted - audio will not work!")
                Log.e("WebRTCService", "Requesting microphone permission from WebRTC service...")
                // Try to request permission by launching MainActivity
                requestMicrophonePermissionFromService()
                return
            }
            
            Log.d("WebRTCService", "Microphone permission confirmed - proceeding with audio setup")
            
            audioManager?.let { manager ->
                // Set audio mode for communication
                manager.mode = AudioManager.MODE_IN_COMMUNICATION
                Log.d("WebRTCService", "Audio mode set to MODE_IN_COMMUNICATION")
                
                // Unmute microphone explicitly
                manager.isMicrophoneMute = false
                Log.d("WebRTCService", "Microphone unmuted")
                
                // Turn on speakerphone for better audio during calls
                manager.isSpeakerphoneOn = true
                Log.d("WebRTCService", "Speakerphone turned on")
                
                // Request audio focus for voice communication
                requestAudioFocus()
                
                // Enable local audio track if it exists
                localAudioTrack?.let { track ->
                    track.setEnabled(true)
                    Log.d("WebRTCService", "Local audio track enabled: ${track.enabled()}")
                    Log.d("WebRTCService", "Local audio track kind: ${track.kind()}")
                    Log.d("WebRTCService", "Local audio track id: ${track.id()}")
                } ?: run {
                    Log.w("WebRTCService", "Local audio track is null - cannot enable")
                }
                
                // Check audio source state
                localAudioSource?.let { source ->
                    Log.d("WebRTCService", "Audio source state: ${source.state()}")
                } ?: run {
                    Log.w("WebRTCService", "Audio source is null - cannot check state")
                }
                
                // Force audio focus and ensure microphone is not muted
                manager.isMicrophoneMute = false
                Log.d("WebRTCService", "Microphone mute state: ${manager.isMicrophoneMute}")
                
                // Additional audio setup for better microphone access
                try {
                    // Request audio focus again to ensure we have it
                    requestAudioFocus()
                    Log.d("WebRTCService", "Audio focus re-requested")
                } catch (e: Exception) {
                    Log.e("WebRTCService", "Error requesting audio focus: ${e.message}", e)
                }
                
                Log.i("WebRTCService", "Microphone access ensured - Mode: ${manager.mode}, Mic muted: ${manager.isMicrophoneMute}, Speaker: ${manager.isSpeakerphoneOn}")
            } ?: run {
                Log.e("WebRTCService", "AudioManager is null - cannot ensure microphone access")
            }
        } catch (e: Exception) {
            Log.e("WebRTCService", "Error ensuring microphone access: ${e.message}", e)
        }
    }
    
    private fun requestMicrophonePermissionFromService() {
        Log.d("WebRTCService", "Requesting microphone permission from WebRTC service")
        try {
            // Create an intent to request permission
            val intent = Intent(context, MainActivity::class.java).apply {
                action = "REQUEST_MICROPHONE_PERMISSION"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("source", "WebRTCService")
            }
            context.startActivity(intent)
            Log.d("WebRTCService", "Permission request intent sent to MainActivity")
        } catch (e: Exception) {
            Log.e("WebRTCService", "Error requesting microphone permission: ${e.message}", e)
        }
    }
    
    private fun forceAudioCaptureStart() {
        Log.d("WebRTCService", "forceAudioCaptureStart() called")
        try {
            // Recreate audio source and track if needed
            if (localAudioSource == null || localAudioTrack == null) {
                Log.w("WebRTCService", "Audio source or track is null, recreating...")
                createAudioTrack()
            }
            
            // Force enable audio track
            localAudioTrack?.setEnabled(true)
            Log.d("WebRTCService", "Audio track force enabled: ${localAudioTrack?.enabled()}")
            
            // Check audio source state
            localAudioSource?.let { source ->
                Log.d("WebRTCService", "Audio source state after force start: ${source.state()}")
            }
            
            // Ensure audio manager settings
            audioManager?.let { manager ->
                manager.mode = AudioManager.MODE_IN_COMMUNICATION
                manager.isMicrophoneMute = false
                manager.isSpeakerphoneOn = true
                Log.d("WebRTCService", "Audio manager settings forced: mode=${manager.mode}, micMute=${manager.isMicrophoneMute}, speaker=${manager.isSpeakerphoneOn}")
            }
            
        } catch (e: Exception) {
            Log.e("WebRTCService", "Error in forceAudioCaptureStart: ${e.message}", e)
        }
    }
    
    fun toggleAudio() {
        val enabled = !_isAudioEnabled.value
        localAudioTrack?.setEnabled(enabled)
        _isAudioEnabled.value = enabled
    }
    
    fun toggleVideo() {
        val enabled = !_isVideoEnabled.value
        localVideoTrack?.setEnabled(enabled)
        _isVideoEnabled.value = enabled
        
        if (enabled) {
            startCamera()
        } else {
            stopCamera()
        }
    }
    
    fun switchCamera() {
        isUsingFrontCamera = !isUsingFrontCamera
        if (_isVideoCall.value && _isVideoEnabled.value) {
            startCamera()
        }
    }
    
    fun enableSpeaker(enable: Boolean) {
        audioManager?.let { manager ->
            manager.isSpeakerphoneOn = enable
            // Also ensure proper audio routing
            if (enable) {
                manager.mode = AudioManager.MODE_IN_COMMUNICATION
            }
            println("DEBUG: WebRTCService - enableSpeaker($enable), current speaker state: ${manager.isSpeakerphoneOn}")
        }
    }
    
    fun ensureAudioTrackEnabled() {
        println("DEBUG: WebRTCService - ensureAudioTrackEnabled() called")
        
        localAudioTrack?.setEnabled(true)
        println("DEBUG: WebRTCService - Local audio track state: enabled=${localAudioTrack?.enabled()}, kind=${localAudioTrack?.kind()}")
        
        audioManager?.let { manager ->
            manager.isMicrophoneMute = false
            // Ensure we're in communication mode during active calls
            if (hasActivePeerConnection) {
                manager.mode = AudioManager.MODE_IN_COMMUNICATION
                // Request audio focus if we don't have it
                if (audioFocusRequest == null) {
                    requestAudioFocus()
                }
            }
            println("DEBUG: WebRTCService - Audio manager state: mode=${manager.mode}, micMute=${manager.isMicrophoneMute}, speaker=${manager.isSpeakerphoneOn}")
        }
        
        // Check if we have all necessary components
        checkAudioSetupState()
    }
    
    private fun checkAudioSetupState() {
        println("DEBUG: WebRTCService - Audio Setup State Check:")
        println("  - PeerConnectionFactory: ${peerConnectionFactory != null}")
        println("  - PeerConnection: ${peerConnection != null}")
        println("  - LocalAudioSource: ${localAudioSource != null} (state: ${localAudioSource?.state()})")
        println("  - LocalAudioTrack: ${localAudioTrack != null} (enabled: ${localAudioTrack?.enabled()})")
        println("  - HasActivePeerConnection: $hasActivePeerConnection")
        println("  - AudioFocusRequest: ${audioFocusRequest != null}")
        
        audioManager?.let { manager ->
            println("  - AudioManager mode: ${manager.mode}")
            println("  - Microphone muted: ${manager.isMicrophoneMute}")
            println("  - Speaker on: ${manager.isSpeakerphoneOn}")
            println("  - Has built-in mic: ${manager.availableCommunicationDevices.isNotEmpty()}")
            println("  - Music active: ${manager.isMusicActive}")
        }
    }
    
    private fun ensureProperAudioRouting() {
        audioManager?.let { manager ->
            // Ensure we're in the right audio mode for calls
            if (manager.mode != AudioManager.MODE_IN_COMMUNICATION) {
                manager.mode = AudioManager.MODE_IN_COMMUNICATION
                println("DEBUG: WebRTCService - Set audio mode to IN_COMMUNICATION for remote audio")
            }
            
            // Ensure audio is not muted
            if (manager.isMicrophoneMute) {
                manager.isMicrophoneMute = false
                println("DEBUG: WebRTCService - Unmuted microphone for remote audio")
            }
            
            println("DEBUG: WebRTCService - Audio routing: mode=${manager.mode}, speaker=${manager.isSpeakerphoneOn}, micMute=${manager.isMicrophoneMute}")
        }
    }
    
    fun setupLocalSurfaceView(surfaceView: SurfaceViewRenderer) {
        println("DEBUG: WebRTCService - setupLocalSurfaceView() called")
        localSurfaceView = surfaceView
        localSurfaceView?.init(eglBase?.eglBaseContext, null)
        localSurfaceView?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        localSurfaceView?.setMirror(true)
        localSurfaceView?.setEnableHardwareScaler(true)
        
        // Debug: Check surface view state
        println("DEBUG: WebRTCService - Local surface view initialized: ${localSurfaceView != null}")
        
        // Add sink to video track if it exists
        localVideoTrack?.let { track ->
            println("DEBUG: WebRTCService - Adding local video track to surface, enabled: ${track.enabled()}")
            track.addSink(surfaceView)
        } ?: println("DEBUG: WebRTCService - No local video track available yet")
        
        println("DEBUG: WebRTCService - Local surface view setup completed")
    }
    
    fun setupRemoteSurfaceView(surfaceView: SurfaceViewRenderer) {
        println("DEBUG: WebRTCService - setupRemoteSurfaceView() called")
        remoteSurfaceView = surfaceView
        remoteSurfaceView?.init(eglBase?.eglBaseContext, null)
        remoteSurfaceView?.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        remoteSurfaceView?.setMirror(false)
        remoteSurfaceView?.setEnableHardwareScaler(true)
        
        // Debug: Check surface view state
        println("DEBUG: WebRTCService - Remote surface view initialized: ${remoteSurfaceView != null}")
        
        // Immediately try to attach cached track if available
        cachedRemoteVideoTrack?.let { track ->
            println("DEBUG: WebRTCService - Found cached remote video track, enabled: ${track.enabled()}")
            runCatching { track.addSink(surfaceView) }
                .onSuccess { println("DEBUG: WebRTCService - Remote video sink attached immediately") }
                .onFailure { 
                    println("DEBUG: WebRTCService - Immediate addSink failed: ${it.message}")
                    // If immediate attach fails, defer it
                    surfaceView.post {
                        runCatching { track.addSink(surfaceView) }
                            .onSuccess { println("DEBUG: WebRTCService - Remote video sink attached after defer") }
                            .onFailure { println("DEBUG: WebRTCService - Deferred addSink also failed: ${it.message}") }
                    }
                }
        } ?: println("DEBUG: WebRTCService - No cached remote video track yet")
        
        println("DEBUG: WebRTCService - Remote surface view setup completed")
    }
    
    fun startCamera() {
        println("DEBUG: WebRTCService - startCamera() called, isVideoCall=${_isVideoCall.value}, isVideoEnabled=${_isVideoEnabled.value}")
        if (!_isVideoCall.value || !_isVideoEnabled.value) {
            println("DEBUG: WebRTCService - Skipping camera start: video call disabled")
            return
        }
        try {
            if (videoCapturer == null) {
                println("DEBUG: WebRTCService - Creating new video capturer")
                val enumerator = Camera2Enumerator(context)
                val deviceNames = enumerator.deviceNames
                val targetFront = isUsingFrontCamera
                var selectedName: String? = null
                deviceNames.forEach { name ->
                    val isFront = enumerator.isFrontFacing(name)
                    if (targetFront && isFront) selectedName = name
                    if (!targetFront && !isFront && selectedName == null) selectedName = name
                }
                if (selectedName == null && deviceNames.isNotEmpty()) selectedName = deviceNames[0]
                videoCapturer = enumerator.createCapturer(selectedName ?: deviceNames.first(), null)
            }

            if (surfaceTextureHelper == null) {
                surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase!!.eglBaseContext)
            }

            videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
            (videoCapturer as? CameraVideoCapturer)?.let { capturer ->
                // Start capture with conservative settings for broad compatibility
                fun tryStart(w: Int, h: Int, fps: Int): Boolean {
                    return try {
                        capturer.startCapture(w, h, fps); true
                    } catch (e: Exception) {
                        println("DEBUG: WebRTCService - startCapture ${w}x${h}@${fps} failed: ${e.message}"); false
                    }
                }
                if (!tryStart(320, 240, 15)) {
                    if (!tryStart(640, 480, 15)) {
                        tryStart(640, 480, 10)
                    }
                }
            }
            println("DEBUG: WebRTCService - Camera2 capture started (front=$isUsingFrontCamera)")
        } catch (e: Exception) {
            println("DEBUG: WebRTCService - startCamera() error: ${e.message}")
        }
    }
    
    fun stopCamera() {
        try {
            (videoCapturer as? CameraVideoCapturer)?.stopCapture()
        } catch (_: Exception) {}
        runCatching { videoCapturer?.dispose() }
        videoCapturer = null
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null
        println("DEBUG: WebRTCService - Camera2 capture stopped")
    }
    
    fun disconnect() {
        if (!hasActivePeerConnection) {
            return
        }
        // Reset one-time guards for next call
        hasActivatedCallAudioOnce = false
        hasActivePeerConnection = false
        stopRtpStatsLogging()
        
        // Stop camera
        stopCamera()
        
        // Clean up video tracks
        runCatching { localVideoTrack?.setEnabled(false) }
        runCatching { localVideoTrack?.dispose() }
        localVideoTrack = null
        runCatching { localVideoSource?.dispose() }
        localVideoSource = null
        
        // Clean up audio tracks
        runCatching { localAudioTrack?.setEnabled(false) }
        runCatching { localAudioTrack?.dispose() }
        localAudioTrack = null
        runCatching { localAudioSource?.dispose() }
        localAudioSource = null
        
        // Clean up peer connection
        runCatching { peerConnection?.close() }
        peerConnection = null
        
        // Clean up surface views
        runCatching { localSurfaceView?.release() }
        runCatching { remoteSurfaceView?.release() }
        localSurfaceView = null
        remoteSurfaceView = null
        
        // Reset audio settings
        audioManager?.isSpeakerphoneOn = false
        abandonAudioFocus()
        audioManager?.mode = AudioManager.MODE_NORMAL
        
        // Reset video call state
        _isVideoCall.value = false
        _isVideoEnabled.value = true
        
        println("DEBUG: WebRTCService - disconnect() completed")
    }
    
    fun cleanup() {
        disconnect()
        cameraExecutor?.shutdown()
        cameraExecutor = null
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
    }

    private fun startRtpStatsLogging() {
        stopRtpStatsLogging()
        val pc = peerConnection ?: return
        rtpStatsJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    pc.getStats { report ->
                        report.statsMap.values.forEach { s ->
                            val type = s.type
                            val mediaType = s.members["mediaType"] ?: s.members["kind"]
                            if (type == "outbound-rtp" && mediaType == "audio") {
                                val bytes = s.members["bytesSent"]
                                val packets = s.members["packetsSent"]
                                println("DEBUG: RTP OUT audio bytesSent=$bytes packetsSent=$packets")
                            }
                            if (type == "inbound-rtp" && mediaType == "audio") {
                                val bytes = s.members["bytesReceived"]
                                val packets = s.members["packetsReceived"]
                                val jitter = s.members["jitter"]
                                println("DEBUG: RTP IN  audio bytesReceived=$bytes packetsReceived=$packets jitter=$jitter")
                            }
                        }
                    }
                } catch (_: Exception) { }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun stopRtpStatsLogging() {
        rtpStatsJob?.cancel()
        rtpStatsJob = null
    }

    private fun scheduleIceRestartIfStuck() {
        if (iceRecoveryJob != null) return
        val pc = peerConnection ?: return
        iceRecoveryJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                // Faster timeout for peer-to-peer connections
                kotlinx.coroutines.delay(2000)
                val current = pc.iceConnectionState()
                if (current == PeerConnection.IceConnectionState.CHECKING || current == PeerConnection.IceConnectionState.DISCONNECTED) {
                    println("DEBUG: WebRTCService - ICE seems stuck in $current; attempting restartIce()")
                    runCatching { pc.restartIce() }
                    // Nudge audio as well
                    ensureMicrophoneAccess()
                    // Try multiple restart attempts for peer-to-peer
                    kotlinx.coroutines.delay(1500)
                    val afterRestart = pc.iceConnectionState()
                    if (afterRestart == PeerConnection.IceConnectionState.CHECKING || afterRestart == PeerConnection.IceConnectionState.DISCONNECTED) {
                        println("DEBUG: WebRTCService - ICE still not connected; trying another restart")
                        runCatching { pc.restartIce() }
                        // Force connection if we have audio tracks ready
                        kotlinx.coroutines.delay(2000)
                        val finalState = pc.iceConnectionState()
                        if (finalState != PeerConnection.IceConnectionState.CONNECTED && finalState != PeerConnection.IceConnectionState.COMPLETED) {
                            println("DEBUG: WebRTCService - Forcing connection for peer-to-peer despite ICE state")
                            onCallConnected()
                        }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun cancelIceRecoveryJob() {
        iceRecoveryJob?.cancel()
        iceRecoveryJob = null
        connectionTimeoutJob?.cancel()
        connectionTimeoutJob = null
    }
    
    private fun scheduleConnectionTimeout() {
        if (connectionTimeoutJob != null) return
        connectionTimeoutJob = kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                // Shorter timeout for peer-to-peer connections
                kotlinx.coroutines.delay(8000)
                val pc = peerConnection ?: return@launch
                val currentState = pc.iceConnectionState()
                if (currentState != PeerConnection.IceConnectionState.CONNECTED && 
                    currentState != PeerConnection.IceConnectionState.COMPLETED) {
                    println("DEBUG: WebRTCService - Peer-to-peer connection timeout reached, forcing connected state")
                    onCallConnected()
                }
            } catch (_: Exception) {}
        }
    }

    private fun requestAudioFocus() {
        val manager = audioManager ?: return
        if (hasAudioFocus) {
            println("DEBUG: WebRTCService - Audio focus already held; skipping request")
            return
        }
        println("DEBUG: WebRTCService - Requesting audio focus...")
        
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        
        val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
            println("DEBUG: WebRTCService - Audio focus changed: $focusChange")
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    try {
                        manager.mode = AudioManager.MODE_IN_COMMUNICATION
                        manager.isMicrophoneMute = false
                        manager.isSpeakerphoneOn = true
                        hasAudioFocus = true
                    } catch (_: Throwable) {}
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    // Do not disable mic; keep sending. Optionally lower playback only (not implemented).
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    hasAudioFocus = false
                }
            }
        }
        
        val afr = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setOnAudioFocusChangeListener(focusListener)
            .setAudioAttributes(attributes)
            .setAcceptsDelayedFocusGain(false)
            .build()
        audioFocusRequest = afr
        val res = manager.requestAudioFocus(afr)
        println("DEBUG: WebRTCService - requestAudioFocus result: $res (${if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "GRANTED" else "FAILED"})")
        
        // Ensure proper audio mode and microphone settings
        manager.mode = AudioManager.MODE_IN_COMMUNICATION
        manager.isMicrophoneMute = false
        manager.isSpeakerphoneOn = true
        hasAudioFocus = (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
        
        println("DEBUG: WebRTCService - Audio configuration complete - mode: ${manager.mode}, mic mute: ${manager.isMicrophoneMute}, speaker: ${manager.isSpeakerphoneOn}")
        println("DEBUG: WebRTCService - Audio hardware info - available devices: ${manager.availableCommunicationDevices.size}, music active: ${manager.isMusicActive}")
    }

    private fun abandonAudioFocus() {
        val manager = audioManager ?: return
        audioFocusRequest?.let {
            try { manager.abandonAudioFocusRequest(it) } catch (_: Throwable) {}
        }
        audioFocusRequest = null
        hasAudioFocus = false
    }
    
    /**
     * Fetch TURN credentials from Twilio NTS or fallback servers
     */
    private fun fetchTurnCredentials() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                println("DEBUG: WebRTCService - Fetching TURN credentials...")
                val result = twilioTokenService.getTurnCredentials()
                
                result.onSuccess { credentials ->
                    println("DEBUG: WebRTCService - TURN credentials fetched successfully")
                    updateIceServers(credentials)
                }.onFailure { error ->
                    println("DEBUG: WebRTCService - Failed to fetch TURN credentials: ${error.message}")
                    // Use fallback TURN servers
                    useFallbackTurnServers()
                }
            } catch (e: Exception) {
                println("DEBUG: WebRTCService - Exception fetching TURN credentials: ${e.message}")
                useFallbackTurnServers()
            }
        }
    }
    
    /**
     * Update ICE servers with fetched credentials
     */
    private fun updateIceServers(credentials: TurnCredentials) {
        val newIceServers = mutableListOf<PeerConnection.IceServer>()
        
        // Keep existing STUN
        newIceServers.addAll(iceServers)
        
        // Add TURN servers from credentials
        credentials.iceServers.forEach { server ->
            server.urls.forEach { url ->
                val builder = PeerConnection.IceServer.builder(url)
                server.username?.let { builder.setUsername(it) }
                server.credential?.let { builder.setPassword(it) }
                newIceServers.add(builder.createIceServer())
            }
        }
        
        iceServers = newIceServers
        println("DEBUG: WebRTCService - Updated ICE servers: ${iceServers.size} servers")
        // Apply to active PeerConnection if present
        runCatching {
            peerConnection?.setConfiguration(PeerConnection.RTCConfiguration(iceServers).apply {
                tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED
                bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE
                rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE
                continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
                iceTransportsType = PeerConnection.IceTransportsType.ALL
                keyType = PeerConnection.KeyType.ECDSA
                sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            })
            println("DEBUG: WebRTCService - Applied new ICE configuration to active PeerConnection")
            peerConnection?.restartIce()
            println("DEBUG: WebRTCService - restartIce() after applying Twilio TURN servers")
        }.onFailure { println("DEBUG: WebRTCService - Failed to apply new ICE config: ${it.message}") }
    }
    
    /**
     * Use fallback TURN servers for development
     */
    private fun useFallbackTurnServers() {
        // Do NOT use public TURN; keep only Twilio STUN as fallback
        println("DEBUG: WebRTCService - Using Twilio STUN-only fallback (no public TURN)")
        iceServers = listOf(
            PeerConnection.IceServer.builder("stun:global.stun.twilio.com:3478?transport=udp").createIceServer()
        )
        // If a connection exists, apply and try again
        runCatching { peerConnection?.setConfiguration(PeerConnection.RTCConfiguration(iceServers)) }
    }
}

// Extension functions for WebRTC
private suspend fun PeerConnection.createOffer(constraints: MediaConstraints): SessionDescription? {
    return try {
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            createOffer(object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    continuation.resumeWith(Result.success(sdp))
                }
                override fun onCreateFailure(error: String) {
                    continuation.resumeWith(Result.failure(Exception(error)))
                }
                override fun onSetSuccess() {}
                override fun onSetFailure(error: String) {}
            }, constraints)
        }
    } catch (e: Exception) {
        null
    }
}

private suspend fun PeerConnection.createAnswer(constraints: MediaConstraints): SessionDescription? {
    return try {
        println("DEBUG: WebRTCService - createAnswer() starting with constraints: $constraints")
        kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            createAnswer(object : SdpObserver {
                override fun onCreateSuccess(sdp: SessionDescription) {
                    println("DEBUG: WebRTCService - createAnswer() success: ${sdp.type} length=${sdp.description.length}")
                    continuation.resumeWith(Result.success(sdp))
                }
                override fun onCreateFailure(error: String) {
                    println("DEBUG: WebRTCService - createAnswer() failure: $error")
                    continuation.resumeWith(Result.failure(Exception(error)))
                }
                override fun onSetSuccess() {}
                override fun onSetFailure(error: String) {}
            }, constraints)
        }
    } catch (e: Exception) {
        println("DEBUG: WebRTCService - createAnswer() exception: ${e.message}")
        null
    }
}

private fun PeerConnection.setLocalDescription(sessionDescription: SessionDescription) {
    setLocalDescription(object : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {}
        override fun onCreateFailure(error: String) {}
        override fun onSetSuccess() {}
        override fun onSetFailure(error: String) {}
    }, sessionDescription)
}

private fun PeerConnection.setRemoteDescription(sessionDescription: SessionDescription) {
    setRemoteDescription(object : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription) {}
        override fun onCreateFailure(error: String) {}
        override fun onSetSuccess() {}
        override fun onSetFailure(error: String) {}
    }, sessionDescription)
}

