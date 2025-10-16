package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.ArtistInfo

interface ArtistRemoteDataSource {

    suspend fun getAllArtists(): List<ArtistInfo>

    suspend fun getArtistById(id: String): ArtistInfo
}
