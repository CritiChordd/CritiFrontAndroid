package com.example.proyecto_movil.uiViews.homePage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.ui.Screens.Home.HomeViewModel
import com.example.proyecto_movil.ui.utils.ScreenBackground
import com.example.proyecto_movil.ui.utils.AlbumCard

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    onAlbumClick: (AlbumInfo) -> Unit = {},
    onReviewProfileImageClicked: (String) -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onFollowingFeedClick: () -> Unit = {}      // ← NUEVO
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
            item {
                SearchSection(
                    isSearchActive = state.isSearchActive,
                    searchQuery = state.searchQuery,
                    isSearching = state.isSearching,
                    albumResults = state.searchAlbumResults,
                    userResults = state.searchUserResults,
                    errorMessage = state.searchError,
                    onToggleSearch = viewModel::toggleSearch,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onClearQuery = viewModel::clearSearch,
                    onAlbumClick = viewModel::onAlbumResultClicked,
                    onUserClick = viewModel::onUserResultClicked,
                    onNotificationsClick = onNotificationsClick,
                    onFollowingFeedClick = onFollowingFeedClick   // ← NUEVO
                )
            }

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

    // Efectos de navegación
    LaunchedEffect(state.openAlbum) {
        state.openAlbum?.let {
            onAlbumClick(it)
            viewModel.consumeOpenAlbum()
        }
    }

    LaunchedEffect(state.navigateToUserId) {
        state.navigateToUserId?.let { uid ->
            onReviewProfileImageClicked(uid)
            viewModel.consumeNavigateToUser()
        }
    }
}

/* ---------- Subcomponentes ---------- */

@Composable
private fun SearchSection(
    isSearchActive: Boolean,
    searchQuery: String,
    isSearching: Boolean,
    albumResults: List<AlbumInfo>,
    userResults: List<UserInfo>,
    errorMessage: String?,
    onToggleSearch: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onAlbumClick: (AlbumInfo) -> Unit,
    onUserClick: (UserInfo) -> Unit,
    onNotificationsClick: () -> Unit,
    onFollowingFeedClick: () -> Unit       // ← NUEVO
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
            keyboardController?.show()
        } else {
            keyboardController?.hide()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Ver notificaciones"
                    )
                }
                // ← NUEVO: Botón para abrir el feed de seguidos
                IconButton(onClick = onFollowingFeedClick) {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "Feed de seguidos"
                    )
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Explorar",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onToggleSearch) {
                Icon(
                    imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Filled.Search,
                    contentDescription = if (isSearchActive) "Cerrar búsqueda" else "Buscar usuarios o álbumes"
                )
            }
        }

        if (isSearchActive) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                label = { Text("Buscar usuarios o álbumes") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = onClearQuery) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedLabelColor = MaterialTheme.colorScheme.primary
                )
            )

            if (isSearching) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val hasAlbumResults = albumResults.isNotEmpty()
            val hasUserResults = userResults.isNotEmpty()

            if (hasAlbumResults) {
                Text(
                    text = "Álbumes",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    albumResults.forEach { album ->
                        AlbumResultRow(album = album, onClick = onAlbumClick)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (hasUserResults) {
                Text(
                    text = "Usuarios",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    userResults.forEach { user ->
                        UserResultRow(user = user, onClick = onUserClick)
                    }
                }
            }

            if (!isSearching && !hasAlbumResults && !hasUserResults && errorMessage != null) {
                val isInfo = errorMessage.contains("No se encontraron", ignoreCase = true)
                Text(
                    text = errorMessage,
                    color = if (isInfo) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun AlbumResultRow(album: AlbumInfo, onClick: (AlbumInfo) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onClick(album) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = album.coverUrl.ifBlank { "https://placehold.co/100x100" },
                contentDescription = "Carátula de ${album.title}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = album.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = album.artist.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun UserResultRow(user: UserInfo, onClick: (UserInfo) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .clickable { onClick(user) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = user.profileImageUrl.ifBlank { "https://placehold.co/100x100" },
                contentDescription = "Avatar de ${user.username}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.medium),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name.ifBlank { user.username },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (user.username.isNotBlank()) {
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
                AlbumCard(
                    album = album,
                    onClick = { onAlbumClick(album) }
                )
            }
        }
    }
}
