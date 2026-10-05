package com.axzydev.checkapp.pages.recurring.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class RecurringItemUi(val id: String, val title: String, val pointCount: Int)

data class RecurringUiState(
    val loading: Boolean = true,
    override val items: List<RecurringItemUi> = emptyList(),
    override val query: String = "",
    val pendingDeleteId: String? = null,
    val pendingDeleteTitle: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<RecurringItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(RecurringItemUi) -> String?>
        get() = listOf(
        { it.title },
        )
}

sealed interface RecurringAction {
    data object Refresh : RecurringAction
    data class Search(val value: String) : RecurringAction
    data class RequestDelete(val id: String) : RecurringAction
    data object CancelDelete : RecurringAction
    data object ConfirmDelete : RecurringAction
}

sealed interface RecurringEffect {
    data object Deleted : RecurringEffect
    data class Error(val message: String) : RecurringEffect
}
