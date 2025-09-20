package com.fitnessss.fitlife.data.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fitnessss.fitlife.MainActivity
import com.fitnessss.fitlife.R
import com.fitnessss.fitlife.data.model.CallSession
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)
    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    
    companion object {
        const val CHANNEL_INCOMING_CALL = "incoming_call_channel"
        const val CHANNEL_ONGOING_CALL = "ongoing_call_channel"
        const val NOTIFICATION_ID_INCOMING_CALL = 1001
        const val NOTIFICATION_ID_ONGOING_CALL = 1002
        
        // Actions
        const val ACTION_ANSWER_CALL = "action_answer_call"
        const val ACTION_DECLINE_CALL = "action_decline_call"
        const val ACTION_END_CALL = "action_end_call"
        const val ACTION_SHOW_INCOMING_CALL = "action_show_incoming_call"
        
        // Extras
        const val EXTRA_CALL_SESSION_ID = "call_session_id"
        const val EXTRA_CALLER_NAME = "caller_name"
    }
    
    init {
        createNotificationChannels()
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Incoming call channel
            val incomingCallChannel = NotificationChannel(
                CHANNEL_INCOMING_CALL,
                "Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for incoming voice calls"
                setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), 
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            }
            
            // Ongoing call channel
            val ongoingCallChannel = NotificationChannel(
                CHANNEL_ONGOING_CALL,
                "Ongoing Calls",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for ongoing voice calls"
                setSound(null, null)
                enableVibration(false)
            }
            
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(incomingCallChannel)
            manager.createNotificationChannel(ongoingCallChannel)
        }
    }
    
    @SuppressLint("MissingPermission")
    fun showIncomingCallNotification(callSession: CallSession) {
        val answerIntent = Intent(context, com.fitnessss.fitlife.ui.screens.call.IncomingCallActivity::class.java).apply {
            action = ACTION_ANSWER_CALL
            putExtra("callSessionId", callSession.id)
            putExtra("callerName", callSession.callerName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val declineIntent = Intent(context, com.fitnessss.fitlife.ui.screens.call.IncomingCallActivity::class.java).apply {
            action = ACTION_DECLINE_CALL
            putExtra("callSessionId", callSession.id)
            putExtra("callerName", callSession.callerName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val answerPendingIntent = PendingIntent.getActivity(
            context, 0, answerIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val declinePendingIntent = PendingIntent.getActivity(
            context, 1, declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val fullScreenActivityIntent = Intent(context, com.fitnessss.fitlife.ui.screens.call.IncomingCallActivity::class.java).apply {
            action = ACTION_SHOW_INCOMING_CALL
            putExtra("callSessionId", callSession.id)
            putExtra("callerName", callSession.callerName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenIntent = PendingIntent.getActivity(
            context, 2, fullScreenActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_INCOMING_CALL)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Incoming call")
            .setContentText("${callSession.callerName} is calling...")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(true)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenIntent, true)
            .addAction(
                android.R.drawable.ic_menu_call,
                "Answer",
                answerPendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Decline",
                declinePendingIntent
            )
            .build()
        
        try {
            notificationManager.notify(NOTIFICATION_ID_INCOMING_CALL, notification)
            startRingtone()
            startVibration()
        } catch (e: SecurityException) {
            // Handle permission not granted
        }
    }
    
    @SuppressLint("MissingPermission")
    fun showOngoingCallNotification(callSession: CallSession) {
        val endCallIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_END_CALL
            putExtra(EXTRA_CALL_SESSION_ID, callSession.id)
        }
        
        val endCallPendingIntent = PendingIntent.getActivity(
            context, 3, endCallIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = buildOngoingCallNotification(callSession, endCallPendingIntent)
        
        try {
            notificationManager.notify(NOTIFICATION_ID_ONGOING_CALL, notification)
        } catch (e: SecurityException) {
            // Handle permission not granted
        }
    }

    fun buildOngoingCallNotification(callSession: CallSession, endCallPendingIntent: PendingIntent? = null): Notification {
        val endIntent = endCallPendingIntent ?: PendingIntent.getActivity(
            context, 3,
            Intent(context, MainActivity::class.java).apply {
                action = ACTION_END_CALL
                putExtra(EXTRA_CALL_SESSION_ID, callSession.id)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, CHANNEL_ONGOING_CALL)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Ongoing call")
            .setContentText("Call with ${getOtherUserName(callSession)}")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "End Call",
                endIntent
            )
            .build()
    }
    
    fun dismissIncomingCallNotification() {
        notificationManager.cancel(NOTIFICATION_ID_INCOMING_CALL)
        stopRingtone()
        stopVibration()
    }
    
    fun dismissOngoingCallNotification() {
        notificationManager.cancel(NOTIFICATION_ID_ONGOING_CALL)
        // Also stop any residual ringtone or vibration if any
        stopRingtone()
        stopVibration()
    }
    
    private fun startRingtone() {
        try {
            stopRingtone() // Stop any existing ringtone
            
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, ringtoneUri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun stopRingtone() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.stop()
            }
            player.release()
        }
        mediaPlayer = null
    }
    
    @Suppress("DEPRECATION")
    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 500, 1000, 500, 1000)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build()
            )
        } else {
            vibrator?.vibrate(pattern, 0)
        }
    }
    
    private fun stopVibration() {
        vibrator?.cancel()
    }
    
    private fun getOtherUserName(callSession: CallSession): String {
        val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        return if (callSession.callerId == currentUserId) {
            callSession.receiverName
        } else {
            callSession.callerName
        }
    }
}
