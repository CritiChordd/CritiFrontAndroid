package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.datasource.AlbumRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.AlbumRetrofitService
import com.example.proyecto_movil.data.dtos.toAlbumInfo
import com.example.proyecto_movil.data.dtos.CreateAlbumDto
import javax.inject.Inject

class AlbumRetrofitDataSourceImpl @Inject constructor(
    private val service: AlbumRetrofitService
) : AlbumRemoteDataSource {

    override suspend fun getAllAlbums(): List<AlbumInfo> {
        return service.getAllAlbums().map { it.toAlbumInfo() }
    }

    override suspend fun getAlbumById(id: Int): AlbumInfo {
        return service.getAlbumById(id).toAlbumInfo()
    }

    override suspend fun createAlbum(request: CreateAlbumDto): AlbumInfo {
        error("Crear álbumes no está soportado por el servicio Retrofit")
    }

    override suspend fun searchAlbums(query: String, limit: Int): List<AlbumInfo> {
        val sanitized = query.trim()
        if (sanitized.length < 2) return emptyList()

        val maxItems = limit.coerceAtLeast(0)
        if (maxItems == 0) return emptyList()

        val normalized = sanitized.lowercase()
        return service.getAllAlbums()
            .map { it.toAlbumInfo() }
            .filter { album ->
                album.title.contains(normalized, ignoreCase = true) ||
                    album.artist.name.contains(normalized, ignoreCase = true)
            }
            .take(maxItems)
    }
}
