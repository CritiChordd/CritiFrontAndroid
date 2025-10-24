package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.datasource.AlbumRemoteDataSource
import com.example.proyecto_movil.data.dtos.AlbumFirestoreDto
import com.example.proyecto_movil.data.dtos.CreateAlbumDto
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import jakarta.inject.Inject
import kotlinx.coroutines.tasks.await

class AlbumFirestoreDataSourceImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : AlbumRemoteDataSource {

    private val collection get() = firestore.collection("albums")

    override suspend fun getAllAlbums(): List<AlbumInfo> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { it.toAlbumInfoOrNull() }
            .sortedBy { it.title.lowercase() }
    }

    override suspend fun getAlbumById(id: Int): AlbumInfo {
        val direct = collection.document(id.toString()).get().await()
        direct.toAlbumInfoOrNull()?.let { return it }

        val fallback = collection
            .whereEqualTo("id", id)
            .limit(1)
            .get()
            .await()

        val album = fallback.documents.firstOrNull()?.toAlbumInfoOrNull()
        return album ?: throw NoSuchElementException("Álbum $id no encontrado en Firestore")
    }

    override suspend fun createAlbum(request: CreateAlbumDto): AlbumInfo {
        val title = request.title.trim()
        val artistName = request.artistName.trim()

        require(title.isNotBlank()) { "El título del álbum es obligatorio" }
        require(artistName.isNotBlank()) { "El nombre del artista es obligatorio" }

        val sanitizedYear = request.year.ifBlank { "Año desconocido" }.trim()
        val sanitizedCover = request.coverUrl.ifBlank { DEFAULT_COVER_URL }.trim()
        val sanitizedArtistImage = request.artistImageUrl.ifBlank { DEFAULT_ARTIST_IMAGE }.trim()
        val sanitizedGenre = request.artistGenre.trim()

        val albumId = request.albumId ?: generateIdentifier()
        val artistId = request.artistId ?: generateIdentifier()

        val payload = mapOf(
            "id" to albumId,
            "title" to title,
            "year" to sanitizedYear,
            "coverUrl" to sanitizedCover,
            "artistId" to artistId,
            "artistName" to artistName,
            "artistImageUrl" to sanitizedArtistImage,
            "artistGenre" to sanitizedGenre
        )

        collection.document(albumId.toString()).set(payload).await()

        return AlbumInfo(
            id = albumId,
            title = title,
            year = sanitizedYear,
            coverUrl = sanitizedCover,
            artist = ArtistInfo(
                id = artistId,
                name = artistName,
                profileImageUrl = sanitizedArtistImage,
                genre = sanitizedGenre
            )
        )
    }

    override suspend fun searchAlbums(query: String, limit: Int): List<AlbumInfo> {
        val sanitized = query.trim()
        if (sanitized.length < 2) return emptyList()

        val normalized = sanitized.lowercase()
        val maxItems = limit.coerceAtLeast(0)

        if (maxItems == 0) return emptyList()

        return getAllAlbums()
            .asSequence()
            .filter { album ->
                album.title.contains(normalized, ignoreCase = true) ||
                    album.artist.name.contains(normalized, ignoreCase = true)
            }
            .take(maxItems)
            .toList()
    }

    private fun DocumentSnapshot.toAlbumInfoOrNull(): AlbumInfo? {
        val dto = this.toObject(AlbumFirestoreDto::class.java) ?: return null
        val id = dto.id ?: this.getLong("id")?.toInt()
        val artistId = dto.artistId ?: this.getLong("artistId")?.toInt()

        if (id == null || artistId == null) return null

        val title = dto.title?.takeIf { it.isNotBlank() } ?: this.getString("title") ?: "Sin título"
        val year = dto.year?.takeIf { it.isNotBlank() } ?: this.getString("year") ?: "Año desconocido"
        val coverUrl = dto.coverUrl?.takeIf { it.isNotBlank() }
            ?: this.getString("coverUrl")
            ?: DEFAULT_COVER_URL

        val artistName = dto.artistName?.takeIf { it.isNotBlank() }
            ?: this.getString("artistName")
            ?: "Desconocido"

        val artistImageUrl = dto.artistImageUrl?.takeIf { it.isNotBlank() }
            ?: this.getString("artistImageUrl")
            ?: DEFAULT_ARTIST_IMAGE

        val artistGenre = dto.artistGenre?.takeIf { it.isNotBlank() }
            ?: this.getString("artistGenre")
            ?: ""

        return AlbumInfo(
            id = id,
            title = title,
            year = year,
            coverUrl = coverUrl,
            artist = ArtistInfo(
                id = artistId,
                name = artistName,
                profileImageUrl = artistImageUrl,
                genre = artistGenre
            )
        )
    }

    private fun generateIdentifier(): Int {
        val now = System.currentTimeMillis()
        return (now % Int.MAX_VALUE).toInt()
    }

    companion object {
        private const val DEFAULT_COVER_URL = "https://placehold.co/600x400?text=No+Cover"
        private const val DEFAULT_ARTIST_IMAGE = "https://placehold.co/300x300?text=No+Image"
    }
}
