package com.axzydev.checkapp.pages.uniformcheck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.time.epochMillisToDateTimeText
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.entities.uniformcheck.model.ChecklistAnswer
import com.axzydev.checkapp.entities.uniformcheck.model.UniformCheckDraft
import com.axzydev.checkapp.features.guardlist.model.ListGuardsUseCase
import com.axzydev.checkapp.features.supervision.model.GetUniformCatalogUseCase
import com.axzydev.checkapp.features.supervision.model.ListUniformChecksUseCase
import com.axzydev.checkapp.features.supervision.model.SaveUniformCheckUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UniformCheckViewModel(
    private val saveUniformCheck: SaveUniformCheckUseCase,
    private val listUniformChecks: ListUniformChecksUseCase,
    private val getCatalog: GetUniformCatalogUseCase,
    private val listGuards: ListGuardsUseCase,
    private val sessionRepository: SessionRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(UniformCheckUiState())
    val state: StateFlow<UniformCheckUiState> = _state.asStateFlow()

    private val _effects = Channel<UniformCheckEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var minCompliantScore: Int = 80

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val catalog = runCatching { getCatalog() }.getOrNull()
            val guards = runCatching { listGuards() }.getOrDefault(emptyList())
            val saved = runCatching { listUniformChecks() }.getOrDefault(emptyList())
            if (catalog != null) minCompliantScore = catalog.minCompliantScore
            _state.update {
                it.copy(
                    loading = false,
                    guards = guards.map { g -> Option(g.id, g.fullName) },
                    items = catalog?.items?.map { item -> ChecklistItemUi(item.key, item.label, true) }.orEmpty(),
                    catalogMissing = catalog == null,
                    savedCount = saved.size,
                )
            }
        }
    }

    fun onAction(action: UniformCheckAction) {
        when (action) {
            is UniformCheckAction.SelectGuard -> _state.update { it.copy(selectedGuardId = action.guardId) }
            is UniformCheckAction.Toggle -> _state.update { current ->
                current.copy(items = current.items.map { if (it.key == action.key) it.copy(ok = action.ok) else it })
            }

            is UniformCheckAction.Notes -> _state.update { it.copy(notes = action.value) }
            UniformCheckAction.Save -> save()
        }
    }

    private fun save() {
        val current = _state.value
        val session = sessionRepository.sessionState.value
        val guardId = current.selectedGuardId
        if (guardId == null || session == null) {
            _state.update { it.copy(error = "Selecciona un guardia") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val guard = current.guards.firstOrNull { it.id == guardId }
            val draft = UniformCheckDraft(
                guardId = guardId,
                clientId = session.clientId,
                scheduleId = null,
                shiftDate = timeProvider.nowEpochMillis().epochMillisToDateTimeText().take(10),
                evaluatedById = session.userId,
                items = current.items.map { ChecklistAnswer(it.key, it.ok) },
                notes = current.notes.trim().ifBlank { null },
                minCompliantScore = minCompliantScore,
            )
            runCatching { saveUniformCheck(draft) }.fold(
                onSuccess = {
                    _state.update { it.copy(saving = false, notes = "", selectedGuardId = null, savedCount = it.savedCount + 1) }
                    _effects.send(UniformCheckEffect.Saved)
                },
                onFailure = { error ->
                    _state.update { it.copy(saving = false, error = error.message ?: "No se pudo guardar la revisión") }
                    _effects.send(UniformCheckEffect.Error(error.message ?: "No se pudo guardar"))
                },
            )
        }
    }
}
