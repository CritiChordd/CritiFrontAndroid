package com.example.proyecto_movil.data


data class ReviewInfo(
    val id: String = "",
    val content: String = "",
    val score: Double = 0.0,
    val isLowScore: Boolean = false,
    val albumId: Int = 0,
    val userId: String = "",
    val firebaseUserId: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val liked: Boolean = false
)
