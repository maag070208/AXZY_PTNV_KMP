package com.axzydev.checkapp.pages.guarddetail.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.features.guardlist.model.GetGuardUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuardDetailViewModel(
    private val getGuard: GetGuardUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GuardDetailUiState())
    val state: StateFlow<GuardDetailUiState> = _state.asStateFlow()

    private var guardId: String = ""

    fun load(guardId: String) {
        this.guardId = guardId
        viewModelScope.launch { refreshInternal() }
    }

    fun onAction(action: GuardDetailAction) {
        when (action) {
            GuardDetailAction.Refresh -> viewModelScope.launch { refreshInternal() }
        }
    }

    private suspend fun refreshInternal() {
        _state.update { it.copy(loading = true, error = null) }
        val guard = getGuard(guardId)
        if (guard == null) {
            _state.update { it.copy(loading = false, error = "Guardia no encontrado") }
            return
        }
        _state.update {
            it.copy(loading = false, name = guard.fullName, username = guard.username, active = guard.active)
        }
    }
}
