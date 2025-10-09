package com.example.proyecto_movil.ui.Screens.UserProfile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.PlaylistInfo
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import kotlin.math.roundToInt

@Composable
fun UserProfileScreen(
    state: UserProfileState,
    user: UserInfo,
    onBackClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onReviewProfileImageClicked: (String) -> Unit,
    onEditProfile: () -> Unit = {},
    onAlbumSelected: (Int) -> Unit = {},
    onReviewSelected: (Int) -> Unit = {}
) {
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
                            .clickable { onBackClick() }
                    )
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onSettingsClick() }
                    )
                }
            },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ---------- Header: usa datos de 'user' (NO del state) ----------
                        val avatar: String = user.avatarUrl.ifEmpty { "https://placehold.co/120x120" }
                        val displayName = remember(user) {
                            user.username.ifBlank {
                                user.id.takeIf { it.isNotBlank() } ?: "Usuario"
                            }
                        }
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
                            text = displayName, // String explícito -> sin ambigüedad
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
                            onClick = { onEditProfile() },
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Editar perfil")
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (state.favoriteAlbums.isNotEmpty()) {
                            val favoritesTitle = "Tus álbumes favoritos (${state.favoriteAlbums.size}/5)"
                            Text(
                                favoritesTitle,
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
                                                onAlbumSelected(album.id)
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
                            state.reviewItems.forEachIndexed { idx, item ->
                                ReviewItem(
                                    item = item,
                                    onClick = { onReviewSelected(idx) }
                                )
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
    }
}

@Composable
private fun ReviewItem(item: UserReviewUi, onClick: () -> Unit) {
    val review = item.review
    val album = item.album
    val coverUrl = album?.coverUrl ?: "https://placehold.co/160x160"
    val albumTitle = album?.title ?: "Álbum desconocido"
    val artistName = album?.artist?.name ?: "Artista desconocido"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coverUrl,
            contentDescription = albumTitle,
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(6.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                albumTitle.uppercase(),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                artistName.uppercase(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            if (review.content.isNotBlank()) {
                Text(
                    review.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
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
        if (review.isFavorite) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Reseña favorita",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
        val scoreColor =
            if (review.score >= 7) Color(0xFF2E7D32)
            else if (review.score >= 5) Color(0xFFF9A825)
            else Color(0xFFC62828)
        Surface(color = scoreColor, shape = RoundedCornerShape(6.dp)) {
            Text(
                text = "${(review.score * 10).roundToInt()}%",
                color = Color.White,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun UserProfileScreenPreview() {
    val sampleArtist = ArtistInfo(
        id = 1,
        name = "Luna Nova",
        profileImageUrl = "https://placehold.co/300x300",
        genre = "Indie"
    )
    val sampleAlbums = listOf(
        AlbumInfo(
            id = 101,
            title = "Noches Eléctricas",
            year = "2023",
            coverUrl = "https://placehold.co/200x200",
            artist = sampleArtist
        ),
        AlbumInfo(
            id = 102,
            title = "Horizontes",
            year = "2022",
            coverUrl = "https://placehold.co/200x200/ff6",
            artist = sampleArtist
        )
    )
    val samplePlaylists = listOf(
        PlaylistInfo(
            id = 1,
            title = "Favoritos recientes",
            description = "Una mezcla de descubrimientos",
            albums = sampleAlbums
        )
    )
    val sampleUser = UserInfo(
        id = "user123",
        username = "CritiLover",
        profileImageUrl = "https://placehold.co/120x120",
        bio = "Fan de los sintetizadores y las reseñas extensas.",
        followers = 128,
        following = 87,
        playlists = samplePlaylists,
        name = "CritiLover",
    )
    val sampleReviews = listOf(
        ReviewInfo(
            id = "review1",
            content = "Una producción impecable con letras profundas.",
            score = 8.0,
            isLowScore = false,
            albumId = sampleAlbums[0].id,
            userId = sampleUser.id,
            createdAt = "2024-01-01",
            updatedAt = "2024-01-01",
            isFavorite = true
        ),
        ReviewInfo(
            id = "review2",
            content = "Ritmos contagiosos ideales para bailar.",
            score = 9.0,
            isLowScore = false,
            albumId = sampleAlbums[1].id,
            userId = sampleUser.id,
            createdAt = "2024-02-14",
            updatedAt = "2024-02-14",
            isFavorite = false
        )
    )
    val sampleReviewItems = sampleReviews.mapIndexed { index, review ->
        UserReviewUi(
            review = review,
            album = sampleAlbums.getOrNull(index)
        )
    }
    val sampleState = UserProfileState(
        user = sampleUser,
        reviews = sampleReviews,
        reviewItems = sampleReviewItems,
        favoriteAlbums = sampleAlbums
    )

    Proyecto_movilTheme {
        UserProfileScreen(
            state = sampleState,
            user = sampleUser,
            onBackClick = {},
            onSettingsClick = {},
            onEditProfile = {},
            onAlbumSelected = {},
            onReviewSelected = {},
            onReviewProfileImageClicked = {}
        )
    }
}
