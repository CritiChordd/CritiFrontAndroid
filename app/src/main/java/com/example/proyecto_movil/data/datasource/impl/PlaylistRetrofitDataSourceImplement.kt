package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.PlaylistInfo
import com.example.proyecto_movil.data.datasource.PlaylistRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.PlaylistRetrofitService
import com.example.proyecto_movil.data.dtos.toPlaylistInfo
import javax.inject.Inject

/**
 * Implementación de PlaylistRemoteDataSource usando Retrofit.
 */
class PlaylistRetrofitDataSourceImpl @Inject constructor(
    private val service: PlaylistRetrofitService
) : PlaylistRemoteDataSource {

    override suspend fun getAllPlaylists(): List<PlaylistInfo> {
        return service.getAllPlaylists().map { it.toPlaylistInfo() }
    }

    override suspend fun getPlaylistById(id: String): PlaylistInfo {
        return service.getPlaylistById(id).toPlaylistInfo()
    }

    override suspend fun createPlaylist(playlist: PlaylistInfo) {
        // Si tu API espera un DTO distinto, mapéalo aquí
        val dto = com.example.proyecto_movil.data.dtos.PlaylistDto(
            id = playlist.id,
            title = playlist.title,
            description = playlist.description,
            albums = emptyList() // o mapeo si el backend lo requiere
        )
        service.createPlaylist(dto)
    }

    override suspend fun updatePlaylist(id: String, playlist: PlaylistInfo) {
        val dto = com.example.proyecto_movil.data.dtos.PlaylistDto(
            id = playlist.id,
            title = playlist.title,
            description = playlist.description,
            albums = emptyList()
        )
        service.updatePlaylist(id, dto)
    }

    override suspend fun deletePlaylist(id: String) {
        service.deletePlaylist(id)
    }
}
