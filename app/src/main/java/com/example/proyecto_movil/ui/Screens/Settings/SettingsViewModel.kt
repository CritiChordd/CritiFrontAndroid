package com.example.proyecto_movil.ui.Screens.Settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState: StateFlow<SettingsState> = _uiState

    init {
        refreshProfile()
    }

    fun refreshIfNeeded() {
        if (_uiState.value.profileUserId.isNullOrBlank()) {
            refreshProfile()
        }
    }

    private fun refreshProfile() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingProfile = true, errorMessage = null) }
            val result = userRepository.getUserById(uid)
            result.onSuccess { user ->
                val displayName = sequenceOf(user.name, user.username)
                    .mapNotNull { it.takeIf(String::isNotBlank) }
                    .firstOrNull()
                    .orEmpty()

                _uiState.update {
                    it.copy(
                        displayName = displayName,
                        username = user.username,
                        avatarUrl = user.profileImageUrl,
                        profileUserId = user.id,
                        isLoadingProfile = false
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoadingProfile = false,
                        errorMessage = error.message ?: "Error al cargar el perfil"
                    )
                }
            }
        }
    }

    fun onBackClicked() = _uiState.update { s -> s.copy(navigateBack = true) }
    fun consumeNavigation() = _uiState.update { s -> s.copy(navigateBack = false) }

    fun consumeNavigateLogin() = _uiState.update { s -> s.copy(navigateToLogin = false) }
    fun consumeNavigateProfile() = _uiState.update { s -> s.copy(navigateToProfile = null) }
    fun consumeError() = _uiState.update { s -> s.copy(errorMessage = null) }

    fun togglePreferDarkMode() = _uiState.update { s -> s.copy(preferDarkMode = !s.preferDarkMode) }

    fun toggleHideActivity() = _uiState.update { s -> s.copy(hideActivity = !s.hideActivity) }
    fun toggleShowRecentAlbums() = _uiState.update { s -> s.copy(showRecentAlbums = !s.showRecentAlbums) }
    fun togglePublicPlaylists() = _uiState.update { s -> s.copy(publicPlaylists = !s.publicPlaylists) }
    fun toggleShowPlaylistsOnProfile() = _uiState.update { s -> s.copy(showPlaylistsOnProfile = !s.showPlaylistsOnProfile) }
    fun toggleShowFollowersAndFollowing() =
        _uiState.update { s -> s.copy(showFollowersAndFollowing = !s.showFollowersAndFollowing) }

    fun toggleAllowExplicitContent() =
        _uiState.update { s -> s.copy(allowExplicitContent = !s.allowExplicitContent) }
    fun toggleShowUnavailableInCountry() =
        _uiState.update { s -> s.copy(showUnavailableInCountry = !s.showUnavailableInCountry) }

    fun togglePushNotifications() =
        _uiState.update { s -> s.copy(pushNotifications = !s.pushNotifications) }
    fun toggleEmailNotifications() =
        _uiState.update { s -> s.copy(emailNotifications = !s.emailNotifications) }

    fun onLanguageClicked() {}

    fun onViewProfileClicked() {
        val targetId = _uiState.value.profileUserId ?: auth.currentUser?.uid
        if (!targetId.isNullOrBlank()) {
            _uiState.update { it.copy(navigateToProfile = targetId) }
        }
    }

    fun onLogoutClicked() {
        auth.signOut()
        _uiState.update { it.copy(navigateToLogin = true) }
    }

    fun onDeactivateAccountClicked() {}
}
