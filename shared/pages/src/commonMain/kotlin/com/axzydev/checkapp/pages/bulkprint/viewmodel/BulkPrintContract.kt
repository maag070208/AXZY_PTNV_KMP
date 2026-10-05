package com.axzydev.checkapp.pages.bulkprint.viewmodel

data class Option(val id: String, val label: String)

data class PrintableLocationUi(
    val id: String,
    val name: String,
    val clientId: String?,
    val clientName: String?,
)

data class QrSheetItem(val id: String, val name: String, val payload: String)

data class BulkPrintUiState(
    val loading: Boolean = true,
    val clients: List<Option> = emptyList(),
    val selectedClientId: String? = null,
    val locations: List<PrintableLocationUi> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val sheet: List<QrSheetItem> = emptyList(),
    val error: String? = null,
) {
    val visibleLocations: List<PrintableLocationUi>
        get() = locations.filter { selectedClientId == null || it.clientId == selectedClientId }
}

sealed interface BulkPrintAction {
    data object Refresh : BulkPrintAction
    data class SelectClient(val clientId: String?) : BulkPrintAction
    data class Toggle(val id: String) : BulkPrintAction
    data object SelectAll : BulkPrintAction
    data object Clear : BulkPrintAction
    data object Generate : BulkPrintAction
    data object ClearSheet : BulkPrintAction
}
