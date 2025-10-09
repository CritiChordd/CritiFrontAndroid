package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class ReviewFirestoreDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
): ReviewRemoteDataSource {
    override suspend fun getAllReviews(): List<ReviewDto> {
        val snapshot = db.collection("reviews").get().await()
        return snapshot.documents.map { doc ->
            val review = doc.toObject(ReviewDto::class.java)
            review?.copy(id = doc.id) ?: throw Exception("Review not found")

        }
    }

    override suspend fun getReviewById(id: String, currentUserId: String): ReviewDto {
        return db.collection("reviews").document(id).get().await().toObject(ReviewDto::class.java)
            ?: throw Exception("Review not found")
    }

    override suspend fun getReviewsByUserId(userId: String): List<ReviewDto> {
        TODO("Not yet implemented")
    }

    override suspend fun createReview(review: CreateReviewDto) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReview(id: String): Unit{
        db.collection("reviews").document(id).delete().await()
        val repliesSnapshot = db.collection("reviews").whereEqualTo("parentReviewId", id).get().await()
        for (doc in repliesSnapshot.documents){
            deleteReview(doc.id)
        }

    }

    override suspend fun updateReview(id: String, review: CreateReviewDto) {
         db.collection("reviews").document(id).set(review).await()


    }

     override suspend fun getReviewReplies(id: String): List<ReviewDto> {
        val snapshot = db.collection("reviews").whereEqualTo("parentReviewId", id).get().await()

                return snapshot.documents.map { doc ->
                    val review = doc.toObject(ReviewDto::class.java)
                    review?.copy(id = doc.id) ?: throw Exception("Review not found")
                }
            }



    override suspend fun sendOrDeleteLike(reviewId: String, liked: Boolean) {
        TODO("Not yet implemented")
    }

    override fun listenAllReviews(): Flow<List<ReviewInfo>> {
        TODO("Not yet implemented")
    }
}