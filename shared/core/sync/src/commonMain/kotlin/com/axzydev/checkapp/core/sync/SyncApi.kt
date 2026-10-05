package com.axzydev.checkapp.core.sync

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.network.ApiClient

/** Acceso remoto al protocolo de sincronización. */
interface SyncApi {
    suspend fun pull(lastPulledAt: Long, resetModels: List<String>): ApiResult<SyncPullResponseDto>
    suspend fun push(body: SyncPushBodyDto): ApiResult<Unit>
    suspend fun hasPendingChanges(lastPulledAt: Long): ApiResult<SyncCheckResponseDto>
}

class KtorSyncApi(private val client: ApiClient) : SyncApi {

    override suspend fun pull(
        lastPulledAt: Long,
        resetModels: List<String>,
    ): ApiResult<SyncPullResponseDto> {
        val query = mutableMapOf<String, Any?>("last_pulled_at" to lastPulledAt)
        if (resetModels.isNotEmpty()) {
            query["reset_models"] = resetModels.joinToString(",")
        }
        return client.get("/sync", query)
    }

    override suspend fun push(body: SyncPushBodyDto): ApiResult<Unit> =
        client.post("/sync", body)

    override suspend fun hasPendingChanges(lastPulledAt: Long): ApiResult<SyncCheckResponseDto> =
        client.get(
            "/sync/check",
            mapOf("last_pulled_at" to lastPulledAt),
        )
}
