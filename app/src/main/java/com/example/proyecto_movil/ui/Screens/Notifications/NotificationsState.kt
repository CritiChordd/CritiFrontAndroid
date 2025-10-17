package com.example.proyecto_movil.ui.Screens.Notifications

import com.example.proyecto_movil.data.NotificationInfo

data class NotificationsState(
    val isLoading: Boolean = true,
    val items: List<NotificationInfo> = emptyList(),
)