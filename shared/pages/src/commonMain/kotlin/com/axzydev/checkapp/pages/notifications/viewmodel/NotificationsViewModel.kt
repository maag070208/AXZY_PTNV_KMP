package com.axzydev.checkapp.pages.notifications.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.features.notifications.model.ListNotificationsUseCase
import com.axzydev.checkapp.features.notifications.model.MarkAllNotificationsReadUseCase
import com.axzydev.checkapp.features.notifications.model.MarkNotificationReadUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val listNotifications: ListNotificationsUseCase,
    private val markRead: MarkNotificationReadUseCase,
    private val markAllRead: MarkAllNotificationsReadUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private val _effects = Channel<NotificationsEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun refresh() {
        viewModelScope.launch { load() }
    }

    fun onAction(action: NotificationsAction) {
        when (action) {
            NotificationsAction.Refresh -> refresh()
            is NotificationsAction.Search -> _state.update { it.copy(query = action.value) }
            NotificationsAction.ToggleUnreadOnly -> {
                _state.update { it.copy(unreadOnly = !it.unreadOnly) }
                refresh()
            }

            is NotificationsAction.MarkRead -> markOne(action.id)
            NotificationsAction.MarkAllRead -> markAll()
        }
    }

    private suspend fun load() {
        _state.update { it.copy(loading = true, error = null) }
        runCatching { listNotifications(_state.value.unreadOnly) }.fold(
            onSuccess = { result ->
                when (result) {
                    is ApiResult.Success -> _state.update {
                        it.copy(
                            loading = false,
                            unreadCount = result.data.unreadCount,
                            items = result.data.notifications.map { n ->
                                NotificationItemUi(n.id, n.title, n.message, n.type, n.read, n.createdAt)
                            },
                        )
                    }

                    is ApiResult.Failure -> _state.update { it.copy(loading = false, error = result.firstMessage) }
                }
            },
            onFailure = { error ->
                _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar las notificaciones") }
            },
        )
    }

    private fun markOne(id: String) {
        _state.update { it.copy(markingId = id, error = null) }
        viewModelScope.launch {
            when (val result = markRead(id)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(markingId = null) }
                    _effects.send(NotificationsEffect.MarkedRead)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(markingId = null, error = result.firstMessage) }
                    _effects.send(NotificationsEffect.Error(result.firstMessage))
                }
            }
        }
    }

    private fun markAll() {
        _state.update { it.copy(markingAll = true, error = null) }
        viewModelScope.launch {
            when (val result = markAllRead()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(markingAll = false) }
                    _effects.send(NotificationsEffect.MarkedAllRead)
                    load()
                }

                is ApiResult.Failure -> {
                    _state.update { it.copy(markingAll = false, error = result.firstMessage) }
                    _effects.send(NotificationsEffect.Error(result.firstMessage))
                }
            }
        }
    }
}
