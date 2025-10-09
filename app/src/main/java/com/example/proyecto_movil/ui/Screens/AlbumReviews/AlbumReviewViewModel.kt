package com.example.proyecto_movil.ui.Screens.AlbumReviews

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class AlbumReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumReviewState())
    val uiState: StateFlow<AlbumReviewState> = _uiState

    fun setAlbumById(albumId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // 🔹 1. Obtener el álbum
                val albumResult = albumRepository.getAlbumById(albumId)
                val album = albumResult.getOrNull()

                if (album == null) {
                    _uiState.update {
                        it.copy(
                            showMessage = true,
                            message = "No se pudo cargar el álbum.",
                            isLoading = false
                        )
                    }
                    return@launch
                }

                // 🔹 2. Obtener las reseñas del álbum
                val reviewsResult = reviewRepository.getReviewsByAlbumId(album.id)
                val reviews: List<ReviewInfo> = reviewsResult.getOrElse {
                    Log.e("AlbumReviewVM", "❌ Error obteniendo reseñas: ${it.message}")
                    emptyList()
                }

                Log.d("AlbumReviewVM", "📀 Album '${album.title}' tiene ${reviews.size} reseñas")

                // 🔹 3. Calcular promedio
                val avg: Int? = if (reviews.isNotEmpty()) {
                    (reviews.sumOf { it.score } / reviews.size).toInt()
                } else null

                // 🔹 4. Actualizar el estado
                _uiState.update {
                    it.copy(
                        albumId = album.id,
                        albumCoverRes = album.coverUrl,
                        albumTitle = album.title,
                        albumArtist = album.artist.name,
                        albumYear = album.year,
                        artistProfileRes = album.artist.profileImageUrl,
                        reviews = reviews,
                        avgPercent = avg,
                        isLoading = false,
                        showMessage = false,
                        message = null
                    )
                }

            } catch (e: Exception) {
                Log.e("AlbumReviewVM", "❌ Error en setAlbumById: ${e.localizedMessage}")
                _uiState.update {
                    it.copy(
                        showMessage = true,
                        message = "Error cargando reseñas: ${e.localizedMessage}",
                        isLoading = false
                    )
                }
            }
        }
    }

    // ---------- Navegación ----------
    fun onArtistClicked() = _uiState.update { it.copy(navigateToArtist = true) }
    fun consumeNavigateArtist() = _uiState.update { it.copy(navigateToArtist = false) }

    fun onUserClicked(userId: String) = _uiState.update { it.copy(openUserId = userId) }
    fun consumeOpenUser() = _uiState.update { it.copy(openUserId = null) }
}
