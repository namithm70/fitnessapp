package com.fitnessss.fitlife.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.fitnessss.fitlife.data.service.AIService
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.SpanStyle

@Composable
fun AIChatBubble(
    aiService: AIService,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var currentMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    
    // Add initial welcome message
    LaunchedEffect(Unit) {
        if (messages.isEmpty()) {
            messages = listOf(
                ChatMessage(
                    text = "Hello! I'm your FitLife AI assistant. I can help with workouts, nutrition, fitness, and health-related questions only. What would you like to know about your fitness journey?",
                    isUser = false,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Box(
        modifier = modifier
    ) {
        // Floating Chat Bubble
        AnimatedVisibility(
            visible = !isExpanded,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .shadow(12.dp, CircleShape)
                    .background(
                        Color.White,
                        CircleShape
                    )
                    .clickable { isExpanded = true }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.SmartToy,
                    contentDescription = "AI Chat",
                    tint = Color.Black,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        // Expanded Chat Window
        AnimatedVisibility(
            visible = isExpanded,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeIn(),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(300)
            ) + fadeOut()
        ) {
            Dialog(
                onDismissRequest = { isExpanded = false },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SmartToy,
                                contentDescription = "AI Assistant",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "FitLife AI Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { isExpanded = false }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        // Messages
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp),
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { message ->
                                ChatMessageItem(message = message)
                            }
                            
                            if (isLoading) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "AI is typing...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Input Section
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = currentMessage,
                                onValueChange = { currentMessage = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusRequester(focusRequester),
                                placeholder = {
                                    Text("Ask about workouts, nutrition, fitness, or health...")
                                },
                                maxLines = 3,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (currentMessage.isNotBlank() && !isLoading) {
                                            val messageToSend = currentMessage
                                            currentMessage = "" // Clear input immediately
                                            scope.launch {
                                                sendMessage(messageToSend, aiService, messages, isLoading) { newMessages, newLoading ->
                                                    messages = newMessages
                                                    isLoading = newLoading
                                                }
                                            }
                                        }
                                    }
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
                                )
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            IconButton(
                                onClick = {
                                    if (currentMessage.isNotBlank() && !isLoading) {
                                        val messageToSend = currentMessage
                                        currentMessage = "" // Clear input immediately
                                        scope.launch {
                                            sendMessage(messageToSend, aiService, messages, isLoading) { newMessages, newLoading ->
                                                messages = newMessages
                                                isLoading = newLoading
                                            }
                                        }
                                    }
                                },
                                enabled = currentMessage.isNotBlank() && !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (currentMessage.isNotBlank() && !isLoading) 
                                        MaterialTheme.colorScheme.primary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Focus the input field when expanded
    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(100)
            focusRequester.requestFocus()
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (message.isUser) 
                    MaterialTheme.colorScheme.primary
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = if (message.isUser) 16.dp else 4.dp,
                topEnd = if (message.isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            if (message.isUser) {
                Text(
                    text = message.text,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = Color.White
                )
            } else {
                Text(
                    text = parseMarkdownText(message.text),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun parseMarkdownText(text: String): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        val textLength = text.length
        
        while (currentIndex < textLength) {
            // Handle bold text (**text**)
            if (currentIndex + 3 < textLength && 
                text.substring(currentIndex, currentIndex + 2) == "**") {
                val endBold = text.indexOf("**", currentIndex + 2)
                if (endBold != -1) {
                    // Add text before bold
                    if (currentIndex > 0) {
                        append(text.substring(0, currentIndex))
                    }
                    // Add bold text
                    withStyle(SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)) {
                        append(text.substring(currentIndex + 2, endBold))
                    }
                    currentIndex = endBold + 2
                    continue
                }
            }
            
            // Handle newlines
            if (currentIndex < textLength && text[currentIndex] == '\n') {
                append('\n')
                currentIndex++
                continue
            }
            
            // Regular text
            if (currentIndex < textLength) {
                append(text[currentIndex])
                currentIndex++
            }
        }
    }
}

private suspend fun sendMessage(
    message: String,
    aiService: AIService,
    currentMessages: List<ChatMessage>,
    currentLoading: Boolean,
    onUpdate: (List<ChatMessage>, Boolean) -> Unit
) {
    // Add user message
    val userMessage = ChatMessage(
        text = message,
        isUser = true,
        timestamp = System.currentTimeMillis()
    )
    val updatedMessages = currentMessages + userMessage
    onUpdate(updatedMessages, true)
    
    try {
        // Get AI response
        val aiResponse = aiService.sendMessage(message)
        
        // Add AI response
        val aiMessage = ChatMessage(
            text = aiResponse,
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        onUpdate(updatedMessages + aiMessage, false)
        
    } catch (e: Exception) {
        // Add error message
        val errorMessage = ChatMessage(
            text = "Sorry, I'm having trouble connecting right now. Please try again later.",
            isUser = false,
            timestamp = System.currentTimeMillis()
        )
        onUpdate(updatedMessages + errorMessage, false)
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long
)
