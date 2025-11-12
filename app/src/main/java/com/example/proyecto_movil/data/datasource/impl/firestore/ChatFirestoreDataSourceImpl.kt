package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.ChatMessage
import com.example.proyecto_movil.data.ConversationSummary
import com.example.proyecto_movil.data.datasource.ChatRemoteDataSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatFirestoreDataSourceImpl(
    private val db: FirebaseFirestore
) : ChatRemoteDataSource {

    override fun listenConversations(userId: String): Flow<List<ConversationSummary>> = callbackFlow {
        if (userId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection(CONVERSATIONS_COLLECTION)
            .whereArrayContains(PARTICIPANTS_FIELD, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val conversations = snapshot?.documents?.map { document ->
                    val data = document.data.orEmpty()
                    ConversationSummary(
                        id = document.id,
                        participantIds = (data[PARTICIPANTS_FIELD] as? List<*>)
                            ?.mapNotNull { it?.toString() }
                            ?: emptyList(),
                        lastMessage = data[LAST_MESSAGE_FIELD]?.toString().orEmpty(),
                        lastSenderId = data[LAST_SENDER_FIELD]?.toString(),
                        lastTimestamp = (data[UPDATED_AT_FIELD] as? com.google.firebase.Timestamp)
                            ?.toDate()?.time ?: 0L
                    )
                }?.sortedByDescending { it.lastTimestamp } ?: emptyList()

                trySend(conversations).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override fun listenMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (conversationId.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection(CONVERSATIONS_COLLECTION)
            .document(conversationId)
            .collection(MESSAGES_COLLECTION)
            .orderBy(TIMESTAMP_FIELD, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.map { document ->
                    val data = document.data.orEmpty()
                    ChatMessage(
                        id = document.id,
                        senderId = data[SENDER_FIELD]?.toString().orEmpty(),
                        receiverId = data[RECEIVER_FIELD]?.toString().orEmpty(),
                        text = data[TEXT_FIELD]?.toString().orEmpty(),
                        timestamp = (data[TIMESTAMP_FIELD] as? com.google.firebase.Timestamp)
                            ?.toDate()?.time ?: 0L
                    )
                } ?: emptyList()

                trySend(messages).isSuccess
            }

        awaitClose { registration.remove() }
    }

    override suspend fun ensureConversation(userA: String, userB: String): String {
        val conversationId = conversationIdFor(userA, userB)
        if (conversationId.isBlank()) return ""

        val conversationRef = db.collection(CONVERSATIONS_COLLECTION).document(conversationId)
        val snapshot = conversationRef.get().await()
        if (!snapshot.exists()) {
            val data = mapOf(
                PARTICIPANTS_FIELD to listOf(userA, userB),
                CREATED_AT_FIELD to FieldValue.serverTimestamp(),
                UPDATED_AT_FIELD to FieldValue.serverTimestamp(),
                LAST_MESSAGE_FIELD to "",
                LAST_SENDER_FIELD to ""
            )
            conversationRef.set(data, SetOptions.merge()).await()
        }
        return conversationId
    }

    override suspend fun sendMessage(
        conversationId: String,
        fromId: String,
        toId: String,
        text: String
    ) {
        if (conversationId.isBlank() || text.isBlank() || fromId.isBlank() || toId.isBlank()) return

        val conversationRef = db.collection(CONVERSATIONS_COLLECTION).document(conversationId)
        val now = FieldValue.serverTimestamp()

        val messageData = mapOf(
            SENDER_FIELD to fromId,
            RECEIVER_FIELD to toId,
            TEXT_FIELD to text,
            TIMESTAMP_FIELD to now
        )

        conversationRef.collection(MESSAGES_COLLECTION).add(messageData).await()

        val updateData = mapOf(
            PARTICIPANTS_FIELD to listOf(fromId, toId),
            LAST_MESSAGE_FIELD to text,
            LAST_SENDER_FIELD to fromId,
            UPDATED_AT_FIELD to now
        )

        conversationRef.set(updateData, SetOptions.merge()).await()
    }

    private fun conversationIdFor(userA: String, userB: String): String {
        if (userA.isBlank() || userB.isBlank()) return ""
        return listOf(userA, userB).sorted().joinToString(separator = "_")
    }

    companion object {
        private const val CONVERSATIONS_COLLECTION = "conversations"
        private const val MESSAGES_COLLECTION = "messages"
        private const val PARTICIPANTS_FIELD = "participantIds"
        private const val LAST_MESSAGE_FIELD = "lastMessage"
        private const val LAST_SENDER_FIELD = "lastSenderId"
        private const val UPDATED_AT_FIELD = "updatedAt"
        private const val CREATED_AT_FIELD = "createdAt"
        private const val SENDER_FIELD = "senderId"
        private const val RECEIVER_FIELD = "receiverId"
        private const val TEXT_FIELD = "text"
        private const val TIMESTAMP_FIELD = "timestamp"
    }
}
