package com.example.proyecto_movil.ui.Screens.Home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState

    init {
        loadAlbums()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            val result = albumRepository.getAllAlbums()
            Log.d("HomeViewModel", "🚀 Cargando álbumes desde el backend...")

            if (result.isSuccess) {
                val albums = result.getOrNull().orEmpty()
                Log.d("HomeViewModel", "✅ Álbumes cargados: ${albums.size}")
                albums.forEach {
                    Log.d("HomeVM", "🎵 ${it.title} (${it.year}) - ${it.artist.name} | Cover: ${it.coverUrl} | ArtistImg: ${it.artist.profileImageUrl}")
                }
                _uiState.update { it.copy(albumList = albums) }
            } else {
                Log.e("HomeViewModel", "❌ Error al cargar álbumes", result.exceptionOrNull())
            }
        }
    }



    fun onAlbumClicked(album: AlbumInfo) =
        _uiState.update { it.copy(openAlbum = album) }

    fun consumeOpenAlbum() =
        _uiState.update { it.copy(openAlbum = null) }

    fun getNewReleases(): List<AlbumInfo> =
        uiState.value.albumList.filter { it.year.toIntOrNull() ?: 0 >= 2023 }

    fun getPopularAlbums(): List<AlbumInfo> =
        uiState.value.albumList.filter { it.id % 2 == 0 }
}
