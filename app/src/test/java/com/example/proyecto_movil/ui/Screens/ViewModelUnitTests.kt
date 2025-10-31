package com.example.proyecto_movil.ui.Screens

import android.util.Log
import com.example.proyecto_movil.MainDispatcherRule
import com.example.proyecto_movil.data.*
import com.example.proyecto_movil.data.repository.*
import io.mockk.mockkStatic
import com.example.proyecto_movil.ui.Screens.Content.ContentViewModel
import com.example.proyecto_movil.ui.Screens.Home.HomeViewModel
import com.example.proyecto_movil.ui.Screens.Login.LoginViewModel
import com.example.proyecto_movil.ui.Screens.Notifications.NotificationsViewModel
import com.example.proyecto_movil.ui.Screens.Register.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import com.google.common.truth.Truth.assertThat

@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelUnitTests {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    init {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.d(any(), any()) } returns 0
    }

    // 1) WelcomeViewModel - simple state updates
    @Test
    fun welcome_onStart_setsNavigateNext_and_consumeNavigation() {
        val vm = com.example.proyecto_movil.ui.Screens.Welcome.WelcomeViewModel()

        // initial
        assertThat(vm.uiState.value.navigateNext).isFalse()

        vm.onStartClicked()
        assertThat(vm.uiState.value.navigateNext).isTrue()

        vm.consumeNavigation()
        assertThat(vm.uiState.value.navigateNext).isFalse()
    }

    // 2) LoginViewModel - success path
    @Test
    fun login_onLoginSuccess_navigatesAfterLogin() = runTest {
        val authRepo = mockk<AuthRepository>()
        coEvery { authRepo.signIn("e@t.com", "password") } returns Result.success(Unit)

        val vm = LoginViewModel(authRepo)
        vm.updateEmail("e@t.com")
        vm.updatePassword("password")

        vm.onLoginClicked()
        advanceUntilIdle()

        assertThat(vm.uiState.value.navigateAfterLogin).isTrue()
    }

    // 3) LoginViewModel - validation errors
    @Test
    fun login_validation_emptyFields_showsError() {
        val authRepo = mockk<AuthRepository>(relaxed = true)
        val vm = LoginViewModel(authRepo)

        // default state has empty email/password
        vm.onLoginClicked()

        assertThat(vm.uiState.value.showMessage).isTrue()
        assertThat(vm.uiState.value.errorMessage).isEqualTo("Completa email y contraseña")
    }

    // 4) RegisterViewModel - validations that don't call Firebase
    @Test
    fun register_missingAcceptedTerms_showsMessage() {
        val auth = mockk<FirebaseAuth>(relaxed = true)
        val firestore = mockk<FirebaseFirestore>(relaxed = true)
        val vm = RegisterViewModel(auth, firestore)

        // default acceptedTerms == false
        vm.onRegisterClicked()

        assertThat(vm.uiState.value.showMessage).isTrue()
        assertThat(vm.uiState.value.errorMessage).isEqualTo("Debes aceptar los términos y condiciones")
    }

    // 5) HomeViewModel - load albums and reviews on init
    @Test
    fun home_loadsAlbumsAndReviews_updatesState() = runTest {
        val albumRepo = mockk<AlbumRepository>()
        val reviewRepo = mockk<ReviewRepository>()
        val userRepo = mockk<UserRepository>()

        val artist = ArtistInfo(id = 1, name = "Artist 1", profileImageUrl = "")
        val album = AlbumInfo(id = 10, title = "A", year = "2024", coverUrl = "", artist = artist)
        val review = ReviewInfo(id = "r1", albumId = 10)

        coEvery { albumRepo.getAllAlbums() } returns Result.success(listOf(album))
        coEvery { reviewRepo.getAllReviews() } returns Result.success(listOf(review))
        coEvery { userRepo.searchUsersByName(any(), any()) } returns Result.success(emptyList())

        val vm = HomeViewModel(albumRepo, reviewRepo, userRepo)

        advanceUntilIdle()

        assertThat(vm.uiState.value.albumList).hasSize(1)
        assertThat(vm.uiState.value.reviewList).hasSize(1)
    }

    // 6) HomeViewModel - search behavior (short query clears, longer queries combine results)
    @Test
    fun home_search_query_behaviour_combinesAlbumAndUserResults() = runTest {
        val albumRepo = mockk<AlbumRepository>()
        val reviewRepo = mockk<ReviewRepository>()
        val userRepo = mockk<UserRepository>()

        val artist = ArtistInfo(id = 2, name = "Artist 2", profileImageUrl = "")
        val album = AlbumInfo(id = 11, title = "Best Album", year = "2024", coverUrl = "", artist = artist)
        val user = UserInfo(id = "u1", name = "User One", username = "user1", profileImageUrl = "")

        coEvery { albumRepo.getAllAlbums() } returns Result.success(listOf(album))
        coEvery { reviewRepo.getAllReviews() } returns Result.success(emptyList())
        coEvery { userRepo.searchUsersByName("user", 8) } returns Result.success(listOf(user))

        val vm = HomeViewModel(albumRepo, reviewRepo, userRepo)
        advanceUntilIdle()

        // short query -> should clear
        vm.onSearchQueryChanged("a")
        advanceUntilIdle()
        assertThat(vm.uiState.value.searchResults).isEmpty()

        // longer query triggers search
        vm.onSearchQueryChanged("user")
        advanceUntilIdle()
        assertThat(vm.uiState.value.searchResults).isNotEmpty()
    }

    // 7) ContentViewModel - setInitial with artistId loads headerTitle from artist repository
    @Test
    fun content_setInitial_withArtist_loadsArtistNameAndAlbums() = runTest {
        val albumRepo = mockk<AlbumRepository>()
        val artistRepo = mockk<ArtistRepository>()

        val artist = ArtistInfo(id = 3, name = "The Band", profileImageUrl = "")
        val album = AlbumInfo(id = 21, title = "AlbumX", year = "2025", coverUrl = "", artist = artist)

        coEvery { albumRepo.getAllAlbums() } returns Result.success(listOf(album))
        coEvery { artistRepo.getArtistById("3") } returns Result.success(artist)

        val vm = ContentViewModel(albumRepo, artistRepo)
        vm.setInitial(artistId = 3, isOwner = true)
        advanceUntilIdle()

        assertThat(vm.uiState.value.headerTitle).isEqualTo("The Band")
        assertThat(vm.uiState.value.albums).hasSize(1)
    }

    // 8) NotificationsViewModel - when auth has no user -> empty list; with user -> collect flow
    @Test
    fun notifications_init_behaviour_withAndWithoutUser() = runTest {
        val repo = mockk<NotificationsRepository>()
        val authNoUser = mockk<FirebaseAuth>()
        every { authNoUser.currentUser } returns null

        val vmNoUser = NotificationsViewModel(repo, authNoUser)
        // no user -> items empty but isLoading false
        assertThat(vmNoUser.uiState.value.items).isEmpty()
        assertThat(vmNoUser.uiState.value.isLoading).isFalse()

        // with user -> repo provides flow
        val firebaseUser = mockk<FirebaseUser>()
        every { firebaseUser.uid } returns "uid-notif"
        val authWithUser = mockk<FirebaseAuth>()
        every { authWithUser.currentUser } returns firebaseUser

        val notif = NotificationInfo(id = "n1", type = "follow", message = "hi", createdAt = 1)
        every { repo.listenUserNotifications("uid-notif") } returns flowOf(listOf(notif))

        val vmWithUser = NotificationsViewModel(repo, authWithUser)
        advanceUntilIdle()

        assertThat(vmWithUser.uiState.value.items).hasSize(1)
        assertThat(vmWithUser.uiState.value.items[0].id).isEqualTo("n1")
    }
}
