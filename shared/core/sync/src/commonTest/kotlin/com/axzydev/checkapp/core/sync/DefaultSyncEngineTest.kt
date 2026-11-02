package com.axzydev.checkapp.core.sync

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.datastore.SettingsKeys
import com.axzydev.checkapp.core.datastore.SettingsStore
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DefaultSyncEngineTest {

    @Test
    fun sync_pulls_then_uploads_media_then_pushes_and_marks_synced() = runTest {
        val local = FakeSyncLocalStore()
        local.pending["kardex"] = mutableListOf(
            PendingRecord(
                id = "k1",
                status = "created",
                data = buildJsonObject {
                    put("id", "k1")
                    put("userId", "u1")
                    put("locationId", "loc1")
                    put("scanType", "RECURRING")
                    put("media", buildJsonArray { add(JsonPrimitive("file:///a.jpg")) })
                },
            ),
        )

        val api = FakeSyncApi(
            pull = SyncPullResponseDto(
                timestamp = 111L,
                changes = mapOf(
                    "client" to SyncChangeSetDto(created = listOf(buildJsonObject { put("id", "c1") })),
                ),
            ),
        )
        val uploader = FakeMediaUploader()
        val settings = FakeSettingsStore()
        val engine = DefaultSyncEngine(api, local, uploader, settings)

        val steps = mutableListOf<SyncStep>()
        engine.sync { steps += it }

        // PULL aplicado
        assertEquals(listOf("client"), local.applied.map { it.first })

        // Media subida y URL reemplazada
        assertEquals(listOf("file:///a.jpg"), uploader.uploaded)
        assertTrue(local.mediaUpdates.any { it.second == "k1" && it.third.contains("https://cdn.example.com/a.jpg") })

        // PUSH con la URL final y lastPulledAt del pull
        val pushed = assertNotNull(api.pushedBody)
        assertEquals(111L, pushed.lastPulledAt)
        val created = assertNotNull(pushed.changes["kardex"]).created.single()
        assertEquals("https://cdn.example.com/a.jpg", (created["media"] as JsonArray)[0].jsonPrimitive.content)

        // Marcado como sincronizado y timestamp persistido
        assertEquals(listOf("kardex" to "k1"), local.synced)
        assertEquals(111L, settings.getLong(SettingsKeys.LAST_SYNC_TIMESTAMP))

        // Secuencia de pasos emitida
        assertTrue(steps.any { it is SyncStep.Pull })
        assertTrue(steps.any { it is SyncStep.MediaUploadStart })
        assertTrue(steps.any { it is SyncStep.MediaUploadProgress })
        assertTrue(steps.any { it is SyncStep.Push })
    }

    @Test
    fun media_upload_failure_aborts_before_push() = runTest {
        val local = FakeSyncLocalStore()
        local.pending["incidents"] = mutableListOf(
            PendingRecord(
                id = "i1",
                status = "created",
                data = buildJsonObject {
                    put("id", "i1")
                    put("media", buildJsonArray { add(JsonPrimitive("file:///x.jpg")) })
                },
            ),
        )
        val api = FakeSyncApi(SyncPullResponseDto(timestamp = 5L))
        val uploader = FakeMediaUploader(shouldFail = true)
        val engine = DefaultSyncEngine(api, local, uploader, FakeSettingsStore())

        val failure = runCatching { engine.sync() }.exceptionOrNull()
        assertTrue(failure is SyncException)
        assertEquals(null, api.pushedBody)
        assertTrue(local.synced.isEmpty())
    }

    /**
     * Un pull fallido (p. ej. porque el servidor exige actualizar la app) NO debe
     * dejar el trabajo capturado offline atrapado en el dispositivo: antes el
     * `sync` hacía pull → push, así que el push nunca llegaba a ejecutarse.
     */
    @Test
    fun pull_failure_still_drains_the_pending_queue() = runTest {
        val local = FakeSyncLocalStore()
        local.pending["kardex"] = mutableListOf(
            PendingRecord(
                id = "k1",
                status = "created",
                data = buildJsonObject {
                    put("id", "k1")
                    put("userId", "u1")
                    put("locationId", "loc1")
                },
            ),
        )
        val api = FakeSyncApi(pullFails = true)
        val engine = DefaultSyncEngine(api, local, FakeMediaUploader(), FakeSettingsStore())

        val failure = runCatching { engine.sync() }.exceptionOrNull()

        // Se propaga el error del pull (la app debe actualizarse)...
        assertTrue(failure is SyncException)
        // ...pero la cola se vació igualmente.
        assertNotNull(api.pushedBody)
        assertEquals(listOf("kardex" to "k1"), local.synced)
    }

    @Test
    fun pushOnly_drains_the_queue_without_pulling() = runTest {
        val local = FakeSyncLocalStore()
        local.pending["incidents"] = mutableListOf(
            PendingRecord(id = "i1", status = "created", data = buildJsonObject { put("id", "i1") }),
        )
        val api = FakeSyncApi(pullFails = true)
        val engine = DefaultSyncEngine(api, local, FakeMediaUploader(), FakeSettingsStore())

        engine.pushOnly()

        assertNotNull(api.pushedBody)
        assertEquals(listOf("incidents" to "i1"), local.synced)
        // No se aplicó ningún pull: sólo se vació la cola.
        assertTrue(local.applied.isEmpty())
    }

    /**
     * El push es atómico: si el servidor rechaza registros, no aplica nada y
     * devuelve el detalle. Ese detalle debe llegar a la UI para poder corregir o
     * descartar el registro; antes se perdía en un mensaje genérico y la cola
     * quedaba atascada para siempre.
     */
    @Test
    fun rejected_push_surfaces_the_offending_records() = runTest {
        val local = FakeSyncLocalStore()
        local.pending["kardex"] = mutableListOf(
            PendingRecord(id = "k9", status = "created", data = buildJsonObject { put("id", "k9") }),
        )
        val rechazo = buildJsonObject {
            put(
                "rejected",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("table", "kardex")
                            put("id", "k9")
                            put("action", "create")
                            put("reason", "El punto de control indicado en locationId no existe en el servidor")
                        },
                    )
                },
            )
        }
        val api = FakeSyncApi(
            pushFailure = ApiResult.Failure(
                listOf("No se pudo sincronizar"),
                statusCode = 400,
                data = rechazo,
            ),
        )
        val engine = DefaultSyncEngine(api, local, FakeMediaUploader(), FakeSettingsStore())

        val failure = runCatching { engine.sync() }.exceptionOrNull()

        val excepcion = assertNotNull(
            failure as? SyncRejectedException,
            "el rechazo del servidor debe exponerse como SyncRejectedException",
        )
        val rechazos = excepcion.rejections
        assertEquals(1, rechazos.size)
        assertEquals("k9", rechazos[0].id)
        assertTrue(rechazos[0].reason.contains("locationId"))
        // El trabajo NO se marca como sincronizado: no se pierde.
        assertTrue(local.synced.isEmpty())
    }
}

