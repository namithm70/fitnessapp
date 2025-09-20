package com.fitnessss.fitlife.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.PropertyName
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

data class ChatMessage(
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val messageType: String = MessageType.TEXT.name,
    // Optional image payload
    val imageUrl: String? = null,
    @get:PropertyName("isRead") @set:PropertyName("isRead")
    var read: Boolean = false,
    @get:PropertyName("isDelivered") @set:PropertyName("isDelivered")
    var delivered: Boolean = false,
    @get:PropertyName("isDeleted") @set:PropertyName("isDeleted")
    var deleted: Boolean = false,
    // Accept legacy Firestore fields without failing mapping (read/delivered/deleted)
    @get:PropertyName("read") @set:PropertyName("read")
    var legacyRead: Any? = null,
    @get:PropertyName("delivered") @set:PropertyName("delivered")
    var legacyDelivered: Any? = null,
    @get:PropertyName("deleted") @set:PropertyName("deleted")
    var legacyDeleted: Any? = null,
    val deletedForEveryone: Boolean = false,
    // Optional audio payload
    val audioUrl: String? = null,
    val audioDurationMs: Long? = null,
    // List of userIds who chose "Delete for me" (hidden for them only)
    val hiddenForUserIds: List<String> = emptyList(),
    val replyToMessageId: String? = null,
    val replyToContent: String? = null,
    val replyToSenderName: String? = null
) {
    // Convert Firestore Timestamp to LocalDateTime
    fun toLocalDateTime(): LocalDateTime {
        return timestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()
    }
    
    // Create from LocalDateTime
    companion object {
        fun create(
            senderId: String,
            senderName: String,
            content: String,
            messageType: MessageType = MessageType.TEXT
        ): ChatMessage {
            return ChatMessage(
                senderId = senderId,
                senderName = senderName,
                content = content,
                messageType = messageType.name
            )
        }
    }
}

data class ChatRoom(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val participants: List<String> = emptyList(),
    val lastMessageId: String? = null,
    val lastMessageContent: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val lastMessageSender: String = "",
    val unreadCount: Map<String, Int> = emptyMap(), // userId -> unread count
    @get:PropertyName("isOnline") @set:PropertyName("isOnline")
    var online: Boolean = false,
    val avatarUrl: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    // Legacy fields from existing Firestore documents - ignore them
    val lastMessageLocalDateTime: Any? = null, // Ignore this field (was HashMap in old docs)
    val localDateTime: Any? = null, // Ignore this field (was HashMap in old docs)
    val read: Any? = null, // Ignore this field
    val delivered: Any? = null, // Ignore this field  
    val deleted: Any? = null // Ignore this field
) {
    fun toLastMessageLocalDateTime(): LocalDateTime {
        return lastMessageTimestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
            ?: LocalDateTime.now()
    }
}

data class CallSession(
    @DocumentId
    val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val participants: List<String> = emptyList(),
    val callType: String = CallType.AUDIO.name,
    val status: String = CallStatus.INITIATING.name,
    val startTime: Timestamp? = null,
    val endTime: Timestamp? = null,
    val duration: Long = 0, // in seconds
    val chatRoomId: String = "",
    // WebRTC signaling data
    val callerSdp: String? = null,
    val receiverSdp: String? = null,
    val iceCandidates: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null,
    // Legacy fields from existing Firestore documents - ignore them
    val startLocalDateTime: Any? = null, // Ignore this field (was HashMap in old docs)
    val endLocalDateTime: Any? = null // Ignore this field (was HashMap in old docs)
) {
    fun toStartLocalDateTime(): LocalDateTime? {
        return startTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    }
    
    fun toEndLocalDateTime(): LocalDateTime? {
        return endTime?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    }
}

enum class MessageType {
    TEXT,
    IMAGE,
    AUDIO,
    VIDEO,
    FILE,
    SYSTEM
}

enum class CallType {
    AUDIO,
    VIDEO
}

enum class CallStatus {
    INITIATING,
    RINGING,
    CONNECTING,
    CONNECTED,
    ENDED,
    MISSED,
    DECLINED,
    FAILED,
    BUSY
}

// Extension functions for formatting
fun LocalDateTime.toDisplayTime(): String {
    val now = LocalDateTime.now()
    return when {
        this.toLocalDate() == now.toLocalDate() -> {
            this.format(DateTimeFormatter.ofPattern("HH:mm"))
        }
        this.toLocalDate() == now.toLocalDate().minusDays(1) -> "Yesterday"
        this.toLocalDate().year == now.toLocalDate().year -> {
            this.format(DateTimeFormatter.ofPattern("MMM dd"))
        }
        else -> {
            this.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        }
    }
}

fun LocalDateTime.toChatTime(): String {
    return this.format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun Timestamp.toDisplayTime(): String {
    return this.toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .toDisplayTime()
}

fun Timestamp.toChatTime(): String {
    return this.toDate().toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
        .toChatTime()
}
