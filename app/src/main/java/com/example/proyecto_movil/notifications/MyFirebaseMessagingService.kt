package com.example.proyecto_movil.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.proyecto_movil.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Persistir el token en el usuario logueado, si lo hay
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
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
        val type = message.data["type"]

        if (type == "review_like") {
            val likerId = message.data["likerId"].orEmpty()
            val reviewSnippet = message.data["reviewSnippet"].orEmpty()

            // Prioriza el body enviado por FCM si viene completo (incluye nombre)
            val pushBody = message.notification?.body ?: message.data["body"]
            if (!pushBody.isNullOrBlank()) {
                showNotification(
                    title = message.notification?.title ?: "Nuevo Like",
                    body = pushBody
                )
                return
            }

            // Si no viene el body con el nombre, intentamos obtener el nombre del usuario que dio like
            CoroutineScope(Dispatchers.IO).launch {
                val likerName = runCatching {
                    if (likerId.isNotBlank()) {
                        val doc = FirebaseFirestore.getInstance().collection("users").document(likerId).get().await()
                        doc.getString("name")?.takeIf { it.isNotBlank() }
                            ?: doc.getString("username")?.takeIf { it.isNotBlank() }
                    } else null
                }.getOrNull() ?: "Alguien"

                val title = "Nuevo Like"
                val body = buildString {
                    append("$likerName le dio like a tu reseña")
                    if (reviewSnippet.isNotBlank()) {
                        append('\n')
                        append('"')
                        append(reviewSnippet)
                        append('"')
                    }
                }
                showNotification(title, body)
            }
            return
        }

        // Caso genérico (seguidores u otros tipos)
        val title = message.notification?.title ?: message.data["title"] ?: "Notificación"
        val body = message.notification?.body ?: message.data["body"] ?: "Tienes una nueva notificación"
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
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

        with(NotificationManagerCompat.from(this)) {
            notify((System.currentTimeMillis() % 100000).toInt(), builder.build())
        }
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
}
