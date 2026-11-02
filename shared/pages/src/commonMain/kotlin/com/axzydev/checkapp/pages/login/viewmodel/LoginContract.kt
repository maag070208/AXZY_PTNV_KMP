package com.axzydev.checkapp.pages.login.viewmodel

import com.axzydev.checkapp.entities.session.model.UserRole

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
    /**
     * "Recordarme": al apagarlo la sesión vive sólo en memoria y hay que volver
     * a autenticarse al reiniciar la app (dispositivo compartido entre guardias).
     *
     * Por defecto `true` para no cambiar el comportamiento que ya tenía la app,
     * que restauraba la sesión al arrancar.
     */
    val rememberMe: Boolean = true,
) {
    val canSubmit: Boolean get() = username.isNotBlank() && password.isNotEmpty() && !loading
}

sealed interface LoginAction {
    data class Username(val value: String) : LoginAction
    data class Password(val value: String) : LoginAction
    data class RememberMe(val value: Boolean) : LoginAction
    data object Submit : LoginAction
}

sealed interface LoginEffect {
    data class LoggedIn(val role: UserRole) : LoginEffect
}
