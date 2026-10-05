package com.axzydev.checkapp.pages.sendnotification.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.notification.model.NotificationDraft
import com.axzydev.checkapp.features.notifications.model.SendNotificationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Envía una notificación ahora (persistente = queda en la bandeja del destinatario). */
class SendNotificationViewModel(
    private val sendNotification: SendNotificationUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SendNotificationUiState())
    val state: StateFlow<SendNotificationUiState> = _state.asStateFlow()

    fun onAction(action: SendNotificationAction) {
        when (action) {
            is SendNotificationAction.Title -> _state.update { it.copy(title = action.value, messageOk = null, error = null) }
            is SendNotificationAction.Message -> _state.update { it.copy(message = action.value, messageOk = null, error = null) }
            is SendNotificationAction.Type -> _state.update { it.copy(type = action.value) }
            SendNotificationAction.TogglePersistent -> _state.update { it.copy(persistent = !it.persistent) }
            is SendNotificationAction.UserId -> _state.update { it.copy(userId = action.value) }
            SendNotificationAction.Send -> send()
            SendNotificationAction.Dismiss -> _state.update { it.copy(messageOk = null, error = null) }
        }
    }

    private fun send() {
        val current = _state.value
        if (current.message.trim().isBlank()) {
            _state.update { it.copy(error = "El mensaje es obligatorio") }
            return
        }
        _state.update { it.copy(sending = true, error = null, messageOk = null) }
        viewModelScope.launch {
            val draft = NotificationDraft(
                message = current.message.trim(),
                title = current.title.trim().ifBlank { null },
                type = current.type,
                userId = current.userId.trim().ifBlank { null },
                persistent = current.persistent,
            )
            when (val result = sendNotification(draft)) {
                is ApiResult.Success -> _state.update {
                    it.copy(sending = false, title = "", message = "", messageOk = "Notificación enviada")
                }

                is ApiResult.Failure -> _state.update { it.copy(sending = false, error = result.firstMessage) }
            }
        }
    }
}
