package org.kikepb.squadfy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kikepb.core.domain.auth.repository.SessionStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val sessionStorage: SessionStorage
) : ViewModel() {

    private val _state = MutableStateFlow(MainState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val authInfo = sessionStorage.observeAuthInfo().firstOrNull()

            _state.update { it.copy(isCheckingAuth = false, isLoggedIn = authInfo != null) }
        }
    }
}

data class MainState(
    val isLoggedIn: Boolean = false,
    val isCheckingAuth: Boolean = true
)