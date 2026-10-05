package com.axzydev.checkapp.pages.maintenance.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class MaintenanceItemUi(
    val id: String,
    val title: String,
    val guardName: String?,
    val categoryName: String?,
    val status: String,
    val createdAt: Long,
)

data class MaintenanceUiState(
    val loading: Boolean = true,
    override val items: List<MaintenanceItemUi> = emptyList(),
    override val query: String = "",
    val resolvingId: String? = null,
    val pendingDeleteId: String? = null,
    val pendingDeleteTitle: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<MaintenanceItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(MaintenanceItemUi) -> String?>
        get() = listOf(
        { it.title },
        { it.guardName },
        { it.categoryName },
        )
}

sealed interface MaintenanceAction {
    data object Refresh : MaintenanceAction
    data class Search(val value: String) : MaintenanceAction
    data class Resolve(val id: String) : MaintenanceAction
    data class RequestDelete(val id: String) : MaintenanceAction
    data object CancelDelete : MaintenanceAction
    data object ConfirmDelete : MaintenanceAction
}

sealed interface MaintenanceEffect {
    data class Error(val message: String) : MaintenanceEffect
    data object Resolved : MaintenanceEffect
    data object Deleted : MaintenanceEffect
}
