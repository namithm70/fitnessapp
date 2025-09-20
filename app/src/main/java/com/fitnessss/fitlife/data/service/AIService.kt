package com.fitnessss.fitlife.data.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIService @Inject constructor(
    private val context: Context
) {
    
    companion object {
        private const val TAG = "AIService"
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent"
        private const val API_KEY = "AIzaSyAyZuLzIIbt3OmK1Belnf_Pb4YSlJWUqcI"
        private const val MAX_RETRIES = 3
        private const val RATE_LIMIT_DELAY = 2000L // 2 seconds between requests to avoid rate limiting
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
            println("DEBUG: AIService - Making request to: ${request.url}")
            chain.proceed(request)
        }
        .build()
    
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    
    // Conversation context for memory
    private val conversationHistory = mutableListOf<String>()
    

    
    suspend fun sendMessage(userMessage: String): String {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG: AIService - Sending message: $userMessage")
                
                // Add user message to conversation history
                conversationHistory.add("User: $userMessage")
                
                // Check network connectivity
                if (!isNetworkAvailable()) {
                    println("DEBUG: AIService - No network connectivity")
                    return@withContext "I'm sorry, I need an internet connection to help you. Please check your network connection and try again."
                }
                
                // Prepare the conversation context
                val context = buildConversationContext(userMessage)
                
                // Create the request body for Google Gemini API
                val requestBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", context)
                                })
                            })
                        })
                    })
                    put("generationConfig", JSONObject().apply {
                        put("temperature", 0.7)
                        put("topK", 40)
                        put("topP", 0.95)
                        put("maxOutputTokens", 1000)
                    })
                }.toString().toRequestBody(jsonMediaType)
                
                // Create the request for Google Gemini API
                val request = Request.Builder()
                    .url("$GEMINI_API_URL?key=$API_KEY")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build()
                
                // Execute the request with retries
                var response: String = "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                var lastException: Exception? = null
                
                for (attempt in 1..MAX_RETRIES) {
                    try {
                        println("DEBUG: AIService - Attempt $attempt of $MAX_RETRIES")
                        
                        client.newCall(request).execute().use { httpResponse ->
                            if (httpResponse.isSuccessful) {
                                val responseBody = httpResponse.body?.string()
                                println("DEBUG: AIService - Raw response: $responseBody")
                                
                                if (!responseBody.isNullOrEmpty()) {
                                    // Parse Google Gemini API response
                                    try {
                                        val jsonResponse = JSONObject(responseBody)
                                        response = if (jsonResponse.has("candidates")) {
                                            val candidates = jsonResponse.getJSONArray("candidates")
                                            if (candidates.length() > 0) {
                                                val candidate = candidates.getJSONObject(0)
                                                if (candidate.has("content")) {
                                                    val content = candidate.getJSONObject("content")
                                                    if (content.has("parts")) {
                                                        val parts = content.getJSONArray("parts")
                                                        if (parts.length() > 0) {
                                                            try {
                                                                val part = parts.getJSONObject(0)
                                                                if (part.has("text")) {
                                                                    part.getString("text") ?: "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                                                } else {
                                                                    "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                                                }
                                                            } catch (e: Exception) {
                                                                "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                                            }
                                                        } else {
                                                            "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                                        }
                                                    } else {
                                                        "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                                    }
                                                } else {
                                                    "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                                }
                                            } else {
                                                "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                            }
                                        } else if (jsonResponse.has("error")) {
                                            throw Exception("API Error: Unknown error occurred")
                                        } else {
                                            "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                        }
                                    } catch (e: Exception) {
                                        println("DEBUG: AIService - JSON parsing error: ${e.message}")
                                        "I'm here to help with your fitness journey! What would you like to know about workouts, nutrition, or motivation?"
                                    }
                                    return@withContext response
                                }
                            } else {
                                when (httpResponse.code) {
                                    429 -> {
                                        println("DEBUG: AIService - Rate limit exceeded (429)")
                                        throw Exception("Rate limit exceeded. Please wait a moment and try again.")
                                    }
                                    403 -> {
                                        println("DEBUG: AIService - API key error (403)")
                                        throw Exception("API key error. Please check your Google Cloud Console settings.")
                                    }
                                    else -> {
                                        throw Exception("HTTP ${httpResponse.code}: ${httpResponse.message}")
                                    }
                                }
                            }
                        }
                        
                        // If we reach here, the request was successful
                        break
                        
                    } catch (e: Exception) {
                        lastException = e
                        println("DEBUG: AIService - Attempt $attempt failed: ${e.message}")
                        
                        if (attempt < MAX_RETRIES) {
                            // Longer delay for rate limit errors
                            val delayTime = if (e.message?.contains("Rate limit") == true) {
                                RATE_LIMIT_DELAY * (attempt + 2) // 4, 6, 8 seconds
                            } else {
                                RATE_LIMIT_DELAY * attempt
                            }
                            kotlinx.coroutines.delay(delayTime)
                        }
                    }
                }
                
                // If all attempts failed, return an error message
                if (response == null) {
                    response = "I'm sorry, I'm having trouble connecting to my AI service right now. Please try again in a moment."
                    println("DEBUG: AIService - All API attempts failed")
                }
                
                // Add AI response to conversation history
                conversationHistory.add("AI: $response")
                
                // Keep conversation history manageable (last 10 exchanges)
                if (conversationHistory.size > 20) {
                    conversationHistory.removeAt(0)
                    conversationHistory.removeAt(0)
                }
                
                println("DEBUG: AIService - Final response: $response")
                response
                
            } catch (e: Exception) {
                println("DEBUG: AIService - Error: ${e.message}")
                Log.e(TAG, "Error sending message", e)
                "I'm sorry, I encountered an error while processing your request. Please try again."
            }
        }
    }
    
    private fun buildConversationContext(userMessage: String): String {
        val conciseInstruction = "IMPORTANT: Respond with exactly 3-4 bullet points (using * or -). Each point should be 1-2 sentences maximum. Keep responses concise and actionable. Use **bold** for section headers only. Do not repeat information."
        
        val contentRestriction = """
            CRITICAL: You are FitLife AI, a fitness and health assistant ONLY. You must ONLY respond to questions related to:
            - Health and fitness
            - Workouts and exercises
            - Nutrition and diet
            - Physical training and sports
            - Wellness and lifestyle
            
            If a user asks about ANY other topic (politics, technology, entertainment, etc.), respond with:
            "I'm sorry, I can only help with health, fitness, nutrition, and workout-related questions. Please ask me about workouts, exercises, nutrition, or fitness goals instead."
            
            Stay focused on fitness and health topics only.
        """.trimIndent()
        
        return if (conversationHistory.isEmpty()) {
            "$contentRestriction\n\nYou are FitLife AI, a helpful fitness assistant. You provide advice on workouts, nutrition, and motivation. Be encouraging and supportive. $conciseInstruction\n\nUser: $userMessage"
        } else {
            // Only include the last 2 exchanges to keep context manageable
            val recentHistory = conversationHistory.takeLast(4).joinToString("\n")
            "$contentRestriction\n\n$conciseInstruction\n\n$recentHistory\nUser: $userMessage\nAI:"
        }
    }
    

    
    fun clearConversation() {
        conversationHistory.clear()
        println("DEBUG: AIService - Conversation history cleared")
    }
    
    fun getConversationHistory(): List<String> {
        return conversationHistory.toList()
    }
    
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}
