package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.dtos.ContentDto
import kotlinx.coroutines.flow.Flow

interface ContentRemoteDataSource {
    suspend fun sendLikeOrDislike(contentId: String, userId: String): Boolean
    fun listenAllContent(): Flow<List<ContentDto>>
    fun listenContentById(contentId: String): Flow<ContentDto?>
    suspend fun isLiked(contentId: String, userId: String): Boolean
    suspend fun getContentById(contentId: String): ContentDto?
}
