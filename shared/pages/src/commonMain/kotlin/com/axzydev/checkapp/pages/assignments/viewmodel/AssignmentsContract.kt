package com.axzydev.checkapp.pages.assignments.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class AssignmentItemUi(
    val id: String,
    val guardName: String?,
    val locationName: String?,
    val status: String,
)

data class Option(val id: String, val label: String)

val AssignmentStatuses: List<String> = listOf("PENDING", "CHECKING", "UNDER_REVIEW", "REVIEWED", "ANOMALY")

data class AssignmentsUiState(
    val loading: Boolean = true,
    override val items: List<AssignmentItemUi> = emptyList(),
    override val query: String = "",
    val guards: List<Option> = emptyList(),
    val locations: List<Option> = emptyList(),
    val showForm: Boolean = false,
    val selectedGuardId: String? = null,
    val selectedLocationId: String? = null,
    val notes: String = "",
    val creating: Boolean = false,
    val statusUpdatingId: String? = null,
    val pendingDeleteId: String? = null,
    val pendingDeleteName: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<AssignmentItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(AssignmentItemUi) -> String?>
        get() = listOf(
        { it.guardName },
        { it.locationName },
        )
}

sealed interface AssignmentsAction {
    data object Refresh : AssignmentsAction
    data class Search(val value: String) : AssignmentsAction
    data object ToggleForm : AssignmentsAction
    data class SelectGuard(val guardId: String) : AssignmentsAction
    data class SelectLocation(val locationId: String) : AssignmentsAction
    data class Notes(val value: String) : AssignmentsAction
    data object Create : AssignmentsAction
    data class AdvanceStatus(val id: String, val currentStatus: String) : AssignmentsAction
    data class RequestDelete(val id: String) : AssignmentsAction
    data object CancelDelete : AssignmentsAction
    data object ConfirmDelete : AssignmentsAction
}

sealed interface AssignmentsEffect {
    data class Error(val message: String) : AssignmentsEffect
    data object Created : AssignmentsEffect
    data object Updated : AssignmentsEffect
    data object Deleted : AssignmentsEffect
}
