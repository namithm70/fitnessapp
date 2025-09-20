package com.fitnessss.fitlife.ui.screens.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fitnessss.fitlife.data.model.ChatMessage
import com.fitnessss.fitlife.data.model.ChatRoom
import com.fitnessss.fitlife.data.model.MessageType
import com.fitnessss.fitlife.data.model.CallSession
import com.fitnessss.fitlife.data.repository.ChatRepository
import com.fitnessss.fitlife.data.repository.CallRepository
import com.fitnessss.fitlife.data.model.CallType
import com.fitnessss.fitlife.data.service.WebRTCService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val callRepository: CallRepository,
    private val webRTCService: WebRTCService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _searchResults = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val searchResults: StateFlow<List<Pair<String, String>>> = _searchResults.asStateFlow()

    private val _outgoingCallSession = MutableStateFlow<CallSession?>(null)
    val outgoingCallSession: StateFlow<CallSession?> = _outgoingCallSession.asStateFlow()
    
    init {
        initializeUser()
        loadChatRooms()
    }
    
    private fun initializeUser() {
        viewModelScope.launch {
            chatRepository.initializeUser()
            chatRepository.updateOnlineStatus(true)
        }
    }
    
    private fun loadChatRooms() {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.getChatRooms()
                .catch { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
                .collect { rooms ->
                    _chatRooms.value = rooms
                    _isLoading.value = false
                }
        }
    }
    
    fun loadMessages(chatRoomId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(chatRoomId)
                .catch { exception ->
                    _error.value = exception.message
                }
                .collect { messagesList ->
                    _messages.value = messagesList
                }
        }
    }
    
    fun sendMessage(
        chatRoomId: String, 
        content: String,
        replyToMessageId: String? = null,
        replyToContent: String? = null,
        replyToSenderName: String? = null
    ) {
        if (content.isBlank()) return
        
        viewModelScope.launch {
            chatRepository.sendMessage(
                chatRoomId = chatRoomId,
                content = content,
                messageType = MessageType.TEXT,
                replyToMessageId = replyToMessageId,
                replyToContent = replyToContent,
                replyToSenderName = replyToSenderName
            )
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }

    fun sendAudioMessage(chatRoomId: String, bytes: ByteArray, durationMs: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.sendAudioMessage(chatRoomId, bytes, durationMs)
                .onSuccess { _isLoading.value = false }
                .onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
        }
    }
    
    fun sendImageMessage(chatRoomId: String, imageBytes: ByteArray) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.sendImageMessage(chatRoomId, imageBytes)
                .onSuccess { _isLoading.value = false }
                .onFailure { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
        }
    }
    
    fun createOrGetChatRoom(
        otherUserId: String,
        otherUserName: String,
        currentUserName: String,
        onSuccess: (ChatRoom) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.createOrGetChatRoom(otherUserId, otherUserName, currentUserName)
                .onSuccess { chatRoom ->
                    _isLoading.value = false
                    onSuccess(chatRoom)
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
        }
    }
    
    fun searchUsers(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            chatRepository.searchUsers(query)
                .onSuccess { users ->
                    _searchResults.value = users
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun hasAudioPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun hasCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun initiateAudioCall(chatRoomId: String) {
        println("DEBUG: ChatViewModel.initiateAudioCall() called with chatRoomId: $chatRoomId")
        viewModelScope.launch {
            // Check audio permissions first
            val hasPerms = hasAudioPermissions()
            println("DEBUG: ChatViewModel - hasAudioPermissions(): $hasPerms")
            if (!hasPerms) {
                println("DEBUG: ChatViewModel - Audio permissions not granted")
                _error.value = "Audio recording permission is required for voice calls"
                return@launch
            }
            
            // Get the other user from the chat room
            val chatRoom = _chatRooms.value.find { it.id == chatRoomId }
            println("DEBUG: ChatViewModel - Found chatRoom: ${chatRoom?.name} with ${chatRoom?.participants?.size} participants")
            if (chatRoom != null) {
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val otherUser = chatRoom.participants.find { it != currentUserId }
                
                if (otherUser != null) {
                    // Get other user name from participants (this is simplified)
                    val otherUserName = chatRoom.name.ifBlank { "User" } // TODO: Resolve actual name from users collection
                    println("DEBUG: ChatViewModel - otherUser: $otherUser, otherUserName: $otherUserName")
                    
                    callRepository.initiateCall(
                        receiverId = otherUser,
                        receiverName = otherUserName,
                        chatRoomId = chatRoomId,
                        callType = CallType.AUDIO
                    ).onSuccess { callSession ->
                        println("DEBUG: ChatViewModel - initiateCall repository success, setting up WebRTC")
                        
                        // Initialize WebRTC and create offer
                        if (webRTCService.createPeerConnection()) {
                            println("DEBUG: ChatViewModel - createPeerConnection success, creating offer")
                            launch {
                                val offer = webRTCService.createOffer()
                                println("DEBUG: ChatViewModel - offer created: type=${offer?.type} length=${offer?.description?.length}")
                                offer?.let { sdp ->
                                    callRepository.updateSignalingData(
                                        callSessionId = callSession.id,
                                        callerSdp = "${sdp.type}:${sdp.description}"
                                    )
                                    callRepository.updateCallStatus(callSession.id, com.fitnessss.fitlife.data.model.CallStatus.RINGING)
                                }
                            }
                        } else {
                            println("DEBUG: ChatViewModel - createPeerConnection failed")
                        }
                        
                        // Expose session for the UI to navigate
                        _outgoingCallSession.value = callSession
                    }.onFailure { exception ->
                        println("DEBUG: ChatViewModel - initiateCall failure: ${exception.message}")
                        _error.value = exception.message
                    }
                } else {
                    println("DEBUG: ChatViewModel - otherUser is null")
                }
            } else {
                println("DEBUG: ChatViewModel - chatRoom not found for id: $chatRoomId")
            }
        }
    }

    fun initiateVideoCall(chatRoomId: String) {
        println("DEBUG: ChatViewModel.initiateVideoCall() called with chatRoomId: $chatRoomId")
        viewModelScope.launch {
            // Check camera and audio permissions first
            val hasAudioPerms = hasAudioPermissions()
            val hasCameraPerms = hasCameraPermissions()
            println("DEBUG: ChatViewModel - hasAudioPermissions(): $hasAudioPerms, hasCameraPermissions(): $hasCameraPerms")
            if (!hasAudioPerms || !hasCameraPerms) {
                println("DEBUG: ChatViewModel - Permissions not granted")
                _error.value = "Camera and microphone permissions are required for video calls"
                return@launch
            }
            
            // Get the other user from the chat room
            val chatRoom = _chatRooms.value.find { it.id == chatRoomId }
            println("DEBUG: ChatViewModel - Found chatRoom: ${chatRoom?.name} with ${chatRoom?.participants?.size} participants")
            if (chatRoom != null) {
                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                val otherUser = chatRoom.participants.find { it != currentUserId }
                
                if (otherUser != null) {
                    // Get other user name from participants (this is simplified)
                    val otherUserName = chatRoom.name.ifBlank { "User" } // TODO: Resolve actual name from users collection
                    println("DEBUG: ChatViewModel - otherUser: $otherUser, otherUserName: $otherUserName")
                    
                    callRepository.initiateCall(
                        receiverId = otherUser,
                        receiverName = otherUserName,
                        chatRoomId = chatRoomId,
                        callType = CallType.VIDEO
                    ).onSuccess { callSession ->
                        println("DEBUG: ChatViewModel - initiateCall repository success, setting up WebRTC")
                        
                        // Initialize WebRTC and create offer
                        if (webRTCService.createPeerConnection(true)) {
                            println("DEBUG: ChatViewModel - createPeerConnection success, creating offer")
                            launch {
                                val offer = webRTCService.createOffer()
                                println("DEBUG: ChatViewModel - offer created: type=${offer?.type} length=${offer?.description?.length}")
                                offer?.let { sdp ->
                                    callRepository.updateSignalingData(
                                        callSessionId = callSession.id,
                                        callerSdp = "${sdp.type}:${sdp.description}"
                                    )
                                    callRepository.updateCallStatus(callSession.id, com.fitnessss.fitlife.data.model.CallStatus.RINGING)
                                }
                            }
                        } else {
                            println("DEBUG: ChatViewModel - createPeerConnection failed")
                        }
                        
                        // Expose session for the UI to navigate
                        _outgoingCallSession.value = callSession
                    }.onFailure { exception ->
                        println("DEBUG: ChatViewModel - initiateCall failure: ${exception.message}")
                        _error.value = exception.message
                    }
                } else {
                    println("DEBUG: ChatViewModel - otherUser is null")
                }
            } else {
                println("DEBUG: ChatViewModel - chatRoom not found for id: $chatRoomId")
            }
        }
    }
    
    fun clearOutgoingCallNavigation() {
        _outgoingCallSession.value = null
    }
    
    fun deleteMessageForMe(chatRoomId: String, messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.deleteMessageForMe(chatRoomId, messageId)
                .onSuccess {
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
        }
    }
    
    fun deleteMessageForEveryone(chatRoomId: String, messageId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            chatRepository.deleteMessageForEveryone(chatRoomId, messageId)
                .onSuccess {
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    _isLoading.value = false
                }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            chatRepository.updateOnlineStatus(false)
        }
    }
}
