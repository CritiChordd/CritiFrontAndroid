package com.example.proyecto_movil.ui.Screens.FollowingFeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.repository.AuthRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FollowingFeedViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(FollowingFeedState())
    val uiState: StateFlow<FollowingFeedState> = _ui

    private var listenJob: Job? = null

    fun start() {
        if (listenJob != null) return
        val uid = authRepository.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _ui.update { it.copy(isLoading = false, error = "Inicia sesión para ver el feed de seguidos.") }
            return
        }
        listenJob = viewModelScope.launch {
            userRepository.listenFollowing(uid).collectLatest { following ->
                loadFeedFromFollowing(following)
            }
        }
    }

    private suspend fun loadFeedFromFollowing(following: List<UserInfo>) {
        _ui.update { it.copy(isLoading = true, error = null) }

        val ids = following.map { it.id }.filter { it.isNotBlank() }.distinct()
        if (ids.isEmpty()) {
            _ui.update { it.copy(isLoading = false, items = emptyList()) }
            return
        }

        // 1) Traer reseñas de cada seguido
        val allReviews = mutableListOf<ReviewInfo>()
        for (id in ids) {
            reviewRepository.getReviewsByUserId(id).onSuccess { list ->
                allReviews += list
            }
        }

        // 2) Ordenar por fecha (si createdAt viene como millis en string)
        fun ts(s: String): Long = s.toLongOrNull() ?: 0L
        val sorted = allReviews.sortedByDescending { ts(it.createdAt) }

        // 3) Mapa de autores: usa los seguidos y completa si falta alguno
        val authors = following.associateBy { it.id }.toMutableMap()
        val missing = sorted.mapNotNull { r ->
            when {
                !r.firebaseUserId.isNullOrBlank() -> r.firebaseUserId
                r.userId.isNotBlank() -> r.userId
                else -> null
            }
        }.distinct().filter { !authors.containsKey(it) }

        for (id in missing) {
            userRepository.getUserById(id).onSuccess { u -> authors[id] = u }
        }

        val items = sorted.map { r ->
            val authorId = when {
                !r.firebaseUserId.isNullOrBlank() -> r.firebaseUserId!!
                r.userId.isNotBlank() -> r.userId
                else -> ""
            }
            FeedItem(review = r, author = authors[authorId])
        }

        _ui.update { it.copy(isLoading = false, items = items) }
    }

    fun onUserClicked(id: String) { _ui.update { it.copy(openUserId = id) } }
    fun consumeOpenUser() { _ui.update { it.copy(openUserId = null) } }

    fun onReviewClicked(id: String) { _ui.update { it.copy(openReviewId = id) } }
    fun consumeOpenReview() { _ui.update { it.copy(openReviewId = null) } }
}
