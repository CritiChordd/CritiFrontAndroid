package com.example.proyecto_movil.data.dtos

data class RegisterUserDto (
    val userName: String,
    val name: String? = null,
    val bio: String? = null,

    )