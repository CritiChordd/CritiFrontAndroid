package com.example.proyecto_movil.data.datasource.impl.firestore

import com.example.proyecto_movil.data.UserInfo
import com.google.firebase.firestore.FieldValue
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

        return data.toUserInfo(defaultId = id)
    }

    suspend fun searchUsersByName(query: String, limit: Long = 10): List<UserInfo> {
        if (query.isBlank()) return emptyList()

        val normalized = query.trim().lowercase()
        val documents = linkedMapOf<String, Map<String, Any?>>()

        suspend fun collectFromField(field: String) {
            runCatching {
                collection
                    .orderBy(field)
                    .startAt(normalized)
                    .endAt(normalized + "\uf8ff")
                    .limit(limit)
                    .get()
                    .await()
            }.getOrNull()?.documents?.forEach { doc ->
                val data = doc.data ?: return@forEach
                documents.putIfAbsent(doc.id, data)
            }
        }

        collectFromField("usernameLowercase")
        collectFromField("nameLowercase")

        if (documents.isEmpty()) {
            val snapshot = collection.limit(limit).get().await()
            snapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach
                val candidate = data.toUserInfo(defaultId = doc.id)
                val matches = sequenceOf(candidate.username, candidate.name, candidate.bio)
                    .any { it.contains(query, ignoreCase = true) }
                if (matches) {
                    documents.putIfAbsent(doc.id, data)
                }
            }
        }

        return documents.mapNotNull { (id, data) ->
            data.toUserInfo(defaultId = id)
        }.take(limit.toInt())
    }

    suspend fun createOrUpdateUser(
        id: String,
        username: String,
        email: String,
        bio: String = "",
        profilePic: String? = null,
        name: String? = null
    ) {
        val resolvedName = name?.takeIf { it.isNotBlank() } ?: username

        val doc = mapOf(
            "id" to id,                               // ← guardado como String
            "username" to username,
            "email" to email,
            "bio" to bio,
            "profileImageUrl" to (profilePic ?: ""),
            "name" to resolvedName,
            "usernameLowercase" to username.lowercase(),
            "nameLowercase" to resolvedName.lowercase(),
            "followers" to 0,
            "followersCount" to 0,
            "following" to 0,
            "followingCount" to 0
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
        updates["usernameLowercase"] = username.lowercase()
        updates["nameLowercase"] = username.lowercase()

        collection.document(id).update(updates).await()
        return getUserById(id)
    }

    suspend fun isFollowing(currentUserId: String, targetUserId: String): Boolean {
        if (currentUserId.isBlank() || targetUserId.isBlank()) return false

        val doc = collection
            .document(currentUserId)
            .collection("following")
            .document(targetUserId)
            .get()
            .await()

        return doc.exists()
    }

    suspend fun followUser(currentUserId: String, targetUserId: String): UserInfo {
        if (currentUserId.isBlank() || targetUserId.isBlank() || currentUserId == targetUserId) {
            return getUserById(targetUserId)
        }

        val currentDoc = collection.document(currentUserId)
        val targetDoc = collection.document(targetUserId)
        val followingDoc = currentDoc.collection("following").document(targetUserId)
        val followerDoc = targetDoc.collection("followers").document(currentUserId)

        db.runTransaction { tx ->
            val alreadyFollowing = tx.get(followingDoc).exists()
            if (!alreadyFollowing) {
                tx.set(
                    followingDoc,
                    mapOf(
                        "userId" to targetUserId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.set(
                    followerDoc,
                    mapOf(
                        "userId" to currentUserId,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )
                tx.update(
                    currentDoc,
                    mapOf(
                        "following" to FieldValue.increment(1),
                        "followingCount" to FieldValue.increment(1)
                    )
                )
                tx.update(
                    targetDoc,
                    mapOf(
                        "followers" to FieldValue.increment(1),
                        "followersCount" to FieldValue.increment(1)
                    )
                )
            }
            null
        }.await()

        return getUserById(targetUserId)
    }

    suspend fun unfollowUser(currentUserId: String, targetUserId: String): UserInfo {
        if (currentUserId.isBlank() || targetUserId.isBlank() || currentUserId == targetUserId) {
            return getUserById(targetUserId)
        }

        val currentDoc = collection.document(currentUserId)
        val targetDoc = collection.document(targetUserId)
        val followingDoc = currentDoc.collection("following").document(targetUserId)
        val followerDoc = targetDoc.collection("followers").document(currentUserId)

        db.runTransaction { tx ->
            val isFollowing = tx.get(followingDoc).exists()
            if (isFollowing) {
                tx.delete(followingDoc)
                tx.delete(followerDoc)
                tx.update(
                    currentDoc,
                    mapOf(
                        "following" to FieldValue.increment(-1),
                        "followingCount" to FieldValue.increment(-1)
                    )
                )
                tx.update(
                    targetDoc,
                    mapOf(
                        "followers" to FieldValue.increment(-1),
                        "followersCount" to FieldValue.increment(-1)
                    )
                )
            }
            null
        }.await()

        return getUserById(targetUserId)
    }

    private fun Map<String, Any?>.toUserInfo(defaultId: String): UserInfo {
        val resolvedUsername = sequenceOf(
            this["username"],
            this["name"],
            this["displayName"],
            this["email"]
        )
            .mapNotNull { it?.toString() }
            .firstOrNull { it.isNotBlank() }
            .orEmpty()

        val resolvedAvatar = this["profileImageUrl"]
            ?: this["profileImageURL"]
            ?: this["profile_pic"]

        val resolvedName = this["name"]?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: resolvedUsername

        fun Any?.asBackendId(): String? = when (this) {
            is Number -> this.toLong().toString()
            is String -> this.takeIf { it.isNotBlank() }
            else -> null
        }

        val backendId = this["backendUserId"].asBackendId()
            ?: this["apiUserId"].asBackendId()
            ?: this["numericId"].asBackendId()

        return UserInfo(
            id = this["id"]?.toString() ?: defaultId,
            name = resolvedName,
            username = resolvedUsername,
            profileImageUrl = resolvedAvatar?.toString().orEmpty(),
            bio = this["bio"]?.toString().orEmpty(),
            followers = sequenceOf(
                this["followers"],
                this["followersCount"],
                this["followers_count"],
            ).mapNotNull { (it as? Number)?.toInt() }.firstOrNull() ?: 0,
            following = sequenceOf(
                this["following"],
                this["followingCount"],
                this["following_count"],
            ).mapNotNull { (it as? Number)?.toInt() }.firstOrNull() ?: 0,
            playlists = emptyList(),
            backendUserId = backendId
        )
    }
}
