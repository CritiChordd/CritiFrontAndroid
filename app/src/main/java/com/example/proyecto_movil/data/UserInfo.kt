package com.example.proyecto_movil.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserInfo(
    val id: String,
    val name: String,
    val username: String,
    val profileImageUrl: String,
    val bio: String = "",
    val followers: Int = 0,
    val following: Int = 0,
    val playlists: List<PlaylistInfo> = emptyList(),
    val backendUserId: String? = null
) : Parcelable {
    
    // Alias para pantallas que usan 'avatarUrl'
    val avatarUrl: String get() = profileImageUrl
}
