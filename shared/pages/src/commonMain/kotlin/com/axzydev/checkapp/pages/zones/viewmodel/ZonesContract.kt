package com.axzydev.checkapp.pages.zones.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class ZoneItemUi(val id: String, val name: String, val active: Boolean)

data class ZonesUiState(
    val loading: Boolean = true,
    override val items: List<ZoneItemUi> = emptyList(),
    override val query: String = "",
    val showForm: Boolean = false,
    val name: String = "",
    val creating: Boolean = false,
    val editingId: String? = null,
    val editName: String = "",
    val saving: Boolean = false,
    val pendingDeleteId: String? = null,
    val pendingDeleteName: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<ZoneItemUi> {
    /** Las zonas sólo tienen nombre, así que se busca por ahí. */
    override val searchFields: List<(ZoneItemUi) -> String?>
        get() = listOf({ it.name })
}

sealed interface ZonesAction {
    data object Refresh : ZonesAction
    data class Search(val value: String) : ZonesAction
    data object ToggleForm : ZonesAction
    data class Name(val value: String) : ZonesAction
    data object Create : ZonesAction
    data class StartEdit(val id: String) : ZonesAction
    data object CancelEdit : ZonesAction
    data class EditName(val value: String) : ZonesAction
    data object SaveEdit : ZonesAction
    data class RequestDelete(val id: String) : ZonesAction
    data object CancelDelete : ZonesAction
    data object ConfirmDelete : ZonesAction
}

sealed interface ZonesEffect {
    data class Error(val message: String) : ZonesEffect
    data object Created : ZonesEffect
    data object Deleted : ZonesEffect
}
