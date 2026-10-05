package com.axzydev.checkapp.entities.maintenance.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.time.isoToEpochMillis
import com.axzydev.checkapp.core.common.uuid.randomUuid
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Maintenances
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.maintenance.model.Maintenance
import com.axzydev.checkapp.entities.maintenance.model.MaintenanceDraft
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
private data class MaintenanceDto(
    val id: String,
    val title: String,
    val description: String? = null,
    val status: String = "PENDING",
    val createdAt: String? = null,
    val media: JsonElement? = null,
    val guard: UserRef? = null,
    // En Maintenance la relación de categoría se llama `categoryRel`.
    val categoryRel: NamedRef? = null,
    val type: NamedRef? = null,
)

interface MaintenanceRepository {
    suspend fun remoteAll(): ApiResult<List<Maintenance>>
    suspend fun resolve(id: String): ApiResult<Maintenance>

    /** Registra un reporte de falla local-first (`_status = created`); el sync lo sube. */
    suspend fun report(userId: String, draft: MaintenanceDraft): Maintenance

    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun localAll(): List<Maintenance>
    suspend fun byUser(userId: String): List<Maintenance>
}

class DefaultMaintenanceRepository(
    private val api: ApiClient,
    private val database: AxzyCheckDatabase,
    private val timeProvider: TimeProvider,
) : MaintenanceRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun remoteAll(): ApiResult<List<Maintenance>> =
        api.get<List<MaintenanceDto>>("/maintenance").map { rows -> rows.map { it.toModel() } }

    override suspend fun resolve(id: String): ApiResult<Maintenance> =
        api.put<MaintenanceDto>("/maintenance/$id/resolve", null).map { it.toModel() }

    override suspend fun report(userId: String, draft: MaintenanceDraft): Maintenance {
        val now = timeProvider.nowEpochMillis()
        val id = randomUuid()
        database.maintenanceQueries.insertMaintenance(
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
        return Maintenance(
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
        when (val result = api.delete<MaintenanceDto>("/maintenance/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun localAll(): List<Maintenance> =
        database.maintenanceQueries.selectAllMaintenances().executeAsList().map { it.toModel() }

    override suspend fun byUser(userId: String): List<Maintenance> =
        database.maintenanceQueries.selectMaintenancesByGuard(userId).executeAsList().map { it.toModel() }

    private fun MaintenanceDto.toModel(): Maintenance = Maintenance(
        id = id,
        title = title,
        guardName = guard?.let { listOfNotNull(it.name, it.lastName).joinToString(" ").trim() },
        categoryName = categoryRel?.name,
        typeName = type?.name,
        description = description,
        status = status,
        createdAt = createdAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() } ?: 0L,
        mediaCount = (media as? JsonArray)?.size ?: 0,
    )

    private fun Maintenances.toModel(): Maintenance = Maintenance(
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
