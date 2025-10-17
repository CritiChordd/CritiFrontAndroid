package com.example.proyecto_movil.ui.Screens.Settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.proyecto_movil.R
import com.example.proyecto_movil.ui.theme.Proyecto_movilTheme
import com.example.proyecto_movil.ui.utils.ClickableSectionTitle
import com.example.proyecto_movil.ui.utils.ScreenBackground

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshIfNeeded() }

    LaunchedEffect(state.navigateBack) {
        if (state.navigateBack) {
            onBackClick()
            viewModel.consumeNavigation()
        }
    }

    LaunchedEffect(state.navigateToProfile) {
        val target = state.navigateToProfile
        if (!target.isNullOrBlank()) {
            onNavigateToProfile(target)
            viewModel.consumeNavigateProfile()
        }
    }

    LaunchedEffect(state.navigateToLogin) {
        if (state.navigateToLogin) {
            onLoggedOut()
            viewModel.consumeNavigateLogin()
        }
    }

    SettingsScreenContent(
        state = state,
        modifier = modifier,
        onBackClick = viewModel::onBackClicked,
        onLogoutClick = viewModel::onLogoutClicked,
        onViewProfile = viewModel::onViewProfileClicked,
        onToggleDarkMode = viewModel::togglePreferDarkMode,
        onLanguageClick = viewModel::onLanguageClicked,
        onToggleHideActivity = viewModel::toggleHideActivity,
        onToggleShowRecentAlbums = viewModel::toggleShowRecentAlbums,
        onTogglePublicPlaylists = viewModel::togglePublicPlaylists,
        onToggleShowPlaylistsOnProfile = viewModel::toggleShowPlaylistsOnProfile,
        onToggleShowFollowersAndFollowing = viewModel::toggleShowFollowersAndFollowing,
        onToggleAllowExplicitContent = viewModel::toggleAllowExplicitContent,
        onToggleShowUnavailableInCountry = viewModel::toggleShowUnavailableInCountry,
        onTogglePushNotifications = viewModel::togglePushNotifications,
        onToggleEmailNotifications = viewModel::toggleEmailNotifications,
        onDeactivateAccount = viewModel::onDeactivateAccountClicked
    )
}

