package com.axzydev.checkapp.entities.guarddiscipline.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.common.time.isoToEpochMillis
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.core.network.DataTableParams
import com.axzydev.checkapp.core.network.DatatableDto
import com.axzydev.checkapp.entities.guarddiscipline.model.GuardDiscipline
import kotlinx.serialization.Serializable

@Serializable
private data class UserRef(val name: String = "", val lastName: String? = null)

@Serializable
private data class NamedRef(val name: String = "")

@Serializable
private data class GuardDisciplineDto(
    val id: String,
    val description: String? = null,
    val status: String = "PENDING",
    val createdAt: String? = null,
    val guard: UserRef? = null,
    val category: NamedRef? = null,
    val type: NamedRef? = null,
)

interface GuardDisciplineRepository {
    suspend fun remoteAll(): ApiResult<List<GuardDiscipline>>
    suspend fun resolve(id: String): ApiResult<GuardDiscipline>
}

class DefaultGuardDisciplineRepository(private val api: ApiClient) : GuardDisciplineRepository {

    override suspend fun remoteAll(): ApiResult<List<GuardDiscipline>> =
        api.post<DatatableDto<GuardDisciplineDto>>(
            "/guard-discipline/datatable",
            DataTableParams(page = 1, limit = LIST_LIMIT),
        ).map { result -> result.rows.map { it.toModel() } }

    override suspend fun resolve(id: String): ApiResult<GuardDiscipline> =
        api.put<GuardDisciplineDto>("/guard-discipline/$id/resolve", null).map { it.toModel() }
}

private fun GuardDisciplineDto.toModel(): GuardDiscipline = GuardDiscipline(
    id = id,
    guardName = guard?.let { listOfNotNull(it.name, it.lastName).joinToString(" ").trim() },
    categoryName = category?.name,
    typeName = type?.name,
    description = description,
    status = status,
    createdAt = createdAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() } ?: 0L,
)

/** Límite alto porque las quejas se filtran en cliente, no en el servidor. */
private const val LIST_LIMIT = 1000
