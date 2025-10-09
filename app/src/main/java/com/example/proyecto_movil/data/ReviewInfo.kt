package com.example.proyecto_movil.data


data class ReviewInfo(
    val id: String,
    val content: String,
    val score: Double,
    val isLowScore: Boolean,
    val albumId: Int,
    val userId: Int,
    val createdAt: String,
    val updatedAt: String,
    val liked: Boolean = false
) {
    constructor() : this("", "", 0.0, false, 0, 0, "", "", false)
}
