package com.example.proyecto_movil.ui.Screens.AlbumReviews

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

    // ✅ El estado interno debe ser del tipo AlbumReviewState, no List<ReviewInfo>
    private val _uiState: MutableStateFlow<AlbumReviewState> = MutableStateFlow(AlbumReviewState())
    val uiState: StateFlow<AlbumReviewState> = _uiState

    /** Cargar reseñas de un álbum por ID */
    fun setAlbumById(albumId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                // Obtener info del álbum
                val albumResult = albumRepository.getAlbumById(albumId.toString())
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

                // ⚠️ Mientras implementas reviews por álbum, pido por usuario para que compile.
                // Si tu repo expone getReviewsByAlbumId(albumId: String), úsalo aquí.
                val reviewsResult = reviewRepository.getReviewsByUserId(
                    // Usa el identificador que tengas disponible; acá dejo album.id como string.
                    album.id.toString()
                )

                val reviews: List<ReviewInfo> = reviewsResult.getOrElse { emptyList() }

                // Promedio simple (Int?) para encajar con tu estado previo
                val avg: Int? = if (reviews.isNotEmpty()) {
                    reviews.sumOf { it.score } / reviews.size
                } else null

                // Actualizar estado de pantalla
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
    fun onArtistClicked() { _uiState.update { it.copy(navigateToArtist = true) } }
    fun consumeNavigateArtist() { _uiState.update { it.copy(navigateToArtist = false) } }

    fun onUserClicked(userId: String) { _uiState.update { it.copy(openUserId = userId) } }
    fun consumeOpenUser() { _uiState.update { it.copy(openUserId = null) } }
}
