package com.example.proyecto_movil.ui.Screens.Rankings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.animation.togetherWith
import com.example.proyecto_movil.R
import com.example.proyecto_movil.ui.utils.ScreenBackground

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RankingsScreen(
    viewModel: RankingsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light

    ScreenBackground(backgroundRes = backgroundRes, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                text = "Rankings",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Lo mejor valorado por la comunidad CritiChord",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            RankingSelector(
                selected = state.selectedCategory,
                onSelected = viewModel::onCategorySelected
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedContent(
                targetState = Triple(state.isLoading, state.errorMessage, state.currentItems),
                transitionSpec = {
                    fadeIn(animationSpec = tween(220)) togetherWith
                        fadeOut(animationSpec = tween(220))
                }, label = "rankingContent"
            ) { (isLoading, errorMessage, items) ->
                when {
                    isLoading -> RankingLoadingPlaceholder()
                    errorMessage != null -> RankingError(message = errorMessage)
                    items.isEmpty() -> RankingEmptyState()
                    else -> RankingList(items = items)
                }
            }
        }
    }
}

@Composable
private fun RankingSelector(
    selected: RankingCategory,
    onSelected: (RankingCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RankingSelectorOption(
            text = "Artistas",
            selected = selected == RankingCategory.ARTISTS,
            onClick = { onSelected(RankingCategory.ARTISTS) },
            modifier = Modifier.weight(1f)
        )
        RankingSelectorOption(
            text = "Álbumes",
            selected = selected == RankingCategory.ALBUMS,
            onClick = { onSelected(RankingCategory.ALBUMS) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RankingSelectorOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val borderColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = contentColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun RankingList(
    items: List<RankingItem>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(items) { index, item ->
            RankingCard(
                position = index + 1,
                item = item
            )
        }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun RankingCard(
    position: Int,
    item: RankingItem,
    modifier: Modifier = Modifier
) {
    val isTopThree = position <= 3
    val containerColor = if (isTopThree) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
    }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = if (isTopThree) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RankBadge(position = position)

            Spacer(modifier = Modifier.width(16.dp))

            RankingImage(imageUrl = item.imageUrl)

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            ScoreBadge(score = item.score)
        }
    }
}

@Composable
private fun RankBadge(position: Int) {
    val backgroundColor = when (position) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> MaterialTheme.colorScheme.primary
    }

    Surface(
        color = backgroundColor,
        shape = CircleShape,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = position.toString(),
                color = if (position <= 3) Color.Black else MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun RankingImage(imageUrl: String) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun ScoreBadge(score: Double) {
    Column(horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = String.format("%.1f", score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Divider(
            modifier = Modifier.width(40.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    }
}

@Composable
private fun RankingLoadingPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.CircularProgressIndicator()
        Text(
            text = "Calculando posiciones...",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RankingError(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No pudimos cargar el ranking",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun RankingEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aún no hay suficientes reseñas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Sigue calificando para desbloquear los rankings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
