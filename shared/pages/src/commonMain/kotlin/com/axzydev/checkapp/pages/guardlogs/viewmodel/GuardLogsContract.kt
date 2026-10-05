package com.axzydev.checkapp.pages.guardlogs.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class GuardLogItemUi(
    val id: String,
    val guardName: String,
    val username: String,
    val loginAt: Long,
    val logoutAt: Long?,
    val durationMinutes: Long?,
)

data class GuardLogsUiState(
    val loading: Boolean = true,
    override val items: List<GuardLogItemUi> = emptyList(),
    override val query: String = "",
    val error: String? = null,
) : QueryableListState<GuardLogItemUi> {
    /** Se busca por guardia y por nombre de usuario. */
    override val searchFields: List<(GuardLogItemUi) -> String?>
        get() = listOf({ it.guardName }, { it.username })
}

sealed interface GuardLogsAction {
    data object Refresh : GuardLogsAction
    data class Search(val value: String) : GuardLogsAction
}
