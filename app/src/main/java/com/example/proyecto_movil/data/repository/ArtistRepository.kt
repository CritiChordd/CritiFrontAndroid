package com.example.proyecto_movil.data.repository

import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.datasource.ArtistRemoteDataSource
import javax.inject.Inject

class ArtistRepository @Inject constructor(
    private val artistRemoteDataSource: ArtistRemoteDataSource
) {

    suspend fun getAllArtists(): Result<List<ArtistInfo>> {
        return try {
            val artists = artistRemoteDataSource.getAllArtists()
            Result.success(artists)
        } catch (e: Exception) {
            println("ArtistRepository - Error en getAllArtists: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getArtistById(id: String): Result<ArtistInfo> {
        return try {
            val artist = artistRemoteDataSource.getArtistById(id)
            Result.success(artist)
        } catch (e: Exception) {
            println("ArtistRepository - Error en getArtistById: ${e.message}")
            Result.failure(e)
        }
    }
}
