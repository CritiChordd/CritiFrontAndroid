package com.example.proyecto_movil.ui.Screens.UserProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileState())
    val uiState: StateFlow<UserProfileState> = _uiState

    // Lo llama tu Screen
    private var lastRequestedUserId: String? = null

    fun setInitialData(userId: String) {
        val alreadyLoaded = lastRequestedUserId == userId && uiState.value.user != null
        if (alreadyLoaded) return

        lastRequestedUserId = userId
        loadUser(userId)
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            userRepository.getUserById(userId).fold(
                onSuccess = { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = user,
                            reviews = emptyList<ReviewInfo>(),     // cargar si tienes fuente
                            favoriteAlbums = emptyList()            // cargar si tienes fuente
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Error cargando usuario")
                    }
                }
            )
        }
    }

    // Acciones que TU Screen usa
    fun onBackClicked()               = _uiState.update { it.copy(navigateBack = true) }
    fun consumeBack()                 = _uiState.update { it.copy(navigateBack = false) }

    fun onSettingsClicked()           = _uiState.update { it.copy(navigateToSettings = true) }
    fun consumeSettings()             = _uiState.update { it.copy(navigateToSettings = false) }

    fun onEditProfileClicked()        = _uiState.update { it.copy(navigateToEditProfile = true) }
    fun consumeEdit()                 = _uiState.update { it.copy(navigateToEditProfile = false) }

    fun onAlbumClicked(id: Int)       = _uiState.update { it.copy(openAlbumId = id) }
    fun consumeOpenAlbum()            = _uiState.update { it.copy(openAlbumId = null) }

    fun onReviewClicked(id: Int)      = _uiState.update { it.copy(openReview = id) }
    fun consumeOpenReview()           = _uiState.update { it.copy(openReview = null) }
}