@Composable
private fun SettingsScreenContent(
    state: SettingsState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onViewProfile: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onLanguageClick: () -> Unit,
    onToggleHideActivity: () -> Unit,
    onToggleShowRecentAlbums: () -> Unit,
    onTogglePublicPlaylists: () -> Unit,
    onToggleShowPlaylistsOnProfile: () -> Unit,
    onToggleShowFollowersAndFollowing: () -> Unit,
    onToggleAllowExplicitContent: () -> Unit,
    onToggleShowUnavailableInCountry: () -> Unit,
    onTogglePushNotifications: () -> Unit,
    onToggleEmailNotifications: () -> Unit,
    onDeactivateAccount: () -> Unit
) {
    val isDarkSystem = isSystemInDarkTheme()
    val useDarkBackground = state.preferDarkMode || isDarkSystem
    val backgroundRes = if (useDarkBackground) R.drawable.fondocriti else R.drawable.fondocriti_light

    ScreenBackground(backgroundRes = backgroundRes) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            item {
                Header(
                    onBackClick = onBackClick,
                    onLogoutClick = onLogoutClick
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            item {
                ProfileSection(
                    displayName = state.displayName.ifBlank { state.username.ifBlank { "Sin nombre" } },
                    username = state.username,
                    avatarUrl = state.avatarUrl,
                    isLoading = state.isLoadingProfile,
                    onViewProfile = onViewProfile
                )
                Spacer(modifier = Modifier.height(32.dp))
                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            item {
                SettingsSection(
                    preferDarkMode = state.preferDarkMode,
                    onToggleDarkMode = onToggleDarkMode,
                    onLanguageClick = onLanguageClick
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                PrivacySection(
                    hideActivity = state.hideActivity,
                    showRecentAlbums = state.showRecentAlbums,
                    publicPlaylists = state.publicPlaylists,
                    showPlaylistsOnProfile = state.showPlaylistsOnProfile,
                    showFollowersAndFollowing = state.showFollowersAndFollowing,
                    onToggleHideActivity = onToggleHideActivity,
                    onToggleShowRecentAlbums = onToggleShowRecentAlbums,
                    onTogglePublicPlaylists = onTogglePublicPlaylists,
                    onToggleShowPlaylistsOnProfile = onToggleShowPlaylistsOnProfile,
                    onToggleShowFollowersAndFollowing = onToggleShowFollowersAndFollowing
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                ContentSection(
                    allowExplicitContent = state.allowExplicitContent,
                    showUnavailableInCountry = state.showUnavailableInCountry,
                    onToggleAllowExplicitContent = onToggleAllowExplicitContent,
                    onToggleShowUnavailableInCountry = onToggleShowUnavailableInCountry
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                NotificationsSection(
                    pushNotifications = state.pushNotifications,
                    emailNotifications = state.emailNotifications,
                    onTogglePush = onTogglePushNotifications,
                    onToggleEmail = onToggleEmailNotifications
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
            item {
                DeactivateAccountButton(onClick = onDeactivateAccount)
            }
        }
    }
}

@Composable
private fun Header(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Volver",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(30.dp)
                .clickable { onBackClick() }
        )
        Text(
            text = "Configuración",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Icon(
            imageVector = Icons.Filled.ExitToApp,
            contentDescription = "Cerrar sesión",
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .size(30.dp)
                .clickable { onLogoutClick() }
        )
    }
}

@Composable
private fun ProfileSection(
    displayName: String,
    username: String,
    avatarUrl: String,
    isLoading: Boolean,
    onViewProfile: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .padding(12.dp)
            )
        } else {
            AsyncImage(
                model = avatarUrl.takeIf { it.isNotBlank() } ?: R.drawable.usuario,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = displayName,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (username.isNotBlank()) {
                Text(
                    text = "@${username}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            Text(
                text = "Ver perfil",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onViewProfile() }
            )
        }
    }
}

@Composable
private fun SettingsSection(
    preferDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onLanguageClick: () -> Unit
) {
    ClickableSectionTitle(title = "Preferencias")
    Spacer(modifier = Modifier.height(16.dp))
    SettingItem(
        text = "Idioma de la aplicación",
        hasSwitch = false,
        onClick = onLanguageClick
    )
    Spacer(modifier = Modifier.height(8.dp))
    SettingItem(
        text = "Modo oscuro",
        hasSwitch = true,
        checked = preferDarkMode,
        onCheckedChange = { onToggleDarkMode() }
    )
}

@Composable
private fun PrivacySection(
    hideActivity: Boolean,
    showRecentAlbums: Boolean,
    publicPlaylists: Boolean,
    showPlaylistsOnProfile: Boolean,
    showFollowersAndFollowing: Boolean,
    onToggleHideActivity: () -> Unit,
    onToggleShowRecentAlbums: () -> Unit,
    onTogglePublicPlaylists: () -> Unit,
    onToggleShowPlaylistsOnProfile: () -> Unit,
    onToggleShowFollowersAndFollowing: () -> Unit
) {
    ClickableSectionTitle(title = "Privacidad y social")
    Spacer(modifier = Modifier.height(16.dp))
    SettingItem("Ocultar tu actividad", true, hideActivity) { onToggleHideActivity() }
    Spacer(modifier = Modifier.height(8.dp))
    SettingItem("Álbumes escuchados recientemente", true, showRecentAlbums) { onToggleShowRecentAlbums() }
    Spacer(modifier = Modifier.height(8.dp))
    SettingItem("Playlists públicas", true, publicPlaylists) { onTogglePublicPlaylists() }
    Spacer(modifier = Modifier.height(8.dp))
    SettingItem("Playlists en tu perfil", true, showPlaylistsOnProfile) { onToggleShowPlaylistsOnProfile() }
    Spacer(modifier = Modifier.height(8.dp))
    SettingItem("Mostrar seguidores y seguidos", true, showFollowersAndFollowing) { onToggleShowFollowersAndFollowing() }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Usuarios bloqueados",
        color = MaterialTheme.colorScheme.error,
        fontSize = 16.sp,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier.clickable { }
    )
}

@Composable
private fun ContentSection(
    allowExplicitContent: Boolean,
    showUnavailableInCountry: Boolean,
    onToggleAllowExplicitContent: () -> Unit,
    onToggleShowUnavailableInCountry: () -> Unit
) {
    ClickableSectionTitle(title = "Contenido y visualización")
    Spacer(modifier = Modifier.height(16.dp))
    SettingItem("Permitir contenido explícito", true, allowExplicitContent) { onToggleAllowExplicitContent() }
    Spacer(modifier = Modifier.height(8.dp))
    SettingItem("Mostrar contenido no disponible en mi país", true, showUnavailableInCountry) { onToggleShowUnavailableInCountry() }
}

@Composable
private fun NotificationsSection(
    pushNotifications: Boolean,
    emailNotifications: Boolean,
    onTogglePush: () -> Unit,
    onToggleEmail: () -> Unit
) {
    ClickableSectionTitle(title = "Notificaciones")
    Spacer(modifier = Modifier.height(16.dp))
    SettingItem("Notificaciones push", true, pushNotifications) { onTogglePush() }
    Spacer(modifier = Modifier.height(8.dp))
    SettingItem("Notificaciones por correo", true, emailNotifications) { onToggleEmail() }
}

@Composable
private fun SettingItem(
    text: String,
    hasSwitch: Boolean,
    checked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!hasSwitch && onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp
        )
        if (hasSwitch) {
            Switch(
                checked = checked,
                onCheckedChange = { onCheckedChange?.invoke(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
private fun DeactivateAccountButton(onClick: () -> Unit) {
    Text(
        text = "Desactivar mi cuenta",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .clickable { onClick() }
    )
}

@Preview(showBackground = true, name = "Settings Light", showSystemUi = true)
@Composable
fun SettingsScreenLightPreview() {
    val previewState = SettingsState(
        displayName = "Alex Critic",
        username = "alex",
        avatarUrl = "",
        preferDarkMode = false,
        hideActivity = false,
        showRecentAlbums = true,
        publicPlaylists = true,
        showPlaylistsOnProfile = true,
        showFollowersAndFollowing = true,
        allowExplicitContent = true,
        showUnavailableInCountry = false,
        pushNotifications = true,
        emailNotifications = false
    )
    Proyecto_movilTheme(useDarkTheme = false) {
        SettingsScreenContent(
            state = previewState,
            onBackClick = {},
            onLogoutClick = {},
            onViewProfile = {},
            onToggleDarkMode = {},
            onLanguageClick = {},
            onToggleHideActivity = {},
            onToggleShowRecentAlbums = {},
            onTogglePublicPlaylists = {},
            onToggleShowPlaylistsOnProfile = {},
            onToggleShowFollowersAndFollowing = {},
            onToggleAllowExplicitContent = {},
            onToggleShowUnavailableInCountry = {},
            onTogglePushNotifications = {},
            onToggleEmailNotifications = {},
            onDeactivateAccount = {}
        )
    }
}

@Preview(showBackground = true, name = "Settings Dark", showSystemUi = true)
@Composable
fun SettingsScreenDarkPreview() {
    val previewState = SettingsState(
        displayName = "Alex Critic",
        username = "alex",
        avatarUrl = "",
        preferDarkMode = true,
        hideActivity = true,
        showRecentAlbums = true,
        publicPlaylists = false,
        showPlaylistsOnProfile = true,
        showFollowersAndFollowing = true,
        allowExplicitContent = true,
        showUnavailableInCountry = false,
        pushNotifications = true,
        emailNotifications = true
    )
    Proyecto_movilTheme(useDarkTheme = true) {
        SettingsScreenContent(
            state = previewState,
            onBackClick = {},
            onLogoutClick = {},
            onViewProfile = {},
            onToggleDarkMode = {},
            onLanguageClick = {},
            onToggleHideActivity = {},
            onToggleShowRecentAlbums = {},
            onTogglePublicPlaylists = {},
            onToggleShowPlaylistsOnProfile = {},
            onToggleShowFollowersAndFollowing = {},
            onToggleAllowExplicitContent = {},
            onToggleShowUnavailableInCountry = {},
            onTogglePushNotifications = {},
            onToggleEmailNotifications = {},
            onDeactivateAccount = {}
        )
    }
}