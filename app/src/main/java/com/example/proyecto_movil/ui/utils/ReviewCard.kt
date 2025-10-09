package com.example.proyecto_movil.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme

@Composable
fun ReviewCard(
    review: ReviewInfo,
    modifier: Modifier = Modifier,
    onUserClick: (Int) -> Unit = {} // ✅ userId ahora es Int
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
                AsyncImage(
                    model = "https://placehold.co/100x100", // ⚠️ reemplaza si tienes user.profilePic
                    contentDescription = "Usuario ${review.userId}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable { onUserClick(review.userId) }
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Usuario ${review.userId}", // ✅ convertir Int a String
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(12.dp))

            // ---------- Álbum + artista ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = "https://placehold.co/200x200", // ⚠️ reemplaza si tienes cover
                    contentDescription = "Álbum ${review.albumId}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))

                )

                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Álbum ${review.albumId}", // ✅ convertir Int a texto
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Artista desconocido", // ⚠️ opcional si agregas artistName
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "(Año ?)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ---------- Contenido reseña ----------
            Text(
                text = review.content,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            // ---------- Puntaje ----------
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
                    text = "${(review.score * 10).toInt()}%", // ✅ convertir a texto y entero
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
