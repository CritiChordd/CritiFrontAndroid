package com.example.proyecto_movil.ui.Screens.Home

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo

data class HomeState(
    val albumList: List<AlbumInfo> = emptyList(),
    val newReleases: List<AlbumInfo> = emptyList(),
    val popularAlbums: List<AlbumInfo> = emptyList(),
    val searchQuery: String = "",
    val reviewList: List<ReviewInfo> = emptyList(),

    val isSearchActive: Boolean = false,
    val isSearching: Boolean = false,
    val searchUserResults: List<UserInfo> = emptyList(),
    val searchAlbumResults: List<AlbumInfo> = emptyList(),
    val searchError: String? = null,

    val navigateToProfile: Boolean = false,
    val navigateToSettings: Boolean = false,
    val openAlbum: AlbumInfo? = null,
    val navigateToUserId: String? = null
)
