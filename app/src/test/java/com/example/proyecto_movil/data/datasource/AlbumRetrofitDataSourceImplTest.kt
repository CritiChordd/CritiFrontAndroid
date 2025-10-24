package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.datasource.impl.retrofit.AlbumRetrofitDataSourceImpl
import com.example.proyecto_movil.data.datasource.services.AlbumRetrofitService
import com.example.proyecto_movil.data.dtos.AlbumDto
import com.example.proyecto_movil.data.dtos.ArtistDto
import com.example.proyecto_movil.data.dtos.CreateAlbumDto
import com.example.proyecto_movil.data.dtos.toAlbumInfo
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakeAlbumService : AlbumRetrofitService {
    var albums: List<AlbumDto> = emptyList()

    override suspend fun getAllAlbums(): List<AlbumDto> = albums

    override suspend fun getAlbumById(albumId: Int): AlbumDto =
        albums.first { it.id == albumId }
}

class AlbumRetrofitDataSourceImplTest {

    private lateinit var service: FakeAlbumService
    private lateinit var dataSource: AlbumRetrofitDataSourceImpl

    @Before
    fun setUp() {
        service = FakeAlbumService()
        dataSource = AlbumRetrofitDataSourceImpl(service)
    }

    @After
    fun tearDown() {
        service.albums = emptyList()
    }

    @Test
    fun getAllAlbums_returnsDtosFromService() = runTest {
        service.albums = listOf(
            AlbumDto(1, title = "A", year = "2020", coverUrl = null, artist = ArtistDto(9, "X", null, null)),
            AlbumDto(2, title = "B", year = "2021", coverUrl = null, artist = ArtistDto(8, "Y", null, null))
        )

        val result = dataSource.getAllAlbums()
        assertThat(result).isEqualTo(service.albums.map { it.toAlbumInfo() })
    }

    @Test
    fun getAlbumById_returnsDto() = runTest {
        service.albums = listOf(AlbumDto(7, title = "A", year = "2020", coverUrl = null, artist = ArtistDto(1, "X", null, null)))

        val album = dataSource.getAlbumById(7)
        assertThat(album).isEqualTo(service.albums.first().toAlbumInfo())
    }

    @Test(expected = IllegalStateException::class)
    fun createAlbum_throwsAsNotSupported() = runTest {
        dataSource.createAlbum(CreateAlbumDto("t", "y", "c", "a", "ai", "g"))
    }
}
