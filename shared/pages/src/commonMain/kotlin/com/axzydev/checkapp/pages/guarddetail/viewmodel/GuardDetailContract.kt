package com.axzydev.checkapp.pages.guarddetail.viewmodel

data class GuardDetailUiState(
    val loading: Boolean = true,
    val name: String = "",
    val username: String = "",
    val active: Boolean = false,
    val error: String? = null,
)

sealed interface GuardDetailAction {
    data object Refresh : GuardDetailAction
}
