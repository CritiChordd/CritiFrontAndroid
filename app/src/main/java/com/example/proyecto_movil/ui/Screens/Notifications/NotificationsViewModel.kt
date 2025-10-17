package com.example.proyecto_movil.ui.Screens.Notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.NotificationInfo
import com.example.proyecto_movil.data.repository.NotificationsRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.proyecto_movil.ui.Screens.Notifications.NotificationsState

/*data class NotificationsState(
    val isLoading: Boolean = true,
    val items: List<NotificationInfo> = emptyList(),
)*/

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repo: NotificationsRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsState())
    val uiState: StateFlow<NotificationsState> = _uiState.asStateFlow()

    init {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                repo.listenUserNotifications(uid).collectLatest { list ->
                    _uiState.value = NotificationsState(isLoading = false, items = list)
                }
            }
        } else {
            _uiState.value = NotificationsState(isLoading = false, items = emptyList())
        }
    }
}

