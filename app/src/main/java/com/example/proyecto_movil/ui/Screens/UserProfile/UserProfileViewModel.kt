package com.example.proyecto_movil.ui.Screens.UserProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository,
    private val albumRepository: AlbumRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileState())
    val uiState: StateFlow<UserProfileState> = _uiState

    // Lo llama tu Screen
    private var lastRequestedUserId: String? = null

    fun setInitialData(userId: String) {
        val alreadyLoaded = lastRequestedUserId == userId && uiState.value.user != null
        if (alreadyLoaded) return

        lastRequestedUserId = userId
        loadUser(userId)
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val userResult = userRepository.getUserById(userId)
            val user = userResult.getOrElse { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Error cargando usuario")
                }
                return@launch
            }

            val reviewsResult = reviewRepository.getReviewsByUserId(userId)
            val reviews = reviewsResult.getOrElse { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        reviews = emptyList(),
                        reviewItems = emptyList(),
                        favoriteAlbums = emptyList(),
                        errorMessage = error.message ?: "Error cargando reseñas"
                    )
                }
                return@launch
            }

            val reviewItems = buildReviewItems(reviews)
            val favoriteAlbums = reviewItems.mapNotNull { it.album }.distinctBy(AlbumInfo::id)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    reviews = reviews,
                    reviewItems = reviewItems,
                    favoriteAlbums = favoriteAlbums
                )
            }
        }
    }

    private suspend fun buildReviewItems(reviews: List<ReviewInfo>): List<UserReviewUi> {
        if (reviews.isEmpty()) return emptyList()

        val albumIds = reviews.mapNotNull { review ->
            review.albumId.takeIf { it != 0 }
        }.distinct()

        val albumMap: Map<Int, AlbumInfo> = coroutineScope {
            albumIds.map { albumId ->
                async {
                    albumRepository.getAlbumById(albumId).getOrNull()?.let { albumId to it }
                }
            }.awaitAll()
                .filterNotNull()
                .toMap()
        }

        return reviews.sortedByDescending { review ->
            review.updatedAt.toLongOrNull()
                ?: review.createdAt.toLongOrNull()
                ?: 0L
        }
            .map { review ->
                UserReviewUi(
                    review = review,
                    album = albumMap[review.albumId]
                )
            }
    }

    // Acciones que TU Screen usa
    fun onBackClicked()               = _uiState.update { it.copy(navigateBack = true) }
    fun consumeBack()                 = _uiState.update { it.copy(navigateBack = false) }

    fun onSettingsClicked()           = _uiState.update { it.copy(navigateToSettings = true) }
    fun consumeSettings()             = _uiState.update { it.copy(navigateToSettings = false) }

    fun onEditProfileClicked()        = _uiState.update { it.copy(navigateToEditProfile = true) }
    fun consumeEdit()                 = _uiState.update { it.copy(navigateToEditProfile = false) }

    fun onAlbumClicked(id: Int)       = _uiState.update { it.copy(openAlbumId = id) }
    fun consumeOpenAlbum()            = _uiState.update { it.copy(openAlbumId = null) }

    fun onReviewClicked(id: Int)      = _uiState.update { it.copy(openReview = id) }
    fun consumeOpenReview()           = _uiState.update { it.copy(openReview = null) }
}
