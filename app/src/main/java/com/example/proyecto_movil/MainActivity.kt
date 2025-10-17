package com.example.proyecto_movil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_movil.navigation.AppNavHost
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import com.example.proyecto_movil.ui.utils.AppLogo
import com.example.proyecto_movil.ui.utils.BackGroundImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationsPermissionIfNeeded()
        refreshFcmTokenIfLogged()

        setContent {
            Proyecto_movilTheme {
                CritiChordApp()
            }
        }
    }

    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val granted = ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }
        }
    }

    private fun refreshFcmTokenIfLogged() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        lifecycleScope.launch {
            runCatching {
                val token = FirebaseMessaging.getInstance().token.await()
                if (!token.isNullOrBlank()) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(uid)
                        .update(mapOf("fcmToken" to token))
                        .await()
                }
            }
        }
    }
}


@Preview
@Composable
fun MainPreview() {
    Proyecto_movilTheme {
        CritiChordApp()
    }
}
