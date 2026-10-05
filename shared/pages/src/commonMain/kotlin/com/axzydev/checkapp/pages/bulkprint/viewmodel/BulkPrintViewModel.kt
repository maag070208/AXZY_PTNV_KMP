package com.axzydev.checkapp.pages.bulkprint.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.features.crudclient.model.ListClientsUseCase
import com.axzydev.checkapp.features.crudlocation.model.ListLocationsUseCase
import com.axzydev.checkapp.features.qr.model.QrPayload
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Genera los QR de los puntos seleccionados para impresión masiva. */
class BulkPrintViewModel(
    private val listLocations: ListLocationsUseCase,
    private val listClients: ListClientsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(BulkPrintUiState())
    val state: StateFlow<BulkPrintUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val clients = listClients()
                val names = clients.associate { it.id to it.name }
                val locations = listLocations().map {
                    PrintableLocationUi(it.id, it.name, it.clientId, it.clientId?.let(names::get))
                }
                Pair(clients.map { Option(it.id, it.name) }, locations)
            }.fold(
                onSuccess = { (clientOptions, locations) ->
                    _state.update { it.copy(loading = false, clients = clientOptions, locations = locations) }
                },
                onFailure = { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las ubicaciones") }
                },
            )
        }
    }

    fun onAction(action: BulkPrintAction) {
        when (action) {
            BulkPrintAction.Refresh -> refresh()
            is BulkPrintAction.SelectClient -> _state.update { it.copy(selectedClientId = action.clientId, selectedIds = emptySet()) }
            is BulkPrintAction.Toggle -> _state.update { current ->
                val ids = if (action.id in current.selectedIds) {
                    current.selectedIds - action.id
                } else {
                    current.selectedIds + action.id
                }
                current.copy(selectedIds = ids, error = null)
            }

            BulkPrintAction.SelectAll -> _state.update { it.copy(selectedIds = it.visibleLocations.map { l -> l.id }.toSet()) }
            BulkPrintAction.Clear -> _state.update { it.copy(selectedIds = emptySet()) }
            BulkPrintAction.Generate -> generate()
            BulkPrintAction.ClearSheet -> _state.update { it.copy(sheet = emptyList()) }
        }
    }

    private fun generate() {
        val current = _state.value
        if (current.selectedIds.isEmpty()) {
            _state.update { it.copy(error = "Selecciona al menos un punto") }
            return
        }
        val sheet = current.locations
            .filter { it.id in current.selectedIds }
            .sortedBy { it.name }
            .map { QrSheetItem(it.id, it.name, QrPayload.forLocation(it.name, it.id)) }
        _state.update { it.copy(sheet = sheet, error = null) }
    }
}
