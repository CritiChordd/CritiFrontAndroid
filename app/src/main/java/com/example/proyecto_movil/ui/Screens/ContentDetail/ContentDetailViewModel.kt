package com.example.proyecto_movil.ui.Screens.ContentDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.repository.ContentRepository
import com.example.proyecto_movil.data.datasource.AuthRemoteDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ContentDetailViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val authRemoteDataSource: AuthRemoteDataSource
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContentDetailState())
    val uiState: StateFlow<ContentDetailState> = _uiState

    fun start(contentId: String) {
        _uiState.update { it.copy(contentId = contentId) }

        viewModelScope.launch {
            contentRepository.listenContentById(contentId).collect { info ->
                if (info != null) {
                    _uiState.update { it.copy(text = info.text, likesCount = info.likesCount) }
                }
            }
        }

        val uid = authRemoteDataSource.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                val liked = contentRepository.isLiked(contentId, uid).getOrElse { false }
                _uiState.update { it.copy(isLiked = liked) }
            }
        }
    }

    fun onToggleLike() {
        val uid = authRemoteDataSource.currentUser?.uid ?: return
        val id = uiState.value.contentId
        if (id.isBlank()) return
        viewModelScope.launch {
            contentRepository.toggleLike(id, uid).onSuccess { likedNow ->
                _uiState.update { it.copy(isLiked = likedNow) }
            }
        }
    }
}

