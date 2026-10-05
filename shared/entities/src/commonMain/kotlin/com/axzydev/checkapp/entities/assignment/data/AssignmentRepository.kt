package com.axzydev.checkapp.entities.assignment.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.database.Assignments
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.assignment.model.Assignment
import com.axzydev.checkapp.entities.assignment.model.AssignmentDraft
import kotlinx.serialization.Serializable

@Serializable
private data class UserRefDto(val name: String = "", val lastName: String? = null)

@Serializable
private data class LocationRefDto(val name: String = "")

@Serializable
private data class AssignmentDto(
    val id: String,
    val guardId: String,
    val locationId: String,
    val status: String = "PENDING",
    val assignedBy: String = "",
    val notes: String? = null,
    val guard: UserRefDto? = null,
    val location: LocationRefDto? = null,
)

@Serializable
private data class CreateAssignmentRequest(
    val guardId: String,
    val locationId: String,
    val assignedBy: String,
    val notes: String? = null,
)

@Serializable
private data class UpdateStatusRequest(val status: String)

interface AssignmentRepository {
    suspend fun remoteAll(): ApiResult<List<Assignment>>
    suspend fun create(draft: AssignmentDraft): ApiResult<Assignment>
    suspend fun updateStatus(id: String, status: String): ApiResult<Assignment>
    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun localAll(): List<Assignment>
}

class DefaultAssignmentRepository(
    private val api: ApiClient,
    private val database: AxzyCheckDatabase,
) : AssignmentRepository {

    override suspend fun remoteAll(): ApiResult<List<Assignment>> =
        api.get<List<AssignmentDto>>("/assignments").map { rows -> rows.map { it.toModel() } }

    override suspend fun create(draft: AssignmentDraft): ApiResult<Assignment> =
        api.post<AssignmentDto>(
            "/assignments",
            CreateAssignmentRequest(draft.guardId, draft.locationId, draft.assignedBy, draft.notes),
        ).map { it.toModel() }

    override suspend fun updateStatus(id: String, status: String): ApiResult<Assignment> =
        api.patch<AssignmentDto>("/assignments/$id/status", UpdateStatusRequest(status)).map { it.toModel() }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/assignments/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun localAll(): List<Assignment> =
        database.assignmentQueries.selectAllAssignments().executeAsList().map { it.toModel() }
}

private fun AssignmentDto.toModel(): Assignment = Assignment(
    id = id,
    guardId = guardId,
    guardName = guard?.let { listOfNotNull(it.name, it.lastName).joinToString(" ").trim() },
    locationId = locationId,
    locationName = location?.name,
    status = status,
    assignedBy = assignedBy,
    notes = notes,
)

private fun Assignments.toModel(): Assignment =
    Assignment(id, guard_id, null, location_id, null, status, assigned_by, notes)
