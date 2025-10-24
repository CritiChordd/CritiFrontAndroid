package com.example.proyecto_movil.ui.Screens.Home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ReviewInfo
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.repository.AlbumRepository
import com.example.proyecto_movil.data.repository.ReviewRepository
import com.example.proyecto_movil.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val albumRepository: AlbumRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState

    private var searchJob: Job? = null
    private var lastSearchQuery: String? = null

    init {
        loadAlbumsAndReviews()
    }

    /** 🔹 Carga inicial de álbumes y reseñas */
    private fun loadAlbumsAndReviews() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "🚀 Cargando álbumes y reseñas desde el backend...")

                val (albumsResult, reviewsResult) = withContext(Dispatchers.IO) {
                    val albumsDeferred = async { albumRepository.getAllAlbums() }
                    val reviewsDeferred = async { reviewRepository.getAllReviews() }
                    albumsDeferred.await() to reviewsDeferred.await()
                }

                val albums = albumsResult.getOrDefault(emptyList())
                val reviews = reviewsResult.getOrDefault(emptyList())
                val newReleases = filterNewReleases(albums)
                val popularAlbums = calculatePopularAlbums(albums, reviews)

                Log.d("HomeViewModel", "✅ Álbumes cargados: ${albums.size}")
                Log.d("HomeViewModel", "✅ Reseñas cargadas: ${reviews.size}")

                _uiState.update {
                    it.copy(
                        albumList = albums,
                        reviewList = reviews,
                        newReleases = newReleases,
                        popularAlbums = popularAlbums
                    )
                }

                albums.forEach {
                    Log.d(
                        "HomeVM",
                        "🎵 ${it.title} (${it.year}) - ${it.artist.name} | Cover: ${it.coverUrl} | ArtistImg: ${it.artist.profileImageUrl}"
                    )
                }

            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ Error al cargar datos", e)
            }
        }
    }

    /** 🔹 Evento al hacer clic en un álbum */
    fun onAlbumClicked(album: AlbumInfo) =
        _uiState.update { it.copy(openAlbum = album) }

    /** 🔹 Consumir navegación al álbum */
    fun consumeOpenAlbum() =
        _uiState.update { it.copy(openAlbum = null) }

    /** 🔍 Mostrar u ocultar la barra de búsqueda */
    fun toggleSearch() {
        val newActive = !_uiState.value.isSearchActive
        _uiState.update {
            it.copy(
                isSearchActive = newActive,
                searchQuery = if (newActive) it.searchQuery else "",
                searchUserResults = if (newActive) it.searchUserResults else emptyList(),
                searchAlbumResults = if (newActive) it.searchAlbumResults else emptyList(),
                searchError = null,
                isSearching = if (newActive) it.isSearching else false
            )
        }
        if (!newActive) {
            searchJob?.cancel()
            searchJob = null
            lastSearchQuery = null
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        val normalizedQuery = query.trim()

        if (normalizedQuery.length < 2) {
            searchJob?.cancel()
            _uiState.update {
                it.copy(
                    searchUserResults = emptyList(),
                    searchAlbumResults = emptyList(),
                    searchError = null,
                    isSearching = false
                )
            }
            lastSearchQuery = null
            return
        }

        if (normalizedQuery == lastSearchQuery) {
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            lastSearchQuery = normalizedQuery
            _uiState.update {
                it.copy(
                    isSearching = true,
                    searchError = null
                )
            }

            delay(250)

            val albumMatches = findAlbumMatches(normalizedQuery)
            _uiState.update {
                it.copy(searchAlbumResults = albumMatches)
            }

            userRepository.searchUsersByName(normalizedQuery, limit = 8).fold(
                onSuccess = { users ->
                    _uiState.update {
                        it.copy(
                            searchUserResults = users,
                            isSearching = false,
                            searchError = if (users.isEmpty() && albumMatches.isEmpty()) {
                                "No se encontraron resultados"
                            } else {
                                null
                            }
                        )
                    }
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            searchUserResults = emptyList(),
                            isSearching = false,
                            searchError = e.message ?: "Error buscando usuarios"
                        )
                    }
                }
            )
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        searchJob = null
        lastSearchQuery = null
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchUserResults = emptyList(),
                searchAlbumResults = emptyList(),
                searchError = null,
                isSearching = false
            )
        }
    }

    fun onUserResultClicked(user: UserInfo) {
        searchJob?.cancel()
        searchJob = null
        _uiState.update {
            it.copy(
                navigateToUserId = user.id,
                isSearchActive = false,
                searchUserResults = emptyList(),
                searchAlbumResults = emptyList(),
                searchQuery = "",
                searchError = null
            )
        }
        lastSearchQuery = null
    }

    fun onAlbumResultClicked(album: AlbumInfo) {
        searchJob?.cancel()
        searchJob = null
        _uiState.update {
            it.copy(
                openAlbum = album,
                isSearchActive = false,
                searchUserResults = emptyList(),
                searchAlbumResults = emptyList(),
                searchQuery = "",
                searchError = null
            )
        }
        lastSearchQuery = null
    }

    fun consumeNavigateToUser() =
        _uiState.update { it.copy(navigateToUserId = null) }

    private fun findAlbumMatches(query: String): List<AlbumInfo> {
        if (query.isBlank()) return emptyList()

        val lowerQuery = query.lowercase()
        return _uiState.value.albumList.filter { album ->
            album.title.lowercase().contains(lowerQuery) ||
                album.artist.name.lowercase().contains(lowerQuery)
        }.take(8)
    }

    private fun filterNewReleases(albums: List<AlbumInfo>): List<AlbumInfo> =
        albums.filter { it.year.toIntOrNull() ?: 0 >= 2023 }

    private fun calculatePopularAlbums(
        albums: List<AlbumInfo>,
        reviews: List<ReviewInfo>
    ): List<AlbumInfo> {
        if (albums.isEmpty() || reviews.isEmpty()) return albums

        val avgScores: Map<Int, Double> = reviews
            .groupBy { it.albumId }
            .mapValues { (_, list) ->
                list.mapNotNull { it.score }.average()
            }

        avgScores.entries.forEach {
            Log.d("HomeVM", "⭐ Album ${it.key} promedio ${"%.2f".format(it.value)}")
        }

        return albums.sortedByDescending { avgScores[it.id] ?: 0.0 }
    }
}
