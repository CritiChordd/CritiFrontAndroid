package com.example.proyecto_movil.ui.Screens.Rankings

import androidx.compose.runtime.Immutable

enum class RankingCategory {
    ARTISTS,
    ALBUMS
}

@Immutable
data class RankingItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val score: Double,
    val imageUrl: String
)

@Immutable
data class RankingsState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedCategory: RankingCategory = RankingCategory.ARTISTS,
    val artistRankings: List<RankingItem> = emptyList(),
    val albumRankings: List<RankingItem> = emptyList()
) {
    val currentItems: List<RankingItem>
        get() = when (selectedCategory) {
            RankingCategory.ARTISTS -> artistRankings
            RankingCategory.ALBUMS -> albumRankings
        }
}
