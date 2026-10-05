package com.axzydev.checkapp.pages.sync.viewmodel

enum class SyncStepStatus { PENDING, ACTIVE, SUCCESS, ERROR }

enum class SyncRunStatus { SYNCING, SUCCESS, ERROR }

enum class ServerStatus { CHECKING, UPDATES_PENDING, UP_TO_DATE }

data class SyncStepUi(val id: Int, val label: String, val status: SyncStepStatus)

data class UploadProgress(val current: Int, val total: Int) {
    val fraction: Float get() = if (total <= 0) 0f else current.toFloat() / total.toFloat()
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
)

sealed interface SyncAction {
    data object Retry : SyncAction
}

sealed interface SyncEffect {
    data object Completed : SyncEffect
}
