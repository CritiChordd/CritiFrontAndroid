package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.ContentInfo
import com.example.proyecto_movil.data.datasource.ContentRemoteDataSource
import com.example.proyecto_movil.data.dtos.ContentDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ContentRepository(
    private val remote: ContentRemoteDataSource
) {
    private fun ContentDto.toInfo() = ContentInfo(
        id = id,
        authorId = authorId,
        text = text,
        likesCount = likesCount,
        createdAt = createdAt
    )

    fun listenAllContent(): Flow<List<ContentInfo>> =
        remote.listenAllContent().map { list -> list.map { it.toInfo() } }

    fun listenContentById(contentId: String): Flow<ContentInfo?> =
        remote.listenContentById(contentId).map { it?.toInfo() }

    suspend fun toggleLike(contentId: String, userId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val likedNow = remote.sendLikeOrDislike(contentId, userId)
                Result.success(likedNow)
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

    suspend fun isLiked(contentId: String, userId: String): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(remote.isLiked(contentId, userId))
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }

    suspend fun getContentById(contentId: String): Result<ContentInfo?> =
        withContext(Dispatchers.IO) {
            try {
                Result.success(remote.getContentById(contentId)?.toInfo())
            } catch (e: Throwable) {
                Result.failure(e)
            }
        }
}
