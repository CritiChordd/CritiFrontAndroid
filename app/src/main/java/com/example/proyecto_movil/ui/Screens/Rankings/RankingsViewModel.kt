package com.example.proyecto_movil.ui.Screens.Rankings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class RankingsViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(RankingsState(isLoading = true))
    val uiState: StateFlow<RankingsState> = _uiState.asStateFlow()

    init {
        loadRankings()
    }

    fun onCategorySelected(category: RankingCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    private fun loadRankings() {
        viewModelScope.launch {
            // Simula una breve carga para mostrar la animación de progreso
            delay(350)

            val artists = listOf(
                RankingItem(
                    id = "artist_beyonce",
                    name = "Beyoncé",
                    subtitle = "R&B • Soul",
                    score = 4.9,
                    imageUrl = "https://images.unsplash.com/photo-1529665253569-6d01c0eaf7b6"
                ),
                RankingItem(
                    id = "artist_kendrick",
                    name = "Kendrick Lamar",
                    subtitle = "Hip-Hop",
                    score = 4.8,
                    imageUrl = "https://images.unsplash.com/photo-1525182008055-f88b95ff7980"
                ),
                RankingItem(
                    id = "artist_taylor",
                    name = "Taylor Swift",
                    subtitle = "Pop",
                    score = 4.7,
                    imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee"
                ),
                RankingItem(
                    id = "artist_rosalia",
                    name = "Rosalía",
                    subtitle = "Flamenco Pop",
                    score = 4.6,
                    imageUrl = "https://images.unsplash.com/photo-1489424731084-a5d8b219a5bb"
                ),
                RankingItem(
                    id = "artist_bad_bunny",
                    name = "Bad Bunny",
                    subtitle = "Reggaetón",
                    score = 4.5,
                    imageUrl = "https://images.unsplash.com/photo-1499424017184-418f6808abf2"
                )
            )

            val albums = listOf(
                RankingItem(
                    id = "album_blonde",
                    name = "Blonde",
                    subtitle = "Frank Ocean",
                    score = 4.9,
                    imageUrl = "https://images.unsplash.com/photo-1526498460520-4c246339dccb"
                ),
                RankingItem(
                    id = "album_to_pimp",
                    name = "To Pimp a Butterfly",
                    subtitle = "Kendrick Lamar",
                    score = 4.8,
                    imageUrl = "https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f"
                ),
                RankingItem(
                    id = "album_motomami",
                    name = "MOTOMAMI",
                    subtitle = "Rosalía",
                    score = 4.7,
                    imageUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745"
                ),
                RankingItem(
                    id = "album_midnights",
                    name = "Midnights",
                    subtitle = "Taylor Swift",
                    score = 4.6,
                    imageUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085"
                ),
                RankingItem(
                    id = "album_un_verano",
                    name = "Un Verano Sin Ti",
                    subtitle = "Bad Bunny",
                    score = 4.5,
                    imageUrl = "https://images.unsplash.com/photo-1498050108023-c5249f4df085"
                )
            )

            _uiState.value = RankingsState(
                isLoading = false,
                artistRankings = artists,
                albumRankings = albums
            )
        }
    }
}
