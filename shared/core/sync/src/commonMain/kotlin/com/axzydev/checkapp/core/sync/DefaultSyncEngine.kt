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
import kotlinx.serialization.json.decodeFromJsonElement

/** Error de sincronización con el mensaje devuelto por la API. */
class SyncException(message: String) : Exception(message) {
    constructor(failure: ApiResult.Failure) : this(failure.firstMessage)
}

/**
 * El servidor rechazó registros concretos: al ser un lote **atómico** no aplicó
 * nada y explica cuáles fallaron y por qué ([rejections]).
 *
 * Reintentar tal cual daría el mismo resultado, así que la app debe corregir o
 * descartar esos registros; mientras tanto el resto de la cola sigue pendiente
 * (no se pierde nada).
 */
class SyncRejectedException(
    val rejections: List<SyncRejectionDto>,
    message: String,
) : Exception(message)

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

    /**
     * Vacía la cola sin hacer pull. Es la salida de emergencia cuando el pull no
     * es posible (p. ej. el servidor exige actualizar la app): el trabajo
     * capturado offline se sube igualmente en lugar de quedar atrapado.
     */
    override suspend fun pushOnly(onStep: (SyncStep) -> Unit) {
        mutex.withLock { drainPending(lastPulledAt(), onStep) }
    }

    override suspend fun hasUnsyncedLocalChanges(): Boolean = local.countUnsynced() > 0

    override suspend fun hasPendingServerChanges(): Boolean {
        val lastPulledAt = lastPulledAt()
        return when (val result = api.hasPendingChanges(lastPulledAt)) {
            is ApiResult.Success -> result.data.hasChanges
            is ApiResult.Failure -> false
        }
    }

    private suspend fun lastPulledAt(): Long =
        settings.getLong(SettingsKeys.LAST_SYNC_TIMESTAMP) ?: 0L

    private suspend fun runSync(onStep: (SyncStep) -> Unit) {
        val lastPulledAt = lastPulledAt()

        // ---------- PULL ----------
        onStep(SyncStep.Pull)
        val resetModels = local.emptyTables(SyncMappings.resetLocalTables)
            .mapNotNull { SyncMappings.apiModelFor(it) }

        val pull = when (val result = api.pull(lastPulledAt, resetModels)) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> {
                // Un pull fallido NO debe dejar la cola atrapada (antes el push
                // nunca llegaba a ejecutarse). Se intenta vaciar lo pendiente y,
                // si el push reporta rechazos, esos son el error más accionable.
                val drained = runCatching { drainPending(lastPulledAt, onStep) }
                (drained.exceptionOrNull() as? SyncRejectedException)?.let { throw it }
                throw SyncException(result)
            }
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
        drainPending(pull.timestamp, onStep)
    }

    /** Medios → push → marcar `synced`. Compartido por el sync completo y el push-only. */
    private suspend fun drainPending(lastPulledAt: Long, onStep: (SyncStep) -> Unit) {
        val pending = DEVICE_WRITABLE_TABLES.associateWith { local.readPending(it).toMutableList() }
        uploadOfflineMedia(pending, onStep)
        pushChanges(pending, lastPulledAt, onStep)
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
            is ApiResult.Failure -> {
                // Si el servidor rechazó registros concretos, se propaga el detalle
                // (tabla/id/motivo) en lugar de un mensaje genérico: es lo único
                // que permite corregirlos o descartarlos y desatascar la cola.
                val rejections = parseRejections(result)
                if (rejections.isNotEmpty()) {
                    throw SyncRejectedException(rejections, rejectionMessage(rejections))
                }
                throw SyncException(result)
            }
        }

        pending.forEach { (table, records) ->
            records.forEach { record ->
                if (record.status == "deleted") local.hardDelete(table, record.id)
                else local.markSynced(table, listOf(record.id))
            }
        }
    }

    private data class MediaJob(val table: String, val recordId: String, val uri: String)

    /**
     * Extrae `data.rejected[]` del error del servidor (lote rechazado).
     * Devuelve vacío si el fallo no trae ese detalle (red, 5xx, validación...).
     */
    private fun parseRejections(failure: ApiResult.Failure): List<SyncRejectionDto> {
        val payload = failure.data as? JsonObject ?: return emptyList()
        val array = payload["rejected"] as? JsonArray ?: return emptyList()
        return array.mapNotNull { element ->
            runCatching { json.decodeFromJsonElement(SyncRejectionDto.serializer(), element) }.getOrNull()
        }
    }

    private fun rejectionMessage(rejections: List<SyncRejectionDto>): String {
        val muestra = rejections.take(3).joinToString("; ") { "${it.table}/${it.id}: ${it.reason}" }
        val resto = if (rejections.size > 3) " (+${rejections.size - 3} más)" else ""
        return "El servidor rechazó ${rejections.size} registro(s): $muestra$resto"
    }

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
        val json = Json { ignoreUnknownKeys = true }
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
