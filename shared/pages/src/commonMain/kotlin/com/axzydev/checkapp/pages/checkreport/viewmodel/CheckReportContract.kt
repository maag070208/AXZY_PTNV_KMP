package com.axzydev.checkapp.pages.checkreport.viewmodel

import com.axzydev.checkapp.platform.location.Coordinates

data class CheckTask(val id: String, val description: String, val done: Boolean)

/** Evidencia multimedia capturada en el punto (aún local, pendiente de subir). */
data class EvidenceItem(val id: String, val uri: String, val isVideo: Boolean)

data class CheckReportUiState(
    val loading: Boolean = true,
    val locationName: String = "",
    val tasks: List<CheckTask> = emptyList(),
    val notes: String = "",
    val media: List<EvidenceItem> = emptyList(),
    val submitting: Boolean = false,
    val error: String? = null,
    /**
     * Posición del aparato al abrir el punto. Se guarda con la marcación: es la
     * constancia de que el guardia estuvo ahí, y lo único que no se puede recuperar
     * después.
     */
    val location: Coordinates? = null,
    /** Se está buscando la posición: mientras tanto la línea dice qué pasa. */
    val locating: Boolean = true,
)

sealed interface CheckReportAction {
    data class ToggleTask(val id: String) : CheckReportAction
    data class Notes(val value: String) : CheckReportAction
    data class AddMedia(val uri: String, val isVideo: Boolean) : CheckReportAction
    data class RemoveMedia(val id: String) : CheckReportAction
    data class MediaError(val message: String) : CheckReportAction
    data class LocationResolved(val coordinates: Coordinates?) : CheckReportAction
    data object Submit : CheckReportAction
}

sealed interface CheckReportEffect {
    data object Submitted : CheckReportEffect
    data class Error(val message: String) : CheckReportEffect
}
