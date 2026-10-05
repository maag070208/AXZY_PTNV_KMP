package com.axzydev.checkapp.pages.clients.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class ClientItemUi(
    val id: String,
    val name: String,
    val address: String?,
    val rfc: String?,
    val contactName: String?,
    val contactPhone: String?,
    val active: Boolean,
)

data class ClientsUiState(
    val loading: Boolean = true,
    override val items: List<ClientItemUi> = emptyList(),
    /** Filtro de búsqueda. Se aplica sobre lo ya cargado, sin ir al servidor. */
    override val query: String = "",
    val showForm: Boolean = false,
    val name: String = "",
    val address: String = "",
    val rfc: String = "",
    val contactName: String = "",
    val contactPhone: String = "",
    val creating: Boolean = false,
    val editingId: String? = null,
    val editName: String = "",
    val editAddress: String = "",
    val editRfc: String = "",
    val editContactName: String = "",
    val editContactPhone: String = "",
    val saving: Boolean = false,
    val pendingDeleteId: String? = null,
    val pendingDeleteName: String? = null,
    val deleting: Boolean = false,
    val error: String? = null,
) : QueryableListState<ClientItemUi> {
    /** Se busca por nombre, RFC y contacto: los tres datos de la tarjeta. */
    override val searchFields: List<(ClientItemUi) -> String?>
        get() = listOf({ it.name }, { it.rfc }, { it.contactName })
}

sealed interface ClientsAction {
    data object Refresh : ClientsAction
    data class Search(val value: String) : ClientsAction
    data object ToggleForm : ClientsAction
    data class Name(val value: String) : ClientsAction
    data class Address(val value: String) : ClientsAction
    data class Rfc(val value: String) : ClientsAction
    data class ContactName(val value: String) : ClientsAction
    data class ContactPhone(val value: String) : ClientsAction
    data object Create : ClientsAction
    data class StartEdit(val id: String) : ClientsAction
    data object CancelEdit : ClientsAction
    data class EditName(val value: String) : ClientsAction
    data class EditAddress(val value: String) : ClientsAction
    data class EditRfc(val value: String) : ClientsAction
    data class EditContactName(val value: String) : ClientsAction
    data class EditContactPhone(val value: String) : ClientsAction
    data object SaveEdit : ClientsAction
    data class RequestDelete(val id: String) : ClientsAction
    data object CancelDelete : ClientsAction
    data object ConfirmDelete : ClientsAction
}

sealed interface ClientsEffect {
    data class Error(val message: String) : ClientsEffect
    data object Created : ClientsEffect
    data object Deleted : ClientsEffect
}
