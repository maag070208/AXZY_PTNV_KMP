package com.axzydev.checkapp.pages.guarddiscipline.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class DisciplineItemUi(
    val id: String,
    val guardName: String?,
    val categoryName: String?,
    val typeName: String?,
    val description: String?,
    val status: String,
    val createdAt: Long,
)

data class GuardDisciplineUiState(
    val loading: Boolean = true,
    override val items: List<DisciplineItemUi> = emptyList(),
    override val query: String = "",
    val resolvingId: String? = null,
    val error: String? = null,
) : QueryableListState<DisciplineItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(DisciplineItemUi) -> String?>
        get() = listOf(
        { it.guardName },
        { it.categoryName },
        { it.description },
        )
}

sealed interface GuardDisciplineAction {
    data object Refresh : GuardDisciplineAction
    data class Search(val value: String) : GuardDisciplineAction
    data class Resolve(val id: String) : GuardDisciplineAction
}

sealed interface GuardDisciplineEffect {
    data class Error(val message: String) : GuardDisciplineEffect
    data object Resolved : GuardDisciplineEffect
}
