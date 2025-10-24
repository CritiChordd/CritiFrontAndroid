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
    val searchResults: List<SearchResult> = emptyList(),
    val searchError: String? = null,

    val navigateToProfile: Boolean = false,
    val navigateToSettings: Boolean = false,
    val openAlbum: AlbumInfo? = null,
    val navigateToUserId: String? = null
)

sealed class SearchResult {
    data class User(val data: UserInfo) : SearchResult()
    data class Album(val data: AlbumInfo) : SearchResult()
}
