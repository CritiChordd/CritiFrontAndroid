package com.example.proyecto_movil.ui.Screens.FollowList

import com.example.proyecto_movil.data.UserInfo

data class FollowListState(
    val isLoading: Boolean = true,
    val users: List<UserInfo> = emptyList(),
    val title: String = "",
    val error: String? = null
)
