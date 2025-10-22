const functions = require("firebase-functions");
const admin = require("firebase-admin");

try {
  admin.initializeApp();
} catch (e) {
  // ignore if already initialized
}

// Envía una notificación al autor de la reseña cuando alguien da like
exports.sendLikeNotification = functions.firestore
  .document("reviews/{reviewId}/likes/{userId}")
  .onCreate(async (snap, context) => {
    const { reviewId, userId: likerId } = context.params;

    // Leer la reseña para conocer el autor
    const reviewRef = admin.firestore().collection("reviews").doc(reviewId);
    const reviewDoc = await reviewRef.get();
    if (!reviewDoc.exists) {
      console.log("Review no encontrada:", reviewId);
      return null;
    }

    const review = reviewDoc.data() || {};
    const authorUid = review.firebase_user_id || review.user_id || null;
    if (!authorUid) {
      console.log("La reseña no tiene firebase_user_id, no se envía notificación");
      return null;
    }

    // Evitar notificar si el autor se da like a sí mismo
    if (authorUid === likerId) {
      console.log("Autor y liker son el mismo usuario, no se notifica");
      return null;
    }

    // Buscar token del autor y datos del liker
    const [userDoc, likerDoc] = await Promise.all([
      admin.firestore().collection("users").doc(authorUid).get(),
      admin.firestore().collection("users").doc(likerId).get(),
    ]);
    if (!userDoc.exists) {
      console.log("Usuario autor no encontrado:", authorUid);
      return null;
    }

    const token = userDoc.get("fcmToken");
    const likerName =
      (likerDoc.exists && (likerDoc.get("name") || likerDoc.get("username"))) ||
      "Alguien";
    const likerAvatarUrl =
      (likerDoc.exists &&
        (likerDoc.get("profileImageUrl") ||
          likerDoc.get("avatarUrl") ||
          likerDoc.get("photoUrl"))) ||
      "";
    const reviewSnippet = (review.content || "").toString().slice(0, 50);
    const message = reviewSnippet
      ? `${likerName} le dio like a tu reseña\n"${reviewSnippet}"`
      : `${likerName} le dio like a tu reseña`;
    if (!token) {
      console.log("Usuario sin fcmToken, no se envía notificación");
      return null;
    }

    const payload = {
      token,
      notification: {
        title: "Nuevo Like",
        body: `${likerName} le dio like a tu reseña`,
      },
      data: {
        type: "review_like",
        reviewId: reviewId,
        likerId: likerId,
        reviewSnippet: reviewSnippet,
        likerName: likerName,
        likerAvatarUrl: likerAvatarUrl,
      },
    };

    try {
      const resp = await admin.messaging().send(payload);
      console.log("Notificación enviada:", resp);
    } catch (err) {
      console.error("Error enviando notificación:", err);
    }
    // Crear documento de notificación para listar en la app
    try {
      await admin.firestore()
        .collection("users").doc(authorUid)
        .collection("notifications")
        .add({
          type: "review_like",
          reviewId: reviewId,
          likerId: likerId,
          likerName: likerName,
          likerAvatarUrl: likerAvatarUrl,
          actorId: likerId,
          actorName: likerName,
          actorImageUrl: likerAvatarUrl,
          reviewSnippet: reviewSnippet,
          message: message,
          createdAt: admin.firestore.FieldValue.serverTimestamp(),
          read: false,
        });
    } catch (err) {
      console.error("Error creando documento de notificación:", err);
    }

    return null;
  });
