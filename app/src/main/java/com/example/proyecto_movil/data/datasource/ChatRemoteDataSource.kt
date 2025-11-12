package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.ChatMessage
import com.example.proyecto_movil.data.ConversationSummary
import kotlinx.coroutines.flow.Flow

interface ChatRemoteDataSource {
    fun listenConversations(userId: String): Flow<List<ConversationSummary>>
    fun listenMessages(conversationId: String): Flow<List<ChatMessage>>
    suspend fun ensureConversation(userA: String, userB: String): String
    suspend fun sendMessage(
        conversationId: String,
        fromId: String,
        toId: String,
        text: String
    )
}
