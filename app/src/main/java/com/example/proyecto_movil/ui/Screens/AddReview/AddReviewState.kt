package com.example.proyecto_movil.ui.Screens.AddReview

import com.example.proyecto_movil.data.AlbumInfo

data class AddReviewState(
    val albumId: Int? = null,
    val albumCoverRes: String = "",
    val albumTitle: String = "",
    val albumArtist: String = "",
    val albumYear: String = "",
    val dateString: String = "",
    val scorePercent: Int = 50,
    val isFavorite: Boolean = false,
    val reviewText: String = "",
    val showMessage: Boolean = false,
    val errorMessage: String = "",
    val availableAlbums: List<AlbumInfo> = emptyList(),
    val backendUserId: String? = null,
    val firebaseUserId: String? = null,
    val favoriteLimit: Int = 5,
    val currentFavoriteCount: Int = 0,
    val navigateCancel: Boolean = false,
    val navigatePublished: Boolean = false,
    val navigateToSettings: Boolean = false
)
