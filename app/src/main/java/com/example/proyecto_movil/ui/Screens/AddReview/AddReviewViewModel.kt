package com.example.proyecto_movil.ui.Screens.AddReview

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class AddReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val albumRepository: AlbumRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddReviewState())
    val uiState: StateFlow<AddReviewState> = _uiState

    init {
        loadAlbums()
        loadCurrentUser()
    }

    /* ---------- Navegación ---------- */

    fun onCancelClicked() =
        _uiState.update { it.copy(navigateCancel = true) }

    fun consumeCancel() =
        _uiState.update { it.copy(navigateCancel = false) }

    fun consumePublished() =
        _uiState.update { it.copy(navigatePublished = false) }

    /* ---------- Publicar Reseña ---------- */

    fun onPublishClicked() {
        val s = _uiState.value

        val currentUserId = auth.currentUser?.uid
        if (currentUserId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    showMessage = true,
                    errorMessage = "Debes iniciar sesión para publicar reseñas"
                )
            }
            return
        }

        val backendUserId = s.backendUserId?.takeIf { it.isNotBlank() }
        if (backendUserId == null) {
            Log.w(
                "AddReviewVM",
                "Publicando reseña sin backendUserId; se usará únicamente el uid de Firebase"
            )
        }

        // 🧩 Validaciones
        when {
            s.albumId == null -> {
                _uiState.update {
                    it.copy(
                        showMessage = true,
                        errorMessage = "Selecciona un álbum antes de publicar 🎵"
                    )
                }
                return
            }

            s.reviewText.isBlank() -> {
                _uiState.update {
                    it.copy(
                        showMessage = true,
                        errorMessage = "Escribe una reseña antes de publicar ✍️"
                    )
                }
                return
            }
        }

        if (s.isFavorite && s.currentFavoriteCount >= s.favoriteLimit) {
            _uiState.update {
                it.copy(
                    showMessage = true,
                    errorMessage = "Solo puedes marcar ${s.favoriteLimit} álbumes como favoritos"
                )
            }
            return
        }

       
        _uiState.update { it.copy(showMessage = false, errorMessage = "") }

        
        viewModelScope.launch {
            try {
                val normalizedScore = (s.scorePercent / 10.0).roundToInt()
                val result = reviewRepository.createReview(
                    content = s.reviewText,
                    score = normalizedScore,
                    albumId = s.albumId!!,
                    userId = backendUserId,
                    firebaseUserId = currentUserId,
                    isFavorite = s.isFavorite
                )

                if (result.isSuccess) {
                    Log.d("AddReviewVM", "✅ Reseña publicada con éxito")
                    _uiState.update {
                        val updatedCount = if (s.isFavorite) s.currentFavoriteCount + 1 else s.currentFavoriteCount
                        it.copy(
                            navigatePublished = true,
                            currentFavoriteCount = updatedCount,
                            isFavorite = false
                        )
                    }
                    refreshFavoriteCount(currentUserId, backendUserId)
                } else {
                    Log.e("AddReviewVM", "❌ Error publicando reseña", result.exceptionOrNull())
                    _uiState.update {
                        it.copy(
                            showMessage = true,
                            errorMessage = "No se pudo publicar la reseña 😔"
                        )
                    }
                }
            } catch (e: Exception) {
                val logMessage = if (e is HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    "⚠️ Error HTTP ${e.code()} ${e.message()} -> $errorBody"
                } else {
                    "⚠️ Error de red o servidor"
                }
                Log.e("AddReviewVM", logMessage, e)
                val uiError = if (e is HttpException && e.code() == 400) {
                    "El servidor rechazó la reseña. Verifica los datos e inténtalo nuevamente."
                } else {
                    e.message ?: "No se pudo publicar la reseña"
                }
                _uiState.update {
                    it.copy(
                        showMessage = true,
                        errorMessage = uiError
                    )
                }
            }
        }
    }

    /* ---------- Actualizaciones de UI ---------- */

    fun updateReviewText(v: String) =
        _uiState.update { it.copy(reviewText = v) }

    fun toggleFavorite() {
        val state = _uiState.value
        if (!state.isFavorite && state.currentFavoriteCount >= state.favoriteLimit) {
            _uiState.update {
                it.copy(
                    showMessage = true,
                    errorMessage = "Solo puedes marcar ${state.favoriteLimit} favoritos"
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isFavorite = !state.isFavorite,
                showMessage = if (state.isFavorite) it.showMessage else false,
                errorMessage = if (state.isFavorite) it.errorMessage else ""
            )
        }
    }

    fun updateScore(score: Int) =
        _uiState.update { it.copy(scorePercent = score) }

    fun updateAlbum(album: AlbumInfo) =
        _uiState.update {
            it.copy(
                albumId = album.id,
                albumTitle = album.title,
                albumArtist = album.artist.name,
                albumYear = album.year,
                albumCoverRes = album.coverUrl,
                showMessage = false,
                errorMessage = ""
            )
        }

    fun onSettingsClicked() =
        _uiState.update { it.copy(navigateToSettings = true) }

    private fun loadAlbums() {
        viewModelScope.launch {
            try {
                val albums = albumRepository.getAllAlbums().getOrElse {
                    Log.e("AddReviewVM", "❌ Error cargando álbumes", it)
                    emptyList()
                }

                _uiState.update { current ->
                    when {
                        albums.isEmpty() -> current.copy(
                            availableAlbums = emptyList(),
                            albumId = null,
                            albumTitle = "",
                            albumArtist = "",
                            albumYear = "",
                            albumCoverRes = "",
                            showMessage = true,
                            errorMessage = "No hay álbumes disponibles para reseñar todavía"
                        )

                        current.albumId != null -> current.copy(
                            availableAlbums = albums,
                            showMessage = false,
                            errorMessage = ""
                        )

                        else -> {
                            val first = albums.first()
                            current.copy(
                                availableAlbums = albums,
                                albumId = first.id,
                                albumTitle = first.title,
                                albumArtist = first.artist.name,
                                albumYear = first.year,
                                albumCoverRes = first.coverUrl,
                                showMessage = false,
                                errorMessage = ""
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("AddReviewVM", "⚠️ Error inesperado cargando álbumes", e)
                _uiState.update {
                    it.copy(
                        showMessage = true,
                        errorMessage = "No se pudieron cargar los álbumes disponibles"
                    )
                }
            }
        }
    }

    private fun loadCurrentUser() {
        val uid = auth.currentUser?.uid ?: run {
            Log.w("AddReviewVM", "No authenticated Firebase user to resolve backend ID")
            return
        }

        viewModelScope.launch {
            val result = userRepository.getUserById(uid)
            result.onSuccess { user ->
                _uiState.update { state ->
                    state.copy(
                        backendUserId = user.backendUserId,
                        firebaseUserId = uid
                    )
                }
                if (user.backendUserId.isNullOrBlank()) {
                    Log.w(
                        "AddReviewVM",
                        "Usuario ${user.id} no tiene backendUserId; se requerirá para publicar reseñas"
                    )
                }
                refreshFavoriteCount(uid, user.backendUserId)
            }.onFailure {
                Log.e("AddReviewVM", "Error obteniendo el usuario para reseñas", it)
                _uiState.update { state ->
                    state.copy(
                        backendUserId = null,
                        firebaseUserId = uid
                    )
                }
                refreshFavoriteCount(uid, null)
            }
        }
    }

    private fun refreshFavoriteCount(firebaseUserId: String, backendUserId: String?) {
        viewModelScope.launch {
            val primaryResult = reviewRepository.getReviewsByUserId(firebaseUserId)
            val firebaseReviews = primaryResult.getOrElse { error ->
                Log.w("AddReviewVM", "No se pudieron obtener las reseñas del usuario: ${error.message}")
                emptyList()
            }

            val backendReviews = if (!backendUserId.isNullOrBlank() && backendUserId != firebaseUserId) {
                reviewRepository.getReviewsByUserId(backendUserId).getOrElse { fallbackError ->
                    Log.w("AddReviewVM", "No se pudieron obtener reseñas usando backendId: ${fallbackError.message}")
                    emptyList()
                }
            } else {
                emptyList()
            }

            val reviews = (firebaseReviews + backendReviews).distinctBy(ReviewInfo::id)

            _uiState.update {
                it.copy(currentFavoriteCount = reviews.count(ReviewInfo::isFavorite))
            }
        }
    }
}
