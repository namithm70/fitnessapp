package com.fitnessss.fitlife.data.config

/**
 * TURN Server Configuration for WebRTC
 * 
 * For production, use Twilio's Network Traversal Service (NTS) with dynamic tokens.
 * For development/testing, you can use these free TURN servers.
 */
object TwilioConfig {
    // Working TURN servers for development/testing
    const val FREE_TURN_USERNAME = "openrelay"
    const val FREE_TURN_PASSWORD = "openrelay"
    const val FREE_TURN_HOST = "turn:openrelay.metered.ca:80"
    
    // Alternative working TURN servers
    const val ALTERNATIVE_TURN_USERNAME = "openrelay"
    const val ALTERNATIVE_TURN_PASSWORD = "openrelay"
    const val ALTERNATIVE_TURN_HOST = "turn:openrelay.metered.ca:443"
    
    // Twilio NTS credentials (when enabled)
    const val TWILIO_API_KEY_SID = "SK7f35817aad42bff09a9ee1c4bbf93a0a"
    const val TWILIO_API_KEY_SECRET = "5EtUUksYMCtrbXmWd2ETnzN6NqK0jKxb"
}
