package com.example.proyecto_movil.data.dtos

import com.google.gson.annotations.SerializedName
import com.example.proyecto_movil.data.AlbumInfo

data class AlbumDto(
    val id: Int,

    @SerializedName("name")
    val title: String? = null,
    val year: String? = null,

    @SerializedName("image_url")
    val coverUrl: String? = null,

    val artist: ArtistDto
)

fun AlbumDto.toAlbumInfo(): AlbumInfo {
    return AlbumInfo(
        id = id,
        title = title ?: "Sin título",
        year = year ?: "Año desconocido",
        coverUrl = coverUrl ?: "https://placehold.co/600x400?text=No+Cover",
        artist = artist.toArtistInfo()
    )
}
