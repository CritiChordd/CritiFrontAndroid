package com.example.proyecto_movil.ui.Screens.ArtistPage

import com.example.proyecto_movil.data.AlbumInfo

data class ArtistPageState(
    val artistId: Int? = null,
    val artistName: String = "",
    val artistProfileRes: String = "", // URL o ruta local
    val followersText: String = "",
    val globalScoreText: String = "",
    val reviewsExtraText: String = "",
    val albums: List<AlbumInfo> = emptyList(),

    // Navegación
    val navigateBack: Boolean = false,
    val openAlbumId: Int? = null,
    val navigateSeeAll: Boolean = false,

    // Mensajería (Snackbar)
    val showMessage: Boolean = false,
    val message: String? = null
)
