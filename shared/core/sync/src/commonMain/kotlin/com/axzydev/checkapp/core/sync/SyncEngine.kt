package com.axzydev.checkapp.core.sync

/**
 * Superficie pública del slice `core:sync`.
 *
 * Define el contrato del motor de sincronización offline-first (Anexo B).
 * La implementación completa llega en la Fase 2.
 */
data class ModelCount(val model: String, val count: Int)

sealed interface SyncStep {
    data object Pull : SyncStep
    data class PullDataReceived(val total: Int, val details: List<ModelCount>) : SyncStep
    data object Push : SyncStep
    data class MediaUploadStart(val total: Int) : SyncStep
    data class MediaUploadProgress(val current: Int, val total: Int, val table: String) : SyncStep
}

/** Tablas que el dispositivo puede escribir offline (el servidor solo acepta push de estas). */
val DEVICE_WRITABLE_TABLES: List<String> = listOf(
    "rounds",
    "kardex",
    "incidents",
    "maintenances",
    "shift_handovers",
    "uniform_checks",
)

/** Tablas que se re-descargan completas cuando están vacías localmente. */
val RESET_MODELS: List<String> = listOf(
    "role", "client", "zone", "user", "schedule", "location", "locationTask",
    "incidentCategory", "incidentType", "recurringConfiguration",
    "recurringLocation", "recurringTask",
)

interface SyncEngine {
    suspend fun sync(onStep: (SyncStep) -> Unit = {})
    suspend fun hasUnsyncedLocalChanges(): Boolean
    suspend fun hasPendingServerChanges(): Boolean
}

/** Implementación no-op para previews y pruebas que no requieren red. */
class NoopSyncEngine : SyncEngine {
    override suspend fun sync(onStep: (SyncStep) -> Unit) = Unit
    override suspend fun hasUnsyncedLocalChanges(): Boolean = false
    override suspend fun hasPendingServerChanges(): Boolean = false
}
