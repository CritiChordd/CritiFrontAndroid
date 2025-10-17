package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.NotificationInfo
import com.example.proyecto_movil.data.datasource.NotificationsRemoteDataSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationsFirestoreDataSourceImpl(
    private val db: FirebaseFirestore
) : NotificationsRemoteDataSource {

    override fun listenUserNotifications(userId: String): Flow<List<NotificationInfo>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref = db.collection("users").document(userId)
            .collection("notifications")
            .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)

        val reg = ref.addSnapshotListener { snap, err ->
            if (err != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snap?.documents?.map { d ->
                val data = d.data ?: emptyMap()
                NotificationInfo(
                    id = d.id,
                    type = data["type"]?.toString() ?: "",
                    reviewId = data["reviewId"]?.toString(),
                    likerId = data["likerId"]?.toString(),
                    likerName = data["likerName"]?.toString(),
                    reviewSnippet = data["reviewSnippet"]?.toString(),
                    actorId = data["actorId"]?.toString(),
                    actorName = data["actorName"]?.toString(),
                    actorImageUrl = data["actorImageUrl"]?.toString(),
                    message = data["message"]?.toString(),
                    createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L,
                    read = (data["read"] as? Boolean) ?: false,
                )
            } ?: emptyList()
            trySend(list).isSuccess
        }

        awaitClose { reg.remove() }
    }

    override suspend fun addFollowNotification(
        userId: String,
        followerId: String,
        followerName: String,
        followerAvatarUrl: String
    ) {
        if (userId.isBlank() || followerId.isBlank()) return

        val notificationRef = db.collection("users")
            .document(userId)
            .collection("notifications")
            .document("follow_$followerId")

        val data = mapOf(
            "type" to "follow",
            "actorId" to followerId,
            "actorName" to followerName,
            "actorImageUrl" to followerAvatarUrl,
            "message" to "$followerName te empezó a seguir",
            "createdAt" to FieldValue.serverTimestamp(),
            "read" to false,
        )

        notificationRef.set(data, SetOptions.merge()).await()
    }

    override suspend fun addLikeNotification(
        userId: String,
        reviewId: String,
        likerId: String,
        likerName: String,
        likerAvatarUrl: String,
        reviewSnippet: String?
    ) {
        if (userId.isBlank() || reviewId.isBlank() || likerId.isBlank()) return

        val notificationRef = db.collection("users")
            .document(userId)
            .collection("notifications")
            .document("like_${'$'}reviewId_${'$'}likerId")

        val data = mapOf(
            "type" to "review_like",
            "reviewId" to reviewId,
            "likerId" to likerId,
            "likerName" to likerName,
            "actorId" to likerId,
            "actorName" to likerName,
            "actorImageUrl" to likerAvatarUrl,
            "reviewSnippet" to (reviewSnippet ?: ""),
            "createdAt" to FieldValue.serverTimestamp(),
            "read" to false,
        )

        notificationRef.set(data, SetOptions.merge()).await()
    }
}
