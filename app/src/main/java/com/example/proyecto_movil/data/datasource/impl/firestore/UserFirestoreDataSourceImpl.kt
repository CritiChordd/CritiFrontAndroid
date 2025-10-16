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

        return UserInfo(
            id = data["id"]?.toString() ?: id,                         // ← String
            username = data["username"]?.toString().orEmpty(),
            profileImageUrl = data["profileImageUrl"]?.toString().orEmpty(),
            bio = data["bio"]?.toString().orEmpty(),
            followers = (data["followers"] as? Number)?.toInt() ?: 0,
            following = (data["following"] as? Number)?.toInt() ?: 0,
            playlists = emptyList()
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

    // --- FCM token management ---
    suspend fun saveFcmToken(userId: String, token: String) {
        collection.document(userId).update(mapOf("fcmToken" to token)).await()
    }

    // --- Follow / Unfollow ---
    suspend fun followUser(followerId: String, targetUserId: String) {
        if (followerId == targetUserId) return
        val followerFollowingRef = collection.document(followerId)
            .collection("following").document(targetUserId)
        val targetFollowersRef = collection.document(targetUserId)
            .collection("followers").document(followerId)

        db.runTransaction { txn ->
            txn.set(followerFollowingRef, mapOf("since" to System.currentTimeMillis()))
            txn.set(targetFollowersRef, mapOf("since" to System.currentTimeMillis()))

            // Optionally maintain counters on user documents
            val followerDoc = collection.document(followerId)
            val targetDoc = collection.document(targetUserId)
            val followerSnap = txn.get(followerDoc)
            val targetSnap = txn.get(targetDoc)
            val following = (followerSnap.getLong("following") ?: 0L).toInt() + 1
            val followers = (targetSnap.getLong("followers") ?: 0L).toInt() + 1
            txn.update(followerDoc, mapOf("following" to following))
            txn.update(targetDoc, mapOf("followers" to followers))
        }.await()
    }

    suspend fun unfollowUser(followerId: String, targetUserId: String) {
        if (followerId == targetUserId) return
        val followerFollowingRef = collection.document(followerId)
            .collection("following").document(targetUserId)
        val targetFollowersRef = collection.document(targetUserId)
            .collection("followers").document(followerId)

        db.runTransaction { txn ->
            txn.delete(followerFollowingRef)
            txn.delete(targetFollowersRef)

            val followerDoc = collection.document(followerId)
            val targetDoc = collection.document(targetUserId)
            val followerSnap = txn.get(followerDoc)
            val targetSnap = txn.get(targetDoc)
            val following = (followerSnap.getLong("following") ?: 0L).toInt().minus(1).coerceAtLeast(0)
            val followers = (targetSnap.getLong("followers") ?: 0L).toInt().minus(1).coerceAtLeast(0)
            txn.update(followerDoc, mapOf("following" to following))
            txn.update(targetDoc, mapOf("followers" to followers))
        }.await()
    }
}
