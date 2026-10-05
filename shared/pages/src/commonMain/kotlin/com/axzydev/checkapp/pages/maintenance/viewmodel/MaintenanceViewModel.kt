package com.axzydev.checkapp.pages.maintenance.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.crudmaintenance.model.DeleteMaintenanceUseCase
import com.axzydev.checkapp.features.crudmaintenance.model.ListMaintenancesUseCase
import com.axzydev.checkapp.features.crudmaintenance.model.ResolveMaintenanceUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MaintenanceViewModel(
    private val listMaintenances: ListMaintenancesUseCase,
    private val resolveMaintenance: ResolveMaintenanceUseCase,
    private val deleteMaintenance: DeleteMaintenanceUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(MaintenanceUiState())
    val state: StateFlow<MaintenanceUiState> = _state.asStateFlow()

    private val _effects = Channel<MaintenanceEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: MaintenanceAction) {
        when (action) {
            MaintenanceAction.Refresh -> refresh()
            is MaintenanceAction.Search -> _state.update { it.copy(query = action.value) }
            is MaintenanceAction.Resolve -> resolve(action.id)
            is MaintenanceAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteTitle = current.items.firstOrNull { it.id == action.id }?.title,
                )
            }

            MaintenanceAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteTitle = null) }
            MaintenanceAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listMaintenances() }.fold(
            onSuccess = { items ->
                _state.update {
                    it.copy(
                        loading = false,
                        items = items.map { m ->
                            MaintenanceItemUi(m.id, m.title, m.guardName, m.categoryName, m.status, m.createdAt)
                        },
                    )
                }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar los mantenimientos") }
            },
        )
    }

    private fun resolve(id: String) {
        _state.update { it.copy(resolvingId = id, error = null) }
        viewModelScope.launch {
            when (val result = resolveMaintenance(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(resolvingId = null) }
                    _effects.send(MaintenanceEffect.Resolved)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(resolvingId = null, error = result.firstMessage) }
                    _effects.send(MaintenanceEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteMaintenance(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteTitle = null) }
                    _effects.send(MaintenanceEffect.Deleted)
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
