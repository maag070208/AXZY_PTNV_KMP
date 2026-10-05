package com.axzydev.checkapp.pages.clients.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.client.model.ClientDraft
import com.axzydev.checkapp.features.crudclient.model.CreateClientUseCase
import com.axzydev.checkapp.features.crudclient.model.DeleteClientUseCase
import com.axzydev.checkapp.features.crudclient.model.ListClientsUseCase
import com.axzydev.checkapp.features.crudclient.model.UpdateClientUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClientsViewModel(
    private val listClients: ListClientsUseCase,
    private val createClient: CreateClientUseCase,
    private val updateClient: UpdateClientUseCase,
    private val deleteClient: DeleteClientUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ClientsUiState())
    val state: StateFlow<ClientsUiState> = _state.asStateFlow()

    private val _effects = Channel<ClientsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: ClientsAction) {
        when (action) {
            ClientsAction.Refresh -> refresh()
            is ClientsAction.Search -> _state.update { it.copy(query = action.value) }
            ClientsAction.ToggleForm -> _state.update { it.copy(showForm = !it.showForm, error = null) }
            is ClientsAction.Name -> _state.update { it.copy(name = action.value) }
            is ClientsAction.Address -> _state.update { it.copy(address = action.value) }
            is ClientsAction.Rfc -> _state.update { it.copy(rfc = action.value) }
            is ClientsAction.ContactName -> _state.update { it.copy(contactName = action.value) }
            is ClientsAction.ContactPhone -> _state.update { it.copy(contactPhone = action.value) }
            ClientsAction.Create -> create()
            is ClientsAction.StartEdit -> startEdit(action.id)
            ClientsAction.CancelEdit -> _state.update {
                it.copy(editingId = null, editName = "", editAddress = "", editRfc = "", editContactName = "", editContactPhone = "")
            }

            is ClientsAction.EditName -> _state.update { it.copy(editName = action.value) }
            is ClientsAction.EditAddress -> _state.update { it.copy(editAddress = action.value) }
            is ClientsAction.EditRfc -> _state.update { it.copy(editRfc = action.value) }
            is ClientsAction.EditContactName -> _state.update { it.copy(editContactName = action.value) }
            is ClientsAction.EditContactPhone -> _state.update { it.copy(editContactPhone = action.value) }
            ClientsAction.SaveEdit -> saveEdit()
            is ClientsAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteName = current.items.firstOrNull { it.id == action.id }?.name,
                )
            }

            ClientsAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteName = null) }
            ClientsAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listClients() }.fold(
            onSuccess = { clients ->
                _state.update {
                    it.copy(
                        loading = false,
                        items = clients.map { c ->
                            ClientItemUi(c.id, c.name, c.address, c.rfc, c.contactName, c.contactPhone, c.active)
                        },
                    )
                }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar los clientes") }
            },
        )
    }

    private fun create() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val draft = ClientDraft(
                name = current.name.trim(),
                address = current.address.trim().ifBlank { null },
                rfc = current.rfc.trim().ifBlank { null },
                contactName = current.contactName.trim().ifBlank { null },
                contactPhone = current.contactPhone.trim().ifBlank { null },
            )
            when (val result = createClient(draft)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(creating = false, showForm = false, name = "", address = "", rfc = "", contactName = "", contactPhone = "")
                    }
                    _effects.send(ClientsEffect.Created)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(creating = false, error = result.firstMessage) }
                    _effects.send(ClientsEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun startEdit(id: String) {
        val client = _state.value.items.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(
                editingId = id,
                editName = client.name,
                editAddress = client.address.orEmpty(),
                editRfc = client.rfc.orEmpty(),
                editContactName = client.contactName.orEmpty(),
                editContactPhone = client.contactPhone.orEmpty(),
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
            val draft = ClientDraft(
                name = current.editName.trim(),
                address = current.editAddress.trim().ifBlank { null },
                rfc = current.editRfc.trim().ifBlank { null },
                contactName = current.editContactName.trim().ifBlank { null },
                contactPhone = current.editContactPhone.trim().ifBlank { null },
            )
            when (val result = updateClient(id, draft)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(
                            saving = false,
                            editingId = null,
                            editName = "",
                            editAddress = "",
                            editRfc = "",
                            editContactName = "",
                            editContactPhone = "",
                        )
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
            when (val result = deleteClient(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteName = null) }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
