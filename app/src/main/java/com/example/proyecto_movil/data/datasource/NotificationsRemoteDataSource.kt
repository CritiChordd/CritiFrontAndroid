package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.NotificationInfo
import kotlinx.coroutines.flow.Flow

interface NotificationsRemoteDataSource {
    fun listenUserNotifications(userId: String): Flow<List<NotificationInfo>>

    suspend fun addFollowNotification(
        userId: String,
        followerId: String,
        followerName: String,
        followerAvatarUrl: String
    )
}

