package com.example.proyecto_movil.data.datasource

import com.example.proyecto_movil.data.dtos.CreateAlbumDto
import com.example.proyecto_movil.data.dtos.AlbumDto


interface AlbumRemoteDataSource {

    suspend fun getAllAlbums(): List<AlbumDto>

    suspend fun getAlbumById(id: Int): AlbumDto

    suspend fun createAlbum(request: CreateAlbumDto): AlbumDto
}
