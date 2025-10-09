package com.example.proyecto_movil.data.datasource.services

import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import retrofit2.http.*

interface ReviewRetrofitService {

    @GET("reviews")
    suspend fun getAllReviews(): List<ReviewDto>

    @GET("reviews/{id}")
    suspend fun getReviewById(@Path("id") id: String): ReviewDto

    @GET("reviews/user/{userId}")
    suspend fun getReviewsByUserId(@Path("userId") userId: String): List<ReviewDto>


    @GET("reviews/album/{albumId}")
    suspend fun getReviewsByAlbumId(@Path("albumId") albumId: String): List<ReviewDto>

    @POST("reviews")
    suspend fun createReview(@Body review: CreateReviewDto)

    @PUT("reviews/{id}")
    suspend fun updateReview(@Path("id") id: String, @Body review: CreateReviewDto)

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: String)

    @GET("reviews/{id}/replies")
    suspend fun getReviewReplies(@Path("id") id: String): List<ReviewDto>
}
