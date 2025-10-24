package com.example.proyecto_movil.data.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

/**
 * Sube imágenes de perfil a Firebase Storage y devuelve la URL pública.
 */
class StorageRepository @Inject constructor(
    private val storage: FirebaseStorage
) {
    suspend fun uploadProfileImage(uri: Uri): Result<String> {
        return try {
            val fileName = "profile_images/${UUID.randomUUID()}.jpg"
            val ref = storage.reference.child(fileName)
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
