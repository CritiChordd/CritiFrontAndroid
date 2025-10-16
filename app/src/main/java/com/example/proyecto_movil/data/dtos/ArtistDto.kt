package com.example.proyecto_movil.data.dtos
// ArtistDto.kt
import android.util.Log
import com.google.gson.annotations.SerializedName
import com.example.proyecto_movil.data.ArtistInfo

data class ArtistDto(
    val id: Int,
    val name: String? = null,

    @SerializedName("image_url")   // 🔹 tu backend manda "image_url"
    val imageUrl: String? = null,

    val genre: String? = null
)

fun ArtistDto.toArtistInfo(): ArtistInfo {
    Log.d("ArtistMapper", "🖼️ Artista: $name -> $imageUrl")
    return ArtistInfo(
        id = id,
        name = name ?: "Desconocido",
        profileImageUrl = imageUrl
            ?: "https://placehold.co/300x300?text=No+Image",
        genre = genre ?: "Sin género"
    )
}
