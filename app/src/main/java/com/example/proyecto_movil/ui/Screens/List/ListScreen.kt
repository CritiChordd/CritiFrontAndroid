package com.example.proyecto_movil.ui.Screens.Lists

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import com.example.proyecto_movil.ui.utils.AlbumCard
import com.example.proyecto_movil.ui.utils.ScreenBackground
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: ListViewModel = hiltViewModel(),
    playlistId: String,                    // <- pásalo desde tu NavHost
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onOpenAlbum: (Int) -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar playlist cuando se abre la pantalla
    LaunchedEffect(playlistId) {
        viewModel.loadPlaylist(playlistId)
    }

    // Manejo de navegación
    LaunchedEffect(state.navigateBack, state.navigateToSettings, state.openAlbumId) {
        if (state.navigateBack) { onBack(); viewModel.consumeBack() }
        if (state.navigateToSettings) { onSettings(); viewModel.consumeSettings() }
        state.openAlbumId?.let { onOpenAlbum(it); viewModel.consumeOpenAlbum() }
    }

    // Mostrar errores en Snackbar
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light

    ScreenBackground(backgroundRes = backgroundRes, modifier = modifier) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { pv ->
            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.errorMessage != null -> {
                    Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                        Text(text = state.errorMessage ?: "Error desconocido")
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .padding(pv)
                    ) {
                        HeaderSection(
                            title = state.title,
                            onBack = { viewModel.onBackClicked() },
                            onSettings = { viewModel.onSettingsClicked() }
                        )
                        ListInfoSection(
                            creatorName = state.creatorName,
                            creatorAvatarRes = state.creatorAvatarRes,
                            title = state.title,
                            description = state.description,
                            likes = state.likes,
                            listenPercent = state.listenPercent
                        )
                        AlbumGrid(
                            albums = state.albums,
                            onOpen = { id -> viewModel.onAlbumClicked(id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    title: String,
    onBack: () -> Unit,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Volver",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(30.dp)
                .clickable { onBack() }
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Ajustes",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(30.dp)
                .clickable { onSettings() }
        )
    }
}

@Composable
private fun ListInfoSection(
    creatorName: String,
    creatorAvatarRes: Int,
    title: String,
    description: String,
    likes: Int,
    listenPercent: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (creatorAvatarRes != 0) {
                Image(
                    painter = painterResource(id = creatorAvatarRes),
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = creatorName.ifBlank { "—" },
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 28.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Likes",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "$likes likes",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = "Reproducciones",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = listenPercent,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun AlbumGrid(
    albums: List<AlbumInfo>,
    onOpen: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            items = albums,
            key = { it.id }
        ) { album ->
            AlbumCard(
                album = album,
                onClick = { onOpen(album.id) },
                onReviewProfileImageClicked = TODO()
            )
        }
    }
}

// Preview solo de la parte de UI (sin ViewModel real)
@Composable
private fun ListScreenPreviewBody() {
    val albums = listOf(
        AlbumInfo(
            id = 1, title = "2000", year = "2000",
            coverUrl = "https://picsum.photos/200",
            artist = ArtistInfo(1, "Bad Bunny", "https://picsum.photos/60", "Urbano")
        ),
        AlbumInfo(
            id = 2, title = "GOODFORYOU", year = "2023",
            coverUrl = "https://picsum.photos/201",
            artist = ArtistInfo(2, "Xokas", "https://picsum.photos/61", "Pop")
        )
    )

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        HeaderSection(title = "Mi Playlist", onBack = {}, onSettings = {})
        ListInfoSection(
            creatorName = "el.xokas",
            creatorAvatarRes = R.drawable.xocas,
            title = "Mi Playlist",
            description = "Descripción de ejemplo",
            likes = 2,
            listenPercent = "100%"
        )
        AlbumGrid(albums = albums, onOpen = {})
    }
}
