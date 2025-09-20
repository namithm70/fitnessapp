package com.fitnessss.fitlife.data.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AppFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var callNotificationService: CallNotificationService

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token will be saved by Session/Login flow using FirebaseAuthService; optional here
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val type = message.data["type"] ?: return
        when (type) {
            "incoming_call" -> {
                val sessionId = message.data["callSessionId"] ?: return
                val callerName = message.data["callerName"] ?: ""
                // Show full-screen incoming notification
                callNotificationService.showIncomingCallNotification(
                    com.fitnessss.fitlife.data.model.CallSession(
                        id = sessionId,
                        callerId = "",
                        callerName = callerName,
                        receiverId = "",
                        receiverName = "",
                        participants = emptyList()
                    )
                )
            }
        }
    }
}


