package com.axzydev.checkapp.pages.sync.viewmodel

import com.axzydev.checkapp.core.sync.syncActionLabel
import com.axzydev.checkapp.core.sync.syncShortId
import com.axzydev.checkapp.core.sync.syncTableLabel

enum class SyncStepStatus { PENDING, ACTIVE, SUCCESS, ERROR }

enum class SyncRunStatus { SYNCING, SUCCESS, ERROR }

enum class ServerStatus { CHECKING, UPDATES_PENDING, UP_TO_DATE }

data class SyncStepUi(val id: Int, val label: String, val status: SyncStepStatus)

data class UploadProgress(val current: Int, val total: Int) {
    val fraction: Float get() = if (total <= 0) 0f else current.toFloat() / total.toFloat()
}

/**
 * Un registro que el servidor rechazó.
 *
 * Existe porque el rechazo **no es un fallo de red**: reintentar tal cual da el
 * mismo resultado. El registro sigue guardado en el celular, así que lo que
 * necesita el guardia es saber *cuál* es y *por qué*, para corregirlo.
 */
data class SyncRejectionUi(
    val table: String,
    val id: String,
    val action: String,
    val reason: String,
) {
    /** "Novedades" en vez de `incidents`. */
    val tableLabel: String get() = syncTableLabel(table)

    /** "Editado" en vez de `update`. */
    val actionLabel: String get() = syncActionLabel(action)

    /** Id recortado: completo no cabe en una fila. */
    val shortId: String get() = syncShortId(id)
}

fun defaultSyncSteps(): List<SyncStepUi> = listOf(
    SyncStepUi(1, "Validando actualizaciones", SyncStepStatus.PENDING),
    SyncStepUi(2, "Descargando cambios del servidor", SyncStepStatus.PENDING),
    SyncStepUi(3, "Subiendo cambios locales", SyncStepStatus.PENDING),
)

data class SyncUiState(
    val status: SyncRunStatus = SyncRunStatus.SYNCING,
    val steps: List<SyncStepUi> = defaultSyncSteps(),
    val upload: UploadProgress? = null,
    val serverStatus: ServerStatus = ServerStatus.CHECKING,
    val error: String? = null,
    /**
     * Registros que el servidor rechazó por su contenido. Vacío salvo en ese caso:
     * un fallo de red no rechaza nada.
     */
    val rejections: List<SyncRejectionUi> = emptyList(),
)

sealed interface SyncAction {
    data object Retry : SyncAction
}

sealed interface SyncEffect {
    data object Completed : SyncEffect
}
