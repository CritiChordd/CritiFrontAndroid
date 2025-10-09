package com.example.proyecto_movil.ui.Screens.Home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState

    init {
        loadAlbumsAndReviews()
    }

    /** 🔹 Carga inicial de álbumes y reseñas */
    private fun loadAlbumsAndReviews() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "🚀 Cargando álbumes y reseñas desde el backend...")

                val albumsResult = albumRepository.getAllAlbums()
                val reviewsResult = reviewRepository.getAllReviews()

                val albums = albumsResult.getOrDefault(emptyList())
                val reviews = reviewsResult.getOrDefault(emptyList())

                Log.d("HomeViewModel", "✅ Álbumes cargados: ${albums.size}")
                Log.d("HomeViewModel", "✅ Reseñas cargadas: ${reviews.size}")

                _uiState.update {
                    it.copy(
                        albumList = albums,
                        reviewList = reviews
                    )
                }

                albums.forEach {
                    Log.d(
                        "HomeVM",
                        "🎵 ${it.title} (${it.year}) - ${it.artist.name} | Cover: ${it.coverUrl} | ArtistImg: ${it.artist.profileImageUrl}"
                    )
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error al cargar datos", e)
            }
        }
    }

    /** 🔹 Evento al hacer clic en un álbum */
    fun onAlbumClicked(album: AlbumInfo) =
        _uiState.update { it.copy(openAlbum = album) }

    /** 🔹 Consumir navegación al álbum */
    fun consumeOpenAlbum() =
        _uiState.update { it.copy(openAlbum = null) }

    /** 🔹 Filtrar lanzamientos recientes */
    fun getNewReleases(): List<AlbumInfo> =
        uiState.value.albumList.filter { it.year.toIntOrNull() ?: 0 >= 2023 }

    /** 🔹 Ordenar álbumes por puntaje promedio */
    fun getPopularAlbums(): List<AlbumInfo> {
        val reviews = uiState.value.reviewList
        val albums = uiState.value.albumList

        if (reviews.isEmpty() || albums.isEmpty()) return albums

        // Calcular el promedio por álbum
        val avgScores: Map<Int, Double> = reviews
            .groupBy { it.albumId }
            .mapValues { (_, list) ->
                list.mapNotNull { it.score }.average()
            }

        // Log de control
        avgScores.entries.forEach {
            Log.d("HomeVM", "⭐ Album ${it.key} promedio ${"%.2f".format(it.value)}")
        }

        // Ordenar los álbumes según su promedio
        return albums.sortedByDescending { avgScores[it.id] ?: 0.0 }
    }
}
