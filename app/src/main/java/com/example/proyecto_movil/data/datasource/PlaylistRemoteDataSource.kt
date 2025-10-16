package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.PlaylistInfo

/**
 * Contrato para las fuentes de datos remotas de playlists.
 */
interface PlaylistRemoteDataSource {

    suspend fun getAllPlaylists(): List<PlaylistInfo>

    suspend fun getPlaylistById(id: String): PlaylistInfo

    suspend fun createPlaylist(playlist: PlaylistInfo)

    suspend fun updatePlaylist(id: String, playlist: PlaylistInfo)

    suspend fun deletePlaylist(id: String)
}
