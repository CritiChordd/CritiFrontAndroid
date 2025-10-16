package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.datasource.impl.firestore.UserFirestoreDataSourceImpl
import com.example.proyecto_movil.data.datasource.impl.retrofit.UserRetrofitDataSourceImpl
import com.example.proyecto_movil.data.dtos.UpdateUserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val userRemoteDataSource: UserRetrofitDataSourceImpl,
    private val userFirestoreDataSource: UserFirestoreDataSourceImpl
) {

    suspend fun registerUser(
        id: String,
        username: String,
        email: String,
        bio: String = "",
        profilePic: String = ""            // ← no-null para evitar error al llamar
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // (Opcional) crear en API
            // userRemoteDataSource.createUser(UserCreateDto(...))

            // Crear en Firestore
            userFirestoreDataSource.createOrUpdateUser(
                id = id,
                username = username,
                email = email,
                bio = bio,
                profilePic = profilePic.ifBlank { "" }
            )
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(id: String): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            val user = userFirestoreDataSource.getUserById(id)
            Result.success(user)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(
        id: String,
        username: String,
        bio: String,
        profilePic: String? = null
    ): Result<UserInfo> = withContext(Dispatchers.IO) {
        try {
            // (Opcional) API – corrige el nombre del parámetro: userDto
            runCatching {
                val dto = UpdateUserDto(username = username, bio = bio, profile_pic = profilePic)
                userRemoteDataSource.updateUser(id = id, userDto = dto) // ← FIX
            }

            // Firestore
            val updated = userFirestoreDataSource.updateUser(
                id = id,
                username = username,
                bio = bio,
                profilePic = profilePic
            )
            Result.success(updated)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun saveFcmToken(userId: String, token: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userFirestoreDataSource.saveFcmToken(userId, token)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun followUser(followerId: String, targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userFirestoreDataSource.followUser(followerId, targetUserId)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(followerId: String, targetUserId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            userFirestoreDataSource.unfollowUser(followerId, targetUserId)
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
