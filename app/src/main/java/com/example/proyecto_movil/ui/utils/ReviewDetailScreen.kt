package com.example.proyecto_movil.ui.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme

@Composable
fun ReviewDetailScreen(
    review: ReviewInfo,
    username: String = "",
    userProfileUrl: String = "",
    albumTitle: String = "",
    albumCoverUrl: String = "",
    artistName: String = "",
    albumYear: String = "",
    liked: Boolean,
    likesCount: Int,
    onToggleLike: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light

    ScreenBackground(backgroundRes = backgroundRes, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // ---------- TopBar ----------
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Usuario ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = userProfileUrl.ifEmpty { "https://placehold.co/100x100" },
                    contentDescription = username,
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .padding(end = 12.dp),
                    contentScale = ContentScale.Crop
                )
                Column {
                    Text(
                        text = username,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Reseña del álbum:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Álbum ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = albumCoverUrl.ifEmpty { "https://placehold.co/200x200" },
                    contentDescription = albumTitle,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(end = 12.dp)
                )
                Column {
                    Text(
                        text = albumTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (albumYear.isNotBlank()) {
                        Text(
                            text = "($albumYear)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // ---------- Contenido ----------
            Text(
                text = review.content,
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(16.dp))

            // ---------- Puntaje ----------
            Text(
                text = "Puntuación: ${review.score}/10",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(8.dp))

            // ---------- Likes ----------
            LikeRow(
                liked = liked,
                likesCount = likesCount,
                onToggleLike = onToggleLike
            )
        }
    }
}

@Composable
fun LikeRow(
    liked: Boolean,
    likesCount: Int,
    onToggleLike: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onToggleLike) {
            Icon(
                imageVector = if (liked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                tint = if (liked) Color.Red else Color.Gray
            )
        }
        Text(
            text = "$likesCount likes",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
