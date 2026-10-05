package com.axzydev.checkapp.pages.schedules.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class ScheduleItemUi(val id: String, val name: String, val startTime: String, val endTime: String)

data class SchedulesUiState(
    val loading: Boolean = true,
    override val items: List<ScheduleItemUi> = emptyList(),
    override val query: String = "",
    val showForm: Boolean = false,
    val name: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val creating: Boolean = false,
    val editingId: String? = null,
    val editName: String = "",
    val editStart: String = "",
    val editEnd: String = "",
    val saving: Boolean = false,
    val pendingDeleteId: String? = null,
    val pendingDeleteName: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<ScheduleItemUi> {
    /** Se busca por nombre y por horario: "21:00" encuentra el turno de noche. */
    override val searchFields: List<(ScheduleItemUi) -> String?>
        get() = listOf({ it.name }, { it.startTime }, { it.endTime })
}

sealed interface SchedulesAction {
    data object Refresh : SchedulesAction
    data class Search(val value: String) : SchedulesAction
    data object ToggleForm : SchedulesAction
    data class Name(val value: String) : SchedulesAction
    data class StartTime(val value: String) : SchedulesAction
    data class EndTime(val value: String) : SchedulesAction
    data object Create : SchedulesAction
    /** Sólo el id: el view-model busca el resto en la lista, como en Clientes. */
    data class StartEdit(val id: String) : SchedulesAction
    data object CancelEdit : SchedulesAction
    data class EditName(val value: String) : SchedulesAction
    data class EditStart(val value: String) : SchedulesAction
    data class EditEnd(val value: String) : SchedulesAction
    data object SaveEdit : SchedulesAction
    data class RequestDelete(val id: String) : SchedulesAction
    data object CancelDelete : SchedulesAction
    data object ConfirmDelete : SchedulesAction
}

sealed interface SchedulesEffect {
    data class Error(val message: String) : SchedulesEffect
    data object Created : SchedulesEffect
    data object Deleted : SchedulesEffect
}
