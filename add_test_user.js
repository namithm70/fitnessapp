// Simple script to add a test user to Firestore
const admin = require('firebase-admin');

// Initialize with your service account key
// Download from Firebase Console -> Project Settings -> Service accounts
try {
  const serviceAccount = require('./service-account-key.json');
  
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
} catch (error) {
  console.log('Service account key not found. Using default credentials...');
  admin.initializeApp();
}

const firestore = admin.firestore();

async function addTestUser() {
  try {
    // Add a test user document
    await firestore.collection('users').doc('testuser123').set({
      displayName: 'Test User',
      email: 'test@example.com',
      isOnline: false,
      lastSeen: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('✅ Test user added successfully!');
    console.log('You can now search for "Test" or "test" in the app');
    
  } catch (error) {
    console.error('❌ Error adding test user:', error);
  } finally {
    process.exit(0);
  }
}

addTestUser();
