package com.example.proyecto_movil.ui.Screens.ReviewDetail

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo

data class ReviewDetailState(
    val isLoading: Boolean = false,
    val review: ReviewInfo? = null,
    val likes: Int = 0,
    val album: AlbumInfo? = null,
    val author: UserInfo? = null,
    val errorMessage: String? = null
)
