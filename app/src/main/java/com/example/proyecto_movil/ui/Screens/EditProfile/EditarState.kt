package com.example.proyecto_movil.ui.Screens.EditProfile

import com.example.proyecto_movil.R

data class EditProfileState(
    val avatarRes: Int = R.drawable.xocas,
    val avatarUrl: String = "",
    val nombrePersona: String = "",
    val nombreUsuario: String = "",
    val email: String = "",
    val password: String = "",
    val mostrarPassword: Boolean = false,

    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,

    val navigateBack: Boolean = false,
    val navigateSaved: Boolean = false
)

