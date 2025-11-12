package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.ChatMessage
import com.example.proyecto_movil.data.ConversationSummary
import com.example.proyecto_movil.data.datasource.ChatRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val remoteDataSource: ChatRemoteDataSource
) {
    fun listenConversations(userId: String): Flow<List<ConversationSummary>> =
        remoteDataSource.listenConversations(userId)

    fun listenMessages(conversationId: String): Flow<List<ChatMessage>> =
        remoteDataSource.listenMessages(conversationId)

    suspend fun ensureConversation(userA: String, userB: String): String =
        remoteDataSource.ensureConversation(userA, userB)

    suspend fun sendMessage(
        conversationId: String,
        fromId: String,
        toId: String,
        text: String
    ) = remoteDataSource.sendMessage(conversationId, fromId, toId, text)

    fun conversationIdFor(userA: String, userB: String): String =
        listOf(userA, userB).sorted().joinToString(separator = "_")
}
