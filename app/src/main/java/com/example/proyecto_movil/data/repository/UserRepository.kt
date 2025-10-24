package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.datasource.impl.firestore.UserFirestoreDataSourceImpl
import com.example.proyecto_movil.data.datasource.impl.retrofit.UserRetrofitDataSourceImpl
import com.example.proyecto_movil.data.dtos.UpdateUserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
        profilePic: String = "",
        name: String? = null
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
                profilePic = profilePic.ifBlank { "" },
                name = name
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
            try {
                val dto = UpdateUserDto(username = username, bio = bio, profile_pic = profilePic)
                userRemoteDataSource.updateUser(id = id, userDto = dto) // ← FIX
            } catch (_: Throwable) { /* ignorado intencionalmente */ }

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

    suspend fun searchUsersByName(query: String, limit: Int = 10): Result<List<UserInfo>> = withContext(Dispatchers.IO) {
        try {
            val users = userFirestoreDataSource.searchUsersByName(query, limit.toLong())
            Result.success(users)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    suspend fun isFollowing(currentUserId: String, targetUserId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(userFirestoreDataSource.isFollowing(currentUserId, targetUserId))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

    suspend fun followUser(currentUserId: String, targetUserId: String): Result<UserInfo> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(userFirestoreDataSource.followUser(currentUserId, targetUserId))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<UserInfo> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(userFirestoreDataSource.unfollowUser(currentUserId, targetUserId))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }


    fun listenFollowers(userId: String): Flow<List<UserInfo>> =
        userFirestoreDataSource.listenFollowers(userId)

    fun listenFollowing(userId: String): Flow<List<UserInfo>> =
        userFirestoreDataSource.listenFollowing(userId)
}
