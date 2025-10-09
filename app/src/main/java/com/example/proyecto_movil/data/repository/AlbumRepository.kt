package com.example.proyecto_movil.data.repository

import android.util.Log
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.datasource.AlbumRemoteDataSource
import javax.inject.Inject

class AlbumRepository @Inject constructor(
    private val albumRemoteDataSource: AlbumRemoteDataSource
) {

    suspend fun getAllAlbums(): Result<List<AlbumInfo>> {
        return try {
            val albums = albumRemoteDataSource.getAllAlbums()
            Result.success(albums)
        } catch (e: Exception) {
            Log.e("AlbumRepository", "Error en getAllAlbums: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAlbumById(id: Int): Result<AlbumInfo> {
        return try {
            val album = albumRemoteDataSource.getAlbumById(id)
            Result.success(album)
        } catch (e: Exception) {
            Log.e("AlbumRepository", "Error en getAlbumById: ${e.message}")
            Result.failure(e)
        }
    }
}
