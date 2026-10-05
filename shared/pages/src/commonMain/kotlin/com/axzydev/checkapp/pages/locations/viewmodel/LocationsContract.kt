package com.axzydev.checkapp.pages.locations.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class LocationItemUi(
    val id: String,
    val name: String,
    val clientId: String?,
    val clientName: String?,
    val reference: String?,
)

data class Option(val id: String, val label: String)

data class LocationsUiState(
    val loading: Boolean = true,
    override val items: List<LocationItemUi> = emptyList(),
    override val query: String = "",
    val clients: List<Option> = emptyList(),
    val showForm: Boolean = false,
    val selectedClientId: String? = null,
    val name: String = "",
    val reference: String = "",
    val creating: Boolean = false,
    val editingId: String? = null,
    val editName: String = "",
    val editReference: String = "",
    val saving: Boolean = false,
    val pendingDeleteId: String? = null,
    val pendingDeleteName: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<LocationItemUi> {
    /** Se busca por nombre, referencia y cliente: los tres datos de la tarjeta. */
    override val searchFields: List<(LocationItemUi) -> String?>
        get() = listOf({ it.name }, { it.reference }, { it.clientName })
}

sealed interface LocationsAction {
    data object Refresh : LocationsAction
    data class Search(val value: String) : LocationsAction
    data object ToggleForm : LocationsAction
    data class SelectClient(val clientId: String) : LocationsAction
    data class Name(val value: String) : LocationsAction
    data class Reference(val value: String) : LocationsAction
    data object Create : LocationsAction
    data class StartEdit(val id: String) : LocationsAction
    data object CancelEdit : LocationsAction
    data class EditName(val value: String) : LocationsAction
    data class EditReference(val value: String) : LocationsAction
    data object SaveEdit : LocationsAction
    data class RequestDelete(val id: String) : LocationsAction
    data object CancelDelete : LocationsAction
    data object ConfirmDelete : LocationsAction
}

sealed interface LocationsEffect {
    data class Error(val message: String) : LocationsEffect
    data object Created : LocationsEffect
}
