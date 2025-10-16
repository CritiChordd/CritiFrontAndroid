package com.example.proyecto_movil.ui.Screens.Home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ContentRepository
import com.example.proyecto_movil.data.datasource.AuthRemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val contentRepository: ContentRepository,
    private val authRemoteDataSource: AuthRemoteDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState

    init {
        loadAlbums()
        collectLiveContent()
    }

    private fun loadAlbums() {
        viewModelScope.launch {
            val result = albumRepository.getAllAlbums()
            Log.d("HomeViewModel", "🚀 Cargando álbumes desde el backend...")

            if (result.isSuccess) {
                val albums = result.getOrNull().orEmpty()
                Log.d("HomeViewModel", "✅ Álbumes cargados: ${albums.size}")
                albums.forEach {
                    Log.d(
                        "HomeVM",
                        "🎵 ${it.title} (${it.year}) - ${it.artist.name} | Cover: ${it.coverUrl} | ArtistImg: ${it.artist.profileImageUrl}"
                    )
                }
                _uiState.update { it.copy(albumList = albums) }
            } else {
                Log.e("HomeViewModel", "❌ Error al cargar álbumes", result.exceptionOrNull())
            }
        }
    }

    private fun collectLiveContent() {
        viewModelScope.launch {
            contentRepository.listenAllContent().collect { feed ->
                _uiState.update { it.copy(contentFeed = feed) }

                val uid = authRemoteDataSource.currentUser?.uid
                if (uid != null) {
                    feed.forEach { item ->
                        launch {
                            val isLiked = contentRepository.isLiked(item.id, uid).getOrElse { false }
                            _uiState.update { st ->
                                st.copy(likedMap = st.likedMap + (item.id to isLiked))
                            }
                        }
                    }
                }
            }
        }
    }

    fun onToggleLike(contentId: String) {
        val uid = authRemoteDataSource.currentUser?.uid ?: return
        viewModelScope.launch {
            contentRepository.toggleLike(contentId, uid)
                .onSuccess { likedNow ->
                    _uiState.update { st ->
                        st.copy(likedMap = st.likedMap + (contentId to likedNow))
                    }
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
