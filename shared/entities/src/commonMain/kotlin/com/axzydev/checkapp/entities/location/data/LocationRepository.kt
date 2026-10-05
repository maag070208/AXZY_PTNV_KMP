package com.axzydev.checkapp.entities.location.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Locations
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.location.model.Location
import com.axzydev.checkapp.entities.location.model.LocationDraft
import kotlinx.serialization.Serializable

@Serializable
private data class LocationDto(
    val id: String,
    val name: String,
    val clientId: String? = null,
    val zoneId: String? = null,
    val reference: String? = null,
    val aisle: String? = null,
    val spot: String? = null,
    val number: String? = null,
    val active: Boolean = true,
)

@Serializable
private data class CreateLocationRequest(
    val name: String,
    val clientId: String,
    val zoneId: String? = null,
    val reference: String? = null,
    val aisle: String? = null,
    val spot: String? = null,
    val number: String? = null,
)

interface LocationRepository {
    suspend fun activeLocations(clientId: String? = null): List<Location>
    suspend fun findById(id: String): Location?
    suspend fun remoteAll(): ApiResult<List<Location>>
    suspend fun create(draft: LocationDraft): ApiResult<Location>
    suspend fun update(id: String, draft: LocationDraft): ApiResult<Location>
    suspend fun delete(id: String): ApiResult<Unit>
}

class DefaultLocationRepository(
    private val database: AxzyCheckDatabase,
    private val api: ApiClient,
) : LocationRepository {

    override suspend fun activeLocations(clientId: String?): List<Location> {
        val rows = if (clientId == null) {
            database.locationQueries.selectActiveLocations().executeAsList()
        } else {
            database.locationQueries.selectLocationsByClient(clientId).executeAsList()
        }
        return rows.map { it.toModel() }
    }

    override suspend fun findById(id: String): Location? =
        database.locationQueries.selectLocationById(id).executeAsOneOrNull()?.toModel()

    override suspend fun remoteAll(): ApiResult<List<Location>> =
        api.get<List<LocationDto>>("/locations").map { rows -> rows.map { it.toModel() } }

    override suspend fun create(draft: LocationDraft): ApiResult<Location> =
        api.post<LocationDto>(
            "/locations",
            CreateLocationRequest(
                name = draft.name,
                clientId = draft.clientId,
                zoneId = draft.zoneId,
                reference = draft.reference,
                aisle = draft.aisle,
                spot = draft.spot,
                number = draft.number,
            ),
        ).map { it.toModel() }

    override suspend fun update(id: String, draft: LocationDraft): ApiResult<Location> =
        api.put<LocationDto>(
            "/locations/$id",
            CreateLocationRequest(
                name = draft.name,
                clientId = draft.clientId,
                zoneId = draft.zoneId,
                reference = draft.reference,
                aisle = draft.aisle,
                spot = draft.spot,
                number = draft.number,
            ),
        ).map { it.toModel() }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<LocationDto>("/locations/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }
}

private fun LocationDto.toModel(): Location =
    Location(id, name, clientId, zoneId, reference, active)

private fun Locations.toModel(): Location =
    Location(id, name, client_id, zone_id, reference, active != 0L)
