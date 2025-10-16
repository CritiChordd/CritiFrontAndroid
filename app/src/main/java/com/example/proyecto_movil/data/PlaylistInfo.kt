package com.example.proyecto_movil.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modelo de UI/Dominio que representa una Playlist.
 */
@Parcelize
data class PlaylistInfo(
    val id: Int,
    val title: String,
    val description: String = "",
    val albums: List<AlbumInfo> = emptyList()
) : Parcelable
