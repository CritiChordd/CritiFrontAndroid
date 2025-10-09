package com.example.proyecto_movil.data.dtos

data class CreateReviewUserDto(
    val name: String? = null,
   val username: String? = null,
   val profile_pic: String? = null
)


data class CreateReviewDto(
    val content: String,
    val score: Int,
    val is_low_score: Boolean,
    val album_id: String,
    val user_id: String = "",
    val firebase_user_id: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)
