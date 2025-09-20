package com.fitnessss.fitlife.data.service

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.fitnessss.fitlife.data.model.CallStatus
import com.fitnessss.fitlife.data.model.ChatMessage
import com.fitnessss.fitlife.data.repository.CallRepository
import com.fitnessss.fitlife.data.repository.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class CallListenerService : Service() {
    
    @Inject
    lateinit var callRepository: CallRepository
    
    @Inject
    lateinit var chatRepository: ChatRepository
    
    @Inject
    lateinit var callNotificationService: CallNotificationService
    
    @Inject
    lateinit var messageNotificationService: MessageNotificationService
    
    private var serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    override fun onCreate() {
        super.onCreate()
        startListeningForIncomingCalls()
        startListeningForNewMessages()
    }
    
    private fun startListeningForIncomingCalls() {
        serviceScope.launch {
            callRepository.listenForIncomingCalls().collect { callSessions ->
                callSessions.forEach { callSession ->
                    when (callSession.status) {
                        CallStatus.INITIATING.name, CallStatus.RINGING.name -> {
                            // Always launch the call activity directly, don't show notification
                            // The IncomingCallActivity will handle dismissing any existing notifications
                            try {
                                val intent = android.content.Intent(applicationContext, com.fitnessss.fitlife.ui.screens.call.IncomingCallActivity::class.java).apply {
                                    action = CallNotificationService.ACTION_SHOW_INCOMING_CALL
                                    putExtra("callSessionId", callSession.id)
                                    putExtra("callerName", callSession.callerName)
                                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP or android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                }
                                startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        CallStatus.CONNECTED.name -> {
                            callNotificationService.dismissIncomingCallNotification()
                            // Promote to foreground to stabilize audio focus during calls
                            try {
                                val notification = callNotificationService.buildOngoingCallNotification(callSession)
                                startForeground(CallNotificationService.NOTIFICATION_ID_ONGOING_CALL, notification)
                            } catch (e: Exception) {
                                // Fallback to normal notification if foreground fails
                                callNotificationService.showOngoingCallNotification(callSession)
                            }
                        }
                        CallStatus.ENDED.name, CallStatus.DECLINED.name, CallStatus.MISSED.name -> {
                            callNotificationService.dismissIncomingCallNotification()
                            callNotificationService.dismissOngoingCallNotification()
                            // Stop foreground when call ends
                            try { stopForeground(STOP_FOREGROUND_REMOVE) } catch (_: Exception) {}
                        }
                    }
                }
            }
        }
    }
    
    private fun startListeningForNewMessages() {
        serviceScope.launch {
            // Listen for all chat rooms the user is part of
            chatRepository.getChatRooms().collect { chatRooms ->
                chatRooms.forEach { chatRoom ->
                    // Listen for new messages in each chat room
                    launch {
                        chatRepository.getMessages(chatRoom.id).collect { messages ->
                            // Find the latest message that's not from the current user
                            val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                            val latestMessage = messages
                                .filter { it.senderId != currentUserId && !it.deleted && !it.deletedForEveryone }
                                .maxByOrNull { it.timestamp?.toDate()?.time ?: 0 }
                            
                            if (latestMessage != null) {
                                // Check if this is a new message (within last 10 seconds)
                                val messageTime = latestMessage.timestamp?.toDate()?.time ?: 0
                                val currentTime = System.currentTimeMillis()
                                val timeDiff = currentTime - messageTime
                                
                                if (timeDiff < 10000) { // 10 seconds
                                    messageNotificationService.showMessageNotification(latestMessage, chatRoom)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onBind(intent: Intent): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Restart service if killed
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    private fun isAppInForeground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = activityManager.runningAppProcesses
        if (runningProcesses != null) {
            for (processInfo in runningProcesses) {
                if (processInfo.processName == packageName) {
                    return processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                }
            }
        }
        return false
    }
}
