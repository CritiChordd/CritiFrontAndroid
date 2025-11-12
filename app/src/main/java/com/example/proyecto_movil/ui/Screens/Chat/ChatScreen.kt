package com.example.proyecto_movil.ui.Screens.Chat

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.composed
import coil.compose.AsyncImage
import com.example.proyecto_movil.data.UserInfo

@Composable
fun ChatScreen(
    state: ChatUiState,
    onConversationSelected: (ConversationPreview) -> Unit,
    onUserSelected: (UserInfo) -> Unit,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onBackFromConversation: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        state.errorMessage?.let { message ->
            ErrorMessage(message = message, onDismiss = onClearError)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Conversaciones",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        ConversationList(
            conversations = state.conversations,
            onConversationSelected = onConversationSelected,
            selectedConversationId = state.selectedConversationId
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (state.following.isNotEmpty()) {
            Text(
                text = "Personas que sigues",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            FollowingRow(users = state.following, onUserSelected = onUserSelected)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            if (state.selectedUser != null) {
                ConversationDetail(
                    user = state.selectedUser,
                    messages = state.messages,
                    messageText = state.messageText,
                    onMessageTextChange = onMessageTextChange,
                    onSendMessage = onSendMessage,
                    onBack = onBackFromConversation
                )
            } else {
                EmptyConversationPlaceholder()
            }
        }
    }
}

@Composable
private fun ConversationList(
    conversations: List<ConversationPreview>,
    onConversationSelected: (ConversationPreview) -> Unit,
    selectedConversationId: String?
) {
    if (conversations.isEmpty()) {
        Text(
            text = "Todavía no tienes conversaciones. ¡Empieza un chat con tus seguidos!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp)
    ) {
        items(conversations, key = { it.conversationId }) { preview ->
            ConversationItem(
                preview = preview,
                isSelected = preview.conversationId == selectedConversationId,
                onClick = { onConversationSelected(preview) }
            )
        }
    }
}

@Composable
private fun ConversationItem(
    preview: ConversationPreview,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val partner = preview.partner
    val title = remember(partner) {
        when {
            partner == null -> "Usuario"
            partner.username.isNotBlank() -> partner.username
            partner.name.isNotBlank() -> partner.name
            else -> partner.id
        }
    }

    val relativeTime = remember(preview.lastTimestamp) {
        if (preview.lastTimestamp == 0L) "" else DateUtils.getRelativeTimeSpanString(
            preview.lastTimestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }

    val background = if (isSelected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickableWithoutRipple(onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .heightIn(min = 56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = partner?.profileImageUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = preview.lastMessage.ifBlank { "Nuevo chat" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (relativeTime.isNotEmpty()) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = relativeTime,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FollowingRow(
    users: List<UserInfo>,
    onUserSelected: (UserInfo) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(users, key = { it.id }) { user ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "Avatar de ${user.username}",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickableWithoutRipple { onUserSelected(user) }
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = user.username.ifBlank { user.name.ifBlank { user.id } },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ConversationDetail(
    user: UserInfo,
    messages: List<ChatMessageUi>,
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = user.username.ifBlank { user.name.ifBlank { "Chat" } },
                style = MaterialTheme.typography.titleMedium
            )
        }

        val listState = rememberLazyListState()
        Box(modifier = Modifier.weight(1f)) {
            MessagesList(messages = messages, listState = listState)
        }

        MessageInput(
            messageText = messageText,
            onMessageTextChange = onMessageTextChange,
            onSendMessage = onSendMessage
        )
    }
}

@Composable
private fun MessagesList(messages: List<ChatMessageUi>, listState: LazyListState) {
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message)
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessageUi) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = if (message.isMine) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (message.isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun MessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = onMessageTextChange,
            modifier = Modifier.weight(1f),
            maxLines = 4,
            placeholder = { Text(text = "Escribe un mensaje") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSendMessage() })
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onSendMessage, enabled = messageText.isNotBlank()) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = "Enviar")
        }
    }
}

@Composable
private fun EmptyConversationPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Selecciona una conversación o inicia un chat nuevo desde tus seguidos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorMessage(message: String, onDismiss: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            TextButton(onClick = onDismiss) {
                Text(text = "Cerrar", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

private fun Modifier.clickableWithoutRipple(onClick: () -> Unit): Modifier =
    composed {
        clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    }
