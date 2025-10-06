package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.data.datasource.AlbumRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.AlbumRetrofitService
import com.example.proyecto_movil.data.dtos.toAlbumInfo
import javax.inject.Inject

class AlbumRetrofitDataSourceImpl @Inject constructor(
    private val service: AlbumRetrofitService
) : AlbumRemoteDataSource {

    override suspend fun getAllAlbums(): List<AlbumInfo> {
        return service.getAllAlbums().map { it.toAlbumInfo() }
    }

    override suspend fun getAlbumById(id: String): AlbumInfo {
        return service.getAlbumById(id).toAlbumInfo()
    }
}
