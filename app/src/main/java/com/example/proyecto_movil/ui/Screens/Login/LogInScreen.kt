package com.example.proyecto_movil.ui.Screens.Login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_movil.R

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onBack: () -> Unit = {},
    onLogin: (email: String, password: String, remember: Boolean) -> Unit = { _, _, _ -> },
    onForgotPassword: () -> Unit = {},
    onRegister: () -> Unit = {},
    modifier: Modifier = Modifier.testTag("loginScreen")
) {
    val state by viewModel.uiState.collectAsState()

    // Navegaciones one-shot
    LaunchedEffect(
        state.navigateBack,
        state.navigateAfterLogin,
        state.navigateToForgot,
        state.navigateToRegister
    ) {
        if (state.navigateBack) {
            onBack()
            viewModel.consumeBack()
        }
        if (state.navigateAfterLogin) {
            onLogin(state.email, state.password, state.remember)
            viewModel.consumeAfterLogin()
        }
        if (state.navigateToForgot) {
            onForgotPassword()
            viewModel.consumeForgot()
        }
        if (state.navigateToRegister) {
            onRegister()
            viewModel.consumeRegister()
        }
    }

    val isDark = isSystemInDarkTheme()
    val backgroundRes = if (isDark) R.drawable.fondocriti else R.drawable.fondocriti_light
    val logoRes = if (isDark) R.drawable.logo else R.drawable.logo_negro

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.showMessage, state.errorMessage) {
        if (state.showMessage && state.errorMessage.isNotBlank()) {
            snackbarHostState.showSnackbar(state.errorMessage)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(modifier)
        ) {
            // Fondo que cubre toda la pantalla
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            IconButton(
                onClick = { viewModel.onBackClicked() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(padding)
                    .padding(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Atrás")
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp)
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Logo",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Accede a tu cuenta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                InputField(
                    value = state.email,
                    onValueChange = viewModel::updateEmail,
                    label = stringResource(R.string.email),
                    isPassword = false,
                    showPassword = false,
                    onTogglePassword = {},
                    modifier = Modifier.testTag("correo-login")
                )

                Spacer(Modifier.height(12.dp))

                InputField(
                    value = state.password,
                    onValueChange = viewModel::updatePassword,
                    label = stringResource(R.string.contra),
                    isPassword = true,
                    showPassword = state.showPassword,
                    onTogglePassword = { viewModel.toggleShowPassword() },
                    modifier = Modifier.testTag("contra-login")
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = state.remember,
                        onCheckedChange = { viewModel.toggleRemember() }
                    )
                    Text(stringResource(R.string.recordarme), fontSize = 14.sp)
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.onLoginClicked() },
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("boton_login")
                ) {
                    Text("Ingresar", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = stringResource(R.string.olvidaste_tu_contrase_a),
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { viewModel.onForgotClicked() }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "¿No tienes una cuenta? Regístrate",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { viewModel.onRegisterClicked() }
                        .testTag("registrate-boton")
                )
            }
        }
    }
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean,
    showPassword: Boolean,
    onTogglePassword: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (isPassword && !showPassword) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Ocultar" else "Mostrar"
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(),
        modifier = modifier.fillMaxWidth()
    )
}
