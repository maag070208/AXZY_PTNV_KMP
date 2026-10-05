package com.axzydev.checkapp.entities.guard.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Users
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.guard.model.Guard
import kotlinx.serialization.Serializable

@Serializable
private data class RoleRefDto(val name: String = "")

@Serializable
private data class GuardUserDto(
    val id: String,
    val name: String,
    val lastName: String? = null,
    val username: String,
    val active: Boolean = true,
    val clientId: String? = null,
    val scheduleId: String? = null,
    val role: RoleRefDto? = null,
)

interface GuardRepository {
    suspend fun guards(): List<Guard>
    suspend fun findById(id: String): Guard?
    suspend fun delete(id: String): ApiResult<Unit>
}

class DefaultGuardRepository(
    private val database: AxzyCheckDatabase,
    private val api: ApiClient,
) : GuardRepository {

    override suspend fun guards(): List<Guard> {
        return when (val result = api.get<List<GuardUserDto>>("/users")) {
            is ApiResult.Success -> result.data
                .filter { it.role?.name.equals("GUARD", ignoreCase = true) }
                .map { it.toModel() }

            is ApiResult.Failure -> database.userQueries.selectGuards().executeAsList().map { it.toModel() }
        }
    }

    override suspend fun findById(id: String): Guard? =
        database.userQueries.selectUserById(id).executeAsOneOrNull()?.toModel()

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<GuardUserDto>("/users/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }
}

private fun GuardUserDto.toModel(): Guard = Guard(id, name, lastName, username, active, clientId, scheduleId)

private fun Users.toModel(): Guard = Guard(id, name, last_name, username, active != 0L, client_id, schedule_id)
