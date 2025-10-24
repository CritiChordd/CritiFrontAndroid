package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.datasource.AlbumRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.AlbumRetrofitService
import com.example.proyecto_movil.data.dtos.AlbumDto
import com.example.proyecto_movil.data.dtos.CreateAlbumDto
import javax.inject.Inject

class AlbumRetrofitDataSourceImpl @Inject constructor(
    private val service: AlbumRetrofitService
) : AlbumRemoteDataSource {

    override suspend fun getAllAlbums(): List<AlbumDto> = service.getAllAlbums()

    override suspend fun getAlbumById(id: Int): AlbumDto = service.getAlbumById(id)

    override suspend fun createAlbum(request: CreateAlbumDto): AlbumDto {
        error("Crear álbumes no está soportado por el servicio Retrofit")
    }
}

