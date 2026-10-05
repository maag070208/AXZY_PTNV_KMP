package com.axzydev.checkapp.pages.guardlogs.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.features.guardlogs.model.ListGuardLogsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuardLogsViewModel(
    private val listGuardLogs: ListGuardLogsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GuardLogsUiState())
    val state: StateFlow<GuardLogsUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: GuardLogsAction) {
        when (action) {
            GuardLogsAction.Refresh -> refresh()
            is GuardLogsAction.Search -> _state.update { it.copy(query = action.value) }
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listGuardLogs() }.fold(
            onSuccess = { logs ->
                _state.update {
                    it.copy(
                        loading = false,
                        items = logs.map { l -> GuardLogItemUi(l.id, l.guardName, l.username, l.loginAt, l.logoutAt, l.durationMinutes) },
                    )
                }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudo cargar la prenómina") }
            },
        )
    }
}
