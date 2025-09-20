/**
 * Example Backend Server for Twilio Network Traversal Service
 * 
 * This is a Node.js/Express example showing how to implement
 * Twilio NTS token generation on your backend server.
 * 
 * For production, implement this on your backend and call it
 * from your Android app instead of using client-side credentials.
 */

const express = require('express');
const twilio = require('twilio');

const app = express();
const port = 3000;

// Your Twilio credentials (keep these secure!)
const accountSid = 'AC4a3e39a73e0cab5649c0b37a37ba6723';
const authToken = 'c628d1ef4698303c21335900bb9e87fb';

// Initialize Twilio client
const client = twilio(accountSid, authToken);

// Middleware
app.use(express.json());

// CORS middleware (adjust for your needs)
app.use((req, res, next) => {
    res.header('Access-Control-Allow-Origin', '*');
    res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept, Authorization');
    next();
});

/**
 * Endpoint to get TURN credentials
 * GET /api/turn-credentials
 */
app.get('/api/turn-credentials', async (req, res) => {
    try {
        console.log('Generating TURN credentials...');
        
        // Generate Twilio NTS token
        const token = await client.tokens.create();
        
        console.log('Token generated successfully');
        
        // Return the ICE servers from the token
        res.json({
            success: true,
            iceServers: token.iceServers,
            ttl: token.ttl // Time to live in seconds
        });
        
    } catch (error) {
        console.error('Error generating TURN credentials:', error);
        res.status(500).json({
            success: false,
            error: 'Failed to generate TURN credentials',
            message: error.message
        });
    }
});

/**
 * Health check endpoint
 */
app.get('/health', (req, res) => {
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Start server
app.listen(port, () => {
    console.log(`Twilio NTS Backend Server running on port ${port}`);
    console.log(`TURN credentials endpoint: http://localhost:${port}/api/turn-credentials`);
});

/**
 * Example usage in your Android app:
 * 
 * 1. Deploy this server to your backend (AWS, Google Cloud, etc.)
 * 2. Update TwilioTokenService.kt to call your server endpoint
 * 3. Replace the fetchFromBackend() method with your server URL
 * 
 * Example Android code:
 * 
 * private suspend fun fetchFromBackend(): Result<TurnCredentials> = withContext(Dispatchers.IO) {
 *     try {
 *         val url = URL("https://your-backend-server.com/api/turn-credentials")
 *         val connection = url.openConnection() as HttpURLConnection
 *         
 *         connection.requestMethod = "GET"
 *         connection.setRequestProperty("Authorization", "Bearer YOUR_API_TOKEN")
 *         
 *         val response = connection.inputStream.bufferedReader().use { it.readText() }
 *         val json = JSONObject(response)
 *         
 *         if (json.getBoolean("success")) {
 *             val iceServersArray = json.getJSONArray("iceServers")
 *             val iceServers = mutableListOf<IceServer>()
 *             
 *             for (i in 0 until iceServersArray.length()) {
 *                 val server = iceServersArray.getJSONObject(i)
 *                 val urls = mutableListOf<String>()
 *                 
 *                 val urlsArray = server.getJSONArray("urls")
 *                 for (j in 0 until urlsArray.length()) {
 *                     urls.add(urlsArray.getString(j))
 *                 }
 *                 
 *                 iceServers.add(IceServer(
 *                     urls = urls,
 *                     username = server.optString("username", null),
 *                     credential = server.optString("credential", null)
 *                 ))
 *             }
 *             
 *             Result.success(TurnCredentials(iceServers))
 *         } else {
 *             Result.failure(Exception(json.getString("error")))
 *         }
 *     } catch (e: Exception) {
 *         Result.failure(e)
 *     }
 * }
 */
