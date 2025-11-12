package com.example.proyecto_movil.ui.Screens.Chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.ChatMessage
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
class ChatDetailViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conversationId: String = savedStateHandle.get<String>("conversationId").orEmpty()
    private val partnerId: String = savedStateHandle.get<String>("partnerId").orEmpty()
    private val usernameArg: String = savedStateHandle.get<String>("username").orEmpty()
    private val displayNameArg: String = savedStateHandle.get<String>("displayName").orEmpty()
    private val imageArg: String = savedStateHandle.get<String>("imageUrl").orEmpty()

    private val currentUserId: String = authRepository.currentUser?.uid.orEmpty()

    private val _uiState = MutableStateFlow(
        ChatDetailUiState(
            conversationId = conversationId,
            partnerId = partnerId,
            partner = if (partnerId.isNotBlank() || usernameArg.isNotBlank() || imageArg.isNotBlank()) {
                UserInfo(
                    id = partnerId,
                    name = displayNameArg,
                    username = usernameArg,
                    profileImageUrl = imageArg
                )
            } else {
                null
            }
        )
    )
    val uiState: StateFlow<ChatDetailUiState> = _uiState

    private var messagesJob: Job? = null

    init {
        if (conversationId.isBlank() || currentUserId.isBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "No se pudo cargar la conversación."
                )
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        if (partnerId.isNotBlank()) {
            loadPartner(partnerId)
            ensureConversation(partnerId)
        }

        watchMessages(conversationId)
    }

    private fun ensureConversation(partnerId: String) {
        viewModelScope.launch {
            try {
                chatRepository.ensureConversation(currentUserId, partnerId)
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "No se pudo cargar la conversación")
                }
            }
        }
    }

    private fun loadPartner(partnerId: String) {
        viewModelScope.launch {
            val result = userRepository.getUserById(partnerId)
            val user = result.getOrNull()
            if (user != null) {
                _uiState.update { it.copy(partner = user) }
            }
        }
    }

    private fun watchMessages(conversationId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            chatRepository.listenMessages(conversationId).collectLatest { messages ->
                val uiMessages = messages.map { it.toUi(currentUserId) }
                _uiState.update {
                    it.copy(messages = uiMessages, isLoading = false)
                }
            }
        }
    }

    fun onMessageTextChanged(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
    }

    fun sendCurrentMessage() {
        val text = _uiState.value.messageText.trim()
        if (text.isEmpty() || conversationId.isBlank() || currentUserId.isBlank() || partnerId.isBlank()) return

        _uiState.update { it.copy(messageText = "") }

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(conversationId, currentUserId, partnerId, text)
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

    fun deleteConversation() {
        if (conversationId.isBlank()) return
        viewModelScope.launch {
            try {
                chatRepository.deleteConversation(conversationId)
                messagesJob?.cancel()
                _uiState.update { it.copy(conversationDeleted = true) }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "No se pudo eliminar el chat")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeDeletion() {
        _uiState.update { it.copy(conversationDeleted = false) }
    }

    private fun ChatMessage.toUi(currentUserId: String): ChatMessageUi = ChatMessageUi(
        id = id,
        text = text,
        isMine = senderId == currentUserId,
        timestamp = timestamp
    )
}

data class ChatDetailUiState(
    val conversationId: String = "",
    val partnerId: String = "",
    val partner: UserInfo? = null,
    val messages: List<ChatMessageUi> = emptyList(),
    val messageText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val conversationDeleted: Boolean = false
)

data class ChatMessageUi(
    val id: String,
    val text: String,
    val isMine: Boolean,
    val timestamp: Long
)
