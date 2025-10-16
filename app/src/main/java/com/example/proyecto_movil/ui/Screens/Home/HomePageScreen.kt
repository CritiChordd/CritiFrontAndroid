package com.example.proyecto_movil.uiViews.homePage

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.ui.Screens.Home.HomeViewModel
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import com.example.proyecto_movil.ui.utils.ScreenBackground
import com.example.proyecto_movil.ui.utils.AlbumCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAlbumClick: (AlbumInfo) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light

    ScreenBackground(backgroundRes = backgroundRes, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Text(
                        text = "Feed en vivo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(8.dp))
                }
                items(state.contentFeed) { post ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(text = post.text, color = MaterialTheme.colorScheme.onBackground)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Likes: ${post.likesCount}",
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        val liked = state.likedMap[post.id] == true
                        Button(onClick = { viewModel.onToggleLike(post.id) }) {
                            Text(if (liked) "Quitar Like" else "Like")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    SectionRow(
                        title = "Novedades",
                        albums = viewModel.getNewReleases(),
                        onAlbumClick = { album -> viewModel.onAlbumClicked(album) }
                    )
                }
                item {
                    SectionRow(
                        title = "Nuevo entre amigos",
                        albums = state.albumList,
                        onAlbumClick = { album -> viewModel.onAlbumClicked(album) }
                    )
                }
                item {
                    SectionRow(
                        title = "Popular entre amigos",
                        albums = viewModel.getPopularAlbums(),
                        onAlbumClick = { album -> viewModel.onAlbumClicked(album) }
                    )
                }
            }
        }
    }

    // Side-effects: abrir álbum
    LaunchedEffect(state.openAlbum) {
        state.openAlbum?.let {
            onAlbumClick(it)
            viewModel.consumeOpenAlbum()
        }
    }
}

/* ---------- Subcomponentes ---------- */

@Composable
private fun SectionRow(
    title: String,
    albums: List<AlbumInfo>,
    onAlbumClick: (AlbumInfo) -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            albums.forEach { album ->
                AlbumCard(album = album) { onAlbumClick(album) }
            }
        }
    }
}


