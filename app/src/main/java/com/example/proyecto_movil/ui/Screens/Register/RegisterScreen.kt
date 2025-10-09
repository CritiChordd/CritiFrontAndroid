package com.example.proyecto_movil.ui.Screens.Register

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.proyecto_movil.R
import com.example.proyecto_movil.ui.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun RegisterScreen(
    viewModel: Any,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onRegister: (String, String, String) -> Unit = { _, _, _ -> },
    onLogin: () -> Unit = {}
) {
    // Adaptador para soportar RegisterViewModelLike y RegisterViewModel reales
    val actualViewModel: RegisterViewModelLike = when (viewModel) {
        is RegisterViewModelLike -> viewModel
        is RegisterViewModel -> object : RegisterViewModelLike {
            override val uiState: StateFlow<RegisterState> = viewModel.uiState
            override fun updateNombrePersona(value: String) = viewModel.updateNombrePersona(value)
            override fun updateNombreUsuario(value: String) = viewModel.updateNombreUsuario(value)
            override fun updateEmail(value: String) = viewModel.updateEmail(value)
            override fun updatePassword(value: String) = viewModel.updatePassword(value)
            override fun toggleMostrarPassword() = viewModel.toggleMostrarPassword()
            override fun toggleAcceptedTerms() = viewModel.toggleAcceptedTerms()
            override fun onRegisterClicked() = viewModel.onRegisterClicked()
            override fun onLoginClicked() = viewModel.onLoginClicked()
            override fun onBackClicked() = viewModel.onBackClicked()
            override fun consumeNavigation() = viewModel.consumeNavigation()
            override fun consumeMessage() = viewModel.consumeMessage()
        }
        else -> error("RegisterScreen necesita un RegisterViewModel o RegisterViewModelLike")
    }

    val state by actualViewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light
    val snackbarHostState = remember { SnackbarHostState() }

    // Mensajes
    LaunchedEffect(state.showMessage, state.errorMessage) {
        if (state.showMessage) {
            state.errorMessage?.let { msg ->
                if (msg.isNotBlank()) snackbarHostState.showSnackbar(message = msg)
            }
            actualViewModel.consumeMessage()
        }
    }

    // Navegación disparada por ViewModel (p. ej., tras registrarse)
    LaunchedEffect(state.navigateAfterRegister) {
        if (state.navigateAfterRegister) {
            onRegister(state.nombreUsuario, state.email, state.password)
            actualViewModel.consumeNavigation()
        }
    }
    // Si tu VM emite "navigateToLogin", también lo respetamos
    LaunchedEffect(state.navigateToLogin) {
        if (state.navigateToLogin) {
            onLogin()
            actualViewModel.consumeNavigation()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = stringResource(id = R.string.fondo_degradado),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // ← Volver (directo, sin pasar por VM)
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .zIndex(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
            ) {
                Spacer(Modifier.height(80.dp))
                LogoApp()
                Spacer(Modifier.height(60.dp))
                Registrate(texto = "Regístrate")
                Spacer(Modifier.height(10.dp))


                FormularioRegistro(
                    nombrePersona = state.nombrePersona,
                    nombreUsuario = state.nombreUsuario,
                    email = state.email,
                    password = state.password,
                    mostrarPassword = state.mostrarPassword,
                    onNombrePersonaChange = actualViewModel::updateNombrePersona,
                    onNombreUsuarioChange = actualViewModel::updateNombreUsuario,
                    onEmailChange = actualViewModel::updateEmail,
                    onPasswordChange = actualViewModel::updatePassword,
                    onTogglePassword = actualViewModel::toggleMostrarPassword
                )

                Spacer(Modifier.height(30.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = state.acceptedTerms,
                        onCheckedChange = { actualViewModel.toggleAcceptedTerms() }
                    )
                    Spacer(Modifier.width(8.dp))
                    Terminos(
                        texto = "He leído y acepto los términos y condiciones",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(16.dp))

                AppButton(
                    text = "Registrarse",
                    onClick = { actualViewModel.onRegisterClicked() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(30.dp))

                // → Inicia sesión (directo, sin depender de flags)
                YatienesCuenta(
                    texto = "¿Ya tienes una cuenta? Inicia sesión",
                    onClick = onLogin
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

/* ---------- Formulario que faltaba ---------- */
@Composable
private fun FormularioRegistro(
    nombrePersona: String,
    nombreUsuario: String,
    email: String,
    password: String,
    mostrarPassword: Boolean,
    onNombrePersonaChange: (String) -> Unit,
    onNombreUsuarioChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = nombrePersona,
            onValueChange = onNombrePersonaChange,
            label = { Text("Nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = onNombreUsuarioChange,
            label = { Text("Nombre de usuario") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (mostrarPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (mostrarPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (mostrarPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
    }
}

/* --- Interfaces para Preview / Adaptador --- */
interface RegisterViewModelLike {
    val uiState: StateFlow<RegisterState>
    fun updateNombrePersona(value: String)
    fun updateNombreUsuario(value: String)
    fun updateEmail(value: String)
    fun updatePassword(value: String)
    fun toggleMostrarPassword()
    fun toggleAcceptedTerms()
    fun onRegisterClicked()
    fun onLoginClicked()
    fun onBackClicked()
    fun consumeNavigation()
    fun consumeMessage()
}

/* Fake ViewModel para Preview */
class FakeRegisterViewModel : RegisterViewModelLike {
    private val _uiState = MutableStateFlow(RegisterState())
    override val uiState: StateFlow<RegisterState> = _uiState
    override fun updateNombrePersona(value: String) {}
    override fun updateNombreUsuario(value: String) {}
    override fun updateEmail(value: String) {}
    override fun updatePassword(value: String) {}
    override fun toggleMostrarPassword() {}
    override fun toggleAcceptedTerms() {}
    override fun onRegisterClicked() {}
    override fun onLoginClicked() {}
    override fun onBackClicked() {}
    override fun consumeNavigation() {}
    override fun consumeMessage() {}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(
            viewModel = FakeRegisterViewModel(),
            onBack = {},
            onRegister = { _, _, _ -> },
            onLogin = {}
        )
    }
}
