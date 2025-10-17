package com.example.proyecto_movil.ui.Screens.EditProfile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.repository.StorageRepository
import com.example.proyecto_movil.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val storageRepository: StorageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileState())
    val uiState: StateFlow<EditProfileState> = _uiState

    private var lastLoadedUserId: String? = null
    private var hasLoadedInitialData: Boolean = false

    fun loadInitialProfile(userId: String) {
        if (userId.isBlank()) return
        if (hasLoadedInitialData && lastLoadedUserId == userId) return

        lastLoadedUserId = userId
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val result = userRepository.getUserById(userId)
            result.fold(
                onSuccess = { user ->
                    hasLoadedInitialData = true
                    _uiState.update { current ->
                        current.copy(
                            avatarUrl = user.profileImageUrl,
                            nombrePersona = user.bio.takeIf { it.isNotBlank() } ?: user.name,
                            nombreUsuario = user.username,
                        )
                    }
                },
                onFailure = { error ->
                    hasLoadedInitialData = false
                    _uiState.update {
                        it.copy(errorMessage = error.message ?: "Error al cargar perfil")
                    }
                }
            )
        }
    }

    fun onBackClicked() = _uiState.update { it.copy(navigateBack = true) }
    fun consumeBack() = _uiState.update { it.copy(navigateBack = false) }

    fun onSaveClicked(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = userRepository.updateUser(
                id = userId,
                username = _uiState.value.nombreUsuario,
                bio = _uiState.value.nombrePersona,
                profilePic = _uiState.value.avatarUrl.ifBlank { null }
            )
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            navigateSaved = true,
                            successMessage = "Perfil actualizado correctamente"
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Error al guardar perfil")
                    }
                }
            )
        }
    }
    fun consumeSaved() = _uiState.update { it.copy(navigateSaved = false) }

    fun updateNombrePersona(v: String) = _uiState.update { it.copy(nombrePersona = v) }
    fun updateNombreUsuario(v: String) = _uiState.update { it.copy(nombreUsuario = v) }
    fun updateEmail(v: String) = _uiState.update { it.copy(email = v) }
    fun updatePassword(v: String) = _uiState.update { it.copy(password = v) }
    fun toggleMostrarPassword() = _uiState.update { it.copy(mostrarPassword = !it.mostrarPassword) }
    fun updateAvatarUrlManually(url: String) = _uiState.update { it.copy(avatarUrl = url) }

    fun uploadAvatarFromPicker(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            val result = storageRepository.uploadProfileImage(uri)
            result.fold(
                onSuccess = { url ->
                    _uiState.update { it.copy(avatarUrl = url, isUploading = false) }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isUploading = false, errorMessage = e.message ?: "Error al subir la imagen")
                    }
                }
            )
        }
    }
}
