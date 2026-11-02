package com.axzydev.checkapp.core.sync

import com.axzydev.checkapp.core.datastore.SyncCatalogs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/** Conjunto de cambios de un modelo, tal como lo envía/recibe `/sync`. */
@Serializable
data class SyncChangeSetDto(
    val created: List<JsonObject> = emptyList(),
    val updated: List<JsonObject> = emptyList(),
    val deleted: List<String> = emptyList(),
) {
    val size: Int get() = created.size + updated.size + deleted.size
}

/** Respuesta de `GET /sync`. */
@Serializable
data class SyncPullResponseDto(
    val timestamp: Long = 0,
    val changes: Map<String, SyncChangeSetDto> = emptyMap(),
    val catalogs: SyncCatalogs? = null,
)

/** Cuerpo de `POST /sync`. */
@Serializable
data class SyncPushBodyDto(
    val changes: Map<String, SyncChangeSetDto>,
    val lastPulledAt: Long,
)

/** Conteo por tabla de lo aplicado por el servidor. */
@Serializable
data class SyncTableCountDto(
    val created: Int = 0,
    val updated: Int = 0,
    val deleted: Int = 0,
)

/**
 * Registro que el servidor NO pudo aplicar, con el motivo exacto.
 *
 * El push es atómico: si hay rechazos no se aplica nada y el servidor responde
 * `400` con `data.rejected[]`. Antes ese detalle se perdía y la cola quedaba
 * atascada con un mensaje genérico, sin saber qué registro fallaba ni por qué.
 */
@Serializable
data class SyncRejectionDto(
    val table: String = "",
    val id: String = "",
    val action: String = "",
    val reason: String = "",
)

/** Respuesta de `POST /sync` cuando el lote SÍ se aplicó (200). */
@Serializable
data class SyncPushResponseDto(
    val applied: Boolean = true,
    val summary: Map<String, SyncTableCountDto> = emptyMap(),
    val ignoredTables: List<String> = emptyList(),
    val rejected: List<SyncRejectionDto> = emptyList(),
)

/** Respuesta de `GET /sync/check`. */
@Serializable
data class SyncCheckResponseDto(
    val hasChanges: Boolean = false,
    val timestamp: Long = 0,
)

/** Respuesta de `POST /uploads`. */
@Serializable
data class UploadResponseDto(
    val success: Boolean = false,
    val url: String? = null,
    val data: UploadDataDto? = null,
)

@Serializable
data class UploadDataDto(val url: String? = null)
