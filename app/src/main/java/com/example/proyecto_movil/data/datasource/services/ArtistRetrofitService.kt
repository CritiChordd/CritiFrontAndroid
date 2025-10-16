package com.example.proyecto_movil.data.datasource.services

import com.example.proyecto_movil.data.dtos.ArtistDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ArtistRetrofitService {

    @GET("artists")
    suspend fun getAllArtists(): List<ArtistDto>

    @GET("artists/{id}")
    suspend fun getArtistById(@Path("id") id: String): ArtistDto
}
