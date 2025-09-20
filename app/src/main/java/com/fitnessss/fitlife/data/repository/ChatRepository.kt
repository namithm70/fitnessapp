package com.fitnessss.fitlife.data.repository

import com.fitnessss.fitlife.data.model.ChatMessage
import com.fitnessss.fitlife.data.model.ChatRoom
import com.fitnessss.fitlife.data.model.CallSession
import com.fitnessss.fitlife.data.model.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    private val chatRoomsCollection = firestore.collection("chatRooms")
    private val usersCollection = firestore.collection("users")
    
    // Get current user ID
    private fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    // Create or get chat room between two users
    suspend fun createOrGetChatRoom(
        otherUserId: String,
        otherUserName: String,
        currentUserName: String
    ): Result<ChatRoom> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            // Create consistent room ID based on user IDs
            val roomId = if (currentUserId < otherUserId) {
                "${currentUserId}_${otherUserId}"
            } else {
                "${otherUserId}_${currentUserId}"
            }
            
            val roomDoc = chatRoomsCollection.document(roomId)
            val existingRoom = roomDoc.get().await()
            
            if (existingRoom.exists()) {
                val chatRoom = existingRoom.toObject(ChatRoom::class.java)!!
                Result.success(chatRoom)
            } else {
                // Create new chat room
                val newRoom = ChatRoom(
                    id = roomId,
                    name = otherUserName,
                    participants = listOf(currentUserId, otherUserId),
                    avatarUrl = null,
                    createdAt = com.google.firebase.Timestamp.now(),
                    updatedAt = com.google.firebase.Timestamp.now()
                )
                
                roomDoc.set(newRoom).await()
                Result.success(newRoom)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Upload audio and send as message
    suspend fun sendAudioMessage(
        chatRoomId: String,
        localFileBytes: ByteArray,
        durationMs: Long
    ): Result<ChatMessage> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val currentUserId = currentUser.uid
            val currentUserName = currentUser.displayName ?: "Unknown User"

            // Ensure participant
            val chatRoomDoc = chatRoomsCollection.document(chatRoomId).get().await()
            val chatRoom = chatRoomDoc.toObject(ChatRoom::class.java)
            if (chatRoom == null || currentUserId !in chatRoom.participants) {
                return Result.failure(Exception("Access denied: User is not a participant in this chat"))
            }

            // Create a temporary approach - save audio as base64 in Firestore directly
            // This bypasses Firebase Storage issues for now
            val audioBase64 = android.util.Base64.encodeToString(localFileBytes, android.util.Base64.DEFAULT)
            
            println("DEBUG: ChatRepository - Saving audio as base64 (size=${localFileBytes.size} bytes, base64 length=${audioBase64.length})")
            
            // For small audio files, we'll embed them directly in Firestore
            // For production, you'd want to use Storage, but this works around the 404 issue
            val downloadUrl = "data:audio/m4a;base64,$audioBase64"

            val message = ChatMessage(
                senderId = currentUserId,
                senderName = currentUserName,
                content = "", // no text
                messageType = MessageType.AUDIO.name,
                audioUrl = downloadUrl,
                audioDurationMs = durationMs
            )

            val messageRef = chatRoomsCollection
                .document(chatRoomId)
                .collection("messages")
                .document()

            val messageWithId = message.copy(id = messageRef.id)
            messageRef.set(messageWithId).await()
            updateChatRoomLastMessage(chatRoomId, messageWithId)
            Result.success(messageWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Compress image to 50–200 KB and send as message
    suspend fun sendImageMessage(
        chatRoomId: String,
        originalBytes: ByteArray
    ): Result<ChatMessage> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val currentUserId = currentUser.uid
            val currentUserName = currentUser.displayName ?: "Unknown User"

            // Ensure participant
            val chatRoomDoc = chatRoomsCollection.document(chatRoomId).get().await()
            val chatRoom = chatRoomDoc.toObject(ChatRoom::class.java)
            if (chatRoom == null || currentUserId !in chatRoom.participants) {
                return Result.failure(Exception("Access denied: User is not a participant in this chat"))
            }

            // Decode to Bitmap
            val bitmap = android.graphics.BitmapFactory.decodeByteArray(originalBytes, 0, originalBytes.size)
                ?: return Result.failure(Exception("Invalid image data"))

            // Target between 50 KB and 200 KB
            val minTarget = 50 * 1024
            val maxTarget = 200 * 1024

            // Try quality compression first (JPEG)
            var quality = 85
            var compressed: ByteArray
            fun compress(q: Int): ByteArray {
                val bos = java.io.ByteArrayOutputStream()
                bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, q.coerceIn(40, 100), bos)
                return bos.toByteArray()
            }

            compressed = compress(quality)
            // If too big, reduce quality progressively
            while (compressed.size > maxTarget && quality > 40) {
                quality -= 10
                compressed = compress(quality)
            }

            // If still too big, resize dimensions (downscale by 0.8 until within range or small)
            if (compressed.size > maxTarget) {
                var scaledBitmap = bitmap
                var scale = 1.0f
                while (compressed.size > maxTarget && scaledBitmap.width > 320 && scaledBitmap.height > 320) {
                    scale *= 0.8f
                    val newW = (bitmap.width * scale).toInt().coerceAtLeast(320)
                    val newH = (bitmap.height * scale).toInt().coerceAtLeast(320)
                    scaledBitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newW, newH, true)
                    val bos = java.io.ByteArrayOutputStream()
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, bos)
                    compressed = bos.toByteArray()
                }
                if (scaledBitmap != bitmap) {
                    scaledBitmap.recycle()
                }
            }

            // If compressed goes below min target and looks too small, we accept it; goal is <= 200KB
            println("DEBUG: ChatRepository - Image compressed to ${compressed.size} bytes (quality=$quality)")

            // As with audio, avoid Storage for now; embed as data URL (JPEG)
            val base64 = android.util.Base64.encodeToString(compressed, android.util.Base64.NO_WRAP)
            val dataUrl = "data:image/jpeg;base64,$base64"

            val message = ChatMessage(
                senderId = currentUserId,
                senderName = currentUserName,
                content = "",
                messageType = MessageType.IMAGE.name,
                imageUrl = dataUrl
            )

            val messageRef = chatRoomsCollection
                .document(chatRoomId)
                .collection("messages")
                .document()

            val messageWithId = message.copy(id = messageRef.id)
            messageRef.set(messageWithId).await()
            updateChatRoomLastMessage(chatRoomId, messageWithId)
            Result.success(messageWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get all chat rooms for current user
    fun getChatRooms(): Flow<List<ChatRoom>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val listener = chatRoomsCollection
            .whereArrayContains("participants", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val chatRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatRoom::class.java)
                } ?: emptyList()
                
                trySend(chatRooms)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Get messages for a specific chat room
    fun getMessages(chatRoomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        println("DEBUG: ChatRepository - getMessages for chatRoomId: $chatRoomId, currentUserId: $currentUserId")
        
        val listener = chatRoomsCollection
            .document(chatRoomId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("DEBUG: ChatRepository - getMessages error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)
                } ?: emptyList()
                
                println("DEBUG: ChatRepository - getMessages retrieved ${messages.size} messages")
                trySend(messages)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Send a message
    suspend fun sendMessage(
        chatRoomId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT,
        replyToMessageId: String? = null,
        replyToContent: String? = null,
        replyToSenderName: String? = null
    ): Result<ChatMessage> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val currentUserId = currentUser.uid
            val currentUserName = currentUser.displayName ?: "Unknown User"
            
            // Verify that the current user is a participant in this chat room
            val chatRoomDoc = chatRoomsCollection.document(chatRoomId).get().await()
            val chatRoom = chatRoomDoc.toObject(ChatRoom::class.java)
            
            if (chatRoom == null || currentUserId !in chatRoom.participants) {
                return Result.failure(Exception("Access denied: User is not a participant in this chat"))
            }
            
            val message = ChatMessage(
                senderId = currentUserId,
                senderName = currentUserName,
                content = content,
                messageType = messageType.name,
                replyToMessageId = replyToMessageId,
                replyToContent = replyToContent,
                replyToSenderName = replyToSenderName
            )
            
            val messageRef = chatRoomsCollection
                .document(chatRoomId)
                .collection("messages")
                .document()
            
            val messageWithId = message.copy(id = messageRef.id)
            messageRef.set(messageWithId).await()
            
            // Update chat room's last message info
            updateChatRoomLastMessage(chatRoomId, messageWithId)
            
            Result.success(messageWithId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update chat room's last message
    private suspend fun updateChatRoomLastMessage(chatRoomId: String, message: ChatMessage) {
        try {
            val lastPreview = when (message.messageType) {
                MessageType.AUDIO.name -> "[Audio]"
                MessageType.IMAGE.name -> "[Image]"
                else -> message.content
            }
            val updates = mapOf(
                "lastMessageId" to message.id,
                "lastMessageContent" to lastPreview,
                "lastMessageSender" to message.senderName,
                "lastMessageTimestamp" to message.timestamp,
                "updatedAt" to com.google.firebase.Timestamp.now()
            )
            
            chatRoomsCollection.document(chatRoomId).update(updates).await()
        } catch (e: Exception) {
            // Log error but don't fail the message send
            e.printStackTrace()
        }
    }
    
    // Mark messages as read
    suspend fun markMessagesAsRead(chatRoomId: String, messageIds: List<String>): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val batch = firestore.batch()
            messageIds.forEach { messageId ->
                val messageRef = chatRoomsCollection
                    .document(chatRoomId)
                    .collection("messages")
                    .document(messageId)
                batch.update(messageRef, "isRead", true)
            }
            
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete message for me only
    suspend fun deleteMessageForMe(chatRoomId: String, messageId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val messageRef = chatRoomsCollection
                .document(chatRoomId)
                .collection("messages")
                .document(messageId)
                
            // Add current user to hiddenForUserIds so only they don't see it
            messageRef.update("hiddenForUserIds", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Delete message for everyone
    suspend fun deleteMessageForEveryone(chatRoomId: String, messageId: String): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val messageRef = chatRoomsCollection
                .document(chatRoomId)
                .collection("messages")
                .document(messageId)
                
            // Check if current user is the sender
            val messageDoc = messageRef.get().await()
            val senderId = messageDoc.getString("senderId")
            
            if (senderId == currentUserId) {
                messageRef.update(
                    mapOf(
                        "content" to "This message was deleted",
                        "deletedForEveryone" to true
                    )
                ).await()
                Result.success(Unit)
            } else {
                Result.failure(Exception("You can only delete your own messages for everyone"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Search for users to start new chats
    suspend fun searchUsers(query: String): Result<List<Pair<String, String>>> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            println("DEBUG: ChatRepository - searchUsers called with query: '$query'")
            println("DEBUG: ChatRepository - currentUserId: $currentUserId")
            
            if (query.isBlank()) {
                println("DEBUG: ChatRepository - Query is blank, returning empty list")
                return Result.success(emptyList())
            }
            
            // Get all users first (for now, we'll do client-side filtering)
            // This is not ideal for large user bases but works for testing
            println("DEBUG: ChatRepository - Fetching users from Firestore...")
            val users = usersCollection
                .limit(100) // Limit to prevent too much data
                .get()
                .await()
            
            println("DEBUG: ChatRepository - Retrieved ${users.documents.size} user documents")
            
            val userList = users.documents.mapNotNull { doc ->
                val userId = doc.id
                val displayName = doc.getString("displayName")
                val email = doc.getString("email")
                
                println("DEBUG: ChatRepository - User: $userId, displayName: '$displayName', email: '$email'")
                
                if (userId != currentUserId && displayName != null) {
                    // Case-insensitive search in display name or email
                    val queryLower = query.lowercase()
                    val matchesName = displayName.lowercase().contains(queryLower)
                    val matchesEmail = email?.lowercase()?.contains(queryLower) == true
                    
                    println("DEBUG: ChatRepository - Query '$queryLower' matches name: $matchesName, matches email: $matchesEmail")
                    
                    if (matchesName || matchesEmail) {
                        println("DEBUG: ChatRepository - User '$displayName' matches search")
                        Pair(userId, displayName)
                    } else null
                } else {
                    println("DEBUG: ChatRepository - Skipping user (current user or null displayName)")
                    null
                }
            }
            
            println("DEBUG: ChatRepository - Search returned ${userList.size} results")
            Result.success(userList)
        } catch (e: Exception) {
            println("DEBUG: ChatRepository - Search error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    // Initialize user document (call when user signs in)
    suspend fun initializeUser(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            
            val userDoc = usersCollection.document(currentUser.uid)
            val userData = mapOf(
                "displayName" to (currentUser.displayName ?: "Unknown User"),
                "email" to currentUser.email,
                "lastSeen" to com.google.firebase.Timestamp.now(),
                "isOnline" to true
            )
            
            userDoc.set(userData, com.google.firebase.firestore.SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update user online status
    suspend fun updateOnlineStatus(isOnline: Boolean): Result<Unit> {
        return try {
            val currentUserId = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
            
            val updates = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to com.google.firebase.Timestamp.now()
            )
            
            usersCollection.document(currentUserId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
