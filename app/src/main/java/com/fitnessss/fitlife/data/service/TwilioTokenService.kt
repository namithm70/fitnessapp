package com.fitnessss.fitlife.data.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to fetch Twilio Network Traversal Service tokens
 * 
 * For production, this should be called from your backend server.
 * For development, we'll use a mock implementation.
 */
@Singleton
class TwilioTokenService @Inject constructor() {
    
    /**
     * Fetch TURN server credentials from Twilio NTS
     * 
     * Note: In production, this should be called from your backend server
     * to keep credentials secure. This is a client-side implementation
     * for development purposes only.
     */
    suspend fun getTurnCredentials(): Result<TurnCredentials> = withContext(Dispatchers.IO) {
        try {
            // Try to fetch from backend server first
            val backendResult = fetchFromBackend()
            if (backendResult.isSuccess) {
                return@withContext backendResult
            }
            
            // Try Twilio Network Traversal Service directly (requires Twilio API Key SID/Secret)
            val twilioNtsResult = fetchFromTwilioNts()
            if (twilioNtsResult.isSuccess) {
                return@withContext twilioNtsResult
            }
            
            // Final fallback: Twilio STUN only (no public TURN)
            return@withContext getFallbackCredentials()
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Production method - calls your backend server
     * Your backend server should implement the Twilio NTS token generation
     */
    private suspend fun fetchFromBackend(): Result<TurnCredentials> = withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.0.2.2:3000/api/nts-token")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                
                // Parse the NTS response
                val credentials = parseNTSResponse(json)
                Result.success(credentials)
            } else {
                Result.failure(Exception("Failed to fetch NTS token: $responseCode"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Development fallback TURN/STUN servers (reliable public servers)
     */
    private fun getFallbackCredentials(): Result<TurnCredentials> {
        val credentials = TurnCredentials(
            iceServers = listOf(
                IceServer(
                    urls = listOf("stun:global.stun.twilio.com:3478?transport=udp"),
                    username = null,
                    credential = null
                )
            )
        )
        return Result.success(credentials)
    }

    /**
     * Calls Twilio Network Traversal Service directly to obtain TURN credentials.
     * Requires Twilio API Key SID/Secret to be configured.
     */
    private suspend fun fetchFromTwilioNts(): Result<TurnCredentials> = withContext(Dispatchers.IO) {
        try {
            val sid = com.fitnessss.fitlife.data.config.TwilioConfig.TWILIO_API_KEY_SID
            val secret = com.fitnessss.fitlife.data.config.TwilioConfig.TWILIO_API_KEY_SECRET
            if (sid.isBlank() || secret.isBlank()) {
                return@withContext Result.failure(Exception("Twilio API credentials not configured"))
            }
            val url = URL("https://networktraversal.twilio.com/v1/Tokens")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            val basic = android.util.Base64.encodeToString("$sid:$secret".toByteArray(), android.util.Base64.NO_WRAP)
            conn.setRequestProperty("Authorization", "Basic $basic")
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true
            // You can set ttl or other params if needed
            conn.outputStream.use { it.write("ttl=3600".toByteArray()) }
            val code = conn.responseCode
            if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val credentials = parseNTSResponse(json)
                if (credentials.iceServers.isNotEmpty()) Result.success(credentials) else Result.failure(Exception("No ICE servers in Twilio response"))
            } else {
                Result.failure(Exception("Twilio NTS failed: HTTP $code"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseNTSResponse(json: JSONObject): TurnCredentials {
        val iceServers = mutableListOf<IceServer>()
        
        try {
            // Try to parse Twilio NTS response first
            if (json.getBoolean("success") && json.has("iceServers")) {
                val iceServersArray = json.getJSONArray("iceServers")
                for (i in 0 until iceServersArray.length()) {
                    val serverObj = iceServersArray.getJSONObject(i)
                    val urls = mutableListOf<String>()
                    
                    // Handle both single URL and URL array
                    if (serverObj.has("urls")) {
                        val urlsValue = serverObj.get("urls")
                        if (urlsValue is String) {
                            urls.add(urlsValue)
                        } else {
                            val urlsArray = serverObj.getJSONArray("urls")
                            for (j in 0 until urlsArray.length()) {
                                urls.add(urlsArray.getString(j))
                            }
                        }
                    }
                    
                    iceServers.add(
                        IceServer(
                            urls = urls,
                            username = if (serverObj.has("username")) serverObj.getString("username") else null,
                            credential = if (serverObj.has("credential")) serverObj.getString("credential") else null
                        )
                    )
                }
            }
            
            // Fall back to fallback servers if available
            if (iceServers.isEmpty() && json.has("fallbackIceServers")) {
                val fallbackArray = json.getJSONArray("fallbackIceServers")
                for (i in 0 until fallbackArray.length()) {
                    val serverObj = fallbackArray.getJSONObject(i)
                    iceServers.add(
                        IceServer(
                            urls = listOf(serverObj.getString("urls")),
                            username = if (serverObj.has("username")) serverObj.getString("username") else null,
                            credential = if (serverObj.has("credential")) serverObj.getString("credential") else null
                        )
                    )
                }
            }
            
        } catch (e: Exception) {
            // If parsing fails, return empty list (will fall back to static servers)
        }
        
        return TurnCredentials(iceServers = iceServers)
    }
}

/**
 * Data classes for TURN credentials
 */
data class TurnCredentials(
    val iceServers: List<IceServer>
)

data class IceServer(
    val urls: List<String>,
    val username: String?,
    val credential: String?
)
