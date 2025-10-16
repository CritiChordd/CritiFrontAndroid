package com.example.proyecto_movil.ui.Screens.Home

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ContentInfo

data class HomeState(
    val albumList: List<AlbumInfo> = emptyList(),
    val searchQuery: String = "",

    val navigateToProfile: Boolean = false,
    val navigateToSettings: Boolean = false,
    val openAlbum: AlbumInfo? = null,

    // Live content feed
    val contentFeed: List<ContentInfo> = emptyList(),
    val likedMap: Map<String, Boolean> = emptyMap()
)
