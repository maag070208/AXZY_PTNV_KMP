package com.axzydev.checkapp.pages.incidents.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.crudincident.model.DeleteIncidentUseCase
import com.axzydev.checkapp.features.crudincident.model.ListIncidentsUseCase
import com.axzydev.checkapp.features.crudincident.model.ResolveIncidentUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IncidentsViewModel(
    private val listIncidents: ListIncidentsUseCase,
    private val resolveIncident: ResolveIncidentUseCase,
    private val deleteIncident: DeleteIncidentUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(IncidentsUiState())
    val state: StateFlow<IncidentsUiState> = _state.asStateFlow()

    private val _effects = Channel<IncidentsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: IncidentsAction) {
        when (action) {
            IncidentsAction.Refresh -> refresh()
            is IncidentsAction.Search -> _state.update { it.copy(query = action.value) }
            is IncidentsAction.Resolve -> resolve(action.id)
            is IncidentsAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteTitle = current.items.firstOrNull { it.id == action.id }?.title,
                )
            }

            IncidentsAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteTitle = null) }
            IncidentsAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listIncidents() }.fold(
            onSuccess = { incidents ->
                _state.update {
                    it.copy(
                        loading = false,
                        items = incidents.map { i ->
                            IncidentItemUi(i.id, i.title, i.guardName, i.categoryName, i.status, i.createdAt)
                        },
                    )
                }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las incidencias") }
            },
        )
    }

    private fun resolve(id: String) {
        _state.update { it.copy(resolvingId = id, error = null) }
        viewModelScope.launch {
            when (val result = resolveIncident(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(resolvingId = null) }
                    _effects.send(IncidentsEffect.Resolved)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(resolvingId = null, error = result.firstMessage) }
                    _effects.send(IncidentsEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteIncident(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteTitle = null) }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
