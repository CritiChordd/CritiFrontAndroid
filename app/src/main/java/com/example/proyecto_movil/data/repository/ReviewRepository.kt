package com.example.proyecto_movil.data.repository

import coil.network.HttpException
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.datasource.impl.retrofit.ReviewRetrofitDataSourceImplement
import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.toReviewInfo

import javax.inject.Inject

class ReviewRepository @Inject constructor(
    private val reviewRemoteDataSource: ReviewRetrofitDataSourceImplement)
{
    suspend fun getReviewsByUserId(userId: String): Result<List<ReviewInfo>> {
        return try {
            val reviews = reviewRemoteDataSource.getReviewsByUserId(userId)
            val reviewsInfo = reviews.map { it.toReviewInfo() }
            Result.success(reviewsInfo)
        } catch (e: HttpException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun createReview(
        content: String,
        score: Int,
        albumId: String,
        userId: String
    ): Result<Unit> {
        return try {
            val boundedScore = score.coerceIn(0, 10)
            val isLowScore = boundedScore < 5

            val createReviewDto = CreateReviewDto(
                content = content,
                score = boundedScore,
                is_low_score = isLowScore,
                album_id = albumId,
                user_id = userId
            )

            reviewRemoteDataSource.createReview(createReviewDto)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getReviewbyId(id: String): Result<ReviewInfo>{
        return try {
            val review = reviewRemoteDataSource.getReviewById(id)
            val reviewInfo = review.toReviewInfo()
            Result.success(reviewInfo)
        }catch(e: HttpException){
            Result.failure(e)
        }
    }

    suspend fun getReviewsByAlbumId(albumId: Int): Result<List<ReviewInfo>> {
    return try {
        // Cuando implementes en tu DataSource, debería ser algo así:
        val reviews = reviewRemoteDataSource.getReviewsByUserId(albumId.toString())
        val reviewsInfo = reviews.map { it.toReviewInfo() }
        Result.success(reviewsInfo)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

}
