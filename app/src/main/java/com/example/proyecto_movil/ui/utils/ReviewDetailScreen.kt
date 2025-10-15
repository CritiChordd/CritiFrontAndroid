package com.example.proyecto_movil.ui.utils

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        }
    }
}

/* ---------- Preview ---------- */
@Preview(name = "ReviewDetail Light", showSystemUi = true)
@Composable
fun ReviewDetailScreenLightPreview() {
    Proyecto_movilTheme(useDarkTheme = false) {
        Surface {
            ReviewDetailScreen(
                review = ReviewInfo(
                    id = "1",
                    content = "Una reseña con mucho detalle sobre este increíble álbum.",
                    score = 9.0,
                    isLowScore = false,
                    albumId = 10,
                    userId = "1",
                    createdAt = "",
                    updatedAt = "",
                    liked = false,
                    isFavorite = true
                ),
                username = "xocas",
                userProfileUrl = "https://placehold.co/100x100",
                albumTitle = "Hybrid Theory",
                albumCoverUrl = "https://placehold.co/200x200",
                artistName = "Linkin Park",
                albumYear = "2000",
                onBack = {}
            )
        }
    }
}

@Preview(name = "ReviewDetail Dark", showSystemUi = true)
@Composable
fun ReviewDetailScreenDarkPreview() {
    Proyecto_movilTheme(useDarkTheme = true) {
        Surface {
            ReviewDetailScreenLightPreview()
        }
    }
}
