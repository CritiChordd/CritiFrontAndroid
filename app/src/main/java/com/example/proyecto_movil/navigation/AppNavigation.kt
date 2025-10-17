package com.example.proyecto_movil.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_movil.data.AlbumInfo
import com.example.proyecto_movil.ui.Screens.Welcome.WelcomeScreen
import com.example.proyecto_movil.ui.Screens.Welcome.WelcomeViewModel
import com.example.proyecto_movil.ui.Screens.Login.LoginScreen
import com.example.proyecto_movil.ui.Screens.Login.LoginViewModel
import com.example.proyecto_movil.ui.Screens.Register.RegisterScreen
import com.example.proyecto_movil.ui.Screens.Register.RegisterViewModel
import com.example.proyecto_movil.uiViews.homePage.HomeScreen
import com.example.proyecto_movil.ui.Screens.Home.HomeViewModel
import com.example.proyecto_movil.ui.Screens.UserProfile.UserProfileScreen
import com.example.proyecto_movil.ui.Screens.UserProfile.UserProfileViewModel
import com.example.proyecto_movil.ui.Screens.Settings.SettingsScreen
import com.example.proyecto_movil.ui.Screens.Settings.SettingsViewModel
import com.example.proyecto_movil.ui.Screens.Content.ContentScreen
import com.example.proyecto_movil.ui.Screens.Content.ContentViewModel
import com.example.proyecto_movil.ui.Screens.AddReview.AddReviewScreen
import com.example.proyecto_movil.ui.Screens.AddReview.AddReviewViewModel
import com.example.proyecto_movil.ui.Screens.EditProfile.EditarPerfilScreen
import com.example.proyecto_movil.ui.Screens.EditProfile.EditProfileViewModel
import com.example.proyecto_movil.ui.Screens.AlbumReviews.AlbumReviewScreen
import com.example.proyecto_movil.ui.Screens.AlbumReviews.AlbumReviewViewModel
import com.example.proyecto_movil.ui.Screens.ReviewDetail.ReviewDetailViewModel
import com.example.proyecto_movil.ui.Screens.Notifications.NotificationsScreen
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import com.example.proyecto_movil.ui.utils.ReviewDetailScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.proyecto_movil.ui.Screens.FollowList.FollowListViewModel
import com.example.proyecto_movil.ui.Screens.FollowList.FollowListScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.proyecto_movil.ui.Screens.FollowingFeed.FollowingFeedScreen
import com.example.proyecto_movil.ui.Screens.FollowingFeed.FollowingFeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Welcome.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        /* ------------------ WELCOME ------------------ */
        composable(Screen.Welcome.route) {
            val vm: WelcomeViewModel = hiltViewModel()
            WelcomeScreen(
                viewModel = vm,
                onStartClick = { navController.navigate(Screen.Login.route) }
            )
        }

        /* ------------------ LOGIN ------------------ */
        composable(Screen.Login.route) {
            val vm: LoginViewModel = hiltViewModel()
            val state = vm.uiState.collectAsState().value

            if (state.navigateAfterLogin) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    vm.consumeAfterLogin()
                }
            }

            LoginScreen(
                viewModel = vm,
                onBack = { navController.navigateUp() },
                onRegister = { navController.navigate(Screen.Register.route) },
                onForgotPassword = { /* TODO */ }
            )
        }

        /* ------------------ REGISTER ------------------ */
        composable(Screen.Register.route) {
            val vm: RegisterViewModel = hiltViewModel()
            val state = vm.uiState.collectAsState().value

            if (state.navigateAfterRegister) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Welcome.route) { inclusive = true }
                        launchSingleTop = true
                    }
                    vm.consumeNavigation()
                }
            }

            RegisterScreen(
                viewModel = vm,
                onBack = { navController.navigateUp() },
                onLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        /* ------------------ HOME ------------------ */
        composable(Screen.Home.route) {
            val vm: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = vm,
                onAlbumClick = { album -> navController.navigate(Screen.Album.createRoute(album.id)) },
                onReviewProfileImageClicked = { uid -> navController.navigate(Screen.Profile.createRoute(uid)) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                onFollowingFeedClick = { navController.navigate(Screen.FollowingFeed.route) } // ← NUEVO
            )
        }

        /* ------------------ PROFILE ------------------ */
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid").orEmpty()
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            val vm: UserProfileViewModel = hiltViewModel()

            LaunchedEffect(uid) { if (uid.isNotBlank()) vm.setInitialData(uid) }
            val state = vm.uiState.collectAsState().value

            LaunchedEffect(state.navigateBack) {
                if (state.navigateBack) {
                    navController.navigateUp()
                    vm.consumeBack()
                }
            }
            LaunchedEffect(state.navigateToSettings) {
                if (state.navigateToSettings) {
                    navController.navigate(Screen.Settings.route)
                    vm.consumeSettings()
                }
            }
            LaunchedEffect(state.navigateToEditProfile) {
                if (state.navigateToEditProfile) {
                    navController.navigate(Screen.EditProfile.route)
                    vm.consumeEdit()
                }
            }
            LaunchedEffect(state.openAlbumId) {
                val albumId = state.openAlbumId
                if (albumId != null) {
                    val album = state.favoriteAlbums.firstOrNull { it.id == albumId }
                    if (album != null) {
                        navController.navigate(Screen.Album.createRoute(album.id))
                    }
                    vm.consumeOpenAlbum()
                }
            }
            LaunchedEffect(state.openReviewId) {
                val reviewId = state.openReviewId
                if (reviewId != null) {
                    navController.navigate(Screen.ReviewDetail.createRoute(reviewId))
                    vm.consumeOpenReview()
                }
            }

            when {
                state.isLoading -> SimpleLoading()
                state.user != null -> {
                    UserProfileScreen(
                        state = state,
                        user = state.user,
                        isOwnProfile = uid.isNotBlank() && uid == currentUid,
                        onBackClick = vm::onBackClicked,
                        onSettingsClick = vm::onSettingsClicked,
                        onEditProfile = vm::onEditProfileClicked,
                        onAlbumSelected = vm::onAlbumClicked,
                        onReviewSelected = vm::onReviewClicked,
                        onReviewProfileImageClicked = { targetUid ->
                            if (targetUid.isNotBlank() && targetUid != uid) {
                                navController.navigate(Screen.Profile.createRoute(targetUid))
                            }
                        },
                        onToggleFollow = vm::onFollowClicked,
                        // 🆕 nuevos callbacks de navegación
                        onOpenFollowers = { ownerUid ->
                            navController.navigate(Screen.Followers.createRoute(ownerUid))
                        },
                        onOpenFollowing = { ownerUid ->
                            navController.navigate(Screen.Following.createRoute(ownerUid))
                        }
                    )
                }
                else -> SimpleError(state.errorMessage ?: "Usuario no encontrado")
            }
        }

        /* FOLLOWERS */
        composable(
            route = Screen.Followers.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: return@composable
            val vm: FollowListViewModel = hiltViewModel(backStackEntry)

            // 👇 Asegura pasar los parámetros antes de recolectar el estado
            LaunchedEffect(uid) {
                vm.setParams(uid = uid, mode = "followers")
            }

            val ui = vm.ui.collectAsState().value
            FollowListScreen(
                state = ui,
                onBack = { navController.navigateUp() },
                onUserClick = { targetUid ->
                    navController.navigate(Screen.Profile.createRoute(targetUid))
                }
            )
        }

        /* FOLLOWING */
        composable(
            route = Screen.Following.route,
            arguments = listOf(navArgument("uid") { type = NavType.StringType })
        ) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: return@composable
            val vm: FollowListViewModel = hiltViewModel(backStackEntry)

            LaunchedEffect(uid) {
                vm.setParams(uid = uid, mode = "following")
            }

            val ui = vm.ui.collectAsState().value
            FollowListScreen(
                state = ui,
                onBack = { navController.navigateUp() },
                onUserClick = { targetUid ->
                    navController.navigate(Screen.Profile.createRoute(targetUid))
                }
            )
        }
        /* ------------------ FOLLOWING FEED ------------------ */
        composable(Screen.FollowingFeed.route) {
            val vm: FollowingFeedViewModel = hiltViewModel()
            val state = vm.uiState.collectAsState()

            LaunchedEffect(Unit) { vm.start() }

            FollowingFeedScreen(
                state = state.value,
                onBack = { navController.navigateUp() },
                onUserClick = { uid -> navController.navigate(Screen.Profile.createRoute(uid)) },
                onOpenReview = { rid -> navController.navigate(Screen.ReviewDetail.createRoute(rid)) }
            )
        }

        /* ------------------ ALBUM ------------------ */
        composable(
            route = Screen.Album.route,
            arguments = listOf(navArgument("albumId") { type = NavType.IntType })
        ) { backStackEntry ->
            val albumId = backStackEntry.arguments?.getInt("albumId")
            val vm: ContentViewModel = hiltViewModel()
            val albumReviewVm: AlbumReviewViewModel = hiltViewModel()

            LaunchedEffect(albumId) { vm.setInitial(artistId = null, isOwner = false) }
            val state = vm.uiState.collectAsState().value
            val selectedAlbum = albumId?.let { id -> state.albums.find { it.id == id } }

            if (selectedAlbum != null) {
                AlbumReviewScreen(
                    album = selectedAlbum,
                    viewModel = albumReviewVm,
                    onArtistClick = {
                        navController.navigate(Screen.ContentArtist.createRoute(selectedAlbum.artist.id))
                    },
                    onUserClick = { userId ->
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                )
            } else {
                SimpleError("Álbum no encontrado")
            }
        }

        /* ------------------ REVIEW DETAIL ------------------ */
        composable(
            route = Screen.ReviewDetail.route,
            arguments = listOf(navArgument("reviewId") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedId = backStackEntry.arguments?.getString("reviewId") ?: return@composable
            val reviewId = Uri.decode(encodedId)

            val vm: ReviewDetailViewModel = hiltViewModel()
            val state = vm.uiState.collectAsState().value
            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            LaunchedEffect(reviewId, userId) {
                if (userId.isNotBlank()) vm.load(reviewId, userId)
            }

            when {
                state.isLoading -> SimpleLoading()
                state.errorMessage != null -> SimpleError(state.errorMessage)
                state.review != null -> {
                    val author = state.author
                    val album = state.album

                    val username = author?.username?.takeIf { it.isNotBlank() }
                        ?: author?.name
                        ?: "Usuario"

                    val avatarUrl = author?.profileImageUrl ?: ""
                    val albumTitle = album?.title ?: ""
                    val coverUrl = album?.coverUrl ?: ""
                    val artistName = album?.artist?.name ?: ""
                    val albumYear = album?.year ?: ""

                    ReviewDetailScreen(
                        review = state.review,
                        username = username,
                        userProfileUrl = avatarUrl,
                        albumTitle = albumTitle,
                        albumCoverUrl = coverUrl,
                        artistName = artistName,
                        albumYear = albumYear,
                        liked = state.review.liked,
                        likesCount = state.review.likesCount,
                        onToggleLike = { vm.toggleLike() },
                        onBack = { navController.popBackStack() }
                    )
                }
                else -> SimpleLoading()
            }
        }

        /* ------------------ NOTIFICATIONS ------------------ */
        composable(Screen.Notifications.route) {
            val vm: com.example.proyecto_movil.ui.Screens.Notifications.NotificationsViewModel = hiltViewModel()
            val state = vm.uiState.collectAsState().value
            NotificationsScreen(
                onBackClick = { navController.navigateUp() },
                state = state,
                onNotificationUserClick = { userId ->
                    if (userId.isNotBlank()) {
                        navController.navigate(Screen.Profile.createRoute(userId))
                    }
                }
            )
        }

        /* ------------------ CONTENT ARTIST ------------------ */
        composable(
            route = Screen.ContentArtist.route,
            arguments = listOf(navArgument("artistId") { type = NavType.IntType })
        ) { backStackEntry ->
            val artistId = backStackEntry.arguments?.getInt("artistId")
            val vm: ContentViewModel = hiltViewModel()
            LaunchedEffect(artistId) { vm.setInitial(artistId = artistId, isOwner = false) }
            ContentScreen(
                viewModel = vm,
                onBack = { navController.navigateUp() },
                onOpenAlbum = { id -> navController.navigate(Screen.Album.createRoute(id)) },
                onSeeAll = { /* TODO */ }
            )
        }

        /* ------------------ SETTINGS ------------------ */
        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                viewModel = vm,
                onBackClick = { navController.navigateUp() },
                onNavigateToProfile = { userId ->
                    navController.navigate(Screen.Profile.createRoute(userId))
                },
                onLoggedOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        /* ------------------ EDIT PROFILE ------------------ */
        composable(Screen.EditProfile.route) {
            val vm: EditProfileViewModel = hiltViewModel()
            val currentUid = FirebaseAuth.getInstance().currentUser?.uid
            if (currentUid == null) {
                SimpleError("Debes iniciar sesión")
            } else {
                EditarPerfilScreen(
                    viewModel = vm,
                    userId = currentUid,
                    onBack = { navController.navigateUp() },
                    onSaved = {
                        navController.navigate(Screen.Profile.createRoute(currentUid)) {
                            popUpTo(Screen.Profile.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        /* ------------------ ADD REVIEW ------------------ */
        composable(Screen.AddReview.route) {
            val vm: AddReviewViewModel = hiltViewModel()
            AddReviewScreen(
                viewModel = vm,
                onCancel = { navController.navigateUp() },
                onPublished = { _, _, _, _ -> navController.navigateUp() }
            )
        }
    }
}

/* ------------------ Utils ------------------ */
@Composable
private fun SimpleError(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun SimpleLoading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AppNavHostPreview() {
    val navController = rememberNavController()
    Proyecto_movilTheme {
        Surface { AppNavHost(navController = navController) }
    }
}
