package com.example.proyecto_movil.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.proyecto_movil.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicInteger

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        serviceScope.launch {
            runCatching {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update(mapOf("fcmToken" to token))
                    .await()
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        when (message.data["type"]) {
            "review_like" -> handleLikeNotification(message)
            else -> {
                val title = message.data["title"] ?: message.notification?.title ?: "Notificación"
                val body = message.data["body"] ?: message.notification?.body ?: "Tienes una nueva notificación"
                val avatar = message.data["actorImageUrl"].orEmpty()
                serviceScope.launch {
                    showNotification(title, body, avatar)
                }
            }
        }
    }

    private fun handleLikeNotification(message: RemoteMessage) {
        val likerId = message.data["likerId"].orEmpty()
        val likerName = message.data["likerName"].orEmpty()
        val reviewSnippet = message.data["reviewSnippet"].orEmpty()
        val avatar = message.data["likerAvatarUrl"].orEmpty()
        val providedTitle = message.data["title"].orEmpty()
        val providedBody = message.data["body"].orEmpty()

        serviceScope.launch {
            val (resolvedName, resolvedAvatar) = resolveLikerInfo(
                likerId = likerId,
                fallbackName = likerName,
                fallbackAvatar = avatar
            )

            val title = providedTitle.ifBlank { "Nuevo Like" }
            val body = providedBody.ifBlank {
                buildString {
                    append("$resolvedName le dio like a tu reseña")
                    if (reviewSnippet.isNotBlank()) {
                        append('\n')
                        append('"')
                        append(reviewSnippet)
                        append('"')
                    }
                }
            }

            showNotification(title, body, resolvedAvatar)
        }
    }

    private suspend fun resolveLikerInfo(
        likerId: String,
        fallbackName: String,
        fallbackAvatar: String
    ): Pair<String, String> {
        if (likerId.isBlank()) {
            return Pair(fallbackName.ifBlank { "Alguien" }, fallbackAvatar)
        }

        return runCatching {
            val doc = FirebaseFirestore.getInstance()
                .collection("users")
                .document(likerId)
                .get()
                .await()

            val name = sequenceOf(
                doc.getString("name"),
                doc.getString("username"),
                fallbackName.takeIf { it.isNotBlank() }
            ).mapNotNull { it?.takeIf { value -> value.isNotBlank() } }
                .firstOrNull()
                ?: "Alguien"

            val avatar = sequenceOf(
                doc.getString("profileImageUrl"),
                doc.getString("profileImageURL"),
                doc.getString("profile_pic"),
                doc.getString("avatarUrl"),
                doc.getString("photoUrl"),
                doc.getString("photoURL"),
                fallbackAvatar.takeIf { it.isNotBlank() }
            ).mapNotNull { it?.takeIf { value -> value.isNotBlank() } }
                .firstOrNull()
                .orEmpty()

            Pair(name, avatar)
        }.getOrElse {
            Pair(fallbackName.ifBlank { "Alguien" }, fallbackAvatar)
        }
    }

    private suspend fun showNotification(title: String, body: String, largeIconUrl: String) {
        val channelId = "likes_channel"
        createNotificationChannel(channelId)

        val shortBody = body.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.let {
                val maxLength = 80
                if (it.length > maxLength) it.take(maxLength - 1) + "…" else it
            }
            ?: body

        val largeIcon = loadBitmap(largeIconUrl)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(shortBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setCategory(NotificationCompat.CATEGORY_SOCIAL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        largeIcon?.let { builder.setLargeIcon(it) }

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID.getAndIncrement(), builder.build())
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        if (url.isBlank()) return null

        return runCatching {
            val loader: ImageLoader = Coil.imageLoader(applicationContext)
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .allowHardware(false)
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                result.drawable.toBitmap()
            } else {
                null
            }
        }.getOrNull()
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Likes"
            val descriptionText = "Notificaciones de likes"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        private val NOTIFICATION_ID = AtomicInteger(4000)
    }
}
