package com.axzydev.checkapp.pages.schedulednotifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.schedulednotification.model.ScheduledNotificationDraft
import com.axzydev.checkapp.features.notifications.model.CreateScheduledNotificationUseCase
import com.axzydev.checkapp.features.notifications.model.DeleteScheduledNotificationUseCase
import com.axzydev.checkapp.features.notifications.model.ListScheduledNotificationsUseCase
import com.axzydev.checkapp.features.notifications.model.ToggleScheduledNotificationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** CRUD de notificaciones programadas (recurrentes o de una sola vez). */
class ScheduledNotificationsViewModel(
    private val listScheduled: ListScheduledNotificationsUseCase,
    private val createScheduled: CreateScheduledNotificationUseCase,
    private val toggleScheduled: ToggleScheduledNotificationUseCase,
    private val deleteScheduled: DeleteScheduledNotificationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduledUiState())
    val state: StateFlow<ScheduledUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: ScheduledAction) {
        when (action) {
            ScheduledAction.Refresh -> refresh()
            is ScheduledAction.Search -> _state.update { it.copy(query = action.value) }
            ScheduledAction.ToggleForm -> _state.update { it.copy(showForm = !it.showForm, error = null) }
            is ScheduledAction.Title -> _state.update { it.copy(title = action.value) }
            is ScheduledAction.Message -> _state.update { it.copy(message = action.value) }
            is ScheduledAction.Type -> _state.update { it.copy(type = action.value) }
            is ScheduledAction.Frequency -> _state.update { it.copy(frequency = action.value) }
            is ScheduledAction.TimeOfDay -> _state.update { it.copy(timeOfDay = action.value) }
            is ScheduledAction.ScheduledAt -> _state.update { it.copy(scheduledAt = action.value) }
            ScheduledAction.TogglePersistent -> _state.update { it.copy(persistent = !it.persistent) }
            ScheduledAction.Create -> create()
            is ScheduledAction.ToggleActive -> toggle(action.id, action.active)
            is ScheduledAction.RequestDelete -> _state.update { current ->
                current.copy(
                    pendingDeleteId = action.id,
                    pendingDeleteTitle = current.items.firstOrNull { it.id == action.id }?.message,
                )
            }

            ScheduledAction.CancelDelete -> _state.update { it.copy(pendingDeleteId = null, pendingDeleteTitle = null) }
            ScheduledAction.ConfirmDelete -> confirmDelete()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        when (val result = listScheduled()) {
            is ApiResult.Success -> _state.update {
                it.copy(
                    loading = false,
                    items = result.data.map { s ->
                        ScheduledItemUi(
                            id = s.id,
                            title = s.title,
                            message = s.message,
                            type = s.type,
                            frequency = s.frequency,
                            timeOfDay = s.timeOfDay,
                            active = s.active,
                            sendCount = s.sendCount,
                            nextSendAt = s.nextSendAt,
                            targetUserName = s.targetUserName,
                        )
                    },
                )
            }

            is ApiResult.Failure -> _state.update { it.copy(loading = false, error = result.firstMessage) }
        }
    }

    private fun create() {
        val current = _state.value
        if (current.message.trim().isBlank()) {
            _state.update { it.copy(error = "El mensaje es obligatorio") }
            return
        }
        _state.update { it.copy(creating = true, error = null) }
        viewModelScope.launch {
            val draft = ScheduledNotificationDraft(
                message = current.message.trim(),
                title = current.title.trim().ifBlank { null },
                type = current.type,
                frequency = current.frequency,
                timeOfDay = current.timeOfDay.trim().ifBlank { null },
                scheduledAt = current.scheduledAt.trim().ifBlank { null },
                persistent = current.persistent,
            )
            when (val result = createScheduled(draft)) {
                is ApiResult.Success -> {
                    _state.update {
                        it.copy(creating = false, showForm = false, message = "", title = "", timeOfDay = "", scheduledAt = "")
                    }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(creating = false, error = result.firstMessage) }
            }
        }
    }

    private fun toggle(id: String, active: Boolean) {
        _state.update { it.copy(togglingId = id, error = null) }
        viewModelScope.launch {
            when (val result = toggleScheduled(id, !active)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(togglingId = null) }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(togglingId = null, error = result.firstMessage) }
            }
        }
    }

    private fun confirmDelete() {
        val id = _state.value.pendingDeleteId ?: return
        _state.update { it.copy(deleting = true, error = null) }
        viewModelScope.launch {
            when (val result = deleteScheduled(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(deleting = false, pendingDeleteId = null, pendingDeleteTitle = null) }
                    load()
                }

                is ApiResult.Failure -> _state.update { it.copy(deleting = false, error = result.firstMessage) }
            }
        }
    }
}
