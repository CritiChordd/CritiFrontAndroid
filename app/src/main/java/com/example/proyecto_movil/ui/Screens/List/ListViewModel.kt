package com.example.proyecto_movil.ui.Screens.Lists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.repository.PlaylistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListState())
    val uiState: StateFlow<ListState> = _uiState

    fun loadPlaylist(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = playlistRepository.getPlaylistById(id)
            result.fold(
                onSuccess = { playlist ->
                    _uiState.update {
                        it.copy(
                            title = playlist.title,
                            description = playlist.description,
                            // estos dos son placeholders si tu API aún no los trae
                            likes = playlist.albums.size,
                            listenPercent = "100%",
                            albums = playlist.albums,
                            isLoading = false
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = e.message ?: "Error al cargar playlist")
                    }
                }
            )
        }
    }

    // Navegación
    fun onBackClicked() = _uiState.update { it.copy(navigateBack = true) }
    fun consumeBack() = _uiState.update { it.copy(navigateBack = false) }

    fun onSettingsClicked() = _uiState.update { it.copy(navigateToSettings = true) }
    fun consumeSettings() = _uiState.update { it.copy(navigateToSettings = false) }

    fun onAlbumClicked(id: Int) = _uiState.update { it.copy(openAlbumId = id) }
    fun consumeOpenAlbum() = _uiState.update { it.copy(openAlbumId = null) }
}
