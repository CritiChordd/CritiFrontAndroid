package com.example.proyecto_movil.ui.Screens.ReviewDetail

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ReviewDetailVM"
private const val REVIEWS = "reviews"
private const val LIKES = "likes"

@HiltViewModel
class ReviewDetailViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val albumRepository: AlbumRepository,
    private val firestore: FirebaseFirestore,
    private val notificationsRepository: com.example.proyecto_movil.data.repository.NotificationsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewDetailState())
    val uiState: StateFlow<ReviewDetailState> = _uiState

    private var currentReviewId: String = ""
    private var currentUserId: String = ""

    fun load(reviewId: String, userId: String) {
        currentReviewId = reviewId
        currentUserId = userId

        viewModelScope.launch {
            // 1?? Obtener review base
            val reviewRes = reviewRepository.getReviewById(reviewId)
            val review = reviewRes.getOrNull()

            if (review != null) {
                _uiState.update { it.copy(review = review, likes = review.likesCount ?: 0) }
            } else {
                Log.e(TAG, "getReviewById error: ${reviewRes.exceptionOrNull()}")
            }

            // 2) Cargar autor (preferir firebaseUserId)
            val authorId = _uiState.value.review?.firebaseUserId
                ?: _uiState.value.review?.userId
                ?: ""
            if (authorId.isNotBlank()) {
                val authorRes = userRepository.getUserById(authorId)
                _uiState.update { s -> s.copy(author = authorRes.getOrNull()) }
            }

            // 3?? Cargar álbum
            val albumId = _uiState.value.review?.albumId
            if (albumId != null) {
                val albumRes = albumRepository.getAlbumById(albumId)
                _uiState.update { s -> s.copy(album = albumRes.getOrNull()) }
            }

            // 4) Listener en tiempo real al conteo de likes basado en la subcolección
            firestore.collection(REVIEWS).document(reviewId)
                .collection(LIKES)
                .addSnapshotListener { snap, _ ->
                    val count = snap?.size() ?: 0
                    _uiState.update { st ->
                        val r = st.review
                        st.copy(
                            likes = count,
                            review = r?.copy(likesCount = count)
                        )
                    }
                }

            // 5?? Listener para saber si el usuario ya dio like
            firestore.collection(REVIEWS).document(reviewId)
                .collection(LIKES).document(userId)
                .addSnapshotListener { likeDoc, _ ->
                    val liked = likeDoc?.exists() == true
                    _uiState.update { st ->
                        val r = st.review
                        st.copy(
                            review = r?.copy(liked = liked)
                        )
                    }
                }
        }
    }

    fun toggleLike() {
        val reviewId = currentReviewId
        val userId = currentUserId
        if (reviewId.isBlank() || userId.isBlank()) return

        val wasLiked = _uiState.value.review?.liked == true
        val delta = if (wasLiked) -1 else +1

        _uiState.update { st ->
            val r = st.review
            st.copy(
                likes = (st.likes + delta).coerceAtLeast(0),
                review = r?.copy(liked = !wasLiked, likesCount = (r?.likesCount ?: 0) + delta)
            )
        }

        viewModelScope.launch {
            val res = reviewRepository.sendOrDeleteReviewLike(reviewId, userId)
            res.exceptionOrNull()?.let {
                Log.e(TAG, "toggleLike error", it)
                _uiState.update { st ->
                    val r = st.review
                    st.copy(
                        likes = (st.likes - delta).coerceAtLeast(0),
                        review = r?.copy(liked = wasLiked, likesCount = (r?.likesCount ?: 0) - delta)
                    )
                }
            } ?: run {
                // En caso de LIKE (no UNLIKE), crear notificación como con 'seguir'
                if (!wasLiked) {
                    val st = _uiState.value
                    val review = st.review
                    val authorUid = review?.firebaseUserId ?: review?.userId.orEmpty()
                    val isSelf = authorUid.isNotBlank() && authorUid == userId
                    if (review != null && authorUid.isNotBlank() && !isSelf) {
                        // Obtener nombre del que dio like
                        val likerInfo = runCatching {
                            userRepository.getUserById(userId).getOrNull()
                        }.getOrNull()

                        val likerName = likerInfo?.username?.takeIf { it.isNotBlank() }
                            ?: likerInfo?.name?.takeIf { it.isNotBlank() }
                            ?: "Alguien"
                        val likerAvatarUrl = likerInfo?.avatarUrl.orEmpty()

                        val snippet = review.content.takeIf { it.isNotBlank() }?.let { it.take(80) }

                        runCatching {
                            notificationsRepository.addLikeNotification(
                                userId = authorUid,
                                reviewId = review.id,
                                likerId = userId,
                                likerName = likerName,
                                likerAvatarUrl = likerAvatarUrl,
                                reviewSnippet = snippet
                            )
                        }
                    }
                }
            }
        }
    }
}
