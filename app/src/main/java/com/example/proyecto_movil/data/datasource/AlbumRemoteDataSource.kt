package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.dtos.CreateAlbumDto


interface AlbumRemoteDataSource {

    suspend fun getAllAlbums(): List<AlbumInfo>

    suspend fun getAlbumById(id: Int): AlbumInfo

    suspend fun createAlbum(request: CreateAlbumDto): AlbumInfo
}
