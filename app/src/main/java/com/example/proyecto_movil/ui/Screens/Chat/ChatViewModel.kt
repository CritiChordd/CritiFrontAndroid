package com.example.proyecto_movil.ui.Screens.Chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.ConversationSummary
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.repository.AuthRepository
import com.example.proyecto_movil.data.repository.ChatRepository
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
class ChatViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val cachedUsers = mutableMapOf<String, UserInfo>()

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private var conversationsJob: Job? = null
    private var followingJob: Job? = null

    init {
        initialize()
    }

    private fun initialize() {
        val uid = authRepository.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Debes iniciar sesión para enviar mensajes.",
                    currentUserId = ""
                )
            }
            return
        }

        _uiState.update { it.copy(currentUserId = uid, isLoading = true, errorMessage = null) }

        observeFollowing(uid)
        observeConversations(uid)
    }

    private fun observeFollowing(uid: String) {
        followingJob?.cancel()
        followingJob = viewModelScope.launch {
            userRepository.listenFollowing(uid).collectLatest { following ->
                following.forEach { cacheUser(it) }
                _uiState.update { state ->
                    state.copy(
                        following = following.sortedBy { it.username.lowercase() }
                    )
                }
            }
        }
    }

    private fun observeConversations(uid: String) {
        conversationsJob?.cancel()
        conversationsJob = viewModelScope.launch {
            chatRepository.listenConversations(uid).collectLatest { conversations ->
                val previews = buildConversationPreviews(uid, conversations)
                _uiState.update { state ->
                    state.copy(
                        conversations = previews,
                        isLoading = false
                    )
                }
            }
        }
    }

    private suspend fun buildConversationPreviews(
        uid: String,
        conversations: List<ConversationSummary>
    ): List<ConversationPreview> {
        val previews = mutableListOf<ConversationPreview>()
        for (conversation in conversations) {
            val partnerId = conversation.participantIds.firstOrNull { it != uid } ?: continue
            val partner = ensureUser(partnerId)
            previews += ConversationPreview(
                conversationId = conversation.id,
                partnerId = partnerId,
                partner = partner,
                lastMessage = conversation.lastMessage,
                lastTimestamp = conversation.lastTimestamp
            )
        }
        return previews.sortedByDescending { it.lastTimestamp }
    }

    private suspend fun ensureUser(id: String): UserInfo? {
        if (id.isBlank()) return null
        cachedUsers[id]?.let { return it }
        val result = userRepository.getUserById(id)
        val user = result.getOrNull()
        if (user != null) {
            cacheUser(user)
        }
        return user
    }

    private fun cacheUser(user: UserInfo) {
        if (user.id.isNotBlank()) {
            cachedUsers[user.id] = user
        }
    }

    fun onConversationSelected(conversationId: String) {
        val state = _uiState.value
        val preview = state.conversations.firstOrNull { it.conversationId == conversationId } ?: return
        if (preview.partnerId.isBlank()) return

        _uiState.update {
            it.copy(
                pendingConversation = PendingConversation(
                    conversationId = preview.conversationId,
                    partnerId = preview.partnerId,
                    partner = preview.partner
                )
            )
        }
    }

    fun onUserSelected(user: UserInfo) {
        if (user.id.isBlank()) return
        val currentUserId = _uiState.value.currentUserId
        if (currentUserId.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Debes iniciar sesión para enviar mensajes.")
            }
            return
        }

        cacheUser(user)
        val conversationId = chatRepository.conversationIdFor(currentUserId, user.id)
        _uiState.update {
            it.copy(
                pendingConversation = PendingConversation(
                    conversationId = conversationId,
                    partnerId = user.id,
                    partner = user
                )
            )
        }

        viewModelScope.launch {
            try {
                chatRepository.ensureConversation(currentUserId, user.id)
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "No se pudo iniciar la conversación")
                }
            }
        }
    }

    fun consumePendingNavigation() {
        _uiState.update { it.copy(pendingConversation = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

data class ChatUiState(
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val following: List<UserInfo> = emptyList(),
    val conversations: List<ConversationPreview> = emptyList(),
    val errorMessage: String? = null,
    val pendingConversation: PendingConversation? = null
)

data class ConversationPreview(
    val conversationId: String,
    val partnerId: String,
    val partner: UserInfo?,
    val lastMessage: String,
    val lastTimestamp: Long
)

data class PendingConversation(
    val conversationId: String,
    val partnerId: String,
    val partner: UserInfo?
)
