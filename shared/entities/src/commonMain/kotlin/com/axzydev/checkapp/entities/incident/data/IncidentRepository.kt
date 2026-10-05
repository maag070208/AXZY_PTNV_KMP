package com.axzydev.checkapp.entities.incident.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.time.isoToEpochMillis
import com.axzydev.checkapp.core.common.uuid.randomUuid
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Incidents
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.incident.model.Incident
import com.axzydev.checkapp.entities.incident.model.IncidentDraft
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement

@Serializable
private data class UserRef(val name: String = "", val lastName: String? = null)

@Serializable
private data class NamedRef(val name: String = "")

@Serializable
private data class IncidentDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: String = "PENDING",
    val createdAt: String? = null,
    val media: JsonElement? = null,
    val guard: UserRef? = null,
    val category: NamedRef? = null,
    val type: NamedRef? = null,
)

interface IncidentRepository {
    suspend fun remoteAll(): ApiResult<List<Incident>>
    suspend fun resolve(id: String): ApiResult<Incident>

    /** Registra una incidencia local-first (`_status = created`); el sync la sube. */
    suspend fun report(userId: String, draft: IncidentDraft): Incident

    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun localAll(): List<Incident>
    suspend fun byUser(userId: String): List<Incident>
}

class DefaultIncidentRepository(
    private val api: ApiClient,
    private val database: AxzyCheckDatabase,
    private val timeProvider: TimeProvider,
) : IncidentRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun remoteAll(): ApiResult<List<Incident>> =
        api.get<List<IncidentDto>>("/incidents").map { rows -> rows.map { it.toModel() } }

    override suspend fun resolve(id: String): ApiResult<Incident> =
        api.put<IncidentDto>("/incidents/$id/resolve", null).map { it.toModel() }

    override suspend fun report(userId: String, draft: IncidentDraft): Incident {
        val now = timeProvider.nowEpochMillis()
        val id = randomUuid()
        database.incidentQueries.insertIncident(
            id = id,
            guard_id = userId,
            title = draft.title,
            category_id = draft.categoryId,
            type_id = draft.typeId,
            description = draft.description,
            media = json.encodeToString(draft.media),
            latitude = draft.latitude,
            longitude = draft.longitude,
            status = "PENDING",
            client_id = draft.clientId,
            created_at = now,
            updated_at = now,
            _status = "created",
        )
        return Incident(
            id = id,
            title = draft.title,
            guardName = null,
            categoryName = null,
            typeName = null,
            description = draft.description,
            status = "PENDING",
            createdAt = now,
            mediaCount = draft.media.size,
        )
    }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/incidents/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun localAll(): List<Incident> =
        database.incidentQueries.selectAllIncidents().executeAsList().map { it.toModel() }

    override suspend fun byUser(userId: String): List<Incident> =
        database.incidentQueries.selectIncidentsByGuard(userId).executeAsList().map { it.toModel() }

    private fun IncidentDto.toModel(): Incident = Incident(
        id = id,
        title = title,
        guardName = guard?.let { listOfNotNull(it.name, it.lastName).joinToString(" ").trim() },
        categoryName = category?.name,
        typeName = type?.name,
        description = description,
        status = status,
        createdAt = createdAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() } ?: 0L,
        mediaCount = (media as? JsonArray)?.size ?: 0,
    )

    private fun Incidents.toModel(): Incident = Incident(
        id = id,
        title = title,
        guardName = null,
        categoryName = null,
        typeName = null,
        description = description,
        status = status,
        createdAt = created_at,
        mediaCount = mediaCountOf(media),
    )

    private fun mediaCountOf(raw: String?): Int {
        if (raw.isNullOrBlank()) return 0
        return runCatching { json.decodeFromString<List<String>>(raw).size }.getOrDefault(0)
    }
}
