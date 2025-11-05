package com.example.proyecto_movil.ui.Screens.Rankings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class RankingsViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingsState(isLoading = true))
    val uiState: StateFlow<RankingsState> = _uiState.asStateFlow()

    init {
        loadRankings()
    }

    fun onCategorySelected(category: RankingCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    private fun loadRankings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val albumsDeferred = async { albumRepository.getAllAlbums() }
            val reviewsDeferred = async { reviewRepository.getAllReviews() }

            val albumsResult = albumsDeferred.await()
            val reviewsResult = reviewsDeferred.await()

            if (albumsResult.isFailure || reviewsResult.isFailure) {
                val error = albumsResult.exceptionOrNull() ?: reviewsResult.exceptionOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.toUserMessage()
                    )
                }
                return@launch
            }

            val albums = albumsResult.getOrThrow()
            val reviews = reviewsResult.getOrThrow()
            val (artistRankings, albumRankings) = computeRankings(albums, reviews)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    artistRankings = artistRankings,
                    albumRankings = albumRankings
                )
            }
        }
    }

    private fun computeRankings(
        albums: List<AlbumInfo>,
        reviews: List<ReviewInfo>
    ): Pair<List<RankingItem>, List<RankingItem>> {
        if (albums.isEmpty() || reviews.isEmpty()) {
            return emptyList<RankingItem>() to emptyList()
        }

        val albumById = albums.associateBy { it.id }

        val albumAggregates = reviews
            .filter { it.albumId != 0 }
            .groupBy { it.albumId }
            .mapNotNull { (albumId, albumReviews) ->
                val album = albumById[albumId] ?: return@mapNotNull null
                val reviewCount = albumReviews.size
                if (reviewCount == 0) return@mapNotNull null

                val totalScore = albumReviews.sumOf { it.score }
                val averageScore = totalScore / reviewCount

                AlbumAggregate(
                    album = album,
                    averageScore = averageScore,
                    totalScore = totalScore,
                    reviewCount = reviewCount
                )
            }
            .sortedWith(
                compareByDescending<AlbumAggregate> { it.averageScore }
                    .thenByDescending { it.reviewCount }
                    .thenBy { it.album.title.lowercase() }
            )

        val albumRankings = albumAggregates.map { aggregate ->
            RankingItem(
                id = aggregate.album.id.toString(),
                name = aggregate.album.title,
                subtitle = listOfNotNull(
                    aggregate.album.artist.name.takeIf { it.isNotBlank() },
                    formatReviewLabel(aggregate.reviewCount)
                ).joinToString(" • "),
                score = aggregate.averageScore,
                imageUrl = aggregate.album.coverUrl.ifBlank { DEFAULT_ALBUM_IMAGE }
            )
        }

        val artistAggregates = albumAggregates
            .groupBy { it.album.artist.id }
            .map { (_, aggregates) ->
                val artist = aggregates.first().album.artist
                val reviewCount = aggregates.sumOf { it.reviewCount }
                val totalScore = aggregates.sumOf { it.totalScore }
                val averageScore = if (reviewCount == 0) 0.0 else totalScore / reviewCount

                ArtistAggregate(
                    artist = artist,
                    reviewCount = reviewCount,
                    averageScore = averageScore
                )
            }
            .sortedWith(
                compareByDescending<ArtistAggregate> { it.averageScore }
                    .thenByDescending { it.reviewCount }
                    .thenBy { it.artist.name.lowercase() }
            )

        val artistRankings = artistAggregates.map { aggregate ->
            val subtitleParts = buildList {
                if (aggregate.artist.genre.isNotBlank()) add(aggregate.artist.genre)
                add(formatReviewLabel(aggregate.reviewCount))
            }

            RankingItem(
                id = aggregate.artist.id.toString(),
                name = aggregate.artist.name,
                subtitle = subtitleParts.joinToString(" • "),
                score = aggregate.averageScore,
                imageUrl = aggregate.artist.profileImageUrl.ifBlank { DEFAULT_ARTIST_IMAGE }
            )
        }

        return artistRankings to albumRankings
    }

    private fun Throwable?.toUserMessage(): String {
        val error = this ?: return GENERIC_ERROR_MESSAGE
        if (error is CancellationException) throw error
        return error.localizedMessage?.takeIf { it.isNotBlank() } ?: GENERIC_ERROR_MESSAGE
    }

    private fun formatReviewLabel(count: Int): String =
        if (count == 1) "1 reseña" else "$count reseñas"

    private data class AlbumAggregate(
        val album: AlbumInfo,
        val averageScore: Double,
        val totalScore: Double,
        val reviewCount: Int
    )

    private data class ArtistAggregate(
        val artist: ArtistInfo,
        val reviewCount: Int,
        val averageScore: Double
    )

    companion object {
        private const val DEFAULT_ALBUM_IMAGE = "https://placehold.co/600x400?text=No+Cover"
        private const val DEFAULT_ARTIST_IMAGE = "https://placehold.co/300x300?text=No+Image"
        private const val GENERIC_ERROR_MESSAGE = "No pudimos obtener los rankings. Intenta nuevamente."
    }
}
