package com.axzydev.checkapp.pages.recurringform.viewmodel

data class ClientOption(val id: String, val name: String)

data class ZoneOption(val id: String, val name: String)

data class LocationOption(
    val id: String,
    val name: String,
    val zoneId: String?,
    val clientId: String?,
)

data class GuardOption(val id: String, val name: String, val clientId: String?)

data class TaskUi(val description: String, val reqPhoto: Boolean)

data class PointUi(val locationId: String, val locationName: String, val tasks: List<TaskUi>)

val RecurringFormStepTitles: List<String> = listOf("Info", "Recorrido", "Asignación", "Resumen")

data class RecurringFormUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val isEditing: Boolean = false,
    val step: Int = 0,
    val title: String = "",
    val clients: List<ClientOption> = emptyList(),
    val selectedClientId: String? = null,
    val zones: List<ZoneOption> = emptyList(),
    val selectedZoneId: String? = null,
    val locations: List<LocationOption> = emptyList(),
    val points: List<PointUi> = emptyList(),
    val guards: List<GuardOption> = emptyList(),
    val selectedGuardIds: Set<String> = emptySet(),
    val error: String? = null,
) {
    val clientName: String?
        get() = clients.firstOrNull { it.id == selectedClientId }?.name

    val selectedGuards: List<GuardOption>
        get() = guards.filter { it.id in selectedGuardIds }

    /** Guardias del cliente seleccionado (o todos si aún no hay cliente). */
    val availableGuards: List<GuardOption>
        get() = guards.filter { selectedClientId == null || it.clientId == selectedClientId }

    /** Ubicaciones del cliente que todavía no están en la hoja de ruta. */
    val availableLocations: List<LocationOption>
        get() = locations.filter { location ->
            location.clientId == selectedClientId && points.none { it.locationId == location.id }
        }
}

sealed interface RecurringFormAction {
    data class Title(val value: String) : RecurringFormAction
    data class SelectClient(val clientId: String) : RecurringFormAction
    data class SelectZone(val zoneId: String?) : RecurringFormAction
    data class AddLocation(val locationId: String) : RecurringFormAction
    data object AddAllFromZone : RecurringFormAction
    data class RemovePoint(val index: Int) : RecurringFormAction
    data class AddTask(val pointIndex: Int) : RecurringFormAction
    data class TaskDescription(val pointIndex: Int, val taskIndex: Int, val value: String) : RecurringFormAction
    data class ToggleTaskPhoto(val pointIndex: Int, val taskIndex: Int) : RecurringFormAction
    data class RemoveTask(val pointIndex: Int, val taskIndex: Int) : RecurringFormAction
    data class ToggleGuard(val guardId: String) : RecurringFormAction
    data object SelectAllGuards : RecurringFormAction
    data object ClearGuards : RecurringFormAction
    data object Next : RecurringFormAction
    data object Back : RecurringFormAction
    data object Submit : RecurringFormAction
    data object DismissError : RecurringFormAction
}

sealed interface RecurringFormEffect {
    data object Saved : RecurringFormEffect
    data class Error(val message: String) : RecurringFormEffect
}
