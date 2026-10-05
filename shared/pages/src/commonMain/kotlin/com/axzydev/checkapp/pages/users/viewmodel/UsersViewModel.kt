package com.axzydev.checkapp.pages.users.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.user.model.UserDraft
import com.axzydev.checkapp.entities.user.model.UserUpdateDraft
import com.axzydev.checkapp.features.crudclient.model.ListClientsUseCase
import com.axzydev.checkapp.features.cruduser.model.CreateUserUseCase
import com.axzydev.checkapp.features.cruduser.model.DeleteUserUseCase
import com.axzydev.checkapp.features.cruduser.model.ListRolesUseCase
import com.axzydev.checkapp.features.cruduser.model.ListUsersUseCase
import com.axzydev.checkapp.features.cruduser.model.UpdateUserUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UsersViewModel(
    private val listUsers: ListUsersUseCase,
    private val createUser: CreateUserUseCase,
    private val updateUser: UpdateUserUseCase,
    private val deleteUser: DeleteUserUseCase,
    private val listRoles: ListRolesUseCase,
    private val listClients: ListClientsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(UsersUiState())
    val state: StateFlow<UsersUiState> = _state.asStateFlow()

    private val _effects = Channel<UsersEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: UsersAction) {
        when (action) {
            UsersAction.Refresh -> refresh()
            is UsersAction.Search -> _state.update { it.copy(query = action.value) }
            UsersAction.ToggleForm -> _state.update { it.copy(showForm = !it.showForm, error = null) }
            is UsersAction.Name -> _state.update { it.copy(name = action.value) }
            is UsersAction.LastName -> _state.update { it.copy(lastName = action.value) }
            is UsersAction.Username -> _state.update { it.copy(username = action.value) }
            is UsersAction.Password -> _state.update { it.copy(password = action.value) }
            is UsersAction.SelectRole -> _state.update { it.copy(selectedRoleId = action.roleId) }
            is UsersAction.SelectClient -> _state.update { it.copy(selectedClientId = action.clientId) }
            UsersAction.Create -> create()
            is UsersAction.StartEdit -> startEdit(action.id)
            UsersAction.CancelEdit -> _state.update {
                it.copy(editingId = null, editName = "", editLastName = "", editRoleId = null, editClientId = null)
            }

            is UsersAction.EditName -> _state.update { it.copy(editName = action.value) }
            is UsersAction.EditLastName -> _state.update { it.copy(editLastName = action.value) }
            is UsersAction.EditRole -> _state.update { it.copy(editRoleId = action.roleId) }
            is UsersAction.EditClient -> _state.update { it.copy(editClientId = action.clientId) }
            UsersAction.SaveEdit -> saveEdit()
            is UsersAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteName = current.items.firstOrNull { it.id == action.id }?.name,
                )
            }

            UsersAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteName = null) }
            UsersAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            val roles = listRoles().map { Option(it.id, it.name) }
            val clients = listClients().map { Option(it.id, it.name) }
            val users = listUsers().map {
                UserItemUi(it.id, it.name, it.lastName, it.username, it.roleName, it.roleId, it.clientId)
            }
            Triple(roles, clients, users)
        }.fold(
            onSuccess = { (roles, clients, users) ->
                _state.update { it.copy(loading = false, roles = roles, clients = clients, items = users) }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar los usuarios") }
            },
        )
    }

    private fun create() {
        val current = _state.value
        val roleId = current.selectedRoleId
        if (current.name.isBlank() || current.username.isBlank() || current.password.isBlank() || roleId == null) {
            _state.update { it.copy(error = "Nombre, usuario, contraseña y rol son obligatorios") }
            return
        }
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val draft = UserDraft(
                name = current.name.trim(),
                username = current.username.trim(),
                password = current.password,
                roleId = roleId,
                lastName = current.lastName.trim().ifBlank { null },
                clientId = current.selectedClientId,
            )
            when (val result = createUser(draft)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            creating = false, showForm = false, name = "", lastName = "",
                            username = "", password = "", selectedRoleId = null, selectedClientId = null,
                        )
                    }
                    _effects.send(UsersEffect.Created)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(creating = false, error = result.firstMessage) }
                    _effects.send(UsersEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun startEdit(id: String) {
        val user = _state.value.items.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(
                editingId = id,
                editName = user.name,
                editLastName = user.lastName.orEmpty(),
                editRoleId = user.roleId,
                editClientId = user.clientId,
                error = null,
            )
        }
    }

    private fun saveEdit() {
        val current = _state.value
        val id = current.editingId ?: return
        if (current.editName.isBlank()) {
            _state.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val draft = UserUpdateDraft(
                name = current.editName.trim(),
                lastName = current.editLastName.trim().ifBlank { null },
                roleId = current.editRoleId,
                clientId = current.editClientId,
            )
            when (val result = updateUser(id, draft)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(editingId = null, editName = "", editLastName = "", editRoleId = null, editClientId = null, saving = false)
                    }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(saving = false, error = result.firstMessage) }
            }
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteUser(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteName = null) }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
