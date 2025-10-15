package com.example.proyecto_movil.ui.Screens.Settings

data class SettingsState(
    val displayName: String = "",
    val username: String = "",
    val avatarUrl: String = "",

    val preferDarkMode: Boolean = true,

    val hideActivity: Boolean = true,
    val showRecentAlbums: Boolean = true,
    val publicPlaylists: Boolean = false,
    val showPlaylistsOnProfile: Boolean = true,
    val showFollowersAndFollowing: Boolean = true,

    val allowExplicitContent: Boolean = true,
    val showUnavailableInCountry: Boolean = false,

    val pushNotifications: Boolean = true,
    val emailNotifications: Boolean = true,

    val profileUserId: String? = null,
    val isLoadingProfile: Boolean = false,
    val errorMessage: String? = null,

    val navigateBack: Boolean = false,
    val navigateToLogin: Boolean = false,
    val navigateToProfile: String? = null
)
