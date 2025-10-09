package com.example.proyecto_movil.data.dtos

import com.example.proyecto_movil.data.ReviewInfo

data class ReviewDto(
    val id: String,
    val content: String,
    val score: Int,
    val is_low_score: Boolean,
    val album_id: String,
    val user_id: String,
    val parentReviewId: String,
    val createdAt: String,
    val updatedAt: String
) {
    constructor() : this("", "", 0, false, "", "", "", "", "")
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
