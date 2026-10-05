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
