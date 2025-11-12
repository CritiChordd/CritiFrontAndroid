package com.example.proyecto_movil.ui.Screens.Chat

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.composed
import coil.compose.AsyncImage
import com.example.proyecto_movil.data.UserInfo

@Composable
fun ChatListScreen(
    state: ChatUiState,
    onConversationSelected: (ConversationPreview) -> Unit,
    onUserSelected: (UserInfo) -> Unit,
    onClearError: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            state.errorMessage?.let { message ->
                ErrorMessage(message = message, onDismiss = onClearError)
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Personas que sigues",
                    style = MaterialTheme.typography.titleMedium
                )
                if (state.following.isNotEmpty()) {
                    FollowingRow(users = state.following, onUserSelected = onUserSelected)
                } else {
                    Text(
                        text = "Aún no sigues a nadie.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Conversaciones",
                    style = MaterialTheme.typography.titleMedium
                )
                Divider()
            }
        }

        if (state.isLoading && state.conversations.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        if (state.conversations.isEmpty() && !state.isLoading) {
            item {
                Text(
                    text = "Todavía no tienes conversaciones. ¡Empieza un chat con tus seguidos!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (state.conversations.isNotEmpty()) {
            items(state.conversations, key = { it.conversationId }) { preview ->
                ConversationItem(
                    preview = preview,
                    onClick = { onConversationSelected(preview) }
                )
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    state: ChatDetailUiState,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onBack: () -> Unit,
    onDeleteChat: () -> Unit,
    onClearError: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
            AsyncImage(
                model = state.partner?.profileImageUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.weight(1f)) {
                val title = state.partner?.username
                    ?.ifBlank { state.partner?.name }
                    ?: state.partnerId.ifBlank { "Chat" }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val subtitle = state.partner?.name
                    ?.takeIf { it.isNotBlank() && it != state.partner?.username }
                subtitle?.let { fullName ->
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Opciones")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(text = "Eliminar chat") },
                        onClick = {
                            menuExpanded = false
                            confirmDelete = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        state.errorMessage?.let { message ->
            ErrorMessage(message = message, onDismiss = onClearError)
            Spacer(modifier = Modifier.height(12.dp))
        }

        val listState = rememberLazyListState()
        Box(modifier = Modifier.weight(1f)) {
            MessagesList(messages = state.messages, listState = listState)
            if (state.isLoading && state.messages.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        MessageInput(
            messageText = state.messageText,
            onMessageTextChange = onMessageTextChange,
            onSendMessage = onSendMessage
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(text = "Eliminar chat") },
            text = { Text(text = "¿Seguro que deseas eliminar esta conversación?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDeleteChat()
                }) {
                    Text(text = "Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(text = "Cancelar")
                }
            }
        )
    }
}

@Composable
private fun ConversationItem(
    preview: ConversationPreview,
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickableWithoutRipple(onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AsyncImage(
            model = partner?.profileImageUrl,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
        )

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
                        .size(64.dp)
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        IconButton(onClick = onSendMessage, enabled = messageText.isNotBlank()) {
            Icon(imageVector = Icons.Filled.Send, contentDescription = "Enviar")
        }
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
