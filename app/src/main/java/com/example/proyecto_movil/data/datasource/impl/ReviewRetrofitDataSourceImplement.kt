package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.ReviewRetrofitService
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject

class ReviewRetrofitDataSourceImplement @Inject constructor(
    private val service: ReviewRetrofitService
) : ReviewRemoteDataSource {

    override suspend fun getAllReviews(): List<ReviewDto> =
        service.getAllReviews()

    override suspend fun getReviewById(id: String, currentUserId: String): ReviewDto =
        service.getReviewById(id)

    override suspend fun getReviewsByUserId(userId: String): List<ReviewDto> =
        service.getReviewsByUserId(userId)

    // ✅ implementación real por álbum
    override suspend fun getReviewsByAlbumId(albumId: String): List<ReviewDto> =
        service.getReviewsByAlbumId(albumId)

    override suspend fun createReview(review: CreateReviewDto) =
        service.createReview(review)

    override suspend fun deleteReview(id: String) =
        service.deleteReview(id)

    override suspend fun updateReview(id: String, review: CreateReviewDto) =
        service.updateReview(id, review)

    override suspend fun getReviewReplies(id: String): List<ReviewDto> =
        service.getReviewReplies(id)

    override suspend fun sendOrDeleteLike(reviewId: String, liked: Boolean) {
        // si más adelante agregas like/unlike en el backend, conéctalo aquí
        throw UnsupportedOperationException("Not implemented for Retrofit yet")
    }

    override fun listenAllReviews(): Flow<List<ReviewInfo>> = emptyFlow()
}
