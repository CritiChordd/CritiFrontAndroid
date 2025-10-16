package com.example.proyecto_movil.ui.Screens.AddReview

data class AddReviewState(
    val albumId: Int = 0, // 👈 Nuevo
    val albumCoverRes: String = "",
    val albumTitle: String = "",
    val albumArtist: String = " Miller",
    val albumYear: String = "",
    val dateString: String = "",
    val scorePercent: Int = 0,
    val liked: Boolean = false,
    val reviewText: String = "",
    val showMessage: Boolean = false,
    val errorMessage: String = "",
    val navigateCancel: Boolean = false,
    val navigatePublished: Boolean = false,
    val navigateToSettings: Boolean = false
)
