package com.axzydev.checkapp.entities.recurringroute.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.recurringroute.model.RecurringPoint
import com.axzydev.checkapp.entities.recurringroute.model.RecurringPointDraft
import com.axzydev.checkapp.entities.recurringroute.model.RecurringRoute
import com.axzydev.checkapp.entities.recurringroute.model.RecurringRouteDraft
import com.axzydev.checkapp.entities.recurringroute.model.RecurringTask
import com.axzydev.checkapp.entities.recurringroute.model.RecurringTaskDraft
import kotlinx.serialization.Serializable

// --- DTOs del contrato con /recurring ---

@Serializable
private data class RecurringTaskDto(
    val description: String,
    val reqPhoto: Boolean = false,
)

@Serializable
private data class LocationRefDto(
    val id: String,
    val name: String? = null,
)

@Serializable
private data class RecurringLocationDto(
    val locationId: String? = null,
    val location: LocationRefDto? = null,
    val tasks: List<RecurringTaskDto> = emptyList(),
)

@Serializable
private data class GuardRefDto(val id: String)

@Serializable
private data class RecurringConfigDto(
    val id: String,
    val title: String,
    val clientId: String? = null,
    val active: Boolean = true,
    val recurringLocations: List<RecurringLocationDto> = emptyList(),
    val guards: List<GuardRefDto> = emptyList(),
)

@Serializable
private data class TaskRequest(val description: String, val reqPhoto: Boolean)

@Serializable
private data class PointRequest(val locationId: String, val tasks: List<TaskRequest>)

@Serializable
private data class RecurringRequest(
    val title: String,
    val clientId: String? = null,
    val locations: List<PointRequest>,
    val guardIds: List<String> = emptyList(),
    val active: Boolean = true,
)

interface RecurringRouteRepository {
    /** Rutas activas locales (lectura offline, alimentada por el sync). */
    suspend fun activeRoutes(clientId: String?): List<RecurringRoute>

    /** Listado remoto (online). */
    suspend fun remoteAll(): ApiResult<List<RecurringRoute>>

    /** Detalle remoto con puntos, tareas y guardias. */
    suspend fun remoteById(id: String): ApiResult<RecurringRoute>

    suspend fun create(draft: RecurringRouteDraft): ApiResult<RecurringRoute>

    suspend fun update(id: String, draft: RecurringRouteDraft): ApiResult<RecurringRoute>

    suspend fun delete(id: String): ApiResult<Unit>
}

class DefaultRecurringRouteRepository(
    private val database: AxzyCheckDatabase,
    private val api: ApiClient,
) : RecurringRouteRepository {

    override suspend fun activeRoutes(clientId: String?): List<RecurringRoute> {
        val configs = if (clientId == null) {
            database.recurringQueries.selectActiveRecurring().executeAsList()
        } else {
            database.recurringQueries.selectActiveRecurringByClient(clientId).executeAsList()
        }
        val locationNames = database.locationQueries.selectActiveLocations()
            .executeAsList()
            .associate { it.id to it.name }

        return configs.map { config ->
            val points = database.recurringQueries.selectRecurringLocations(config.id)
                .executeAsList()
                .map { recurringLocation ->
                    val tasks = database.recurringQueries.selectRecurringTasks(recurringLocation.id)
                        .executeAsList()
                        .map { RecurringTask(it.id, it.description, it.req_photo != 0L) }
                    RecurringPoint(
                        id = recurringLocation.id,
                        locationId = recurringLocation.location_id,
                        locationName = locationNames[recurringLocation.location_id],
                        order = recurringLocation.sort_order.toInt(),
                        tasks = tasks,
                    )
                }
            RecurringRoute(
                id = config.id,
                title = config.title,
                clientId = config.client_id,
                active = config.active != 0L,
                points = points,
            )
        }
    }

    override suspend fun remoteAll(): ApiResult<List<RecurringRoute>> =
        api.get<List<RecurringConfigDto>>("/recurring").map { rows -> rows.map { it.toModel() } }

    override suspend fun remoteById(id: String): ApiResult<RecurringRoute> =
        api.get<RecurringConfigDto>("/recurring/$id").map { it.toModel() }

    override suspend fun create(draft: RecurringRouteDraft): ApiResult<RecurringRoute> =
        api.post<RecurringConfigDto>("/recurring", draft.toRequest()).map { it.toModel() }

    override suspend fun update(id: String, draft: RecurringRouteDraft): ApiResult<RecurringRoute> =
        api.put<RecurringConfigDto>("/recurring/$id", draft.toRequest()).map { it.toModel() }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/recurring/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }
}

private fun RecurringConfigDto.toModel(): RecurringRoute = RecurringRoute(
    id = id,
    title = title,
    clientId = clientId,
    active = active,
    points = recurringLocations.mapIndexed { index, dto ->
        RecurringPoint(
            id = dto.locationId ?: dto.location?.id ?: index.toString(),
            locationId = dto.locationId ?: dto.location?.id.orEmpty(),
            locationName = dto.location?.name,
            order = index,
            tasks = dto.tasks.mapIndexed { taskIndex, task ->
                RecurringTask(
                    id = "$index-$taskIndex",
                    description = task.description,
                    reqPhoto = task.reqPhoto,
                )
            },
        )
    },
    guardIds = guards.map { it.id },
)

private fun RecurringRouteDraft.toRequest(): RecurringRequest = RecurringRequest(
    title = title,
    clientId = clientId,
    locations = points.map { point ->
        PointRequest(
            locationId = point.locationId,
            tasks = point.tasks.map { TaskRequest(it.description, it.reqPhoto) },
        )
    },
    guardIds = guardIds,
    active = active,
)
