package com.fitnessss.fitlife.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.gestures.rememberTransformableState
import com.fitnessss.fitlife.data.model.CallType
import androidx.compose.foundation.gestures.transformable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.fitnessss.fitlife.data.model.ChatMessage
import com.fitnessss.fitlife.data.model.ChatRoom
import com.fitnessss.fitlife.data.model.MessageType
import com.fitnessss.fitlife.data.model.toDisplayTime
import com.fitnessss.fitlife.data.model.toChatTime
import com.fitnessss.fitlife.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val chatRooms by viewModel.chatRooms.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    var isSearchMode by remember { mutableStateOf(false) }
    
    LaunchedEffect(error) {
        error?.let {
            // Handle error - could show a snackbar
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            ChatListTopBar(
                isSearchMode = isSearchMode,
                searchQuery = searchQuery,
                onSearchModeChange = { isSearchMode = it },
                onSearchQueryChange = { 
                    searchQuery = it
                    if (it.isNotEmpty()) {
                        viewModel.searchUsers(it)
                    }
                },
                onClearSearch = {
                    searchQuery = ""
                    isSearchMode = false
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isSearchMode) {
                // Search results view
                if (searchQuery.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Search for users to start a chat",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (searchResults.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No users found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try searching with a different name",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { userPair ->
                            UserSearchResultItem(
                                userId = userPair.first,
                                userName = userPair.second,
                                onClick = {
                                    val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown User"
                                    viewModel.createOrGetChatRoom(
                                        otherUserId = userPair.first,
                                        otherUserName = userPair.second,
                                        currentUserName = currentUserName
                                    ) { chatRoom ->
                                        searchQuery = ""
                                        isSearchMode = false
                                        navController.navigate(Screen.ChatDetail.createRoute(chatRoom.id))
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                // Regular chat list view
                if (isLoading && chatRooms.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (chatRooms.isEmpty()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No chats yet",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start a new conversation",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { isSearchMode = true }) {
                            Text("Start Chat")
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(chatRooms) { chatRoom ->
                            ChatRoomItem(
                                chatRoom = chatRoom,
                                onClick = {
                                    navController.navigate(Screen.ChatDetail.createRoute(chatRoom.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListTopBar(
    isSearchMode: Boolean,
    searchQuery: String,
    onSearchModeChange: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit
) {
    if (isSearchMode) {
        // Search mode top bar
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { 
                        Text(
                            text = "Search users...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = onClearSearch) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )
    } else {
        // Normal mode top bar
        TopAppBar(
            title = { Text("Chats", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { onSearchModeChange(true) }) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )
    }
}

@Composable
fun UserSearchResultItem(
    userId: String,
    userName: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // User info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = userName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap to start chatting",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Start chat icon
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Start Chat",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ChatRoomItem(
    chatRoom: ChatRoom,
    onClick: () -> Unit
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown User"
    val unreadCount = chatRoom.unreadCount[currentUserId] ?: 0
    
    // Determine the correct name to display (other participant's name)
    val displayName = remember(chatRoom.participants, currentUserId, currentUserName) {
        // If the chat room name is the current user's name, we need to find the other participant's name
        if (chatRoom.name == currentUserName) {
            // The chat room was created from the other user's perspective
            // We need to find the other participant's name
            val otherParticipant = chatRoom.participants.find { it != currentUserId }
            if (otherParticipant != null) {
                // For now, we'll use a placeholder, but ideally we'd fetch from users collection
                "User ${otherParticipant.takeLast(4)}" // Show last 4 chars of ID as fallback
            } else {
                chatRoom.name
            }
        } else {
            // The chat room name is correct (other participant's name)
            chatRoom.name
        }
    }
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (chatRoom.lastMessageContent.isNotEmpty()) {
                    Text(
                        text = "${chatRoom.lastMessageSender}: ${chatRoom.lastMessageContent}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                chatRoom.lastMessageTimestamp?.let { timestamp ->
                    Text(
                        text = timestamp.toDisplayTime(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge {
                        Text(unreadCount.toString())
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatDialog(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<Pair<String, String>>,
    onUserSelected: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Chat") },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search users") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (searchResults.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(searchResults) { (userId, userName) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUserSelected(userId, userName) }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(userName)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    navController: NavController,
    chatRoomId: String,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var messageText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    LaunchedEffect(chatRoomId) {
        viewModel.loadMessages(chatRoomId)
    }
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    
    LaunchedEffect(error) {
        error?.let {
            // Handle error
            viewModel.clearError()
        }
    }

    // Navigate to outgoing call when session is created
    val outgoingCallSession by viewModel.outgoingCallSession.collectAsStateWithLifecycle()
    LaunchedEffect(outgoingCallSession?.id) {
        val session = outgoingCallSession
        if (session != null) {
            val isVideoCall = session.callType == CallType.VIDEO.name
            if (isVideoCall) {
                navController.navigate(
                    Screen.VideoOutgoingCall.createRoute(
                        session.id,
                        session.receiverName
                    )
                )
            } else {
                navController.navigate(
                    Screen.OutgoingCall.createRoute(
                        session.id,
                        session.receiverName
                    )
                )
            }
            viewModel.clearOutgoingCallNavigation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Runtime audio permission request launcher
                    val audioPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        println("DEBUG: Permission result received: $permissions")
                        val granted = permissions[android.Manifest.permission.RECORD_AUDIO] == true
                        println("DEBUG: RECORD_AUDIO granted: $granted")
                        if (granted) {
                            println("DEBUG: Calling viewModel.initiateAudioCall($chatRoomId)")
                            viewModel.initiateAudioCall(chatRoomId)
                        } else {
                            println("DEBUG: Permission denied, showing toast")
                            android.widget.Toast.makeText(context, "Microphone permission is required", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    fun requestOrStartCall() {
                        println("DEBUG: Call button clicked! chatRoomId: $chatRoomId")
                        val recordGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        println("DEBUG: RECORD_AUDIO permission already granted: $recordGranted")
                        if (recordGranted) {
                            println("DEBUG: Permission already granted, calling viewModel.initiateAudioCall($chatRoomId)")
                            viewModel.initiateAudioCall(chatRoomId)
                        } else {
                            println("DEBUG: Permission not granted, launching permission request")
                            audioPermissionLauncher.launch(arrayOf(
                                android.Manifest.permission.RECORD_AUDIO
                            ))
                        }
                    }
                    IconButton(onClick = { 
                        // Initiate audio call with runtime permission request
                        requestOrStartCall()
                    }) {
                        Icon(Icons.Filled.Call, contentDescription = "Audio Call")
                    }
                    // Runtime camera and audio permission request launcher for video calls
                    val videoPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        println("DEBUG: Video permission result received: $permissions")
                        val audioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] == true
                        val cameraGranted = permissions[android.Manifest.permission.CAMERA] == true
                        println("DEBUG: RECORD_AUDIO granted: $audioGranted, CAMERA granted: $cameraGranted")
                        if (audioGranted && cameraGranted) {
                            println("DEBUG: Calling viewModel.initiateVideoCall($chatRoomId)")
                            viewModel.initiateVideoCall(chatRoomId)
                        } else {
                            println("DEBUG: Permissions denied, showing toast")
                            android.widget.Toast.makeText(context, "Camera and microphone permissions are required for video calls", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    fun requestOrStartVideoCall() {
                        println("DEBUG: Video call button clicked! chatRoomId: $chatRoomId")
                        val audioGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.RECORD_AUDIO
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        val cameraGranted = androidx.core.content.ContextCompat.checkSelfPermission(
                            context, android.Manifest.permission.CAMERA
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        println("DEBUG: RECORD_AUDIO permission already granted: $audioGranted, CAMERA permission already granted: $cameraGranted")
                        if (audioGranted && cameraGranted) {
                            println("DEBUG: Permissions already granted, calling viewModel.initiateVideoCall($chatRoomId)")
                            viewModel.initiateVideoCall(chatRoomId)
                        } else {
                            println("DEBUG: Permissions not granted, launching permission request")
                            videoPermissionLauncher.launch(arrayOf(
                                android.Manifest.permission.RECORD_AUDIO,
                                android.Manifest.permission.CAMERA
                            ))
                        }
                    }
                    IconButton(onClick = { 
                        // Initiate video call with runtime permission request
                        requestOrStartVideoCall()
                    }) {
                        Icon(Icons.Filled.VideoCall, contentDescription = "Video Call")
                    }
                }
            )
        },
        bottomBar = {
            MessageInputBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(chatRoomId, messageText.trim())
                        messageText = ""
                        keyboardController?.hide()
                    }
                },
                onSendAudio = { bytes, duration ->
                    viewModel.sendAudioMessage(chatRoomId, bytes, duration)
                    messageText = ""
                    keyboardController?.hide()
                },
                onSendImage = { bytes ->
                    viewModel.sendImageMessage(chatRoomId, bytes)
                    messageText = ""
                    keyboardController?.hide()
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(messages) { message ->
                MessageBubble(
                    message = message,
                    onDeleteForMe = { messageId ->
                        viewModel.deleteMessageForMe(chatRoomId, messageId)
                    },
                    onDeleteForEveryone = { messageId ->
                        viewModel.deleteMessageForEveryone(chatRoomId, messageId)
                    },
                    onCopyMessage = { content ->
                        clipboardManager.setText(AnnotatedString(content))
                        // Show a toast
                        android.widget.Toast.makeText(context, "Message copied", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onReplyToMessage = { message ->
                        replyingToMessage = message
                    }
                )
            }
        }
        
        // Reply preview bar
        if (replyingToMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Replying to ${replyingToMessage?.senderName}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = replyingToMessage?.content ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { replyingToMessage = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel reply"
                        )
                    }
                }
            }
        }
        
        // Message input bar
        MessageInputBar(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendMessage = {
                if (messageText.isNotBlank()) {
                    val currentReply = replyingToMessage
                    viewModel.sendMessage(
                        chatRoomId = chatRoomId,
                        content = messageText,
                        replyToMessageId = currentReply?.id,
                        replyToContent = currentReply?.content,
                        replyToSenderName = currentReply?.senderName
                    )
                    messageText = ""
                    replyingToMessage = null
                    keyboardController?.hide()
                }
            },
            onSendAudio = { bytes, duration ->
                viewModel.sendAudioMessage(chatRoomId, bytes, duration)
            },
            onSendImage = { bytes ->
                viewModel.sendImageMessage(chatRoomId, bytes)
            },
            replyingTo = replyingToMessage?.let { "Replying to ${it.senderName}" }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    onDeleteForMe: (String) -> Unit = {},
    onDeleteForEveryone: (String) -> Unit = {},
    onCopyMessage: (String) -> Unit = {},
    onReplyToMessage: (ChatMessage) -> Unit = {}
) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isOwnMessage = message.senderId == currentUserId
    var showDeleteMenu by remember { mutableStateOf(false) }
    
    // Skip messages hidden for current user (Delete for me)
    if (message.hiddenForUserIds.contains(currentUserId)) {
        return
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp),
        horizontalArrangement = if (isOwnMessage) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = { showDeleteMenu = true }
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (isOwnMessage) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isOwnMessage) 16.dp else 4.dp,
                bottomEnd = if (isOwnMessage) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Show reply context if this is a reply
                if (message.replyToMessageId != null && !message.replyToContent.isNullOrBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOwnMessage) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text(
                                text = "Replying to ${message.replyToSenderName ?: "Unknown"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = if (isOwnMessage) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                }
                            )
                            Text(
                                text = message.replyToContent ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isOwnMessage) {
                                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                }
                            )
                        }
                    }
                }
                
                if (!isOwnMessage) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                
                if (message.messageType == MessageType.AUDIO.name && message.audioUrl != null) {
                    AudioMessageBubble(
                        audioUrl = message.audioUrl,
                        durationMs = message.audioDurationMs ?: 0L,
                        isOwn = isOwnMessage
                    )
                } else if (message.messageType == MessageType.IMAGE.name && message.imageUrl != null) {
                    // Render base64 image data URL
                    val dataUrl = message.imageUrl
                    val isData = dataUrl.startsWith("data:image/") && dataUrl.contains("base64,")
                    val showImageDialog = remember { mutableStateOf(false) }
                    if (isData) {
                        val base64Part = dataUrl.substringAfter("base64,", "")
                        val bytes = try { android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT) } catch (e: Exception) { null }
                        if (bytes != null) {
                            val bmp = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bmp != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Image message",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { showImageDialog.value = true }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (showImageDialog.value) {
                                    androidx.compose.ui.window.Dialog(
                                        onDismissRequest = { showImageDialog.value = false },
                                        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.9f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            var scale by remember { mutableStateOf(1f) }
                                            var offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
                                            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                                                scale = (scale * zoomChange).coerceIn(1f, 4f)
                                                offset += panChange
                                            }
                                            androidx.compose.foundation.Image(
                                                bitmap = bmp.asImageBitmap(),
                                                contentDescription = "Image preview",
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .graphicsLayer(
                                                        scaleX = scale,
                                                        scaleY = scale,
                                                        translationX = offset.x,
                                                        translationY = offset.y
                                                    )
                                                    .transformable(transformState)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Text(text = "[Image]", style = MaterialTheme.typography.bodyMedium)
                            }
                        } else {
                            Text(text = "[Image]", style = MaterialTheme.typography.bodyMedium)
                        }
                    } else {
                        Text(text = "[Image]", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    Text(
                        text = if (message.deletedForEveryone) "This message was deleted" else message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isOwnMessage) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = message.toLocalDateTime().toChatTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isOwnMessage) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
        }
        
        // Message options dropdown menu
        DropdownMenu(
            expanded = showDeleteMenu,
            onDismissRequest = { showDeleteMenu = false }
        ) {
            if (!message.deletedForEveryone) {
                DropdownMenuItem(
                    text = { Text("Copy message") },
                    onClick = {
                        onCopyMessage(message.content)
                        showDeleteMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Reply") },
                    onClick = {
                        onReplyToMessage(message)
                        showDeleteMenu = false
                    }
                )
                HorizontalDivider()
            }
            DropdownMenuItem(
                text = { Text("Delete for me") },
                onClick = {
                    onDeleteForMe(message.id)
                    showDeleteMenu = false
                }
            )
            if (isOwnMessage && !message.deletedForEveryone) {
                DropdownMenuItem(
                    text = { Text("Delete for everyone") },
                    onClick = {
                        onDeleteForEveryone(message.id)
                        showDeleteMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun AudioMessageBubble(audioUrl: String, durationMs: Long, isOwn: Boolean) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val context = LocalContext.current
    val mediaPlayer = remember { android.media.MediaPlayer() }
    val durationSec = (durationMs / 1000).toInt().coerceAtLeast(1)
    
    // Handle base64 data URLs
    val canPlay = remember(audioUrl) { 
        audioUrl.startsWith("data:audio/") && audioUrl.contains("base64,")
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        IconButton(
            onClick = { 
                if (canPlay) {
                    if (isPlaying) {
                        // Pause playback
                        try {
                            mediaPlayer.pause()
                            isPlaying = false
                        } catch (e: Exception) {
                            println("DEBUG: Error pausing audio: ${e.message}")
                        }
                    } else {
                        // Start playback
                        try {
                            if (audioUrl.startsWith("data:audio/m4a;base64,")) {
                                // Decode base64 and play
                                val base64Data = audioUrl.substring("data:audio/m4a;base64,".length)
                                val audioBytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                                
                                // Create temporary file for playback
                                val tempFile = java.io.File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.m4a")
                                tempFile.writeBytes(audioBytes)
                                
                                mediaPlayer.reset()
                                mediaPlayer.setDataSource(tempFile.absolutePath)
                                mediaPlayer.prepareAsync()
                                mediaPlayer.setOnPreparedListener {
                                    mediaPlayer.start()
                                    isPlaying = true
                                }
                                mediaPlayer.setOnCompletionListener {
                                    isPlaying = false
                                    progress = 0f
                                    tempFile.delete() // Clean up temp file
                                }
                                mediaPlayer.setOnErrorListener { _, what, extra ->
                                    println("DEBUG: MediaPlayer error: what=$what, extra=$extra")
                                    isPlaying = false
                                    tempFile.delete()
                                    true
                                }
                            }
                        } catch (e: Exception) {
                            println("DEBUG: Error playing audio: ${e.message}")
                            isPlaying = false
                        }
                    }
                }
            },
            enabled = canPlay
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isOwn) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    if (canPlay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            color = if (isOwn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "${durationSec}s",
            style = MaterialTheme.typography.bodySmall,
            color = if (isOwn) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    // Update progress while playing
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            try {
                if (mediaPlayer.isPlaying) {
                    val current = mediaPlayer.currentPosition.toFloat()
                    val total = mediaPlayer.duration.toFloat()
                    progress = if (total > 0) current / total else 0f
                }
            } catch (e: Exception) {
                // MediaPlayer might not be ready yet
            }
            kotlinx.coroutines.delay(100)
        }
    }
    
    // Cleanup when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            try {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
                mediaPlayer.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendAudio: (ByteArray, Long) -> Unit,
    onSendImage: (ByteArray) -> Unit,
    replyingTo: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            // Image picker state
            var selectedImageBytes by rememberSaveable { mutableStateOf<ByteArray?>(null) }
            var selectedImagePreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
            val imagePicker = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri ->
                if (uri != null) {
                    try {
                        val input = context.contentResolver.openInputStream(uri)
                        val bytesRead = input?.readBytes()
                        input?.close()
                        if (bytesRead != null) {
                            selectedImageBytes = bytesRead
                            selectedImagePreview = android.graphics.BitmapFactory.decodeByteArray(bytesRead, 0, bytesRead.size)
                        }
                    } catch (e: Exception) {
                        println("DEBUG: UI - Error reading selected image: ${e.message}")
                    }
                }
            }

            // Audio record controls
            var isRecording by rememberSaveable { mutableStateOf(false) }
            val haptic = LocalHapticFeedback.current
            val cacheDir = context.cacheDir
            val recorder = remember { AudioRecorder() }
            var recordedFilePath by rememberSaveable { mutableStateOf<String?>(null) }
            var recordedDuration by rememberSaveable { mutableStateOf(0L) }
            var recordingStart by rememberSaveable { mutableStateOf(0L) }

            // Ticker for timer
            val tick by produceState(0L, isRecording) {
                while (isRecording) {
                    value = System.currentTimeMillis()
                    kotlinx.coroutines.delay(200)
                }
            }

            // Mic / Stop button
            IconButton(
                onClick = {
                if (!isRecording) {
                    val file = recorder.start(cacheDir)
                    recordedFilePath = file.absolutePath
                    recordedDuration = 0L
                    recordingStart = System.currentTimeMillis()
                    isRecording = true
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                } else {
                    val (file, duration) = recorder.stop()
                    recordedFilePath = file?.absolutePath
                    recordedDuration = duration
                    isRecording = false
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            },
                modifier = Modifier.size(40.dp)
            ) {
                val icon = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic
                Icon(icon, contentDescription = if (isRecording) "Stop" else "Record", modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isRecording) {
                // Animated waveform + timer, hide the text input while recording
                val infinite = rememberInfiniteTransition(label = "record-wave")
                val a1 by infinite.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "a1"
                )
                val a2 by infinite.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "a2"
                )
                val a3 by infinite.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "a3"
                )

                val secs = (((tick - recordingStart).coerceAtLeast(0L)) / 1000).toInt()
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Waveform bars
                    val base = 28.dp
                    @Composable
                    fun Bar(scale: Float) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(6.dp)
                                .height(base * scale)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                    Bar(a1)
                    Bar(a2)
                    Bar(a3)
                    Bar(a2)
                    Bar(a1)

                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Recording… ${secs}s",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // While recording, only show a disabled send button placeholder
                IconButton(onClick = { /* no-op during recording */ }, enabled = false) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (recordedFilePath != null) {
                // Audio preview (when we have a recorded file ready to send)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Mic,
                            contentDescription = "Audio message",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Audio Message",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(recordedDuration / 1000)}s",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        // Delete recorded audio button
                        IconButton(
                            onClick = {
                                recordedFilePath?.let { path ->
                                    java.io.File(path).delete()
                                }
                                recordedFilePath = null
                                recordedDuration = 0L
                            }
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete audio",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Send audio button
                IconButton(
                    onClick = {
                        recordedFilePath?.let { path ->
                            val file = java.io.File(path)
                            if (file.exists()) {
                                try {
                                    val bytes = file.readBytes()
                                    println("DEBUG: UI - Sending audio file: ${file.absolutePath}, size: ${bytes.size}")
                                    onSendAudio(bytes, recordedDuration)
                                    // Only clear state after successful send
                                    recordedFilePath = null
                                    recordedDuration = 0L
                                    file.delete() // Clean up the temp file after successful send
                                } catch (e: Exception) {
                                    println("DEBUG: UI - Error reading audio file: ${e.message}")
                                }
                            } else {
                                println("DEBUG: UI - Audio file does not exist: $path")
                                // Clear invalid state
                                recordedFilePath = null
                                recordedDuration = 0L
                            }
                        }
                    }
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send audio",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else if (selectedImageBytes != null && selectedImagePreview != null) {
                // Image preview UI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = selectedImagePreview!!.asImageBitmap(),
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Image ready",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            selectedImageBytes = null
                            selectedImagePreview = null
                        }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Remove image", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = {
                    selectedImageBytes?.let { bytes ->
                        onSendImage(bytes)
                        selectedImageBytes = null
                        selectedImagePreview = null
                    }
                }) {
                    Icon(Icons.Filled.Send, contentDescription = "Send image", tint = MaterialTheme.colorScheme.primary)
                }
            } else {
                // Text input (visible when no recording and no recorded file)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 0.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .heightIn(min = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = messageText,
                            onValueChange = onMessageTextChange,
                            modifier = Modifier
                                .weight(1f),
                            placeholder = { Text("Type a message…") },
                            maxLines = 4,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = MaterialTheme.colorScheme.primary
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = { onSendMessage() }
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { imagePicker.launch("image/*") },
                            colors = IconButtonDefaults.filledTonalIconButtonColors(),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Filled.Image, contentDescription = "Attach image", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onSendMessage,
                    enabled = messageText.isNotBlank(),
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}
