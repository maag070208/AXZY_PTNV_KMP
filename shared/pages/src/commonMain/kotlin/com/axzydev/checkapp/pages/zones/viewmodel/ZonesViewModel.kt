package com.axzydev.checkapp.pages.zones.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.zone.model.ZoneDraft
import com.axzydev.checkapp.features.crudzone.model.CreateZoneUseCase
import com.axzydev.checkapp.features.crudzone.model.DeleteZoneUseCase
import com.axzydev.checkapp.features.crudzone.model.ListZonesUseCase
import com.axzydev.checkapp.features.crudzone.model.UpdateZoneUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ZonesViewModel(
    private val listZones: ListZonesUseCase,
    private val createZone: CreateZoneUseCase,
    private val updateZone: UpdateZoneUseCase,
    private val deleteZone: DeleteZoneUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ZonesUiState())
    val state: StateFlow<ZonesUiState> = _state.asStateFlow()

    private val _effects = Channel<ZonesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var clientId: String = ""

    fun load(clientId: String) {
        this.clientId = clientId
        viewModelScope.launch { refreshInternal() }
    }

    fun onAction(action: ZonesAction) {
        when (action) {
            ZonesAction.Refresh -> viewModelScope.launch { refreshInternal() }
            is ZonesAction.Search -> _state.update { it.copy(query = action.value) }
            ZonesAction.ToggleForm -> _state.update { it.copy(showForm = !it.showForm, error = null) }
            is ZonesAction.Name -> _state.update { it.copy(name = action.value) }
            ZonesAction.Create -> create()
            is ZonesAction.StartEdit -> startEdit(action.id)
            ZonesAction.CancelEdit -> _state.update { it.copy(editingId = null, editName = "") }
            is ZonesAction.EditName -> _state.update { it.copy(editName = action.value) }
            ZonesAction.SaveEdit -> saveEdit()
            is ZonesAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteName = current.items.firstOrNull { it.id == action.id }?.name,
                )
            }

            ZonesAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteName = null) }
            ZonesAction.ConfirmDelete -> confirmDelete()
        }
    }

    /**
     * Busca la zona en la lista ya cargada.
     *
     * La acción sólo lleva el id para que la pantalla no tenga que reconstruir el
     * item; se ignora si ya no existe.
     */
    private fun startEdit(id: String) {
        val zone = _state.value.items.firstOrNull { it.id == id } ?: return
        _state.update { it.copy(editingId = id, editName = zone.name, error = null) }
    }

    private suspend fun refreshInternal() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listZones(clientId) }.fold(
            onSuccess = { zones ->
                _state.update { it.copy(loading = false, items = zones.map { z -> ZoneItemUi(z.id, z.name, z.active) }) }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las zonas") }
            },
        )
    }

    private fun create() {
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            when (val result = createZone(ZoneDraft(name, clientId))) {
                is ApiResult.Success -> {
                    _state.update { it.copy(creating = false, name = "") }
                    _effects.send(ZonesEffect.Created)
                    refreshInternal()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(creating = false, error = result.firstMessage) }
                    _effects.send(ZonesEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun saveEdit() {
        val id = _state.value.editingId ?: return
        val name = _state.value.editName.trim()
        if (name.isBlank()) {
            _state.update { it.copy(error = "El nombre es obligatorio") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = updateZone(id, name)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false, editingId = null, editName = "") }
                    refreshInternal()
                }

                is ApiResult.Failure -> _state.update { it.copy(saving = false, error = result.firstMessage) }
            }
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteZone(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteName = null) }
                    _effects.send(ZonesEffect.Deleted)
                    refreshInternal()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
