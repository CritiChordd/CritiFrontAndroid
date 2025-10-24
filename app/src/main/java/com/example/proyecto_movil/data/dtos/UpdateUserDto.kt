package com.example.proyecto_movil.data.dtos

data class UpdateUserDto(
    val username: String,
    val bio: String,
    val profile_pic: String? = null
)
