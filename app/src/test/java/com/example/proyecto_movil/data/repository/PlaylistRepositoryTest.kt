package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.datasource.impl.retrofit.PlaylistRetrofitDataSourceImpl
import com.example.proyecto_movil.data.datasource.services.PlaylistRetrofitService
import com.example.proyecto_movil.data.dtos.AlbumDto
import com.example.proyecto_movil.data.dtos.ArtistDto
import com.example.proyecto_movil.data.dtos.PlaylistDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakePlaylistService : PlaylistRetrofitService {
    val playlists: MutableList<PlaylistDto> = mutableListOf()
    var deletedIds: MutableList<String> = mutableListOf()

    override suspend fun getAllPlaylists(): List<PlaylistDto> = playlists

    override suspend fun getPlaylistById(playlistId: String): PlaylistDto =
        playlists.first { it.id.toString() == playlistId }

    override suspend fun createPlaylist(playlist: PlaylistDto) {
        playlists.add(playlist)
    }

    override suspend fun updatePlaylist(playlistId: String, playlist: PlaylistDto) {}

    override suspend fun deletePlaylist(playlistId: String) {
        deletedIds.add(playlistId)
    }
}

class PlaylistRepositoryTest {

    private lateinit var fakeService: FakePlaylistService
    private lateinit var repository: PlaylistRepository

    @Before
    fun setUp() {
        fakeService = FakePlaylistService()
        val dataSource = PlaylistRetrofitDataSourceImpl(fakeService)
        repository = PlaylistRepository(dataSource)
    }

    @After
    fun tearDown() {
        fakeService.playlists.clear()
        fakeService.deletedIds.clear()
    }

    @Test
    fun getAllPlaylists_returnsSuccess() = runTest {
        fakeService.playlists += PlaylistDto(
            id = 1,
            title = "My playlist",
            description = "Desc",
            albums = listOf(AlbumDto(1, "A", "2020", null, ArtistDto(1, "Artist", null, null)))
        )

        val result = repository.getAllPlaylists()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
    }

    @Test
    fun deletePlaylist_invokesService() = runTest {
        val result = repository.deletePlaylist("7")

        assertThat(result.isSuccess).isTrue()
        assertThat(fakeService.deletedIds).containsExactly("7")
    }
}
