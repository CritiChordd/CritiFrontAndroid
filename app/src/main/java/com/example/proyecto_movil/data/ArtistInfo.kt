package com.example.proyecto_movil.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ArtistInfo(
    val id: Int,
    val name: String,
    val profileImageUrl: String,
    val genre: String = ""
) : Parcelable
