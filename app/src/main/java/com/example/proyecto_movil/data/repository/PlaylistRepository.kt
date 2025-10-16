package com.example.proyecto_movil.data.repository

import coil.network.HttpException
import com.example.proyecto_movil.data.PlaylistInfo
import com.example.proyecto_movil.data.datasource.impl.retrofit.PlaylistRetrofitDataSourceImpl
import javax.inject.Inject

class PlaylistRepository @Inject constructor(
    private val playlistRemoteDataSource: PlaylistRetrofitDataSourceImpl
) {

    suspend fun getAllPlaylists(): Result<List<PlaylistInfo>> = try {
        Result.success(playlistRemoteDataSource.getAllPlaylists())
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getPlaylistById(id: String): Result<PlaylistInfo> = try {
        Result.success(playlistRemoteDataSource.getPlaylistById(id))
    } catch (e: HttpException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun createPlaylist(playlist: PlaylistInfo): Result<Unit> = try {
        playlistRemoteDataSource.createPlaylist(playlist)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePlaylist(id: String, playlist: PlaylistInfo): Result<Unit> = try {
        playlistRemoteDataSource.updatePlaylist(id, playlist)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePlaylist(id: String): Result<Unit> = try {
        playlistRemoteDataSource.deletePlaylist(id)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
