package com.example.proyecto_movil.data.repository

import android.net.Uri
import com.example.proyecto_movil.data.datasource.StorageRemoteDataSource
import java.util.UUID

/**
 * Sube imágenes de perfil usando el DataSource y devuelve la URL pública.
 */
class StorageRepository(
    private val storageRemoteDataSource: StorageRemoteDataSource
) {
    suspend fun uploadProfileImage(uri: Uri): Result<String> = try {
        val path = "profile_images/${UUID.randomUUID()}.jpg"
        val url = storageRemoteDataSource.uploadImage(path, uri)
        Result.success(url)
    } catch (e: Throwable) { Result.failure(e) }
}

