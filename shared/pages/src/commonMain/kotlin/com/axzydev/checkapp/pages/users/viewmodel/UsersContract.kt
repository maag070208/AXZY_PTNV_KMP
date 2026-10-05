package com.axzydev.checkapp.pages.users.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class UserItemUi(
    val id: String,
    val name: String,
    val lastName: String?,
    val username: String,
    val roleName: String?,
    val roleId: String?,
    val clientId: String?,
)

data class Option(val id: String, val label: String)

data class UsersUiState(
    val loading: Boolean = true,
    override val items: List<UserItemUi> = emptyList(),
    override val query: String = "",
    val roles: List<Option> = emptyList(),
    val clients: List<Option> = emptyList(),
    val showForm: Boolean = false,
    val name: String = "",
    val lastName: String = "",
    val username: String = "",
    val password: String = "",
    val selectedRoleId: String? = null,
    val selectedClientId: String? = null,
    val creating: Boolean = false,
    val editingId: String? = null,
    val editName: String = "",
    val editLastName: String = "",
    val editRoleId: String? = null,
    val editClientId: String? = null,
    val saving: Boolean = false,
    val pendingDeleteId: String? = null,
    val pendingDeleteName: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<UserItemUi> {
    /** Se busca por nombre, usuario y rol: los tres datos de la tarjeta. */
    override val searchFields: List<(UserItemUi) -> String?>
        get() = listOf({ it.name }, { it.lastName }, { it.username }, { it.roleName })
}

sealed interface UsersAction {
    data object Refresh : UsersAction
    data class Search(val value: String) : UsersAction
    data object ToggleForm : UsersAction
    data class Name(val value: String) : UsersAction
    data class LastName(val value: String) : UsersAction
    data class Username(val value: String) : UsersAction
    data class Password(val value: String) : UsersAction
    data class SelectRole(val roleId: String) : UsersAction
    data class SelectClient(val clientId: String) : UsersAction
    data object Create : UsersAction
    data class StartEdit(val id: String) : UsersAction
    data object CancelEdit : UsersAction
    data class EditName(val value: String) : UsersAction
    data class EditLastName(val value: String) : UsersAction
    data class EditRole(val roleId: String) : UsersAction
    data class EditClient(val clientId: String) : UsersAction
    data object SaveEdit : UsersAction
    data class RequestDelete(val id: String) : UsersAction
    data object CancelDelete : UsersAction
    data object ConfirmDelete : UsersAction
}

sealed interface UsersEffect {
    data class Error(val message: String) : UsersEffect
    data object Created : UsersEffect
    data object Deleted : UsersEffect
}
