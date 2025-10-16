package com.example.proyecto_movil.ui.Screens.Lists

import com.example.proyecto_movil.data.AlbumInfo

data class ListState(
    val title: String = "Lista",
    val description: String = "",
    val creatorName: String = "",
    val creatorAvatarRes: Int = 0, // si luego pasas URL, cámbialo a String
    val likes: Int = 0,
    val listenPercent: String = "0%",

    val albums: List<AlbumInfo> = emptyList(),

    // Estado UI
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Navegación
    val navigateBack: Boolean = false,
    val navigateToSettings: Boolean = false,
    val openAlbumId: Int? = null
)
