/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const { setGlobalOptions } = require("firebase-functions");
const { logger } = require("firebase-functions");
const functions = require("firebase-functions");
const admin = require("firebase-admin");

// Initialize Admin SDK once
try {
  admin.app();
} catch (e) {
  admin.initializeApp();
}

// For cost control, you can set the maximum number of containers that can be
// running at the same time. This helps mitigate the impact of unexpected
// traffic spikes by instead downgrading performance. This limit is a
// per-function limit. You can override the limit for each function using the
// `maxInstances` option in the function's options, e.g.
// `onRequest({ maxInstances: 5 }, (req, res) => { ... })`.
// NOTE: setGlobalOptions does not apply to functions using the v1 API. V1
// functions should each use functions.runWith({ maxInstances: 10 }) instead.
// In the v1 API, each function can only serve one request per container, so
// this will be the maximum concurrent request count.
setGlobalOptions({ maxInstances: 10 });

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

// Notify target user when a new follower document is created
exports.onNewFollower = functions.firestore
  .document("users/{targetUserId}/followers/{followerId}")
  .onCreate(async (snap, context) => {
    const { targetUserId, followerId } = context.params;
    logger.info(`New follower: ${followerId} -> ${targetUserId}`);

    try {
      const userDoc = await admin.firestore().collection("users").doc(targetUserId).get();
      const fcmToken = userDoc.get("fcmToken");
      if (!fcmToken) {
        logger.warn(`No FCM token for user ${targetUserId}`);
        return null;
      }

      const followerDoc = await admin.firestore().collection("users").doc(followerId).get();
      const followerName = followerDoc.get("username") || "Alguien";

      const message = {
        token: fcmToken,
        notification: {
          title: "Nuevo seguidor",
          body: `${followerName} te ha comenzado a seguir.`,
        },
        data: {
          type: "new_follower",
          followerId,
        },
      };

      const response = await admin.messaging().send(message);
      logger.info(`FCM sent: ${response}`);
      return null;
    } catch (err) {
      logger.error("Error sending follower notification", err);
      return null;
    }
  });
