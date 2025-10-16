package com.example.proyecto_movil.ui.Screens.Register

data class RegisterState(
    val nombrePersona: String = "",
    val nombreUsuario: String = "",
    val email: String = "",
    val password: String = "",
    val mostrarPassword: Boolean = false,
    val acceptedTerms: Boolean = false,

    val errorMessage: String? = null,
    val showMessage: Boolean = false,

    val navigateAfterRegister: Boolean = false,
    val navigateToLogin: Boolean = false,
    val navigateBack: Boolean = false
)
