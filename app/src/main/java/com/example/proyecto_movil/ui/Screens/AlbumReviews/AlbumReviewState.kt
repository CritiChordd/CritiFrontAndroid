package com.example.proyecto_movil.ui.Screens.AlbumReviews

import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo

data class AlbumReviewState(
    val albumId: Int = 0,
    val albumCoverRes: String = "",
    val albumTitle: String = "",
    val albumArtist: String = "",
    val albumYear: String = "",
    val artistProfileRes: String = "",
    val reviews: List<ReviewInfo> = emptyList(),
    val reviewItems: List<AlbumReviewItem> = emptyList(),
    val avgPercent: Int? = null,

    // Navegación existente
    val navigateToArtist: Boolean = false,
    val openUserId: String? = null,

    // Snackbar (mensajes transientes desde try/catch)
    val showMessage: Boolean = false,
    val message: String? = null,

    // (opcional) loader
    val isLoading: Boolean = false
)

data class AlbumReviewItem(
    val review: ReviewInfo,
    val author: UserInfo?
)
