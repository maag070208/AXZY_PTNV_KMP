package com.axzydev.checkapp.core.sync

import kotlinx.serialization.json.JsonObject

/** Registro local pendiente de subir, ya convertido a campos de API (camelCase). */
data class PendingRecord(
    val id: String,
    /** `created` | `updated` | `deleted`. */
    val status: String,
    val data: JsonObject,
)

/**
 * Acceso genérico al SQLite local para el motor de sync.
 * La implementación real usa SQL crudo sobre el `SqlDriver` de SQLDelight
 * (el esquema es genérico por naturaleza).
 */
interface SyncLocalStore {
    suspend fun readPending(table: String): List<PendingRecord>
    suspend fun applyChanges(apiModel: String, changeSet: SyncChangeSetDto)
    suspend fun updateMediaJson(table: String, id: String, mediaJson: String)
    suspend fun markSynced(table: String, ids: List<String>)
    suspend fun hardDelete(table: String, id: String)
    suspend fun countUnsynced(): Int
    suspend fun emptyTables(tables: List<String>): List<String>
    suspend fun clearAll()
}

/** Lectura de bytes de un archivo local (multiplataforma vía Okio). */
interface FileBytesReader {
    suspend fun read(uri: String): ByteArray?
}
