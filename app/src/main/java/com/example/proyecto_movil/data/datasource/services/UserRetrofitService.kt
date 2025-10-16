package com.example.proyecto_movil.data.datasource.services

import com.example.proyecto_movil.data.dtos.CreateReviewDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.example.proyecto_movil.data.dtos.UpdateUserDto
import com.example.proyecto_movil.data.dtos.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserRetrofitService {

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") userId: String): UserProfileDto

    @GET("users/{id}/reviews")
    suspend fun getUserReviews(@Path("id") userId: String): List<ReviewDto>

    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") userId: String,
        @Body body: UpdateUserDto
    ): UserProfileDto


}
