package com.example.proyecto_movil.ui.Screens.AddReview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.proyecto_movil.R
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.ui.utils.ScreenBackground
import kotlin.math.roundToInt

@Composable
fun AddReviewScreen(
    viewModel: AddReviewViewModel,
    onCancel: () -> Unit,
    onPublished: (AlbumInfo, String, Int, Boolean) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light
    val scrollState = rememberScrollState()
    val albums = state.availableAlbums

    ScreenBackground(backgroundRes = backgroundRes) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Dropdown para seleccionar álbum
            AlbumDropdown(
                albums = albums,
                selectedTitle = state.albumTitle,
                onAlbumSelected = { album -> viewModel.updateAlbum(album) },
                enabled = albums.isNotEmpty()
            )

            // Campo de texto para la reseña
            OutlinedTextField(
                value = state.reviewText,
                onValueChange = { viewModel.updateReviewText(it) },
                label = { Text("Escribe tu reseña") },
                modifier = Modifier.fillMaxWidth()
            )

            // Slider para puntaje
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val scoreOutOfTen = remember(state.scorePercent) {
                    (state.scorePercent / 10.0).roundToInt()
                }
                Text("Puntaje: ${state.scorePercent}% (${scoreOutOfTen}/10)")
                Slider(
                    value = state.scorePercent.toFloat(),
                    onValueChange = { viewModel.updateScore(it.toInt()) },
                    valueRange = 0f..100f,
                    steps = 10
                )
            }

            // Checkbox para like
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.liked,
                    onCheckedChange = { viewModel.toggleLike() }
                )
                Text(if (state.liked) "¡Me gustó!" else "No me gustó")
            }

            // Mensaje de error si no hay texto
            if (state.showMessage) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = { viewModel.onCancelClicked() }) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { viewModel.onPublishClicked() },
                    enabled = state.albumId != null
                ) {
                    Text("Publicar")
                }
            }
        }
    }

    // Navegación al cancelar
    LaunchedEffect(state.navigateCancel) {
        if (state.navigateCancel) {
            onCancel()
            viewModel.consumeCancel()
        }
    }

    // Navegación al publicar
    LaunchedEffect(state.navigatePublished) {
        if (state.navigatePublished) {
            val selectedAlbum = albums.firstOrNull { it.id == state.albumId }
            if (selectedAlbum != null) {
                onPublished(selectedAlbum, state.reviewText, state.scorePercent, state.liked)
            }
            viewModel.consumePublished()
        }
    }
}

/* ---------- Dropdown para álbumes ---------- */
@Composable
fun AlbumDropdown(
    albums: List<AlbumInfo>,
    selectedTitle: String,
    onAlbumSelected: (AlbumInfo) -> Unit,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = enabled
        ) {
            val placeholder = when {
                !enabled -> "Cargando álbumes..."
                selectedTitle.isBlank() -> "Selecciona un álbum"
                else -> selectedTitle
            }
            Text(placeholder)
        }
        DropdownMenu(
            expanded = expanded && enabled,
            onDismissRequest = { expanded = false }
        ) {
            albums.forEach { album ->
                DropdownMenuItem(
                    text = { Text(album.title) },
                    onClick = {
                        onAlbumSelected(album)
                        expanded = false
                    }
                )
            }
        }
    }

}

