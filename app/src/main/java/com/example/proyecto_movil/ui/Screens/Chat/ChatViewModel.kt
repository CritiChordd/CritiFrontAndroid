package com.example.proyecto_movil.ui.Screens.Chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.ChatMessage
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
    private var messagesJob: Job? = null

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
                    val selectedConversationId = state.selectedConversationId
                    val selectedUser = selectedConversationId?.let { selectedId ->
                        previews.firstOrNull { it.conversationId == selectedId }?.partner
                            ?: state.selectedUser
                    } ?: state.selectedUser

                    state.copy(
                        conversations = previews,
                        isLoading = false,
                        selectedUser = selectedUser
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
        val user = preview.partner
        if (state.currentUserId.isBlank() || preview.partnerId.isBlank()) return

        _uiState.update {
            it.copy(
                selectedConversationId = preview.conversationId,
                selectedUser = user,
                messages = emptyList()
            )
        }

        if (user == null) {
            viewModelScope.launch {
                val fetched = ensureUser(preview.partnerId)
                if (fetched != null) {
                    _uiState.update { current ->
                        if (current.selectedConversationId == preview.conversationId) {
                            current.copy(selectedUser = fetched)
                        } else {
                            current
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            chatRepository.ensureConversation(state.currentUserId, preview.partnerId)
        }
        watchMessages(preview.conversationId)
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
                selectedConversationId = conversationId,
                selectedUser = user,
                messages = emptyList()
            )
        }

        viewModelScope.launch {
            chatRepository.ensureConversation(currentUserId, user.id)
        }
        watchMessages(conversationId)
    }

    private fun watchMessages(conversationId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.listenMessages(conversationId).collectLatest { messages ->
                val currentUser = _uiState.value.currentUserId
                val uiMessages = messages.map { it.toUi(currentUser) }
                _uiState.update { it.copy(messages = uiMessages) }
            }
        }
    }

    fun onMessageTextChanged(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
    }

    fun sendCurrentMessage() {
        val state = _uiState.value
        val text = state.messageText.trim()
        val toUser = state.selectedUser ?: return
        val fromId = state.currentUserId
        if (text.isEmpty() || fromId.isBlank() || toUser.id.isBlank()) return

        val conversationId = state.selectedConversationId
            ?: chatRepository.conversationIdFor(fromId, toUser.id)

        _uiState.update { it.copy(messageText = "") }

        viewModelScope.launch {
            try {
                chatRepository.ensureConversation(fromId, toUser.id)
                chatRepository.sendMessage(conversationId, fromId, toUser.id, text)
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        errorMessage = e.message ?: "No se pudo enviar el mensaje",
                        messageText = text
                    )
                }
            }
        }
    }

    fun onBackFromConversation() {
        messagesJob?.cancel()
        _uiState.update {
            it.copy(
                selectedConversationId = null,
                selectedUser = null,
                messages = emptyList()
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun ChatMessage.toUi(currentUserId: String): ChatMessageUi = ChatMessageUi(
        id = id,
        text = text,
        isMine = senderId == currentUserId,
        timestamp = timestamp
    )
}

data class ChatUiState(
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val following: List<UserInfo> = emptyList(),
    val conversations: List<ConversationPreview> = emptyList(),
    val selectedConversationId: String? = null,
    val selectedUser: UserInfo? = null,
    val messages: List<ChatMessageUi> = emptyList(),
    val messageText: String = "",
    val errorMessage: String? = null
)

data class ConversationPreview(
    val conversationId: String,
    val partnerId: String,
    val partner: UserInfo?,
    val lastMessage: String,
    val lastTimestamp: Long
)

data class ChatMessageUi(
    val id: String,
    val text: String,
    val isMine: Boolean,
    val timestamp: Long
)
