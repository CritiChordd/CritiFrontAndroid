package com.example.proyecto_movil.ui.Screens.FollowingFeed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.proyecto_movil.ui.components.ReviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowingFeedScreen(
    state: FollowingFeedState,
    onBack: () -> Unit,
    onUserClick: (String) -> Unit,
    onOpenReview: (String) -> Unit = {},
    modifier: Modifier = Modifier.testTag("followingFeedScreen")
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Seguidos") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Error",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.items) { item ->
                            ReviewCard(
                                review = item.review,
                                author = item.author,
                                onUserClick = onUserClick
                            )
                        }
                    }
                }
            }
        }
    }
}
