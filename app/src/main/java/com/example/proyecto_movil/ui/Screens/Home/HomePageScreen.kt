package com.example.proyecto_movil.uiViews.homePage

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 🔹 Sección: Novedades
            item {
                SectionRow(
                    title = "Novedades",
                    albums = viewModel.getNewReleases(),
                    onAlbumClick = { album -> viewModel.onAlbumClicked(album) }
                )
            }

            // 🔹 Sección: Nuevo entre amigos
            item {
                SectionRow(
                    title = "Nuevo entre amigos",
                    albums = state.albumList,
                    onAlbumClick = { album -> viewModel.onAlbumClicked(album) }
                )
            }

            // 🔹 Sección: Popular entre amigos
            item {
                SectionRow(
                    title = "Popular entre amigos",
                    albums = viewModel.getPopularAlbums(),
                    onAlbumClick = { album -> viewModel.onAlbumClicked(album) }
                )
            }
        }
    }

    // Efecto de navegación
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

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(albums) { album ->
                AlbumCard(album = album) { onAlbumClick(album) }
            }
        }
    }
}
