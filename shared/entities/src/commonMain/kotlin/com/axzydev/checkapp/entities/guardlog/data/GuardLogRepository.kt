package com.axzydev.checkapp.entities.guardlog.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.common.time.isoToEpochMillis
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.core.network.DataTableParams
import com.axzydev.checkapp.core.network.DatatableDto
import com.axzydev.checkapp.entities.guardlog.model.GuardLog
import kotlinx.serialization.Serializable

@Serializable
private data class UserRef(val name: String = "", val lastName: String? = null, val username: String = "")

@Serializable
private data class GuardLogDto(
    val id: String,
    val loginAt: String? = null,
    val logoutAt: String? = null,
    val user: UserRef? = null,
)

interface GuardLogRepository {
    suspend fun datatable(page: Int, limit: Int): ApiResult<List<GuardLog>>
}

class DefaultGuardLogRepository(private val api: ApiClient) : GuardLogRepository {

    override suspend fun datatable(page: Int, limit: Int): ApiResult<List<GuardLog>> =
        api.post<DatatableDto<GuardLogDto>>(
            "/guard-logs/datatable",
            DataTableParams(page = page, limit = limit, sort = mapOf("key" to "loginAt", "direction" to "desc")),
        ).map { result -> result.rows.map { it.toModel() } }
}

private fun GuardLogDto.toModel(): GuardLog {
    val name = user?.let { listOfNotNull(it.name, it.lastName).joinToString(" ").trim() } ?: "Guardia"
    return GuardLog(
        id = id,
        guardName = name,
        username = user?.username ?: "",
        loginAt = loginAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() } ?: 0L,
        logoutAt = logoutAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() },
    )
}
