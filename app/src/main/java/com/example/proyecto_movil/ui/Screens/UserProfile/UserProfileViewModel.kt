package com.example.proyecto_movil.ui.Screens.UserProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.repository.NotificationsRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
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
    private val albumRepository: AlbumRepository,
    private val notificationsRepository: NotificationsRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileState())
    val uiState: StateFlow<UserProfileState> = _uiState

    // Lo llama tu Screen
    private var lastRequestedUserId: String? = null
    private var cachedCurrentUser: UserInfo? = null

    fun setInitialData(userId: String) {
        val alreadyLoaded = lastRequestedUserId == userId && uiState.value.user != null
        if (alreadyLoaded) return

        lastRequestedUserId = userId
        loadUser(userId)
    }

    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    isFollowActionInProgress = false,
                    followStatusKnown = false
                )
            }
            val userResult = userRepository.getUserById(userId)
            val user = userResult.getOrElse { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "Error cargando usuario")
                }
                return@launch
            }

            val currentUid = auth.currentUser?.uid
            val canFollow = currentUid != null && currentUid != userId
            var isFollowing = false
            var followStatusKnown = false

            if (canFollow && currentUid != null) {
                val followResult = userRepository.isFollowing(currentUid, userId)
                followResult.onSuccess {
                    isFollowing = it
                    followStatusKnown = true
                }.onFailure {
                    followStatusKnown = true
                }

                if (cachedCurrentUser == null) {
                    cachedCurrentUser = userRepository.getUserById(currentUid).getOrNull()
                }
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
                        openReviewId = null,
                        errorMessage = error.message ?: "Error cargando reseñas"
                    )
                }
                return@launch
            }

            val reviewItems = buildReviewItems(reviews)
            val favoriteAlbums = reviewItems
                .filter { it.review.isFavorite }
                .mapNotNull { it.album }
                .distinctBy(AlbumInfo::id)
                .take(5)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    user = user,
                    reviews = reviews,
                    reviewItems = reviewItems,
                    favoriteAlbums = favoriteAlbums,
                    openReviewId = null,
                    canFollow = canFollow,
                    isFollowing = isFollowing,
                    followStatusKnown = if (canFollow) followStatusKnown else false
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

    fun onReviewClicked(id: String)   = _uiState.update { it.copy(openReviewId = id) }
    fun consumeOpenReview()           = _uiState.update { it.copy(openReviewId = null) }

    fun onFollowClicked() {
        val state = uiState.value
        val targetUser = state.user ?: return
        val currentUid = auth.currentUser?.uid ?: return
        if (currentUid == targetUser.id) return
        if (!state.canFollow) return

        val currentlyFollowing = state.isFollowing

        viewModelScope.launch {
            _uiState.update { it.copy(isFollowActionInProgress = true) }

            val result = if (currentlyFollowing) {
                userRepository.unfollowUser(currentUid, targetUser.id)
            } else {
                userRepository.followUser(currentUid, targetUser.id)
            }

            result.onSuccess { updatedUser ->
                _uiState.update {
                    it.copy(
                        user = updatedUser,
                        isFollowing = !currentlyFollowing,
                        isFollowActionInProgress = false,
                        followStatusKnown = true
                    )
                }

                if (!currentlyFollowing) {
                    val followerInfo = ensureCurrentUserInfo(currentUid)
                    followerInfo?.let { info ->
                        runCatching {
                            notificationsRepository.addFollowNotification(
                                userId = targetUser.id,
                                followerId = info.id,
                                followerName = info.username.ifBlank { info.name },
                                followerAvatarUrl = info.profileImageUrl
                            )
                        }
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isFollowActionInProgress = false,
                        errorMessage = error.message ?: "Error actualizando seguimiento"
                    )
                }
            }
        }
    }

    private suspend fun ensureCurrentUserInfo(currentUid: String): UserInfo? {
        cachedCurrentUser?.let { return it }
        val info = userRepository.getUserById(currentUid).getOrNull()
        cachedCurrentUser = info
        return info
    }
}
