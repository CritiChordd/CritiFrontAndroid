package com.example.proyecto_movil.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AlbumInfo(
    val id: Int,
    val title: String,
    val year: String,
    val coverUrl: String,
    val artist: ArtistInfo
) : Parcelable
