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
        return db.collection("reviews").get().await().toObjects(ReviewDto::class.java)
    }

    override suspend fun getReviewById(
        id: String,
        currentUserId: String
    ): ReviewDto {
        TODO("Not yet implemented")
    }

    override suspend fun getReviewsByUserId(userId: String): List<ReviewDto> {
        TODO("Not yet implemented")
    }

    override suspend fun createReview(review: CreateReviewDto) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReview(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateReview(
        id: String,
        review: CreateReviewDto
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getReviewReplies(id: String): List<ReviewDto> {
        TODO("Not yet implemented")
    }

    override suspend fun sendOrDeleteLike(reviewId: String, liked: Boolean) {
        TODO("Not yet implemented")
    }

    override fun listenAllReviews(): Flow<List<ReviewInfo>> {
        TODO("Not yet implemented")
    }
}