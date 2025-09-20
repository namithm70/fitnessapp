package com.fitnessss.fitlife.data.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fitnessss.fitlife.MainActivity
import com.fitnessss.fitlife.R
import com.fitnessss.fitlife.data.model.ChatMessage
import com.fitnessss.fitlife.data.model.ChatRoom
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    
    companion object {
        const val CHANNEL_MESSAGES = "messages_channel"
        const val NOTIFICATION_ID_MESSAGE_BASE = 2000
        
        // Actions
        const val ACTION_REPLY = "action_reply"
        const val ACTION_MARK_READ = "action_mark_read"
        
        // Extras
        const val EXTRA_CHAT_ROOM_ID = "chat_room_id"
        const val EXTRA_MESSAGE_ID = "message_id"
        const val EXTRA_SENDER_NAME = "sender_name"
        const val EXTRA_MESSAGE_CONTENT = "message_content"
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Messages channel
            val messagesChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION), 
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 100, 500)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(messagesChannel)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun showMessageNotification(message: ChatMessage, chatRoom: ChatRoom) {
        // Don't show notification if user sent the message
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        if (message.senderId == currentUserId) return
        
        // Don't show notification for deleted messages
        if (message.deleted || message.deletedForEveryone) return
        
        val chatIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            putExtra(EXTRA_CHAT_ROOM_ID, chatRoom.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val markReadIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_MARK_READ
            putExtra(EXTRA_CHAT_ROOM_ID, chatRoom.id)
            putExtra(EXTRA_MESSAGE_ID, message.id)
        }
        
        val chatPendingIntent = PendingIntent.getActivity(
            context, 0, chatIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val markReadPendingIntent = PendingIntent.getActivity(
            context, 1, markReadIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Create notification content
        val contentText = when {
            message.replyToContent != null -> "Replied to \"${message.replyToContent}\": ${message.content}"
            else -> message.content
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(message.senderName)
            .setContentText(contentText)
            .setSubText(getOtherParticipantName(chatRoom))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(chatPendingIntent)
            .addAction(
                android.R.drawable.ic_menu_view,
                "Mark as Read",
                markReadPendingIntent
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(contentText)
                    .setSummaryText(getOtherParticipantName(chatRoom))
            )
            .setWhen(message.timestamp?.toDate()?.time ?: System.currentTimeMillis())
            .setShowWhen(true)
            .build()
        
        try {
            // Use chat room ID hash as notification ID to group messages from same chat
            val notificationId = NOTIFICATION_ID_MESSAGE_BASE + chatRoom.id.hashCode()
            notificationManager.notify(notificationId, notification)
        } catch (e: SecurityException) {
            // Handle permission not granted
        }
    }
    
    @SuppressLint("MissingPermission")
    fun showGroupedMessageNotifications(messages: List<Pair<ChatMessage, ChatRoom>>) {
        if (messages.isEmpty()) return
        
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        
        // Group messages by chat room
        val messagesByRoom = messages
            .filter { (message, _) -> message.senderId != currentUserId && !message.deleted && !message.deletedForEveryone }
            .groupBy { it.second.id }
        
        if (messagesByRoom.isEmpty()) return
        
        // If only one chat room, show individual notification
        if (messagesByRoom.size == 1) {
            val (_, chatRoom) = messages.first()
            val lastMessage = messages.last().first
            showMessageNotification(lastMessage, chatRoom)
            return
        }
        
        // Create summary notification for multiple chats
        val summaryIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val summaryPendingIntent = PendingIntent.getActivity(
            context, 2, summaryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val totalMessages = messages.size
        val totalChats = messagesByRoom.size
        
        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_MESSAGES)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle("$totalMessages new messages")
            .setContentText("From $totalChats conversations")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(summaryPendingIntent)
            .setGroup("MESSAGE_GROUP")
            .setGroupSummary(true)
            .setStyle(
                NotificationCompat.InboxStyle().let { inboxStyle ->
                    inboxStyle.setSummaryText("$totalMessages new messages from $totalChats chats")
                    messagesByRoom.entries.take(5).forEach { (_, messagesInRoom) ->
                        val chatRoom = messagesInRoom.first().second
                        val messageCount = messagesInRoom.size
                        val lastMessage = messagesInRoom.last().first
                        inboxStyle.addLine("${getOtherParticipantName(chatRoom)}: ${if (messageCount > 1) "$messageCount messages" else lastMessage.content}")
                    }
                    inboxStyle
                }
            )
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID_MESSAGE_BASE, summaryNotification)
            
            // Show individual notifications for each chat
            messagesByRoom.forEach { (chatRoomId, messagesInRoom) ->
                val chatRoom = messagesInRoom.first().second
                val lastMessage = messagesInRoom.last().first
                showMessageNotification(lastMessage, chatRoom)
            }
        } catch (e: SecurityException) {
            // Handle permission not granted
        }
    }
    
    fun dismissMessageNotification(chatRoomId: String) {
        val notificationId = NOTIFICATION_ID_MESSAGE_BASE + chatRoomId.hashCode()
        notificationManager.cancel(notificationId)
    }
    
    fun dismissAllMessageNotifications() {
        // Cancel summary notification
        notificationManager.cancel(NOTIFICATION_ID_MESSAGE_BASE)
        
        // Cancel group notifications (Android will handle individual notifications)
        notificationManager.cancelAll()
    }
    
    private fun getOtherParticipantName(chatRoom: ChatRoom): String {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        // This is simplified - in a real app, you'd fetch the actual user names
        return "Chat" // TODO: Get actual participant names
    }
}
