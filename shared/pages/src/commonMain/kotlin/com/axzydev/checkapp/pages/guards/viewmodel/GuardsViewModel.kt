package com.axzydev.checkapp.pages.guards.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.guardlist.model.DeleteGuardUseCase
import com.axzydev.checkapp.features.guardlist.model.ListGuardsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuardsViewModel(
    private val listGuards: ListGuardsUseCase,
    private val deleteGuard: DeleteGuardUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GuardsUiState())
    val state: StateFlow<GuardsUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: GuardsAction) {
        when (action) {
            GuardsAction.Refresh -> refresh()
            is GuardsAction.Search -> _state.update { it.copy(query = action.value) }
            is GuardsAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteName = current.items.firstOrNull { it.id == action.id }?.name,
                )
            }

            GuardsAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteName = null) }
            GuardsAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listGuards() }.fold(
            onSuccess = { guards ->
                _state.update { it.copy(loading = false, items = guards.map { g -> GuardItemUi(g.id, g.fullName, g.username) }) }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar los guardias") }
            },
        )
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteGuard(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteName = null) }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
