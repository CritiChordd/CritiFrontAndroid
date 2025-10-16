package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.AlbumInfo


interface AlbumRemoteDataSource {

    suspend fun getAllAlbums(): List<AlbumInfo>

    suspend fun getAlbumById(id: String): AlbumInfo
}
