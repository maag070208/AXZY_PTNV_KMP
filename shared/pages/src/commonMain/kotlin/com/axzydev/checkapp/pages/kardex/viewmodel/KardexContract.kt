package com.axzydev.checkapp.pages.kardex.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class KardexItemUi(
    val id: String,
    val locationName: String,
    val timestamp: Long,
    val notes: String?,
    val mediaCount: Int,
)

data class KardexUiState(
    val loading: Boolean = true,
    override val items: List<KardexItemUi> = emptyList(),
    override val query: String = "",
    val error: String? = null,
) : QueryableListState<KardexItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(KardexItemUi) -> String?>
        get() = listOf(
        { it.locationName },
        { it.notes },
        )
}

sealed interface KardexAction {
    data object Refresh : KardexAction
    data class Search(val value: String) : KardexAction
}
