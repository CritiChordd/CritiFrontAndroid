package com.example.proyecto_movil.data.dtos

import com.example.proyecto_movil.data.PlaylistInfo

/**
 * DTO que representa la estructura JSON de una Playlist desde la API.
 */
data class PlaylistDto(
    val id: Int,
    val title: String,
    val description: String,
    val albums: List<AlbumDto> // cada álbum dentro de la playlist
)

/**
 * Mapper: convierte PlaylistDto → PlaylistInfo
 */
fun PlaylistDto.toPlaylistInfo(): PlaylistInfo {
    return PlaylistInfo(
        id = id,
        title = title,
        description = description,
        albums = albums.map { it.toAlbumInfo() }
    )
}
