package com.fitnessss.fitlife.data.repository

import com.fitnessss.fitlife.data.model.CallSession
import com.fitnessss.fitlife.data.model.CallStatus
import com.fitnessss.fitlife.data.model.CallType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val callSessionsCollection = firestore.collection("callSessions")
    
    // Get current user ID
    private fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    // Initiate a new call
    suspend fun initiateCall(
        receiverId: String,
        receiverName: String,
        chatRoomId: String,
        callType: CallType = CallType.AUDIO
    ): Result<CallSession> {
        return try {
            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not authenticated"))
            val currentUserId = currentUser.uid
            val currentUserName = currentUser.displayName ?: "Unknown User"
            
            val callSession = CallSession(
                callerId = currentUserId,
                callerName = currentUserName,
                receiverId = receiverId,
                receiverName = receiverName,
                participants = listOf(currentUserId, receiverId),
                callType = callType.name,
                status = CallStatus.INITIATING.name,
                chatRoomId = chatRoomId,
                createdAt = Timestamp.now()
            )
            
            val docRef = callSessionsCollection.add(callSession).await()
            val createdSession = callSession.copy(id = docRef.id)
            
            // Update with the generated ID
            docRef.set(createdSession).await()
            
            Result.success(createdSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Listen for incoming calls
    fun listenForIncomingCalls(): Flow<List<CallSession>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val listener = callSessionsCollection
            .whereEqualTo("receiverId", currentUserId)
            .whereIn("status", listOf(CallStatus.INITIATING.name, CallStatus.RINGING.name))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val callSessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CallSession::class.java)
                } ?: emptyList()
                
                trySend(callSessions)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Listen for call session updates
    fun listenForCallSession(callSessionId: String): Flow<CallSession?> = callbackFlow {
        if (callSessionId.isBlank()) {
            close(IllegalArgumentException("callSessionId is blank"))
            return@callbackFlow
        }
        val listener = callSessionsCollection
            .document(callSessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val callSession = snapshot?.toObject(CallSession::class.java)
                trySend(callSession)
            }
        
        awaitClose { listener.remove() }
    }
    
    // Update call status
    suspend fun updateCallStatus(callSessionId: String, status: CallStatus): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name,
                "updatedAt" to Timestamp.now()
            )
            
            // Add timestamps for specific statuses
            when (status) {
                CallStatus.CONNECTED -> {
                    updates["startTime"] = Timestamp.now()
                }
                CallStatus.ENDED, CallStatus.DECLINED, CallStatus.MISSED, CallStatus.FAILED -> {
                    updates["endTime"] = Timestamp.now()
                }
                else -> {}
            }
            
            callSessionsCollection.document(callSessionId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Update WebRTC signaling data
    suspend fun updateSignalingData(
        callSessionId: String,
        callerSdp: String? = null,
        receiverSdp: String? = null,
        iceCandidates: List<String>? = null
    ): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>("updatedAt" to Timestamp.now())
            
            callerSdp?.let { 
                updates["callerSdp"] = it
                println("DEBUG: CallRepository - updateSignalingData: setting callerSdp for session $callSessionId")
            }
            receiverSdp?.let { 
                updates["receiverSdp"] = it
                println("DEBUG: CallRepository - updateSignalingData: setting receiverSdp for session $callSessionId")
            }
            iceCandidates?.let { updates["iceCandidates"] = it }
            
            callSessionsCollection.document(callSessionId).update(updates).await()
            println("DEBUG: CallRepository - updateSignalingData: successfully updated session $callSessionId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: CallRepository - updateSignalingData: error updating session $callSessionId: ${e.message}")
            Result.failure(e)
        }
    }
    
    // Answer a call
    suspend fun answerCall(callSessionId: String): Result<Unit> {
        return updateCallStatus(callSessionId, CallStatus.CONNECTING)
    }
    
    // Decline a call
    suspend fun declineCall(callSessionId: String): Result<Unit> {
        return updateCallStatus(callSessionId, CallStatus.DECLINED)
    }
    
    // End a call
    suspend fun endCall(callSessionId: String): Result<Unit> {
        return try {
            val callDoc = callSessionsCollection.document(callSessionId).get().await()
            val callSession = callDoc.toObject(CallSession::class.java)
            
            if (callSession?.startTime != null && callSession.endTime == null) {
                // Calculate duration
                val duration = (Timestamp.now().seconds - callSession.startTime!!.seconds)
                
                val updates = mapOf(
                    "status" to CallStatus.ENDED.name,
                    "endTime" to Timestamp.now(),
                    "duration" to duration,
                    "updatedAt" to Timestamp.now()
                )
                
                callSessionsCollection.document(callSessionId).update(updates).await()
            } else {
                updateCallStatus(callSessionId, CallStatus.ENDED)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Get call history for a user
    fun getCallHistory(): Flow<List<CallSession>> = callbackFlow {
        val currentUserId = getCurrentUserId()
        if (currentUserId == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }
        
        val listener = callSessionsCollection
            .whereArrayContains("participants", currentUserId)
            .whereIn("status", listOf(CallStatus.ENDED.name, CallStatus.DECLINED.name, CallStatus.MISSED.name))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val callHistory = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CallSession::class.java)
                } ?: emptyList()
                
                trySend(callHistory)
            }
        
        awaitClose { listener.remove() }
    }
}
