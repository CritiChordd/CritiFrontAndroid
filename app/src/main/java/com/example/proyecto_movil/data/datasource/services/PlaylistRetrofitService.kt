package com.example.proyecto_movil.data.datasource.services

import com.example.proyecto_movil.data.dtos.PlaylistDto
import retrofit2.http.*

/**
 * Define los endpoints REST para manejar playlists.
 */
interface PlaylistRetrofitService {

    @GET("playlists")
    suspend fun getAllPlaylists(): List<PlaylistDto>

    @GET("playlists/{id}")
    suspend fun getPlaylistById(@Path("id") playlistId: String): PlaylistDto

    @POST("playlists")
    suspend fun createPlaylist(@Body playlist: PlaylistDto)

    @PUT("playlists/{id}")
    suspend fun updatePlaylist(@Path("id") playlistId: String, @Body playlist: PlaylistDto)

    @DELETE("playlists/{id}")
    suspend fun deletePlaylist(@Path("id") playlistId: String)
}
