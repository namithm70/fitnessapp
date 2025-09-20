// Firebase Admin SDK script to create test users for chat functionality
// Run this with: node create_test_users.js

const admin = require('firebase-admin');

// Initialize Firebase Admin SDK
// You'll need to download your service account key from Firebase Console
// and save it as 'service-account-key.json' in this directory
const serviceAccount = require('./service-account-key.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const auth = admin.auth();
const firestore = admin.firestore();

async function createTestUsers() {
  try {
    console.log('Creating test users...');
    
    // Test User 1
    const user1 = await auth.createUser({
      email: 'testuser1@fitlife.com',
      password: 'password123',
      displayName: 'John Doe'
    });
    
    // Add user1 to Firestore users collection
    await firestore.collection('users').doc(user1.uid).set({
      displayName: 'John Doe',
      email: 'testuser1@fitlife.com',
      isOnline: false,
      lastSeen: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('✅ Created user 1:', user1.uid, '- John Doe');
    
    // Test User 2
    const user2 = await auth.createUser({
      email: 'testuser2@fitlife.com',
      password: 'password123',
      displayName: 'Jane Smith'
    });
    
    // Add user2 to Firestore users collection
    await firestore.collection('users').doc(user2.uid).set({
      displayName: 'Jane Smith',
      email: 'testuser2@fitlife.com',
      isOnline: false,
      lastSeen: admin.firestore.FieldValue.serverTimestamp()
    });
    
    console.log('✅ Created user 2:', user2.uid, '- Jane Smith');
    
    console.log('\n🎉 Test users created successfully!');
    console.log('\nYou can now log in with:');
    console.log('User 1: testuser1@fitlife.com / password123');
    console.log('User 2: testuser2@fitlife.com / password123');
    console.log('\nThese users should now be searchable in the chat functionality.');
    
  } catch (error) {
    console.error('❌ Error creating test users:', error);
  } finally {
    process.exit(0);
  }
}

createTestUsers();
