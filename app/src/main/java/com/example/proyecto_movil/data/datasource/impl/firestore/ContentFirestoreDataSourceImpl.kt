package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.datasource.ContentRemoteDataSource
import com.example.proyecto_movil.data.dtos.ContentDto
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await


class ContentFirestoreDataSourceImpl(
    private val db: FirebaseFirestore
) : ContentRemoteDataSource {

    private fun contentCollection() = db.collection("content")

    override suspend fun sendLikeOrDislike(contentId: String, userId: String): Boolean {
        val contentRef = contentCollection().document(contentId)
        val likeRef = contentRef.collection("likes").document(userId)

        // Return value indicates whether it's now liked (true) or unliked (false)
        return db.runTransaction { txn ->
            val contentSnap = txn.get(contentRef)
            if (!contentSnap.exists()) {
                throw IllegalStateException("Content not found: $contentId")
            }

            val likeSnap = txn.get(likeRef)
            val currentCount = (contentSnap.getLong("likes_count") ?: 0L).toInt()

            if (likeSnap.exists()) {
                txn.delete(likeRef as DocumentReference)
                txn.update(contentRef, mapOf("likes_count" to (currentCount - 1).coerceAtLeast(0)))
                false
            } else {
                txn.set(likeRef, mapOf("userId" to userId, "createdAt" to System.currentTimeMillis()))
                txn.update(contentRef, mapOf("likes_count" to currentCount + 1))
                true
            }
        }.await()
    }

    override fun listenAllContent(): Flow<List<ContentDto>> = callbackFlow {
        val query = contentCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snap, err ->
            if (err != null) {
                // Surface empty list on error for simplicity
                trySend(emptyList()).isSuccess
                return@addSnapshotListener
            }
            val list = snap?.documents?.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                ContentDto(
                    id = doc.id,
                    authorId = data["authorId"]?.toString().orEmpty(),
                    text = data["text"]?.toString().orEmpty(),
                    likesCount = (data["likes_count"] as? Number)?.toInt() ?: 0,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                )
            } ?: emptyList()
            trySend(list).isSuccess
        }

        awaitClose { listener.remove() }
    }

    override fun listenContentById(contentId: String): Flow<ContentDto?> = callbackFlow {
        val ref = contentCollection().document(contentId)
        val listener = ref.addSnapshotListener { doc, err ->
            if (err != null) {
                trySend(null).isSuccess
                return@addSnapshotListener
            }
            if (doc != null && doc.exists()) {
                val data = doc.data ?: emptyMap<String, Any>()
                val dto = ContentDto(
                    id = doc.id,
                    authorId = data["authorId"]?.toString().orEmpty(),
                    text = data["text"]?.toString().orEmpty(),
                    likesCount = (data["likes_count"] as? Number)?.toInt() ?: 0,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
                )
                trySend(dto).isSuccess
            } else {
                trySend(null).isSuccess
            }
        }
        awaitClose { listener.remove() }
    }

    override suspend fun isLiked(contentId: String, userId: String): Boolean {
        val ref = contentCollection().document(contentId).collection("likes").document(userId)
        val snap = ref.get().await()
        return snap.exists()
    }

    override suspend fun getContentById(contentId: String): ContentDto? {
        val doc = contentCollection().document(contentId).get().await()
        if (!doc.exists()) return null
        val data = doc.data ?: return null
        return ContentDto(
            id = doc.id,
            authorId = data["authorId"]?.toString().orEmpty(),
            text = data["text"]?.toString().orEmpty(),
            likesCount = (data["likes_count"] as? Number)?.toInt() ?: 0,
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L
        )
    }
}
