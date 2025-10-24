package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.datasource.AlbumRemoteDataSource
import com.example.proyecto_movil.data.dtos.CreateAlbumDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakeAlbumRemoteDataSource : AlbumRemoteDataSource {
    var albums: MutableList<AlbumInfo> = mutableListOf()

    override suspend fun getAllAlbums(): List<AlbumInfo> = albums

    override suspend fun getAlbumById(id: Int): AlbumInfo =
        albums.first { it.id == id }

    override suspend fun createAlbum(request: CreateAlbumDto): AlbumInfo {
        throw IllegalStateException("Not supported in fake")
    }
}

class AlbumRepositoryTest {

    private lateinit var fakeDataSource: FakeAlbumRemoteDataSource
    private lateinit var repository: AlbumRepository

    @Before
    fun setUp() {
        fakeDataSource = FakeAlbumRemoteDataSource()
        repository = AlbumRepository(fakeDataSource)
    }

    @After
    fun tearDown() {
        fakeDataSource.albums.clear()
    }

    @Test
    fun getAllAlbums_successReturnsData() = runTest {
        fakeDataSource.albums.add(
            AlbumInfo(
                id = 1,
                title = "A",
                year = "2020",
                coverUrl = "cover",
                artist = ArtistInfo(1, "Artist", "img", "genre")
            )
        )

        val result = repository.getAllAlbums()
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrThrow()).hasSize(1)
    }

    @Test
    fun getAlbumById_whenMissing_returnsFailure() = runTest {
        val result = repository.getAlbumById(99)

        assertThat(result.isFailure).isTrue()
    }
}