// ---------------- Fakes ----------------

private class FakeSyncApi(
    private val pull: SyncPullResponseDto = SyncPullResponseDto(timestamp = 0L),
    private val pullFails: Boolean = false,
    private val pushFailure: ApiResult.Failure? = null,
) : SyncApi {
    var pushedBody: SyncPushBodyDto? = null
    var pushCalls = 0

    override suspend fun pull(lastPulledAt: Long, resetModels: List<String>): ApiResult<SyncPullResponseDto> =
        if (pullFails) {
            ApiResult.Failure(listOf("Aplicación desactualizada"), statusCode = 400)
        } else {
            ApiResult.Success(pull)
        }

    override suspend fun push(body: SyncPushBodyDto): ApiResult<SyncPushResponseDto> {
        pushedBody = body
        pushCalls++
        return pushFailure ?: ApiResult.Success(SyncPushResponseDto(applied = true))
    }

    override suspend fun hasPendingChanges(lastPulledAt: Long): ApiResult<SyncCheckResponseDto> =
        ApiResult.Success(SyncCheckResponseDto(hasChanges = true))
}

private class FakeMediaUploader(private val shouldFail: Boolean = false) : MediaUploader {
    val uploaded = mutableListOf<String>()

    override suspend fun upload(
        uri: String,
        type: MediaType,
        location: String,
        roundId: String?,
    ): UploadResult {
        uploaded += uri
        return if (shouldFail) {
            UploadResult(success = false, error = "boom", networkError = true)
        } else {
            UploadResult(success = true, url = "https://cdn.example.com/${uri.substringAfterLast('/')}")
        }
    }
}

private class FakeSyncLocalStore : SyncLocalStore {
    val pending = mutableMapOf<String, MutableList<PendingRecord>>()
    val applied = mutableListOf<Pair<String, SyncChangeSetDto>>()
    val synced = mutableListOf<Pair<String, String>>()
    val mediaUpdates = mutableListOf<Triple<String, String, String>>()

    override suspend fun readPending(table: String): List<PendingRecord> = pending[table]?.toList() ?: emptyList()
    override suspend fun applyChanges(apiModel: String, changeSet: SyncChangeSetDto) {
        applied += apiModel to changeSet
    }

    override suspend fun updateMediaJson(table: String, id: String, mediaJson: String) {
        mediaUpdates += Triple(table, id, mediaJson)
    }

    override suspend fun markSynced(table: String, ids: List<String>) {
        ids.forEach { synced += table to it }
    }

    override suspend fun hardDelete(table: String, id: String) = Unit
    override suspend fun countUnsynced(): Int = pending.values.sumOf { it.size }
    override suspend fun emptyTables(tables: List<String>): List<String> = emptyList()
    override suspend fun clearAll() = Unit
}

private class FakeSettingsStore : SettingsStore {
    private val map = mutableMapOf<String, Any?>()

    override suspend fun getString(key: String): String? = map[key] as? String
    override suspend fun putString(key: String, value: String?) {
        map[key] = value
    }

    override suspend fun getLong(key: String): Long? = map[key] as? Long
    override suspend fun putLong(key: String, value: Long?) {
        map[key] = value
    }

    override suspend fun getBoolean(key: String): Boolean? = map[key] as? Boolean
    override suspend fun putBoolean(key: String, value: Boolean?) {
        map[key] = value
    }

    override suspend fun remove(key: String) {
        map.remove(key)
    }

    override suspend fun clear() = map.clear()
}
