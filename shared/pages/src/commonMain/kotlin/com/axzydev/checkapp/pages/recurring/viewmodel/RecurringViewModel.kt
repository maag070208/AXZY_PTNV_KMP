package com.axzydev.checkapp.pages.recurring.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.crudrecurring.model.DeleteRecurringUseCase
import com.axzydev.checkapp.features.crudrecurring.model.ListRecurringUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RecurringViewModel(
    private val listRecurring: ListRecurringUseCase,
    private val deleteRecurring: DeleteRecurringUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(RecurringUiState())
    val state: StateFlow<RecurringUiState> = _state.asStateFlow()

    private val _effects = Channel<RecurringEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { listRecurring() }.fold(
                onSuccess = { routes ->
                    _state.update {
                        it.copy(
                            loading = false,
                            items = routes.map { route -> RecurringItemUi(route.id, route.title, route.points.size) },
                        )
                    }
                },
                onFailure = { error ->
                    _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las rutas") }
                },
            )
        }
    }

    fun onAction(action: RecurringAction) {
        when (action) {
            RecurringAction.Refresh -> refresh()
            is RecurringAction.Search -> _state.update { it.copy(query = action.value) }
            is RecurringAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteTitle = current.items.firstOrNull { it.id == action.id }?.title,
                )
            }

            RecurringAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteTitle = null) }
            RecurringAction.ConfirmDelete -> confirmDelete()
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteRecurring(id)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(deleting = false, pendingDeleteId = null, pendingDeleteTitle = null)
                    }
                    _effects.send(RecurringEffect.Deleted)
                    refresh()
                }

                is ApiResult.Failure -> {
                    val message = result.messages.firstOrNull() ?: "No se pudo eliminar la ruta"
                    _state.update { it.copy(deleting = false, error = message) }
                    _effects.send(RecurringEffect.Error(message))
                }
            }
        }
    }
}
