package com.example.proyecto_movil.ui.Screens.AlbumReviews

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.ui.components.ReviewCard
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import com.example.proyecto_movil.ui.utils.*

@Composable
fun AlbumReviewScreen(
    album: AlbumInfo,
    viewModel: AlbumReviewViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
    onArtistClick: () -> Unit = {},
    onUserClick: (String) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(album.id) {
        viewModel.setAlbumById(album.id)
    }

    LaunchedEffect(state.navigateToArtist, state.openUserId) {
        if (state.navigateToArtist) {
            onArtistClick()
            viewModel.consumeNavigateArtist()
        }
        state.openUserId?.let { id ->
            onUserClick(id)
            viewModel.consumeOpenUser()
        }
    }

    val backgroundRes =
        if (isSystemInDarkTheme()) R.drawable.fondocriti else R.drawable.fondocriti_light

    ScreenBackground(backgroundRes = backgroundRes, modifier = modifier) {
        SettingsIcon(modifier = Modifier.align(Alignment.TopEnd))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 55.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                TitleBar(text = stringResource(id = R.string.titulo_resenas))
                Spacer(Modifier.height(16.dp))
            }

            item {
                AlbumHeader(
                    coverRes = state.albumCoverRes,
                    title = state.albumTitle,
                    artist = state.albumArtist,
                    year = state.albumYear
                )
            }

            item {
                AsyncImage(
                    model = state.artistProfileRes,
                    contentDescription = "Foto de ${state.albumArtist}",
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .clickable { viewModel.onArtistClicked() },
                    contentScale = ContentScale.Crop
                )
            }

            item {
                ScoreRow(
                    scoreLabel = stringResource(id = R.string.puntaje_album),
                    usersHint = stringResource(id = R.string.cantidad_usuarios_alb),
                    scoreValue = state.avgPercent?.let { "$it%" } ?: "N/A"
                )
            }

            // Botón "Abrir en Spotify" (abre una búsqueda del álbum + artista)
            item {
                Button(
                    onClick = {

                        val query = "${state.albumTitle} ${state.albumArtist}"

                        val encoded = android.net.Uri.encode(query.trim())

                        val url = "https://open.spotify.com/search/albums/$encoded"

                        val intent = android.content.Intent(
                            android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(url)
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Abrir en Spotify"
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = "Abrir en Spotify")
                }
            }


            item {
                ClickableSectionTitle(
                    title = stringResource(id = R.string.resenas_album),
                    onSeeAll = { }
                )
            }

            if (state.reviewItems.isEmpty()) {
                item {
                    Text(
                        text = "Aún no hay reseñas para este álbum",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(state.reviewItems) { item ->
                    ReviewCard(
                        review = item.review,
                        author = item.author,
                        albumTitle = state.albumTitle,
                        albumArtist = state.albumArtist,
                        albumYear = state.albumYear,
                        albumCoverUrl = state.albumCoverRes,
                        onUserClick = viewModel::onUserClicked
                    )
                }
            }
        }
    }
}
