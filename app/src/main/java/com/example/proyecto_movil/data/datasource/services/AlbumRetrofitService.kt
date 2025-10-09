package com.example.proyecto_movil.data.datasource.services

import com.example.proyecto_movil.data.dtos.AlbumDto
import retrofit2.http.GET
import retrofit2.http.Path

interface AlbumRetrofitService {

    @GET("albums")
    suspend fun getAllAlbums(): List<AlbumDto>

    @GET("albums/{id}")
    suspend fun getAlbumById(@Path("id") albumId: Int): AlbumDto
}
