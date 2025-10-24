package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.datasource.impl.retrofit.AlbumRetrofitDataSourceImpl
import com.example.proyecto_movil.data.datasource.services.AlbumRetrofitService
import com.example.proyecto_movil.data.dtos.AlbumDto
import com.example.proyecto_movil.data.dtos.ArtistDto
import com.example.proyecto_movil.data.dtos.CreateAlbumDto
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeAlbumService : AlbumRetrofitService {
    var albums: List<AlbumDto> = emptyList()

    override suspend fun getAllAlbums(): List<AlbumDto> = albums

    override suspend fun getAlbumById(albumId: Int): AlbumDto =
        albums.first { it.id == albumId }
}

class AlbumRetrofitDataSourceImplTest {

    @Test
    fun getAllAlbums_returnsDtosFromService() = runTest {
        val service = FakeAlbumService().apply {
            albums = listOf(
                AlbumDto(1, title = "A", year = "2020", coverUrl = null, artist = ArtistDto(9, "X", null, null)),
                AlbumDto(2, title = "B", year = "2021", coverUrl = null, artist = ArtistDto(8, "Y", null, null))
            )
        }
        val ds = AlbumRetrofitDataSourceImpl(service)

        val result = ds.getAllAlbums()
        assertThat(result).isEqualTo(service.albums)
    }

    @Test
    fun getAlbumById_returnsDto() = runTest {
        val service = FakeAlbumService().apply {
            albums = listOf(AlbumDto(7, title = "A", year = "2020", coverUrl = null, artist = ArtistDto(1, "X", null, null)))
        }
        val ds = AlbumRetrofitDataSourceImpl(service)

        val album = ds.getAlbumById(7)
        assertThat(album.id).isEqualTo(7)
    }

    @Test(expected = IllegalStateException::class)
    fun createAlbum_throwsAsNotSupported() = runTest {
        val service = FakeAlbumService()
        val ds = AlbumRetrofitDataSourceImpl(service)
        ds.createAlbum(CreateAlbumDto("t","y","c","a","ai","g"))
    }
}

