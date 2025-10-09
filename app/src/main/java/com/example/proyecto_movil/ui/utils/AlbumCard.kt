package com.example.proyecto_movil.ui.utils

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage

@Composable
fun AlbumCard(
    album: AlbumInfo,
    onClick: () -> Unit,
    onReviewProfileImageClicked: (String) -> Unit
) {
    Log.d("AlbumCard", "📀 Album: ${album.title}, Cover: ${album.coverUrl}")
    Column(
        modifier = Modifier
            .width(120.dp)
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = album.coverUrl,              // aquí pasa el String (URL)
            contentDescription = album.title,
            modifier = Modifier
                .size(120.dp)                    // imagen cuadrada
                .clip(RoundedCornerShape(8.dp)), // esquinas redondeadas (opcional)
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = album.title,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,                // 🔑 permitimos hasta 2 líneas
            modifier = Modifier.width(120.dp) // se ajusta al ancho de la imagen
        )

        Text(
            text = album.artist.name,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            modifier = Modifier.width(120.dp) // también alineado con la imagen
        )
    }
}



