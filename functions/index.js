const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();

exports.onCallSessionCreated = onDocumentCreated("callSessions/{sessionId}", async (event) => {
  const snap = event.data;
  if (!snap) return;
  const data = snap.data();
  try {
    const receiverId = data.receiverId;
    if (!receiverId) return;
    const userDoc = await db.collection("users").doc(receiverId).get();
    const token = userDoc.get("fcmToken");
    if (!token) return;

    await getMessaging().send({
      token,
      data: {
        type: "incoming_call",
        callSessionId: event.params.sessionId,
        callerName: data.callerName || "",
      },
      android: { priority: "high" },
    });
  } catch (e) {
    console.error("onCallSessionCreated error", e);
  }
});


