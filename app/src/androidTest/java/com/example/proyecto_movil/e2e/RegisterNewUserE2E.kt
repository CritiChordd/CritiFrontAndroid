package com.example.proyecto_movil.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        Firebase.auth.useEmulator("10.0.2.2", 9099)
        Firebase.firestore.useEmulator("10.0.2.2", 8080)
    }
    catch (e: Exception) {

    }


    val authRemoteDataSource = AuthRemoteDataSource(Firebase.auth)
    val userRemoteDataSource = UserFirestoreDataSourceImpl(Firebase.firestore)
    authRepository = AuthRepository(authRemoteDataSource)
    userRepository = UserRepository(userRemoteDataSource,authRemoteDataSource)
    runBlocking {
        authRepository.signUp("admin@admin.com", "123456")
    authRepository.signOut()

    }
}

    @Test
    fun navigate_fromStart_toLogin(){
        composeRule.onNodeWithTag("boton_comenzar").performClick()
        composeRule.onNodeWithTag("loginScreen").assertIsDisplayed()

    }
    @Test
    fun registerUser_shortPassword_showMessage(){

        composeRule.onNodeWithTag()
    }

}