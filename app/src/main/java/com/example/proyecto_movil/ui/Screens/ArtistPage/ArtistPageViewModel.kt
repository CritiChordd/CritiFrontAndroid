package com.example.proyecto_movil.ui.Screens.ArtistPage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.ReviewInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ArtistPageViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    // ✅ El estado interno es ArtistPageState, no List<ReviewInfo>
    private val _uiState = MutableStateFlow(ArtistPageState())
    val uiState: StateFlow<ArtistPageState> = _uiState

    /**
     * Carga la información del artista y sus álbumes asociados
     */
    fun setArtist(artistId: Int?) {
        if (artistId == null) {
            _uiState.update {
                it.copy(
                    showMessage = true,
                    message = "El ID del artista es nulo."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(showMessage = false, message = null) }

            try {
                // 🔹 Obtener todos los álbumes y filtrar los del artista
                val albumsResult = albumRepository.getAllAlbums()
                val albums = albumsResult.getOrElse { emptyList() }
                    .filter { it.artist.id == artistId }

                // ⚠️ Si no hay álbumes, se asume que no se encontró artista
                val artist: ArtistInfo? = albums.firstOrNull()?.artist
                if (artist == null) {
                    _uiState.update {
                        it.copy(
                            showMessage = true,
                            message = "No se encontró el artista con ID $artistId"
                        )
                    }
                    return@launch
                }

                // 🔹 Obtener reseñas: aquí deberías tener un método por álbum o por artista
                // mientras tanto, simulo con emptyList()
                val allReviews: List<ReviewInfo> = emptyList()

                val artistReviews = allReviews.filter { review ->
                    albums.any { it.id == review.albumId }
                }

                val reviewsCount = artistReviews.size
                val followersText = "${(1000..9999).random()} seguidores"
                val avgScore = if (artistReviews.isNotEmpty()) {
                    artistReviews.sumOf { it.score } / artistReviews.size
                } else 0

                // 🔹 Actualizar UI
                _uiState.update {
                    it.copy(
                        artistId = artist.id,
                        artistName = artist.name,
                        artistProfileRes = artist.profileImageUrl,
                        followersText = followersText,
                        globalScoreText = "Puntaje promedio: $avgScore",
                        reviewsExtraText = "de $reviewsCount reseñas",
                        albums = albums,
                        showMessage = false,
                        message = null
                    )
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        showMessage = true,
                        message = "Error al cargar artista: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /* ---------------- Navegación ---------------- */
    fun onBackClicked() = _uiState.update { it.copy(navigateBack = true) }
    fun consumeBack() = _uiState.update { it.copy(navigateBack = false) }

    fun onSeeAllClicked() = _uiState.update { it.copy(navigateSeeAll = true) }
    fun consumeSeeAll() = _uiState.update { it.copy(navigateSeeAll = false) }

    fun onAlbumClicked(id: Int) = _uiState.update { it.copy(openAlbumId = id) }
    fun consumeOpenAlbum() = _uiState.update { it.copy(openAlbumId = null) }

    /* ---------------- Snackbar ---------------- */
    fun consumeMessage() = _uiState.update { it.copy(showMessage = false, message = null) }
}