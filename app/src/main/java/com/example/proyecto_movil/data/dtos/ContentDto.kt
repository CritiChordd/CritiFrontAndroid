package com.example.proyecto_movil.data.dtos

data class ContentDto(
    val id: String = "",
    val authorId: String = "",
    val text: String = "",
    val likesCount: Int = 0,
    val createdAt: Long = 0L
)

