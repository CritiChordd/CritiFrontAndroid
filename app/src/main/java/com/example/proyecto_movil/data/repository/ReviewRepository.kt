package com.example.proyecto_movil.data.repository

import coil.network.HttpException
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.datasource.ReviewRemoteDataSource
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.toReviewInfo
import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val reviewRemoteDataSource: ReviewRemoteDataSource
) {
    suspend fun getReviewsByUserId(userId: String): Result<List<ReviewInfo>> = try {
        Result.success(reviewRemoteDataSource.getReviewsByUserId(userId).map { it.toReviewInfo() })
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getAllReviews(): Result<List<ReviewInfo>> {
        return try {
            val reviews = reviewRemoteDataSource.getAllReviews()
            val reviewInfos = reviews.map { it.toReviewInfo() }
            Result.success(reviewInfos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getReviewsByAlbumId(albumId: Int): Result<List<ReviewInfo>> {
        return try {
            val reviews = reviewRemoteDataSource.getReviewsByAlbumId(albumId.toString())
            val reviewsInfo = reviews.map { it.toReviewInfo() }
            Result.success(reviewsInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun createReview(
        content: String,
        score: Int,
        albumId: Int,
        userId: String?,
        firebaseUserId: String,
        isFavorite: Boolean
    ): Result<Unit> = try {
        val now = System.currentTimeMillis().toString()
        val dto = CreateReviewDto(
            content = content,
            score = score,
            is_low_score = score < 5,
            album_id = albumId.toString(),
            user_id = userId ?: "",
            firebase_user_id = firebaseUserId,
            createdAt = now,
            updatedAt = now,
            is_favorite = isFavorite
        )
        reviewRemoteDataSource.createReview(dto)
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun getReviewById(id: String): Result<ReviewInfo> = try {
        Result.success(reviewRemoteDataSource.getReviewById(id).toReviewInfo())
    } catch (e: HttpException) { Result.failure(e) }
}
