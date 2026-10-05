package com.axzydev.checkapp.pages.schedulednotifications.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class ScheduledItemUi(
    val id: String,
    val title: String?,
    val message: String,
    val type: String,
    val frequency: String,
    val timeOfDay: String?,
    val active: Boolean,
    val sendCount: Int,
    val nextSendAt: Long?,
    val targetUserName: String?,
)

data class ScheduledUiState(
    val loading: Boolean = true,
    override val items: List<ScheduledItemUi> = emptyList(),
    override val query: String = "",
    val showForm: Boolean = false,
    val title: String = "",
    val message: String = "",
    val type: String = "info",
    val frequency: String = "ONCE",
    val timeOfDay: String = "",
    val scheduledAt: String = "",
    val persistent: Boolean = false,
    val creating: Boolean = false,
    val togglingId: String? = null,
    val pendingDeleteId: String? = null,
    val pendingDeleteTitle: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<ScheduledItemUi> {
    override val searchFields: List<(ScheduledItemUi) -> String?>
        get() = listOf({ it.title }, { it.message }, { it.targetUserName })
}

sealed interface ScheduledAction {
    data object Refresh : ScheduledAction
    data class Search(val value: String) : ScheduledAction
    data object ToggleForm : ScheduledAction
    data class Title(val value: String) : ScheduledAction
    data class Message(val value: String) : ScheduledAction
    data class Type(val value: String) : ScheduledAction
    data class Frequency(val value: String) : ScheduledAction
    data class TimeOfDay(val value: String) : ScheduledAction
    data class ScheduledAt(val value: String) : ScheduledAction
    data object TogglePersistent : ScheduledAction
    data object Create : ScheduledAction
    data class ToggleActive(val id: String, val active: Boolean) : ScheduledAction
    data class RequestDelete(val id: String) : ScheduledAction
    data object CancelDelete : ScheduledAction
    data object ConfirmDelete : ScheduledAction
}
