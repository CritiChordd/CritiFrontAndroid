package com.example.proyecto_movil.data.datasource.impl.retrofit

import com.example.proyecto_movil.data.ArtistInfo
import com.example.proyecto_movil.data.datasource.ArtistRemoteDataSource
import com.example.proyecto_movil.data.datasource.services.ArtistRetrofitService
import com.example.proyecto_movil.data.dtos.toArtistInfo
import javax.inject.Inject

class ArtistRetrofitDataSourceImpl @Inject constructor(
    private val service: ArtistRetrofitService
) : ArtistRemoteDataSource {

    override suspend fun getAllArtists(): List<ArtistInfo> {
        return service.getAllArtists().map { it.toArtistInfo() }
    }

    override suspend fun getArtistById(id: String): ArtistInfo {
        return service.getArtistById(id).toArtistInfo()
    }
}
