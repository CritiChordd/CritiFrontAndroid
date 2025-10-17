package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.NotificationInfo
import com.example.proyecto_movil.data.datasource.NotificationsRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NotificationsRepository @Inject constructor(
    private val ds: NotificationsRemoteDataSource
) {
    fun listenUserNotifications(userId: String): Flow<List<NotificationInfo>> =
        ds.listenUserNotifications(userId)

    suspend fun addFollowNotification(
        userId: String,
        followerId: String,
        followerName: String,
        followerAvatarUrl: String
    ) = ds.addFollowNotification(userId, followerId, followerName, followerAvatarUrl)

    suspend fun addLikeNotification(
        userId: String,
        reviewId: String,
        likerId: String,
        likerName: String,
        likerAvatarUrl: String,
        reviewSnippet: String?
    ) = ds.addLikeNotification(userId, reviewId, likerId, likerName, likerAvatarUrl, reviewSnippet)
}
