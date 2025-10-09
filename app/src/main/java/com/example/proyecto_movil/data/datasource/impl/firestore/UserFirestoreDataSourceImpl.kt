package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserFirestoreDataSourceImpl(
    private val db: FirebaseFirestore
) {
    private val collection = db.collection("users")

    suspend fun getUserById(id: String): UserInfo {
        val snap = collection.document(id).get().await()
        if (!snap.exists()) throw IllegalStateException("Usuario no encontrado en Firestore: $id")

        val data = snap.data.orEmpty()

        val resolvedUsername = sequenceOf(
            data["username"],
            data["name"],
            data["displayName"],
            data["email"]
        )
            .mapNotNull { it?.toString() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()

        val resolvedAvatar = data["profileImageUrl"]
            ?: data["profileImageURL"]
            ?: data["profile_pic"]

        val backendId = (data["backendUserId"] as? Number)
            ?: (data["apiUserId"] as? Number)
            ?: (data["numericId"] as? Number)

        return UserInfo(
            id = data["id"]?.toString() ?: id,                         // ← String
            username = resolvedUsername,
            profileImageUrl = resolvedAvatar?.toString().orEmpty(),
            bio = data["bio"]?.toString().orEmpty(),
            followers = (data["followers"] as? Number)?.toInt() ?: 0,
            following = (data["following"] as? Number)?.toInt() ?: 0,
            playlists = emptyList(),
            backendUserId = backendId?.toInt()
        )
    }

    suspend fun createOrUpdateUser(
        id: String,
        username: String,
        email: String,
        bio: String = "",
        profilePic: String? = null,
        name: String? = null
    ) {
        val doc = mapOf(
            "id" to id,                               // ← guardado como String
            "username" to username,
            "email" to email,
            "bio" to bio,
            "profileImageUrl" to (profilePic ?: ""),
            "name" to (name ?: ""),
            "followers" to 0,
            "following" to 0
        )
        collection.document(id).set(doc).await()
    }

    suspend fun updateUser(
        id: String,
        username: String,
        bio: String,
        profilePic: String? = null
    ): UserInfo {
        val updates = mutableMapOf<String, Any>(
            "username" to username,
            "bio" to bio
        )
        if (profilePic != null) updates["profileImageUrl"] = profilePic

        collection.document(id).update(updates).await()
        return getUserById(id)
    }
}
