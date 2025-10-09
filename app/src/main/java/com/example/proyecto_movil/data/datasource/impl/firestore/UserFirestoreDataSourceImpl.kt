package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.dtos.ReviewDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserFirestoreDataSourceImpl(
    private val db: FirebaseFirestore
) {
    private val collection = db.collection("users")

    suspend fun getUserById(id: String): UserInfo {
        val docRef = db.collection("users").document(id)
        val snap = collection.document(id).get().await()
        if (!snap.exists()) throw IllegalStateException("Usuario no encontrado en Firestore: $id")

        val data = snap.data.orEmpty()

        return UserInfo(
            id = data["id"]?.toString() ?: id,                         // ← String
            username = data["username"]?.toString().orEmpty(),
            profileImageUrl = data["profileImageUrl"]?.toString().orEmpty(),
            bio = data["bio"]?.toString().orEmpty(),
            followers = (data["followers"] as? Number)?.toInt() ?: 0,
            following = (data["following"] as? Number)?.toInt() ?: 0,
            playlists = emptyList(),
            name = data["name"]?.toString().orEmpty()
        )
    }

    suspend fun createOrUpdateUser(
        id: String,
        username: String,
        email: String,
        bio: String = "",
        profilePic: String? = null
    ) {
        val doc = mapOf(
            "id" to id,                               // ← guardado como String
            "username" to username,
            "email" to email,
            "bio" to bio,
            "profileImageUrl" to (profilePic ?: ""),
            "followers" to 0,
            "following" to 0
        )
        collection.document(id).set(doc).await()
    }
  suspend fun getUserReviews(id: String): List<ReviewDto> {
       val snapshot = db.collection("reviews").whereEqualTo("userId", id).get().await()
       return snapshot.documents.map { doc ->
         val review = doc.toObject(ReviewDto::class.java)
         review?.copy(id = doc.id) ?: throw Exception("Review not found")
       }

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
