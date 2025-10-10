package com.example.proyecto_movil.data.dtos

/**
 * Información necesaria para crear un nuevo álbum en Firestore.
 * Todos los campos de texto se limpian en el repositorio antes de enviarse.
 */
data class CreateAlbumDto(
    val title: String,
    val year: String,
    val coverUrl: String,
    val artistName: String,
    val artistImageUrl: String,
    val artistGenre: String,
    val albumId: Int? = null,
    val artistId: Int? = null
)

/**
 * Representación sencilla de los documentos almacenados en la colección `albums`.
 */
data class AlbumFirestoreDto(
    val id: Int? = null,
    val title: String? = null,
    val year: String? = null,
    val coverUrl: String? = null,
    val artistId: Int? = null,
    val artistName: String? = null,
    val artistImageUrl: String? = null,
    val artistGenre: String? = null
)
