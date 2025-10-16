package com.example.proyecto_movil.ui.Screens.Content

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ArtistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val artistRepository: ArtistRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentState())
    val uiState: StateFlow<ContentState> = _uiState

    fun setInitial(artistId: Int? = null, isOwner: Boolean = false) {
        _uiState.update { it.copy(isOwner = isOwner, artistId = artistId) }
        loadAlbums(artistId)
    }

    private fun loadAlbums(artistId: Int? = null) {
        viewModelScope.launch {
            val result = albumRepository.getAllAlbums()
            if (result.isSuccess) {
                val allAlbums = result.getOrNull().orEmpty()
                val albums = artistId?.let { id ->
                    allAlbums.filter { it.artist.id == id }
                } ?: allAlbums

                if (artistId != null) {
                    loadArtistName(artistId, albums)
                } else {
                    _uiState.update {
                        it.copy(
                            headerTitle = "Contenido",
                            albums = albums
                        )
                    }
                }
            } else {
                Log.e("ContentViewModel", "❌ Error al cargar álbumes", result.exceptionOrNull())
            }
        }
    }

    private fun loadArtistName(artistId: Int, albums: List<AlbumInfo>) {
        viewModelScope.launch {
            val result = artistRepository.getArtistById(artistId.toString())
            if (result.isSuccess) {
                val artist = result.getOrNull()
                _uiState.update {
                    it.copy(
                        headerTitle = artist?.name ?: "Contenido",
                        albums = albums
                    )
                }
            } else {
                Log.e("ContentViewModel", "❌ Error cargando artista", result.exceptionOrNull())
                _uiState.update {
                    it.copy(
                        headerTitle = "Contenido",
                        albums = albums
                    )
                }
            }
        }
    }

    // -------------------
    // Acciones de UI
    // -------------------

    fun onBackClicked() = _uiState.update { it.copy(navigateBack = true) }
    fun consumeBack() = _uiState.update { it.copy(navigateBack = false) }

    fun onOpenAlbum(id: Int) = _uiState.update { it.copy(openAlbumId = id) }
    fun consumeOpenAlbum() = _uiState.update { it.copy(openAlbumId = null) }

    fun onSeeAllClicked() = _uiState.update { it.copy(seeAllDiscography = true) }
    fun consumeSeeAll() = _uiState.update { it.copy(seeAllDiscography = false) }

    fun onEditAlbumClicked(id: Int) = _uiState.update { it.copy(editAlbumId = id) }
    fun consumeEditAlbum() = _uiState.update { it.copy(editAlbumId = null) }
}
