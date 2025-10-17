package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.datasource.UserRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.UserRetrofitService
import com.example.proyecto_movil.data.dtos.RegisterUserDto
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.example.proyecto_movil.data.dtos.UpdateUserDto
import com.example.proyecto_movil.data.dtos.toUserUI
import javax.inject.Inject

/**
 * Implementación de UserRemoteDataSource que usa Retrofit
 * para comunicarse con la API del backend.
 */
class UserRetrofitDataSourceImpl @Inject constructor(
    private val service: UserRetrofitService
) : UserRemoteDataSource {

    /** Obtiene un usuario por ID */
    override suspend fun getUserById(id: String): UserInfo {
        return service.getUserById(id).toUserUI()
    }

    /** Obtiene las reseñas de un usuario */
    override suspend fun getUserReviews(id: String): List<ReviewDto> {
        return service.getUserReviews(id)
    }

    /** Actualiza la información de un usuario */
    override suspend fun updateUser(id: String, userDto: UpdateUserDto): UserInfo {
        val dto = service.updateUser(id, userDto)
        return dto.toUserUI()
    }

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
