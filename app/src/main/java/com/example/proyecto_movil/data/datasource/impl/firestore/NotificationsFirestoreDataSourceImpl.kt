package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.NotificationInfo
import com.example.proyecto_movil.data.datasource.NotificationsRemoteDataSource
import com.google.firebase.firestore.FirebaseFirestore
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
                    createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()?.time ?: 0L,
                    read = (data["read"] as? Boolean) ?: false,
                )
            } ?: emptyList()
            trySend(list).isSuccess
        }

        awaitClose { reg.remove() }
    }
}

