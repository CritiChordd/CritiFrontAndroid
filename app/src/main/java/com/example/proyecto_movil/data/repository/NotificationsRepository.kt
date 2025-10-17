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
}

