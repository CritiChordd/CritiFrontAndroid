package com.example.proyecto_movil.ui.Screens.Lists

import com.example.proyecto_movil.MainDispatcherRule
import com.example.proyecto_movil.data.dtos.AlbumDto
import com.example.proyecto_movil.data.dtos.ArtistDto
import com.example.proyecto_movil.data.dtos.PlaylistDto
import com.example.proyecto_movil.data.repository.PlaylistRepository
import com.example.proyecto_movil.data.datasource.impl.retrofit.PlaylistRetrofitDataSourceImpl
import com.example.proyecto_movil.data.datasource.services.PlaylistRetrofitService
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    // Integration test: verifies end-to-end mapping through repository and ViewModel on success
    @Test
    fun loadPlaylist_populatesStateFromRepository() = runTest {
        val service = object : PlaylistRetrofitService {
            override suspend fun getAllPlaylists(): List<PlaylistDto> = emptyList()

            override suspend fun getPlaylistById(playlistId: String): PlaylistDto {
                return PlaylistDto(
                    id = 7,
                    title = "Favoritos",
                    description = "Lo mejor del año",
                    albums = listOf(
                        AlbumDto(
                            id = 1,
                            title = null,
                            year = null,
                            coverUrl = null,
                            artist = ArtistDto(
                                id = 11,
                                name = null,
                                imageUrl = null,
                                genre = null
                            )
                        )
                    )
                )
            }

            override suspend fun createPlaylist(playlist: PlaylistDto) = Unit

            override suspend fun updatePlaylist(playlistId: String, playlist: PlaylistDto) = Unit

            override suspend fun deletePlaylist(playlistId: String) = Unit
        }
        val repository = PlaylistRepository(PlaylistRetrofitDataSourceImpl(service))
        val viewModel = ListViewModel(repository)

        viewModel.loadPlaylist("7")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).isNull()
        assertThat(state.title).isEqualTo("Favoritos")
        assertThat(state.description).isEqualTo("Lo mejor del año")
        assertThat(state.albums).hasSize(1)
        val album = state.albums.first()
        assertThat(album.title).isEqualTo("Sin título")
        assertThat(album.year).isEqualTo("Año desconocido")
        assertThat(album.coverUrl).contains("No+Cover")
        assertThat(album.artist.name).isEqualTo("Desconocido")
        assertThat(album.artist.genre).isEqualTo("Sin género")
        assertThat(state.likes).isEqualTo(1)
    }

    // Integration test: verifies error propagation from repository to ViewModel state
    @Test
    fun loadPlaylist_whenRepositoryFails_setsErrorState() = runTest {
        val service = object : PlaylistRetrofitService {
            override suspend fun getAllPlaylists(): List<PlaylistDto> = emptyList()

            override suspend fun getPlaylistById(playlistId: String): PlaylistDto {
                throw IllegalStateException("Fallo en servicio")
            }

            override suspend fun createPlaylist(playlist: PlaylistDto) = Unit

            override suspend fun updatePlaylist(playlistId: String, playlist: PlaylistDto) = Unit

            override suspend fun deletePlaylist(playlistId: String) = Unit
        }
        val repository = PlaylistRepository(PlaylistRetrofitDataSourceImpl(service))
        val viewModel = ListViewModel(repository)

        viewModel.loadPlaylist("999")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.errorMessage).contains("Fallo en servicio")
        assertThat(state.albums).isEmpty()
    }
}
