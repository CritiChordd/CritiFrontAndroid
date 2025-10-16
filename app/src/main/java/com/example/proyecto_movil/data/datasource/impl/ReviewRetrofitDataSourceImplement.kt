package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.ReviewRetrofitService
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Implementación del ReviewRemoteDataSource que usa Retrofit
 * para comunicarse con la API remota.
 */
class ReviewRetrofitDataSourceImplement @Inject constructor(
    private val service: ReviewRetrofitService
) : ReviewRemoteDataSource {

    override suspend fun getAllReviews(): List<ReviewDto> {
        return service.getAllReviews()
    }

    override suspend fun getReviewById(id: String, currentUserId: String): ReviewDto {
        return service.getReviewById(id)
    }

    override suspend fun getReviewsByUserId(userId: String): List<ReviewDto> {
        return service.getReviewsByUserId(userId)
    }


    override suspend fun createReview(review: CreateReviewDto) {
        service.createReview(review)
    }

    override suspend fun deleteReview(id: String) {
        service.deleteReview(id)
    }

    override suspend fun updateReview(id: String, review: CreateReviewDto) {
        service.updateReview(id, review)
    }

    override suspend fun getReviewReplies(id: String): List<ReviewDto> {
        return service.getReviewReplies(id)
    }

    override suspend fun sendOrDeleteLike(reviewId: String, liked: Boolean) {
        // TODO: implementar cuando el endpoint esté disponible
    }

    override fun listenAllReviews(): Flow<List<ReviewInfo>> {
        // TODO: implementar si se usa un flujo en tiempo real (WebSocket / Firebase)
        throw NotImplementedError("listenAllReviews() not yet implemented")
    }
}
