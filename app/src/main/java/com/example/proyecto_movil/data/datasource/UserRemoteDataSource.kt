package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.dtos.RegisterUserDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.example.proyecto_movil.data.dtos.UpdateUserDto


interface UserRemoteDataSource {
    suspend fun getUserById(id: String): UserInfo
    suspend fun getUserReviews(id: String): List<ReviewDto>

    suspend fun updateUser(id: String, userDto: UpdateUserDto): UserInfo

    suspend fun registerUser(registerUserDto: RegisterUserDto, userId: String): Unit
}
