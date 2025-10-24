package com.example.proyecto_movil

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.proyecto_movil.navigation.Screen
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala el Splash Screen (Android 12+)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        requestNotificationsPermissionIfNeeded()
        refreshFcmTokenIfLogged()

        setContent {
            Proyecto_movilTheme {
                // ✅ Detecta si hay usuario autenticado en Firebase
                val user = FirebaseAuth.getInstance().currentUser
                val startDestination by remember {
                    mutableStateOf(
                        if (user != null) Screen.Home.route else Screen.Welcome.route
                    )
                }

                // 🔥 Llama al composable principal con el destino inicial correcto
                CritiChordApp(startDestination)
            }
        }
    }

    /** ------------------- Permisos ------------------- **/
    private fun requestNotificationsPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            val granted = ContextCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }
        }
    }

    /** ------------------- Token FCM ------------------- **/
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
