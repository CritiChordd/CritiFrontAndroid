package com.example.proyecto_movil.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import kotlin.math.roundToInt

@Composable
fun ReviewCard(
    review: ReviewInfo,
    author: UserInfo?,
    modifier: Modifier = Modifier,
    albumTitle: String? = null,
    albumArtist: String? = null,
    albumYear: String? = null,
    albumCoverUrl: String? = null,
    onUserClick: (String) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ---------- Usuario ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                val resolvedUserId = remember(author?.id, review.userId, review.firebaseUserId) {
                    author?.id?.takeIf { it.isNotBlank() }
                        ?: review.firebaseUserId?.takeIf { it.isNotBlank() }
                        ?: review.userId.takeIf { it.isNotBlank() }
                }
                val avatarUrl = remember(author) {
                    author?.profileImageUrl?.takeIf { it.isNotBlank() }
                        ?: "https://placehold.co/100x100"
                }
                val displayName = remember(author) {
                    sequenceOf(author?.name, author?.username)
                        .mapNotNull { it?.takeIf(String::isNotBlank) }
                        .firstOrNull()
                        ?: "Usuario"
                }
                val usernameHandle = remember(author) {
                    author?.username?.takeIf { it.isNotBlank() }?.let { "@${it}" }
                }
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(enabled = resolvedUserId != null) {
                            resolvedUserId?.let(onUserClick)
                        }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!usernameHandle.isNullOrBlank()) {
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = usernameHandle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- Álbum + artista (opcional) ----------
            val hasAlbumInfo = !albumTitle.isNullOrBlank() || !albumArtist.isNullOrBlank()
            if (hasAlbumInfo) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = albumCoverUrl?.takeIf { it.isNotBlank() }
                            ?: "https://placehold.co/200x200",
                        contentDescription = albumTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(Modifier.width(12.dp))
                    Column {
                        if (!albumTitle.isNullOrBlank()) {
                            Text(
                                text = albumTitle,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!albumArtist.isNullOrBlank()) {
                            Text(
                                text = albumArtist,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                        if (!albumYear.isNullOrBlank()) {
                            Text(
                                text = "($albumYear)",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))
            }

            // ---------- Contenido reseña ----------
            Text(
                text = review.content,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            // ---------- Puntaje ----------
            val scorePercent = remember(review.score) { (review.score * 10).roundToInt() }
            val scoreColor = when {
                review.score >= 7 -> MaterialTheme.colorScheme.primary
                review.score >= 5 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.error
            }

            Surface(
                color = scoreColor,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "$scorePercent%",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview
fun ReviewCardPreview() {
    Proyecto_movilTheme {
        ReviewCard(
            review = ReviewInfo(
                id = "1",
                content = "Esta reseña destaca la producción y las letras del álbum.",
                score = 8.5,
                isLowScore = false,
                albumId = 10,
                userId = "user123",
                createdAt = "2024-01-01",
                updatedAt = "2024-01-01",
                liked = true,
                isFavorite = false
            ),
            author = UserInfo(
                id = "user123",
                name = "Alex Critic",
                username = "alex",
                profileImageUrl = "https://placehold.co/100x100"
            ),
            albumTitle = "Hybrid Theory",
            albumArtist = "Linkin Park",
            albumYear = "2000",
            albumCoverUrl = "https://placehold.co/200x200",
            onUserClick = {}
        )
    }
}
