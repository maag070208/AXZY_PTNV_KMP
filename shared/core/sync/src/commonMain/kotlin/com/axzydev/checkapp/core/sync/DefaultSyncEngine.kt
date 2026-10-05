package com.axzydev.checkapp.core.sync

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.connectivity.ConnectivityObserver
import com.axzydev.checkapp.core.datastore.SettingsKeys
import com.axzydev.checkapp.core.datastore.SettingsStore
import com.axzydev.checkapp.core.datastore.SyncCatalogs
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/** Error de sincronización con el mensaje devuelto por la API. */
class SyncException(message: String) : Exception(message) {
    constructor(failure: ApiResult.Failure) : this(failure.firstMessage)
}

/**
 * Motor de sincronización offline-first (Anexo B):
 * PULL → cola de medios (reemplaza `file://` por URL) → PUSH → marcar `synced`.
 *
 * Es *single-flight*: si ya hay un sync en curso, el nuevo intento espera.
 */
class DefaultSyncEngine(
    private val api: SyncApi,
    private val local: SyncLocalStore,
    private val mediaUploader: MediaUploader,
    private val settings: SettingsStore,
) : SyncEngine {

    private val mutex = Mutex()

    override suspend fun sync(onStep: (SyncStep) -> Unit) {
        mutex.withLock { runSync(onStep) }
    }

    override suspend fun hasUnsyncedLocalChanges(): Boolean = local.countUnsynced() > 0

    override suspend fun hasPendingServerChanges(): Boolean {
        val lastPulledAt = settings.getLong(SettingsKeys.LAST_SYNC_TIMESTAMP) ?: 0L
        return when (val result = api.hasPendingChanges(lastPulledAt)) {
            is ApiResult.Success -> result.data.hasChanges
            is ApiResult.Failure -> false
        }
    }

    private suspend fun runSync(onStep: (SyncStep) -> Unit) {
        val lastPulledAt = settings.getLong(SettingsKeys.LAST_SYNC_TIMESTAMP) ?: 0L

        // ---------- PULL ----------
        onStep(SyncStep.Pull)
        val resetModels = local.emptyTables(SyncMappings.resetLocalTables)
            .mapNotNull { SyncMappings.apiModelFor(it) }

        val pull = when (val result = api.pull(lastPulledAt, resetModels)) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> throw SyncException(result)
        }

        val details = pull.changes.mapNotNull { (model, change) ->
            if (change.size == 0) null else ModelCount(SyncMappings.displayNameFor(model), change.size)
        }
        if (details.isNotEmpty()) {
            onStep(SyncStep.PullDataReceived(details.sumOf { it.count }, details))
        }

        pull.changes.forEach { (model, change) -> local.applyChanges(model, change) }
        pull.catalogs?.let { settings.putString(SettingsKeys.SYNC_CATALOGS, Json.encodeToString(SyncCatalogs.serializer(), it)) }
        settings.putLong(SettingsKeys.LAST_SYNC_TIMESTAMP, pull.timestamp)

        // ---------- MEDIA + PUSH ----------
        val pending = DEVICE_WRITABLE_TABLES.associateWith { local.readPending(it).toMutableList() }
        uploadOfflineMedia(pending, onStep)
        pushChanges(pending, pull.timestamp, onStep)
    }

    private suspend fun uploadOfflineMedia(
        pending: Map<String, MutableList<PendingRecord>>,
        onStep: (SyncStep) -> Unit,
    ) {
        val jobs = mutableListOf<MediaJob>()
        MEDIA_TABLES.forEach { table ->
            pending[table]?.forEach { record ->
                localUris(record).forEach { uri -> jobs += MediaJob(table, record.id, uri) }
            }
        }
        if (jobs.isEmpty()) return

        onStep(SyncStep.MediaUploadStart(jobs.size))
        jobs.forEachIndexed { index, job ->
            onStep(SyncStep.MediaUploadProgress(index + 1, jobs.size, job.table))

            val type = if (isVideo(job.uri)) MediaType.VIDEO else MediaType.IMAGE
            val result = mediaUploader.upload(job.uri, type, job.table, null)
            val url = result.url
            if (!result.success || url == null) {
                throw SyncException(result.error ?: "No se pudo subir la evidencia")
            }

            val records = pending[job.table] ?: return@forEachIndexed
            val position = records.indexOfFirst { it.id == job.recordId }
            if (position >= 0) {
                val updated = replaceUri(records[position], job.uri, url)
                records[position] = updated
                val mediaJson = updated.data["media"]?.toString() ?: "[]"
                local.updateMediaJson(job.table, job.recordId, mediaJson)
            }
        }
    }

    private suspend fun pushChanges(
        pending: Map<String, MutableList<PendingRecord>>,
        lastPulledAt: Long,
        onStep: (SyncStep) -> Unit,
    ) {
        val apiChanges = mutableMapOf<String, SyncChangeSetDto>()
        pending.forEach { (table, records) ->
            if (records.isEmpty()) return@forEach
            val model = SyncMappings.apiModelFor(table) ?: return@forEach
            apiChanges[model] = SyncChangeSetDto(
                created = records.filter { it.status == "created" }.map { it.data },
                updated = records.filter { it.status == "updated" }.map { it.data },
                deleted = records.filter { it.status == "deleted" }.map { it.id },
            )
        }
        if (apiChanges.isEmpty()) return

        onStep(SyncStep.Push)
        when (val result = api.push(SyncPushBodyDto(apiChanges, lastPulledAt))) {
            is ApiResult.Success -> Unit
            is ApiResult.Failure -> throw SyncException(result)
        }

        pending.forEach { (table, records) ->
            records.forEach { record ->
                if (record.status == "deleted") local.hardDelete(table, record.id)
                else local.markSynced(table, listOf(record.id))
            }
        }
    }

    private data class MediaJob(val table: String, val recordId: String, val uri: String)

    private fun localUris(record: PendingRecord): List<String> {
        val media = record.data["media"] as? JsonArray ?: return emptyList()
        return media
            .mapNotNull { (it as? JsonPrimitive)?.contentOrNull }
            .filter { uri -> LOCAL_URI_PREFIXES.any { uri.startsWith(it) } }
    }

    private fun replaceUri(record: PendingRecord, from: String, to: String): PendingRecord {
        val media = record.data["media"] as? JsonArray ?: return record
        val updated = JsonArray(
            media.map { element ->
                if ((element as? JsonPrimitive)?.contentOrNull == from) JsonPrimitive(to) else element
            },
        )
        return record.copy(data = JsonObject(record.data + ("media" to updated)))
    }

    private fun isVideo(uri: String): Boolean = uri.lowercase().let {
        it.endsWith(".mp4") || it.endsWith(".mov") || it.endsWith(".3gp")
    }

    private companion object {
        val MEDIA_TABLES = listOf("incidents", "maintenances", "kardex")
        val LOCAL_URI_PREFIXES = listOf("file://", "/", "ph://", "content://")
    }
}

/**
 * Sincroniza en segundo plano si hay red. Nunca lanza: lo pendiente se
 * reintenta en la próxima reconexión.
 */
suspend fun SyncEngine.syncInBackground(connectivity: ConnectivityObserver): Boolean {
    if (!connectivity.isConnected()) return false
    return runCatching { sync() }.isSuccess
}
