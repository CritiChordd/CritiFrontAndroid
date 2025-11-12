package com.example.proyecto_movil.data

data class ConversationSummary(
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String? = null,
    val lastTimestamp: Long = 0L
)
