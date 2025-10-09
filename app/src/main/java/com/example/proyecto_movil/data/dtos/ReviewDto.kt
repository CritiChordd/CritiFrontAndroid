package com.example.proyecto_movil.data.dtos

import com.example.proyecto_movil.data.ReviewInfo

data class ReviewDto(
    val id: String = "",
    val content: String = "",
    val score: Double = 0.0,
    val is_low_score: Boolean = false,
    val album_id: Int = 0,
    val user_id: String = "",
    val firebase_user_id: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val is_favorite: Boolean = false
)


fun ReviewDto.toReviewInfo(): ReviewInfo {
    return ReviewInfo(
        id = id,
        content = content,
        score = score,
        isLowScore = is_low_score,
        albumId = album_id,
        userId = user_id,
        firebaseUserId = firebase_user_id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        liked = false,
        isFavorite = is_favorite
    )
}
