package com.example.proyecto_movil.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    fun signOut() {
        auth.signOut()
    }

    // Obtiene el token actual de FCM y lo persiste en el documento del usuario
    suspend fun refreshFcmTokenAndSave() {
        val uid = auth.currentUser?.uid ?: return
        runCatching {
            val token = FirebaseMessaging.getInstance().token.await()
            if (!token.isNullOrBlank()) {
                firestore.collection("users")
                    .document(uid)
                    .update(mapOf("fcmToken" to token))
                    .await()
            }
        }
    }
}
