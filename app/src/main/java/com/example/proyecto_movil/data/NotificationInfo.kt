package com.example.proyecto_movil.data

data class NotificationInfo(
    val id: String,
    val type: String,
    val reviewId: String? = null,
    val likerId: String? = null,
    val likerName: String? = null,
    val reviewSnippet: String? = null,
    val createdAt: Long = 0L,
    val read: Boolean = false,
)

