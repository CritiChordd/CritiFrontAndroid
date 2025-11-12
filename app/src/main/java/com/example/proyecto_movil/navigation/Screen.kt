package com.example.proyecto_movil.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    object Welcome : Screen("welcome")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Chat : Screen("chat")
    object ChatDetail : Screen("chat/detail/{conversationId}?partnerId={partnerId}&username={username}&displayName={displayName}&imageUrl={imageUrl}") {
        fun createRoute(
            conversationId: String,
            partnerId: String,
            username: String?,
            displayName: String?,
            imageUrl: String?
        ): String {
            val encodedPartnerId = Uri.encode(partnerId)
            val encodedUsername = Uri.encode(username.orEmpty())
            val encodedDisplayName = Uri.encode(displayName.orEmpty())
            val encodedImage = Uri.encode(imageUrl.orEmpty())
            return "chat/detail/$conversationId?partnerId=$encodedPartnerId&username=$encodedUsername&displayName=$encodedDisplayName&imageUrl=$encodedImage"
        }
    }

    // Perfil por UID (String)
    object Profile : Screen("profile/{uid}") {
        fun createRoute(uid: String) = "profile/$uid"
        fun createRoute(uid: Int) = "profile/$uid" // compatibilidad si en algún lado aún llega Int
    }

    object Album : Screen("album/{albumId}") {
        fun createRoute(albumId: Int) = "album/$albumId"
        fun createRoute(albumId: String) = "album/$albumId"
    }

    object ContentArtist : Screen("content_artist/{artistId}") {
        fun createRoute(artistId: Int) = "content_artist/$artistId"
        fun createRoute(artistId: String) = "content_artist/$artistId"
    }

    object ContentUser : Screen("content/user")
    object Settings : Screen("settings")
    object EditProfile : Screen("editProfile")
    object AddReview : Screen("addReview")
    object ReviewDetail : Screen("reviewDetail/{reviewId}") {
        fun createRoute(reviewId: String) = "reviewDetail/${Uri.encode(reviewId)}"
    }
    object Notifications : Screen("notifications")

    object FollowingFeed : Screen("followingFeed")

    object Followers : Screen("followers/{uid}") {
        fun createRoute(uid: String) = "followers/$uid"
    }
    object Following : Screen("following/{uid}") {
        fun createRoute(uid: String) = "following/$uid"
    }

    object Rankings : Screen("rankings")
}
