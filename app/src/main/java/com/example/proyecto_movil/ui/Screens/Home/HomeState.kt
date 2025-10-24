package com.example.proyecto_movil.ui.Screens.Home

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo

data class HomeState(
    val albumList: List<AlbumInfo> = emptyList(),
    val searchQuery: String = "",
    val reviewList: List<ReviewInfo> = emptyList(),

    val isSearchActive: Boolean = false,
    val isSearching: Boolean = false,
    val searchResults: List<HomeSearchResult> = emptyList(),
    val searchError: String? = null,

    val navigateToProfile: Boolean = false,
    val navigateToSettings: Boolean = false,
    val openAlbum: AlbumInfo? = null,
    val navigateToUserId: String? = null
)

sealed interface HomeSearchResult {
    data class Album(val album: AlbumInfo) : HomeSearchResult
    data class User(val user: UserInfo) : HomeSearchResult
}
