package com.axzydev.checkapp.pages.shifthandover.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.time.epochMillisToDateTimeText
import com.axzydev.checkapp.entities.session.repository.SessionRepository
import com.axzydev.checkapp.entities.shifthandover.model.ShiftHandoverDraft
import com.axzydev.checkapp.entities.uniformcheck.model.ChecklistAnswer
import com.axzydev.checkapp.features.crudclient.model.ListClientsUseCase
import com.axzydev.checkapp.features.crudschedule.model.ListSchedulesUseCase
import com.axzydev.checkapp.features.supervision.model.GetShiftHandoverCatalogUseCase
import com.axzydev.checkapp.features.supervision.model.ListShiftHandoversUseCase
import com.axzydev.checkapp.features.supervision.model.SaveShiftHandoverUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShiftHandoverViewModel(
    private val saveShiftHandover: SaveShiftHandoverUseCase,
    private val listShiftHandovers: ListShiftHandoversUseCase,
    private val getCatalog: GetShiftHandoverCatalogUseCase,
    private val listClients: ListClientsUseCase,
    private val listSchedules: ListSchedulesUseCase,
    private val sessionRepository: SessionRepository,
    private val timeProvider: TimeProvider,
) : ViewModel() {

    private val _state = MutableStateFlow(ShiftHandoverUiState())
    val state: StateFlow<ShiftHandoverUiState> = _state.asStateFlow()

    private val _effects = Channel<ShiftHandoverEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val catalog = runCatching { getCatalog() }.getOrDefault(emptyList())
            val clients = runCatching { listClients() }.getOrDefault(emptyList())
            val schedules = runCatching { listSchedules() }.getOrDefault(emptyList())
            val saved = runCatching { listShiftHandovers() }.getOrDefault(emptyList())

            _state.update {
                it.copy(
                    loading = false,
                    clients = clients.map { c -> Option(c.id, c.name) },
                    schedules = schedules.map { s -> Option(s.id, "${s.name} (${s.startTime}-${s.endTime})") },
                    items = catalog.map { item -> ChecklistItemUi(item.key, item.label, true) },
                    catalogMissing = catalog.isEmpty(),
                    savedCount = saved.size,
                )
            }
        }
    }

    fun onAction(action: ShiftHandoverAction) {
        when (action) {
            is ShiftHandoverAction.SelectClient -> _state.update { it.copy(selectedClientId = action.clientId) }
            is ShiftHandoverAction.SelectSchedule -> _state.update { it.copy(selectedScheduleId = action.scheduleId) }
            is ShiftHandoverAction.Credentials -> _state.update { it.copy(credentials = action.value) }
            is ShiftHandoverAction.Tarjetones -> _state.update { it.copy(tarjetones = action.value) }
            is ShiftHandoverAction.Novedades -> _state.update { it.copy(novedades = action.value) }
            is ShiftHandoverAction.Toggle -> _state.update { current ->
                current.copy(items = current.items.map { if (it.key == action.key) it.copy(ok = action.ok) else it })
            }

            is ShiftHandoverAction.SetReported -> _state.update { it.copy(reportedToAdmin = action.value) }
            ShiftHandoverAction.Save -> save()
        }
    }

    private fun save() {
        val current = _state.value
        val session = sessionRepository.sessionState.value
        val clientId = current.selectedClientId
        val scheduleId = current.selectedScheduleId
        if (clientId == null || scheduleId == null || session == null) {
            _state.update { it.copy(error = "Selecciona cliente y horario") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val draft = ShiftHandoverDraft(
                clientId = clientId,
                scheduleId = scheduleId,
                shiftDate = timeProvider.nowEpochMillis().epochMillisToDateTimeText().take(10),
                credentialsCount = current.credentials.toIntOrNull(),
                tarjetonesCount = current.tarjetones.toIntOrNull(),
                novedades = current.novedades.trim().ifBlank { null },
                checklist = current.items.map { ChecklistAnswer(it.key, it.ok) },
                elements = emptyList(),
                reportedToAdmin = current.reportedToAdmin,
                createdById = session.userId,
            )
            runCatching { saveShiftHandover(draft) }.fold(
                onSuccess = {
                    _state.update {
                        it.copy(saving = false, credentials = "", tarjetones = "", novedades = "", savedCount = it.savedCount + 1)
                    }
                    _effects.send(ShiftHandoverEffect.Saved)
                },
                onFailure = { error ->
                    _state.update { it.copy(saving = false, error = error.message ?: "No se pudo guardar la entrega") }
                    _effects.send(ShiftHandoverEffect.Error(error.message ?: "No se pudo guardar"))
                },
            )
        }
    }
}
