package com.example.proyecto_movil

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_movil.navigation.AppNavHost
import com.example.proyecto_movil.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.*

@Composable
fun CritiChordApp(startDestination: String = Screen.Welcome.route) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (shouldShowTopBar(currentRoute)) {
                CritiChordTopBar()
            }
        },
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                CritiChordBottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            AppNavHost(navController = navController, startDestination = startDestination)
        }
    }
}

/* -------- VISIBILIDAD -------- */
private fun shouldShowTopBar(route: String?): Boolean {
    val hideIn = listOf(
        Screen.Welcome.route,
        Screen.Login.route,
        Screen.Register.route
    )
    return route != null && route !in hideIn
}

private fun shouldShowBottomBar(route: String?): Boolean {
    if (route == null) return false
    return listOf(Screen.Home.route, Screen.FollowingFeed.route, "profile").any {
        route.startsWith(it)
    }
}

/* -------- BOTTOM NAV -------- */
data class BottomNavItem(
    val filledIcon: ImageVector,
    val outlineIcon: ImageVector,
    val route: String
)

@Composable
fun CritiChordBottomNavigationBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentBase = currentRoute?.substringBefore("/")

    val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    val items = remember(currentUid) {
        listOf(
            BottomNavItem(Icons.Filled.Home, Icons.Outlined.Home, Screen.Home.route),
            BottomNavItem(Icons.Filled.AddCircle, Icons.Outlined.AddCircle, Screen.AddReview.route),
            BottomNavItem(
                Icons.Filled.Person,
                Icons.Outlined.Person,
                currentUid?.let { Screen.Profile.createRoute(it) } ?: Screen.Login.route
            )
        )
    }

    NavigationBar {
        items.forEach { item ->
            val base = item.route.substringBefore("/")
            val selected = currentBase == base
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                        restoreState = true
                        popUpTo(Screen.Home.route) { saveState = true }
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.filledIcon else item.outlineIcon,
                        contentDescription = item.route
                    )
                }
            )
        }
    }
}

/* -------- TOP BAR -------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CritiChordTopBar() {
    val isDark = isSystemInDarkTheme()
    val logoRes = if (isDark) R.drawable.logo else R.drawable.logo_negro

    CenterAlignedTopAppBar(
        title = {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Logo CritiChord",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "CritiChord",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    )
}
