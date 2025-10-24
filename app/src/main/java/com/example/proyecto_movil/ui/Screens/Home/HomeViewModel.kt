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
import com.example.proyecto_movil.ui.Screens.Home.SearchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
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

    init {
        loadAlbumsAndReviews()
    }

    /** 🔹 Carga inicial de álbumes y reseñas */
    private fun loadAlbumsAndReviews() {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "🚀 Cargando álbumes y reseñas desde el backend...")

                val albumsResult = albumRepository.getAllAlbums()
                val reviewsResult = reviewRepository.getAllReviews()

                val albums = albumsResult.getOrDefault(emptyList())
                val reviews = reviewsResult.getOrDefault(emptyList())

                Log.d("HomeViewModel", "✅ Álbumes cargados: ${albums.size}")
                Log.d("HomeViewModel", "✅ Reseñas cargadas: ${reviews.size}")

                _uiState.update {
                    it.copy(
                        albumList = albums,
                        reviewList = reviews
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
                searchResults = if (newActive) it.searchResults else emptyList(),
                searchError = null,
                isSearching = if (newActive) it.isSearching else false
            )
        }
        if (!newActive) {
            searchJob?.cancel()
            searchJob = null
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }

        if (query.length < 2) {
            searchJob?.cancel()
            _uiState.update { it.copy(searchResults = emptyList(), searchError = null, isSearching = false) }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchError = null) }

            val userResult = userRepository.searchUsersByName(query, limit = 8)
            val albumResult = albumRepository.searchAlbums(query, limit = 8)

            val results = buildList {
                userResult.getOrNull()?.let { users ->
                    addAll(users.map { SearchResult.User(it) })
                }
                albumResult.getOrNull()?.let { albums ->
                    addAll(albums.map { SearchResult.Album(it) })
                }
            }

            val failures = listOfNotNull(userResult.exceptionOrNull(), albumResult.exceptionOrNull())
            val errorMessage = when {
                results.isEmpty() && failures.isNotEmpty() ->
                    failures.joinToString(" / ") { it.message ?: "Error al buscar" }

                results.isEmpty() -> "No se encontraron usuarios ni álbumes"
                failures.isNotEmpty() -> "Algunos resultados no pudieron cargarse"
                else -> null
            }

            _uiState.update {
                it.copy(
                    searchResults = results,
                    isSearching = false,
                    searchError = errorMessage
                )
            }
        }
    }

    fun clearSearch() {
        searchJob?.cancel()
        searchJob = null
        _uiState.update {
            it.copy(
                searchQuery = "",
                searchResults = emptyList(),
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
                searchResults = emptyList(),
                searchQuery = "",
                searchError = null
            )
        }
    }

    fun consumeNavigateToUser() =
        _uiState.update { it.copy(navigateToUserId = null) }

    fun onAlbumResultClicked(album: AlbumInfo) {
        searchJob?.cancel()
        searchJob = null
        _uiState.update {
            it.copy(
                openAlbum = album,
                isSearchActive = false,
                searchResults = emptyList(),
                searchQuery = "",
                searchError = null
            )
        }
    }

    /** 🔹 Filtrar lanzamientos recientes */
    fun getNewReleases(): List<AlbumInfo> =
        uiState.value.albumList.filter { it.year.toIntOrNull() ?: 0 >= 2023 }

    /** 🔹 Ordenar álbumes por puntaje promedio */
    fun getPopularAlbums(): List<AlbumInfo> {
        val reviews = uiState.value.reviewList
        val albums = uiState.value.albumList

        if (reviews.isEmpty() || albums.isEmpty()) return albums

        // Calcular el promedio por álbum
        val avgScores: Map<Int, Double> = reviews
            .groupBy { it.albumId }
            .mapValues { (_, list) ->
                list.mapNotNull { it.score }.average()
            }

        // Log de control
        avgScores.entries.forEach {
            Log.d("HomeVM", "⭐ Album ${it.key} promedio ${"%.2f".format(it.value)}")
        }

        // Ordenar los álbumes según su promedio
        return albums.sortedByDescending { avgScores[it.id] ?: 0.0 }
    }
}
