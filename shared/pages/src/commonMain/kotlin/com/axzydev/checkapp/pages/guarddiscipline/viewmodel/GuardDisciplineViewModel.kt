package com.axzydev.checkapp.pages.guarddiscipline.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.cruddiscipline.model.ListDisciplinesUseCase
import com.axzydev.checkapp.features.cruddiscipline.model.ResolveDisciplineUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GuardDisciplineViewModel(
    private val listDisciplines: ListDisciplinesUseCase,
    private val resolveDiscipline: ResolveDisciplineUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(GuardDisciplineUiState())
    val state: StateFlow<GuardDisciplineUiState> = _state.asStateFlow()

    private val _effects = Channel<GuardDisciplineEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: GuardDisciplineAction) {
        when (action) {
            GuardDisciplineAction.Refresh -> refresh()
            is GuardDisciplineAction.Search -> _state.update { it.copy(query = action.value) }
            is GuardDisciplineAction.Resolve -> resolve(action.id)
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listDisciplines() }.fold(
            onSuccess = { items ->
                _state.update {
                    it.copy(
                        loading = false,
                        items = items.map { d ->
                            DisciplineItemUi(d.id, d.guardName, d.categoryName, d.typeName, d.description, d.status, d.createdAt)
                        },
                    )
                }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudo cargar la disciplina") }
            },
        )
    }

    private fun resolve(id: String) {
        _state.update { it.copy(resolvingId = id, error = null) }
        viewModelScope.launch {
            when (val result = resolveDiscipline(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(resolvingId = null) }
                    _effects.send(GuardDisciplineEffect.Resolved)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(resolvingId = null, error = result.firstMessage) }
                    _effects.send(GuardDisciplineEffect.Error(result.firstMessage))
                }
            }
        }
    }
}
