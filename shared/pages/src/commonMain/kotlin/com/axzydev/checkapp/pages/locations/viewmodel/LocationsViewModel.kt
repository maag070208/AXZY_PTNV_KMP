package com.axzydev.checkapp.pages.locations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.location.model.LocationDraft
import com.axzydev.checkapp.features.crudclient.model.ListClientsUseCase
import com.axzydev.checkapp.features.crudlocation.model.CreateLocationUseCase
import com.axzydev.checkapp.features.crudlocation.model.DeleteLocationUseCase
import com.axzydev.checkapp.features.crudlocation.model.ListLocationsUseCase
import com.axzydev.checkapp.features.crudlocation.model.UpdateLocationUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationsViewModel(
    private val listLocations: ListLocationsUseCase,
    private val createLocation: CreateLocationUseCase,
    private val updateLocation: UpdateLocationUseCase,
    private val deleteLocation: DeleteLocationUseCase,
    private val listClients: ListClientsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(LocationsUiState())
    val state: StateFlow<LocationsUiState> = _state.asStateFlow()

    private val _effects = Channel<LocationsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: LocationsAction) {
        when (action) {
            LocationsAction.Refresh -> refresh()
            is LocationsAction.Search -> _state.update { it.copy(query = action.value) }
            LocationsAction.ToggleForm -> _state.update { it.copy(showForm = !it.showForm, error = null) }
            is LocationsAction.SelectClient -> _state.update { it.copy(selectedClientId = action.clientId) }
            is LocationsAction.Name -> _state.update { it.copy(name = action.value) }
            is LocationsAction.Reference -> _state.update { it.copy(reference = action.value) }
            LocationsAction.Create -> create()
            is LocationsAction.StartEdit -> startEdit(action.id)
            LocationsAction.CancelEdit -> _state.update { it.copy(editingId = null, editName = "", editReference = "") }
            is LocationsAction.EditName -> _state.update { it.copy(editName = action.value) }
            is LocationsAction.EditReference -> _state.update { it.copy(editReference = action.value) }
            LocationsAction.SaveEdit -> saveEdit()
            is LocationsAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteName = current.items.firstOrNull { it.id == action.id }?.name,
                )
            }

            LocationsAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteName = null) }
            LocationsAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching {
            val clients = listClients()
            val names = clients.associate { it.id to it.name }
            val locations = listLocations().map {
                LocationItemUi(it.id, it.name, it.clientId, it.clientId?.let(names::get), it.reference)
            }
            Pair(clients.map { Option(it.id, it.name) }, locations)
        }.fold(
            onSuccess = { (clientOptions, locations) ->
                _state.update { it.copy(loading = false, clients = clientOptions, items = locations) }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las ubicaciones") }
            },
        )
    }

    private fun create() {
        val current = _state.value
        val clientId = current.selectedClientId
        if (current.name.isBlank() || clientId == null) {
            _state.update { it.copy(error = "Selecciona un cliente y escribe un nombre") }
            return
        }
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val draft = LocationDraft(
                name = current.name.trim(),
                clientId = clientId,
                reference = current.reference.trim().ifBlank { null },
            )
            when (val result = createLocation(draft)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(creating = false, showForm = false, name = "", reference = "") }
                    _effects.send(LocationsEffect.Created)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(creating = false, error = result.firstMessage) }
                    _effects.send(LocationsEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun startEdit(id: String) {
        val location = _state.value.items.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(editingId = id, editName = location.name, editReference = location.reference.orEmpty(), error = null)
        }
    }

    private fun saveEdit() {
        val current = _state.value
        val id = current.editingId ?: return
        val location = current.items.firstOrNull { it.id == id }
        val clientId = location?.clientId
        if (current.editName.isBlank() || clientId == null) {
            _state.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val draft = LocationDraft(
                name = current.editName.trim(),
                clientId = clientId,
                reference = current.editReference.trim().ifBlank { null },
            )
            when (val result = updateLocation(id, draft)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false, editingId = null, editName = "", editReference = "") }
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
            when (val result = deleteLocation(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteName = null) }
                    _effects.send(LocationsEffect.Deleted)
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
