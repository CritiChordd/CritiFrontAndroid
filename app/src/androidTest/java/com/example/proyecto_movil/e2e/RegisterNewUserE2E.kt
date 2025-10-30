package com.example.proyecto_movil.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.proyecto_movil.MainActivity
import com.example.proyecto_movil.data.datasource.AuthRemoteDataSource
import com.example.proyecto_movil.data.datasource.impl.firestore.UserFirestoreDataSourceImpl
import com.example.proyecto_movil.data.repository.AuthRepository
import com.example.proyecto_movil.data.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@HiltAndroidTest
class RegisterNewUserE2E {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule (order=1)
    var composeRule = createAndroidComposeRule<MainActivity>()

private lateinit var authRepository: AuthRepository
private lateinit var userRepository: UserRepository

@Before
fun setup() {
    hiltRule.inject()
    try {
     //   Firebase.auth.useEmulator("10.0.2.2", 9099)
       // Firebase.firestore.useEmulator("10.0.2.2", 8080)
    }
    catch (e: Exception) {

    }


   // val authRemoteDataSource = AuthRemoteDataSource(Firebase.auth)
    val userRemoteDataSource = UserFirestoreDataSourceImpl(Firebase.firestore)
   // authRepository = AuthRepository(authRemoteDataSource)
   // userRepository = UserRepository(userRemoteDataSource,authRemoteDataSource)
    //runBlocking {
     //   authRepository.signUp("admin@admin.com", "123456")
 //   authRepository.signOut()

   // }
}

    @Test
    fun navigate_fromStart_toRegisterFail(){
        composeRule.onNodeWithTag("boton_comenzar").performClick()
       // composeRule.onNodeWithTag("loginScreen").assertIsDisplayed()
        composeRule.onNodeWithTag("registrate-boton").performClick()
        composeRule.onNodeWithTag("txtNombre").performTextInput("prueba")
        composeRule.onNodeWithTag("txtNombreUsuario").performTextInput("pruebaUsuario")
        composeRule.onNodeWithTag("txtEmail").performTextInput("pruebaUsuario@gmail.com")
        composeRule.onNodeWithTag("txtPassword").performTextInput("1234")
        composeRule.onNodeWithTag("terminos").performClick()
    composeRule.onNodeWithTag("boton_registrarse-final").performClick()
    composeRule.onNodeWithText("El password debe tener al menos 6 caracteres").assertIsDisplayed()
    }
    @Test
    fun caso1(){
        composeRule.onNodeWithTag("boton_comenzar").performClick()
        // composeRule.onNodeWithTag("loginScreen").assertIsDisplayed()
        composeRule.onNodeWithTag("registrate-boton").performClick()
        composeRule.onNodeWithTag("txtNombre").performTextInput("prueba")
        composeRule.onNodeWithTag("txtNombreUsuario").performTextInput("pruebaUsuario")
        composeRule.onNodeWithTag("txtEmail").performTextInput("pruebaUsuario@gmail.com")
        composeRule.onNodeWithTag("txtPassword").performTextInput("123456")
        composeRule.onNodeWithTag("check_terminos").performClick()
        composeRule.onNodeWithTag("boton_registrarse-final").performClick()
        composeRule.waitUntil(15_000) {
            composeRule.onAllNodesWithTag("loginScreen").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithTag("correo-login").performTextInput("pruebaUsuario@gmail.com")
        composeRule.onNodeWithTag("contra-login").performTextInput("123456")
    }

    @Test
    fun caso2(){
        composeRule.onNodeWithTag("boton_comenzar").performClick()
        composeRule.onNodeWithTag("correo-login").performTextInput("pruebaUsuario@gmail.com")
        composeRule.onNodeWithTag("contra-login").performTextInput("123456")
        composeRule.onNodeWithTag("boton_login").performClick()
        composeRule.waitUntil(25000) {
            composeRule.onAllNodesWithTag("homeScreen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("homeScreen").assertIsDisplayed()
        composeRule.onNodeWithTag("searchSection").performClick()
        composeRule.onNodeWithTag("searchTextField").performTextInput("PachoG")
        composeRule.waitUntil(25_000) {
            composeRule.onAllNodesWithTag("userResult", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("userResult").performClick()
        composeRule.waitUntil(25000) {
            composeRule.onAllNodesWithTag("userProfileScreen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("userProfileScreen").assertIsDisplayed()
        composeRule.onNodeWithTag("followButton").performClick()
        composeRule.onNodeWithTag("backButton").performClick()
        composeRule.waitUntil(25000) {
            composeRule.onAllNodesWithTag("homeScreen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("homeScreen").assertIsDisplayed()
        composeRule.onNodeWithTag("searchSection").performClick()
        composeRule.onNodeWithTag("searchTextField").performTextInput("PachoG")
        composeRule.waitUntil(25_000) {
            composeRule.onAllNodesWithTag("userResult", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("userResult").performClick()
        composeRule.waitUntil(25000) {
            composeRule.onAllNodesWithTag("userProfileScreen").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("userProfileScreen").assertIsDisplayed()
       
        composeRule.onNodeWithTag("backButton").performClick()
        composeRule.onNodeWithTag("Lista-amigos").performClick()
        composeRule.waitUntil(25000) {
            composeRule.onAllNodesWithTag("followingFeed").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("followingFeed").assertIsDisplayed()

    }

}