package com.example.proyecto_movil.ui.Screens.UserProfile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo

@Composable
fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    user: UserInfo,
    reviews: List<ReviewInfo>,
    onBackClick: () -> Unit = {},
    onAlbumClick: (AlbumInfo) -> Unit = {},
    onReviewClick: (ReviewInfo) -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(
        state.navigateBack,
        state.navigateToSettings,
        state.navigateToEditProfile,
        state.openAlbumId,
        state.openReview
    ) {
        if (state.navigateBack) {
            onBackClick()
            viewModel.consumeBack()
        }
        if (state.navigateToSettings) {
            onSettingsClick()
            viewModel.consumeSettings()
        }
        if (state.navigateToEditProfile) {
            onEditProfile()
            viewModel.consumeEdit()
        }
        state.openAlbumId?.let { albumId ->
            state.favoriteAlbums.firstOrNull { it.id == albumId }?.let { onAlbumClick(it) }
            viewModel.consumeOpenAlbum()
        }
        // Si openReview es Int? (índice) en tu VM:
        state.openReview?.let { idx ->
            val review = state.reviews.getOrNull(idx)
            if (review != null) onReviewClick(review)
            viewModel.consumeOpenReview()
        }
    }

    // Mapa de álbumes para resolver reseñas -> usa los playlists del usuario
    val albumMap = remember(user.playlists) {
        user.playlists
            .flatMap { it.albums }
            .associateBy { it.id.toString() }
    }

    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Atrás",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { viewModel.onBackClicked() }
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { viewModel.onSettingsClicked() }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ---------- Header: usa datos de 'user' (NO del state) ----------
                val avatar: String = user.avatarUrl.ifEmpty { "https://placehold.co/120x120" }
                AsyncImage(
                    model = avatar,
                    contentDescription = user.username,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = user.username, // String explícito -> sin ambigüedad
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${user.followers} seguidores • ${user.following} siguiendo",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.onEditProfileClicked() },
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Editar perfil", color = MaterialTheme.colorScheme.onPrimaryContainer)
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (state.favoriteAlbums.isNotEmpty()) {
                    Text(
                        "Tus álbumes favoritos",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) {
                        items(state.favoriteAlbums.size) { index ->
                            val album = state.favoriteAlbums[index]
                            Column(
                                modifier = Modifier
                                    .width(120.dp)
                                    .clickable {
                                        // VM espera Int (id)
                                        viewModel.onAlbumClicked(album.id)
                                        onAlbumClick(album)
                                    },
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                AsyncImage(
                                    model = album.coverUrl,
                                    contentDescription = album.title,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(Modifier.height(6.dp))
                                Text(
                                    album.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    fontSize = 14.sp
                                )
                                Text(
                                    album.artist.name,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Tus reseñas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // forEachIndexed para tener el índice (Int) que el VM espera
                    state.reviews.forEachIndexed { idx, review ->
                        val album = albumMap[review.albumId]
                        if (album != null) {
                            ReviewItem(
                                review = review,
                                album = album,
                                onClick = {
                                    viewModel.onReviewClicked(idx) // VM: Int
                                    onReviewClick(review)          // UI: ReviewInfo
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* navegación extra */ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver reseñas y playlists", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewInfo, album: AlbumInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = album.coverUrl,
            contentDescription = album.title,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                album.title.uppercase(),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                album.artist.name.uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            OutlinedButton(
                onClick = onClick,
                shape = RoundedCornerShape(50),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Text("Ver reseña", fontSize = 12.sp)
            }
        }
        Spacer(Modifier.width(8.dp))
        val scoreColor =
            if (review.score >= 7) Color(0xFF2E7D32)
            else if (review.score >= 5) Color(0xFFF9A825)
            else Color(0xFFC62828)
        Surface(color = scoreColor, shape = RoundedCornerShape(6.dp)) {
            Text(
                text = "${(review.score * 10)}%",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
