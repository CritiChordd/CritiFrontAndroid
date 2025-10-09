package com.example.proyecto_movil.ui.Screens.ReviewDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewDetailViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val albumRepository: AlbumRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewDetailState())
    val uiState: StateFlow<ReviewDetailState> = _uiState

    private var lastLoadedId: String? = null

    fun load(reviewId: String) {
        if (reviewId.isBlank()) return
        if (lastLoadedId == reviewId && _uiState.value.review != null) return

        lastLoadedId = reviewId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val review = reviewRepository.getReviewById(reviewId).getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No se pudo cargar la reseña"
                    )
                }
                return@launch
            }

            val album = review.albumId.takeIf { it != 0 }?.let { albumId ->
                albumRepository.getAlbumById(albumId).getOrNull()
            }

            val author = review.userId.takeIf { it.isNotBlank() }?.let { userId ->
                userRepository.getUserById(userId).getOrNull()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    review = review,
                    album = album,
                    author = author
                )
            }
        }
    }

    fun consumeError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
