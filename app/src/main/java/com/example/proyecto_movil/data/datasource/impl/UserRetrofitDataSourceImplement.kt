package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.datasource.UserRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.UserRetrofitService
import com.example.proyecto_movil.data.dtos.RegisterUserDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.example.proyecto_movil.data.dtos.UpdateUserDto
import com.example.proyecto_movil.data.dtos.UserProfileDto
import javax.inject.Inject

/**
 * Implementación de UserRemoteDataSource que usa Retrofit
 * para comunicarse con la API del backend.
 */
class UserRetrofitDataSourceImpl @Inject constructor(
    private val service: UserRetrofitService
) : UserRemoteDataSource {

    /** Obtiene un usuario por ID */
    override suspend fun getUserById(id: String): UserProfileDto = service.getUserById(id)

    /** Obtiene las reseñas de un usuario */
    override suspend fun getUserReviews(id: String): List<ReviewDto> = service.getUserReviews(id)

    /** Actualiza la información de un usuario */
    override suspend fun updateUser(id: String, userDto: UpdateUserDto): UserProfileDto =
        service.updateUser(id, userDto)

    override suspend fun registerUser(registerUserDto: RegisterUserDto, userId: String){
        //to-do
    }

    override suspend fun followOrUnfollowUser(
        currentUserId: String,
        targetUserId: String
    ) {
        TODO("Not yet implemented")
    }

}
