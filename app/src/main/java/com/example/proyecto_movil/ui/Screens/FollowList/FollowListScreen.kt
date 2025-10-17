package com.example.proyecto_movil.ui.Screens.FollowList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


@ExperimentalMaterial3Api
@Composable
fun FollowListScreen(
    state: FollowListState,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(state.error)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.users, key = { it.id }) { user ->
                    ListItem(
                        headlineContent = { Text(user.name.ifBlank { user.username }) },
                        supportingContent = { Text("@${user.username}") },
                        leadingContent = {
                            AsyncImage(
                                model = user.profileImageUrl.ifBlank { user.avatarUrl },
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        modifier = Modifier.fillMaxWidth().clickable { onUserClick(user.id) }
                    )
                    Divider()
                }
            }
        }
    }
}
