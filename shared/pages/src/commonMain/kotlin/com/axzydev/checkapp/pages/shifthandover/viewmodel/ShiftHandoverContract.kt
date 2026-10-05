package com.axzydev.checkapp.pages.shifthandover.viewmodel

data class ChecklistItemUi(val key: String, val label: String, val ok: Boolean)
data class Option(val id: String, val label: String)

data class ShiftHandoverUiState(
    val loading: Boolean = true,
    val clients: List<Option> = emptyList(),
    val schedules: List<Option> = emptyList(),
    val selectedClientId: String? = null,
    val selectedScheduleId: String? = null,
    val credentials: String = "",
    val tarjetones: String = "",
    val novedades: String = "",
    val items: List<ChecklistItemUi> = emptyList(),
    val reportedToAdmin: Boolean = false,
    val saving: Boolean = false,
    val catalogMissing: Boolean = false,
    val savedCount: Int = 0,
    val error: String? = null,
)

sealed interface ShiftHandoverAction {
    data class SelectClient(val clientId: String) : ShiftHandoverAction
    data class SelectSchedule(val scheduleId: String) : ShiftHandoverAction
    data class Credentials(val value: String) : ShiftHandoverAction
    data class Tarjetones(val value: String) : ShiftHandoverAction
    data class Novedades(val value: String) : ShiftHandoverAction
    data class Toggle(val key: String, val ok: Boolean) : ShiftHandoverAction
    data class SetReported(val value: Boolean) : ShiftHandoverAction
    data object Save : ShiftHandoverAction
}

sealed interface ShiftHandoverEffect {
    data class Error(val message: String) : ShiftHandoverEffect
    data object Saved : ShiftHandoverEffect
}
