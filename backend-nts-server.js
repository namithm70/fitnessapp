const express = require('express');
const twilio = require('twilio');
const cors = require('cors');

const app = express();
const port = 3000;

// Twilio credentials (in production, use environment variables)
const accountSid = 'AC4a3e39a73e0cab5649c0b37a37ba6723';
const authToken = 'c628d1ef4698303c21335900bb9e87fb';
const apiKeySid = 'SK7f35817aad42bff09a9ee1c4bbf93a0a';
const apiKeySecret = '5EtUUksYMCtrbXmWd2ETnzN6NqK0jKxb';

// Initialize Twilio client with API Key (more secure than Account SID/Token)
const client = twilio(apiKeySid, apiKeySecret, { accountSid });

app.use(cors());
app.use(express.json());

// Endpoint to generate Network Traversal Service tokens
app.post('/api/nts-token', async (req, res) => {
    try {
        console.log('Generating NTS token...');
        
        // Generate NTS token
        const token = await client.tokens.create();
        
        console.log('NTS token generated successfully');
        console.log('ICE Servers:', JSON.stringify(token.iceServers, null, 2));
        
        // Return only the ice servers configuration
        res.json({
            success: true,
            iceServers: token.iceServers,
            accountSid: token.accountSid,
            ttl: token.ttl
        });
        
    } catch (error) {
        console.error('Error generating NTS token:', error);
        
        // Fallback to public TURN servers if NTS fails
        const fallbackIceServers = [
            {
                urls: 'turn:openrelay.metered.ca:80',
                username: 'openrelay',
                credential: 'openrelay'
            },
            {
                urls: 'turn:openrelay.metered.ca:443',
                username: 'openrelay',
                credential: 'openrelay'
            },
            {
                urls: 'stun:stun.l.google.com:19302'
            }
        ];
        
        res.json({
            success: false,
            error: error.message,
            fallbackIceServers: fallbackIceServers
        });
    }
});

// Health check endpoint
app.get('/api/health', (req, res) => {
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

app.listen(port, () => {
    console.log(`NTS Token Server running at http://localhost:${port}`);
    console.log('Endpoints:');
    console.log('- POST /api/nts-token - Generate Network Traversal Service tokens');
    console.log('- GET /api/health - Health check');
});
