package com.axzydev.checkapp.entities.zone.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Zones
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.core.network.DataTableParams
import com.axzydev.checkapp.core.network.DatatableDto
import com.axzydev.checkapp.entities.zone.model.Zone
import com.axzydev.checkapp.entities.zone.model.ZoneDraft
import kotlinx.serialization.Serializable

@Serializable
private data class ZoneDto(
    val id: String,
    val clientId: String,
    val name: String,
    val active: Boolean = true,
)

@Serializable
private data class CreateZoneRequest(val name: String, val clientId: String)

@Serializable
private data class UpdateZoneRequest(val name: String)

interface ZoneRepository {
    suspend fun remoteAll(): ApiResult<List<Zone>>
    suspend fun create(draft: ZoneDraft): ApiResult<Zone>
    suspend fun update(id: String, name: String): ApiResult<Zone>
    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun localByClient(clientId: String): List<Zone>
}

class DefaultZoneRepository(
    private val api: ApiClient,
    private val database: AxzyCheckDatabase,
) : ZoneRepository {

    override suspend fun remoteAll(): ApiResult<List<Zone>> =
        api.post<DatatableDto<ZoneDto>>(
            "/zones/datatable",
            DataTableParams(page = 1, limit = LIST_LIMIT),
        ).map { result -> result.rows.map { it.toModel() } }

    override suspend fun create(draft: ZoneDraft): ApiResult<Zone> =
        api.post<ZoneDto>("/zones", CreateZoneRequest(draft.name, draft.clientId)).map { it.toModel() }

    override suspend fun update(id: String, name: String): ApiResult<Zone> =
        api.put<ZoneDto>("/zones/$id", UpdateZoneRequest(name)).map { it.toModel() }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/zones/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun localByClient(clientId: String): List<Zone> =
        database.zoneQueries.selectZonesByClient(clientId).executeAsList().map { it.toModel() }
}

private fun ZoneDto.toModel(): Zone = Zone(id, clientId, name, active)

private fun Zones.toModel(): Zone = Zone(id, client_id, name, active != 0L)

/** Límite alto porque las zonas se filtran en cliente, no en el servidor. */
private const val LIST_LIMIT = 1000
