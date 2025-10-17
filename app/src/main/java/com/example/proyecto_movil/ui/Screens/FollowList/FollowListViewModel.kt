package com.example.proyecto_movil.ui.Screens.FollowList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_movil.data.UserInfo
import com.example.proyecto_movil.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class FollowListViewModel @Inject constructor(
    private val repo: UserRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(FollowListState())
    val ui: StateFlow<FollowListState> = _ui.asStateFlow()

    private var currentJob: Job? = null
    private var currentUid: String = ""
    private var currentMode: String = "followers" // "followers" | "following"

    /**
     * Parámetros explícitos desde la navegación.
     * ¡No hay trabajo en init! Evita que arranque en "followers" por defecto.
     */
    fun setParams(uid: String, mode: String) {
        if (uid == currentUid && mode == currentMode) return
        currentUid = uid
        currentMode = mode

        _ui.update {
            it.copy(
                isLoading = true,
                error = null,
                title = if (mode == "following") "Seguidos" else "Seguidores",
                users = emptyList()
            )
        }

        // Cancelar cualquier escucha anterior antes de empezar la nueva
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val flow = if (mode == "following") {
                repo.listenFollowing(uid)
            } else {
                repo.listenFollowers(uid)
            }

            flow.collect { list ->
                _ui.update { s ->
                    s.copy(isLoading = false, users = list, error = null)
                }
            }
        }
    }
}
