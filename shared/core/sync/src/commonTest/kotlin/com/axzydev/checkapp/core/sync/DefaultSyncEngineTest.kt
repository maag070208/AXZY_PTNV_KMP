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
}

// ---------------- Fakes ----------------

private class FakeSyncApi(private val pull: SyncPullResponseDto) : SyncApi {
    var pushedBody: SyncPushBodyDto? = null

    override suspend fun pull(lastPulledAt: Long, resetModels: List<String>): ApiResult<SyncPullResponseDto> =
        ApiResult.Success(pull)

    override suspend fun push(body: SyncPushBodyDto): ApiResult<Unit> {
        pushedBody = body
        return ApiResult.Success(Unit)
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
