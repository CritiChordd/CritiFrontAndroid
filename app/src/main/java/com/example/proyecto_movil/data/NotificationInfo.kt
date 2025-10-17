package com.example.proyecto_movil.data

data class NotificationInfo(
    val id: String,
    val type: String,
    val reviewId: String? = null,
    val likerId: String? = null,
    val likerName: String? = null,
    val likerAvatarUrl: String? = null,
    val reviewSnippet: String? = null,
    val actorId: String? = null,
    val actorName: String? = null,
    val actorImageUrl: String? = null,
    val message: String? = null,
    val createdAt: Long = 0L,
    val read: Boolean = false,
)

