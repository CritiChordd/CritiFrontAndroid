package com.example.proyecto_movil.ui.Screens.UserProfile

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo

data class UserProfileState(
    val isLoading: Boolean = false,
    val user: UserInfo? = null,
    val reviews: List<ReviewInfo> = emptyList(),
    val reviewItems: List<UserReviewUi> = emptyList(),
    val favoriteAlbums: List<AlbumInfo> = emptyList(),
    val errorMessage: String? = null,

    // Nombres que Screen espera
    val navigateBack: Boolean = false,
    val navigateToSettings: Boolean = false,
    val navigateToEditProfile: Boolean = false,
    val openAlbumId: Int? = null,
    val openReview: Int? = null
)

data class UserReviewUi(
    val review: ReviewInfo,
    val album: AlbumInfo?
)
