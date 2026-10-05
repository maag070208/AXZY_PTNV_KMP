package com.axzydev.checkapp.pages.incidents.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class IncidentItemUi(
    val id: String,
    val title: String,
    val guardName: String?,
    val categoryName: String?,
    val status: String,
    val createdAt: Long,
)

data class IncidentsUiState(
    val loading: Boolean = true,
    override val items: List<IncidentItemUi> = emptyList(),
    override val query: String = "",
    val resolvingId: String? = null,
    val pendingDeleteId: String? = null,
    val pendingDeleteTitle: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<IncidentItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(IncidentItemUi) -> String?>
        get() = listOf(
        { it.title },
        { it.guardName },
        { it.categoryName },
        )
}

sealed interface IncidentsAction {
    data object Refresh : IncidentsAction
    data class Search(val value: String) : IncidentsAction
    data class Resolve(val id: String) : IncidentsAction
    data class RequestDelete(val id: String) : IncidentsAction
    data object CancelDelete : IncidentsAction
    data object ConfirmDelete : IncidentsAction
}

sealed interface IncidentsEffect {
    data class Error(val message: String) : IncidentsEffect
    data object Resolved : IncidentsEffect
}
