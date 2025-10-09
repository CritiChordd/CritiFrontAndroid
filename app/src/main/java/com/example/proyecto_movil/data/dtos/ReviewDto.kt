package com.example.proyecto_movil.data.dtos

import com.example.proyecto_movil.data.ReviewInfo

data class ReviewDto(
    val id: String,
    val content: String,
    val score: Double,
    val is_low_score: Boolean,
    val album_id: Int,
    val user_id: Int,
    val createdAt: String,
    val updatedAt: String
) {
    constructor() : this("", "", 0.0, false, 0, 0, "", "")
}


fun ReviewDto.toReviewInfo(): ReviewInfo {
    return ReviewInfo(
        id = id,
        content = content,
        score = score,
        isLowScore = is_low_score,
        albumId = album_id,
        userId = user_id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        liked = false
    )
}
