package com.axzydev.checkapp.pages.login.viewmodel

import com.axzydev.checkapp.entities.session.model.UserRole

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val loading: Boolean = false,
    val error: String? = null,
) {
    val canSubmit: Boolean get() = username.isNotBlank() && password.isNotEmpty() && !loading
}

sealed interface LoginAction {
    data class Username(val value: String) : LoginAction
    data class Password(val value: String) : LoginAction
    data object Submit : LoginAction
}

sealed interface LoginEffect {
    data class LoggedIn(val role: UserRole) : LoginEffect
}
