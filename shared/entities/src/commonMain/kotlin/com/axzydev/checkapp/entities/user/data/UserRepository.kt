package com.axzydev.checkapp.entities.user.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Users
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.user.model.User
import com.axzydev.checkapp.entities.user.model.UserDraft
import com.axzydev.checkapp.entities.user.model.UserUpdateDraft
import kotlinx.serialization.Serializable

@Serializable
private data class RoleRefDto(val id: String, val name: String, val value: String)

@Serializable
private data class UserDto(
    val id: String,
    val name: String,
    val lastName: String? = null,
    val username: String,
    val active: Boolean = true,
    val roleId: String? = null,
    val clientId: String? = null,
    val scheduleId: String? = null,
    val role: RoleRefDto? = null,
)

@Serializable
private data class CreateUserRequest(
    val name: String,
    val username: String,
    val password: String,
    val roleId: String,
    val lastName: String? = null,
    val clientId: String? = null,
    val scheduleId: String? = null,
)

@Serializable
private data class UpdateUserRequest(
    val name: String,
    val lastName: String? = null,
    val roleId: String? = null,
    val clientId: String? = null,
)

@Serializable
private data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

interface UserRepository {
    suspend fun remoteAll(): ApiResult<List<User>>
    suspend fun create(draft: UserDraft): ApiResult<User>
    suspend fun update(id: String, draft: UserUpdateDraft): ApiResult<User>
    suspend fun changePassword(id: String, oldPassword: String, newPassword: String): ApiResult<Unit>
    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun localAll(): List<User>
    suspend fun findById(id: String): User?
}

class DefaultUserRepository(
    private val api: ApiClient,
    private val database: AxzyCheckDatabase,
) : UserRepository {

    override suspend fun remoteAll(): ApiResult<List<User>> =
        api.get<List<UserDto>>("/users").map { rows -> rows.map { it.toModel() } }

    override suspend fun create(draft: UserDraft): ApiResult<User> =
        api.post<UserDto>(
            "/users",
            CreateUserRequest(
                name = draft.name,
                username = draft.username,
                password = draft.password,
                roleId = draft.roleId,
                lastName = draft.lastName,
                clientId = draft.clientId,
                scheduleId = draft.scheduleId,
            ),
        ).map { it.toModel() }

    override suspend fun update(id: String, draft: UserUpdateDraft): ApiResult<User> =
        api.put<UserDto>(
            "/users/$id",
            UpdateUserRequest(
                name = draft.name,
                lastName = draft.lastName,
                roleId = draft.roleId,
                clientId = draft.clientId,
            ),
        ).map { it.toModel() }

    override suspend fun changePassword(id: String, oldPassword: String, newPassword: String): ApiResult<Unit> =
        when (val result = api.put<UserDto>("/users/$id/password", ChangePasswordRequest(oldPassword, newPassword))) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/users/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun localAll(): List<User> =
        database.userQueries.selectAllUsers().executeAsList().map { it.toModel() }

    override suspend fun findById(id: String): User? =
        database.userQueries.selectUserById(id).executeAsOneOrNull()?.toModel()
}

private fun UserDto.toModel(): User =
    User(id, name, lastName, username, active, roleId, role?.name, clientId, scheduleId)

private fun Users.toModel(): User =
    User(id, name, last_name, username, active != 0L, role_id, null, client_id, schedule_id)
