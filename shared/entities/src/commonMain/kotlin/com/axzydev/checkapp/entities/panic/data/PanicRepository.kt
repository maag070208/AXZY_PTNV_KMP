package com.axzydev.checkapp.entities.panic.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.datastore.SettingsKeys
import com.axzydev.checkapp.core.datastore.SettingsStore
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.panic.model.PanicAlertDraft
import com.axzydev.checkapp.entities.panic.model.PanicResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class PanicRequest(
    val source: String,
    val triggerLatitude: Double? = null,
    val triggerLongitude: Double? = null,
    val triggerAccuracy: Double? = null,
    val message: String? = null,
)

@Serializable
private data class PanicResponse(val id: String = "")

interface PanicRepository {
    /** Dispara el pánico; si no hay red, encola localmente. */
    suspend fun trigger(draft: PanicAlertDraft): PanicResult

    /** Reintenta las alertas encoladas. Devuelve cuántas se enviaron. */
    suspend fun flushPending(): Int
}

class DefaultPanicRepository(
    private val api: ApiClient,
    private val settings: SettingsStore,
) : PanicRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun trigger(draft: PanicAlertDraft): PanicResult {
        val request = draft.toRequest()
        return when (val result = api.post<PanicResponse>("/panic-alerts", request)) {
            is ApiResult.Success -> PanicResult.Sent
            is ApiResult.Failure ->
                if (result.networkError) {
                    enqueue(request)
                    PanicResult.Queued
                } else {
                    PanicResult.Failed(result.firstMessage)
                }
        }
    }

    override suspend fun flushPending(): Int {
        val pending = readQueue()
        if (pending.isEmpty()) return 0
        val remaining = mutableListOf<PanicRequest>()
        var sent = 0
        pending.forEach { request ->
            when (val result = api.post<PanicResponse>("/panic-alerts", request)) {
                is ApiResult.Success -> sent++
                is ApiResult.Failure -> if (result.networkError) remaining.add(request)
            }
        }
        writeQueue(remaining)
        return sent
    }

    private suspend fun enqueue(request: PanicRequest) {
        writeQueue(readQueue() + request)
    }

    private suspend fun readQueue(): List<PanicRequest> {
        val raw = settings.getString(SettingsKeys.PENDING_PANIC_ALERTS) ?: return emptyList()
        return runCatching { json.decodeFromString<List<PanicRequest>>(raw) }.getOrDefault(emptyList())
    }

    private suspend fun writeQueue(queue: List<PanicRequest>) {
        settings.putString(
            SettingsKeys.PENDING_PANIC_ALERTS,
            if (queue.isEmpty()) null else json.encodeToString(queue),
        )
    }
}

private fun PanicAlertDraft.toRequest(): PanicRequest = PanicRequest(
    source = source,
    triggerLatitude = latitude,
    triggerLongitude = longitude,
    triggerAccuracy = accuracy,
    message = message,
)
