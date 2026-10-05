package com.axzydev.checkapp.pages.schedules.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.schedule.model.ScheduleDraft
import com.axzydev.checkapp.features.crudschedule.model.CreateScheduleUseCase
import com.axzydev.checkapp.features.crudschedule.model.DeleteScheduleUseCase
import com.axzydev.checkapp.features.crudschedule.model.ListSchedulesUseCase
import com.axzydev.checkapp.features.crudschedule.model.UpdateScheduleUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SchedulesViewModel(
    private val listSchedules: ListSchedulesUseCase,
    private val createSchedule: CreateScheduleUseCase,
    private val updateSchedule: UpdateScheduleUseCase,
    private val deleteSchedule: DeleteScheduleUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SchedulesUiState())
    val state: StateFlow<SchedulesUiState> = _state.asStateFlow()

    private val _effects = Channel<SchedulesEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: SchedulesAction) {
        when (action) {
            SchedulesAction.Refresh -> refresh()
            is SchedulesAction.Search -> _state.update { it.copy(query = action.value) }
            SchedulesAction.ToggleForm -> _state.update { it.copy(showForm = !it.showForm, error = null) }
            is SchedulesAction.Name -> _state.update { it.copy(name = action.value) }
            is SchedulesAction.StartTime -> _state.update { it.copy(startTime = action.value) }
            is SchedulesAction.EndTime -> _state.update { it.copy(endTime = action.value) }
            SchedulesAction.Create -> create()
            is SchedulesAction.StartEdit -> startEdit(action.id)

            SchedulesAction.CancelEdit -> _state.update { it.copy(editingId = null, editName = "", editStart = "", editEnd = "") }
            is SchedulesAction.EditName -> _state.update { it.copy(editName = action.value) }
            is SchedulesAction.EditStart -> _state.update { it.copy(editStart = action.value) }
            is SchedulesAction.EditEnd -> _state.update { it.copy(editEnd = action.value) }
            SchedulesAction.SaveEdit -> saveEdit()
            is SchedulesAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteName = current.items.firstOrNull { it.id == action.id }?.name,
                )
            }

            SchedulesAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteName = null) }
            SchedulesAction.ConfirmDelete -> confirmDelete()
        }
    }

    /**
     * Busca el horario en la lista ya cargada.
     *
     * La acción sólo lleva el id para que la pantalla no tenga que reconstruir el
     * item; se ignora si ya no existe (por ejemplo si se borró desde otro sitio).
     */
    private fun startEdit(id: String) {
        val schedule = _state.value.items.firstOrNull { it.id == id } ?: return
        _state.update {
            it.copy(
                editingId = id,
                editName = schedule.name,
                editStart = schedule.startTime,
                editEnd = schedule.endTime,
                error = null,
            )
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listSchedules() }.fold(
            onSuccess = { schedules ->
                _state.update {
                    it.copy(loading = false, items = schedules.map { s -> ScheduleItemUi(s.id, s.name, s.startTime, s.endTime) })
                }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar los horarios") }
            },
        )
    }

    private fun create() {
        val current = _state.value
        if (current.name.isBlank() || current.startTime.isBlank() || current.endTime.isBlank()) {
            _state.update { it.copy(error = "Nombre, hora de inicio y hora de fin son obligatorios") }
            return
        }
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val draft = ScheduleDraft(current.name.trim(), current.startTime.trim(), current.endTime.trim())
            when (val result = createSchedule(draft)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(creating = false, showForm = false, name = "", startTime = "", endTime = "") }
                    _effects.send(SchedulesEffect.Created)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(creating = false, error = result.firstMessage) }
                    _effects.send(SchedulesEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun saveEdit() {
        val current = _state.value
        val id = current.editingId ?: return
        if (current.editName.isBlank() || current.editStart.isBlank() || current.editEnd.isBlank()) {
            _state.update { it.copy(error = "Nombre, hora de inicio y hora de fin son obligatorios") }
            return
        }
        _state.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val draft = ScheduleDraft(current.editName.trim(), current.editStart.trim(), current.editEnd.trim())
            when (val result = updateSchedule(id, draft)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false, editingId = null, editName = "", editStart = "", editEnd = "") }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(saving = false, error = result.firstMessage) }
            }
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteSchedule(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteName = null) }
                    _effects.send(SchedulesEffect.Deleted)
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
