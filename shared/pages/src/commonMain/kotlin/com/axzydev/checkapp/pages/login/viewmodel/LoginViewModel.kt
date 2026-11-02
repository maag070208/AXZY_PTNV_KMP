package com.axzydev.checkapp.pages.login.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.authlogin.model.LoginUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val login: LoginUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.Username -> _state.update { it.copy(username = action.value, error = null) }
            is LoginAction.Password -> _state.update { it.copy(password = action.value, error = null) }
            // No se limpia el error al marcar la casilla: si el mensaje es el del
            // límite de intentos, borrarlo al tocar "Recordarme" confundiría.
            is LoginAction.RememberMe -> _state.update { it.copy(rememberMe = action.value) }
            LoginAction.Submit -> submit()
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            when (val result = login(current.username, current.password, current.rememberMe)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(loading = false) }
                    _effects.send(LoginEffect.LoggedIn(result.data.role))
                }

                is ApiResult.Failure -> _state.update {
                    it.copy(loading = false, error = loginErrorMessage(result))
                }
            }
        }
    }

    /**
     * El login tiene límite de intentos por IP (10 cada 15 minutos). Un `429` no
     * significa "credenciales incorrectas": decírselo al guardia lo llevaría a
     * reescribir la contraseña una y otra vez sin entender qué pasa.
     */
    private fun loginErrorMessage(failure: ApiResult.Failure): String =
        if (failure.statusCode == TOO_MANY_REQUESTS) {
            "Demasiados intentos. Espera unos minutos e intenta de nuevo."
        } else {
            failure.firstMessage
        }
}

private const val TOO_MANY_REQUESTS = 429
