package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue

class ReviewFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
): ReviewRemoteDataSource {
    override suspend fun getAllReviews(): List<ReviewDto> {
        val snapshot = db.collection("reviews").get().await()
        return snapshot.documents.mapNotNull { it.toReviewDtoOrNull() }
    }

    override suspend fun getReviewById(id: String, currentUserId: String): ReviewDto {
        val doc = db.collection("reviews").document(id).get().await()
        return doc.toReviewDtoOrNull() ?: throw Exception("Review not found")
    }

    override suspend fun getReviewsByUserId(userId: String): List<ReviewDto> {
        if (userId.isBlank()) return emptyList()

        val backendSnapshot = db.collection("reviews")
            .whereEqualTo("user_id", userId)
            .get()
            .await()

        val firebaseSnapshot = db.collection("reviews")
            .whereEqualTo("firebase_user_id", userId)
            .get()
            .await()

        return (backendSnapshot.documents + firebaseSnapshot.documents)
            .distinctBy { it.id }
            .mapNotNull { it.toReviewDtoOrNull() }
    }

    override suspend fun getReviewsByAlbumId(albumId: String): List<ReviewDto> {
        val albumQueryValue: Any = albumId.toIntOrNull() ?: albumId
        val snapshot = db.collection("reviews")
            .whereEqualTo("album_id", albumQueryValue)
            .get()
            .await()
        return snapshot.documents.mapNotNull { it.toReviewDtoOrNull() }
    }

    override suspend fun createReview(review: CreateReviewDto) {
        val collection = db.collection("reviews")
        val docRef = collection.document()
        val now = System.currentTimeMillis().toString()
        val createdAt = review.createdAt.ifBlank { now }
        val updatedAt = review.updatedAt.ifBlank { now }
        val albumIdValue: Any = review.album_id.toIntOrNull() ?: review.album_id

        val payload = mapOf(
            "id" to docRef.id,
            "content" to review.content,
            "score" to review.score.toDouble(),
            "is_low_score" to review.is_low_score,
            "album_id" to albumIdValue,
            "user_id" to review.user_id,
            "firebase_user_id" to review.firebase_user_id,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "is_favorite" to review.is_favorite
        )

        docRef.set(payload).await()
    }

    override suspend fun deleteReview(id: String): Unit{
        db.collection("reviews").document(id).delete().await()
        val repliesSnapshot = db.collection("reviews").whereEqualTo("parentReviewId", id).get().await()
        for (doc in repliesSnapshot.documents){
            deleteReview(doc.id)
        }

    }

    override suspend fun updateReview(id: String, review: CreateReviewDto) {
        val now = System.currentTimeMillis().toString()
        val albumIdValue: Any = review.album_id.toIntOrNull() ?: review.album_id
        val payload = mapOf(
            "content" to review.content,
            "score" to review.score.toDouble(),
            "is_low_score" to review.is_low_score,
            "album_id" to albumIdValue,
            "user_id" to review.user_id,
            "firebase_user_id" to review.firebase_user_id,
            "createdAt" to review.createdAt.ifBlank { now },
            "updatedAt" to review.updatedAt.ifBlank { now },
            "is_favorite" to review.is_favorite
        )
        db.collection("reviews").document(id).set(payload).await()
    }

     override suspend fun getReviewReplies(id: String): List<ReviewDto> {
        val snapshot = db.collection("reviews").whereEqualTo("parentReviewId", id).get().await()

        return snapshot.documents.mapNotNull { it.toReviewDtoOrNull() }
    }

    override fun listenAllReviews(): Flow<List<ReviewDto>> {
        return callbackFlow {
            val registration = db.collection("reviews").addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val reviews = snapshot?.documents
                    ?.mapNotNull { document -> document.toReviewDtoOrNull() }
                    .orEmpty()

                trySend(reviews).isSuccess
            }

            awaitClose { registration.remove() }
        }
    }

    override suspend fun sendOrDeleteReviewLike(reviewId: String, userId: String) {
        val reviewRef = db.collection("reviews").document(reviewId)
        val likeRef = reviewRef.collection("likes").document(userId)

        val likeDoc = likeRef.get().await()
        if (likeDoc.exists()) {
            likeRef.delete().await()
        } else {
            likeRef.set(mapOf("timestamp" to FieldValue.serverTimestamp())).await()
        }
    }


    private fun DocumentSnapshot.toReviewDtoOrNull(): ReviewDto? {
        val base = this.toObject(ReviewDto::class.java) ?: return null
        val data = data ?: emptyMap<String, Any?>()

        val resolvedAlbumId = when (val raw = data["album_id"]) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull() ?: base.album_id
            else -> base.album_id
        }

        val resolvedUserId = base.user_id.takeIf { it.isNotBlank() }
            ?: data["user_id"]?.toString().orEmpty()

        val resolvedFirebaseId = base.firebase_user_id ?: data["firebase_user_id"]?.toString()

        val resolvedCreatedAt = base.createdAt.takeIf { it.isNotBlank() }
            ?: data["createdAt"]?.toString().orEmpty()

        val resolvedUpdatedAt = base.updatedAt.takeIf { it.isNotBlank() }
            ?: data["updatedAt"]?.toString().orEmpty()

        val resolvedFavorite = when (val rawFavorite = data["is_favorite"]) {
            is Boolean -> rawFavorite
            is Number -> rawFavorite.toInt() != 0
            is String -> rawFavorite.equals("true", ignoreCase = true) || rawFavorite == "1"
            else -> base.is_favorite
        }

        // 👇 Agrega este bloque antes del return
        val resolvedLikesCount = when (val raw = data["likesCount"]) {
            is Number -> raw.toInt()
            is String -> raw.toIntOrNull() ?: 0
            else -> base.likesCount // usa el valor por defecto del DTO si no existe
        }

        return base.copy(
            id = id,
            album_id = resolvedAlbumId,
            user_id = resolvedUserId,
            firebase_user_id = resolvedFirebaseId,
            createdAt = resolvedCreatedAt,
            updatedAt = resolvedUpdatedAt,
            is_favorite = resolvedFavorite,
            likesCount = resolvedLikesCount // 👈 ahora sí existe
        )
    }

}
