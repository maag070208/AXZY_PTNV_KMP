package com.axzydev.checkapp.pages.guards.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class GuardItemUi(val id: String, val name: String, val username: String)

data class GuardsUiState(
    val loading: Boolean = true,
    override val items: List<GuardItemUi> = emptyList(),
    override val query: String = "",
    val pendingDeleteId: String? = null,
    val pendingDeleteName: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<GuardItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(GuardItemUi) -> String?>
        get() = listOf(
        { it.name },
        { it.username },
        )
}

sealed interface GuardsAction {
    data object Refresh : GuardsAction
    data class Search(val value: String) : GuardsAction
    data class RequestDelete(val id: String) : GuardsAction
    data object CancelDelete : GuardsAction
    data object ConfirmDelete : GuardsAction
}

sealed interface GuardsEffect {
    data object Deleted : GuardsEffect
    data class Error(val message: String) : GuardsEffect
}
