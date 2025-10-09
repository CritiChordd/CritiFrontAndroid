package com.example.proyecto_movil.data.dtos

import com.example.proyecto_movil.data.UserInfo

/**
 * DTO que refleja exactamente la respuesta de la API.
 */
data class UserProfileDto(
    val id: String,           // ← String
    val username: String,
    val profile_pic: String,  // URL
    val bio: String,
    val followers: Int,
    val following: Int,
    val createdAt: String,
    val updatedAt: String,
)

/** Mapeo DTO -> UI model */
fun UserProfileDto.toUserUI(): UserInfo {
    return UserInfo(
        id = id,                          // ← String
        username = username,
        profileImageUrl = profile_pic,
        bio = bio,
        followers = followers,
        following = following,
        playlists = emptyList(),
        backendUserId = id.takeIf { it.isNotBlank() }
    )
}

/** Para update en API */
data class UpdateUserDto(
    val username: String,
    val bio: String,
    val profile_pic: String? = null
)
