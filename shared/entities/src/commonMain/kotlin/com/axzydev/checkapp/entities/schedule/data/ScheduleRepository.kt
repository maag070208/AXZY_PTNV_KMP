package com.axzydev.checkapp.entities.schedule.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Schedules
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.schedule.model.Schedule
import com.axzydev.checkapp.entities.schedule.model.ScheduleDraft
import kotlinx.serialization.Serializable

@Serializable
private data class ScheduleDto(
    val id: String,
    val name: String,
    val startTime: String,
    val endTime: String,
    val active: Boolean = true,
)

@Serializable
private data class CreateScheduleRequest(val name: String, val startTime: String, val endTime: String)

@Serializable
private data class UpdateScheduleRequest(val name: String, val startTime: String, val endTime: String)

interface ScheduleRepository {
    suspend fun remoteAll(): ApiResult<List<Schedule>>
    suspend fun create(draft: ScheduleDraft): ApiResult<Schedule>
    suspend fun update(id: String, draft: ScheduleDraft): ApiResult<Schedule>
    suspend fun delete(id: String): ApiResult<Unit>
    suspend fun localAll(): List<Schedule>
}

class DefaultScheduleRepository(
    private val api: ApiClient,
    private val database: AxzyCheckDatabase,
) : ScheduleRepository {

    override suspend fun remoteAll(): ApiResult<List<Schedule>> =
        api.get<List<ScheduleDto>>("/schedules").map { rows -> rows.map { it.toModel() } }

    override suspend fun create(draft: ScheduleDraft): ApiResult<Schedule> =
        api.post<ScheduleDto>(
            "/schedules",
            CreateScheduleRequest(draft.name, draft.startTime, draft.endTime),
        ).map { it.toModel() }

    override suspend fun update(id: String, draft: ScheduleDraft): ApiResult<Schedule> =
        api.put<ScheduleDto>(
            "/schedules/$id",
            UpdateScheduleRequest(draft.name, draft.startTime, draft.endTime),
        ).map { it.toModel() }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/schedules/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }

    override suspend fun localAll(): List<Schedule> =
        database.scheduleQueries.selectAllSchedules().executeAsList().map { it.toModel() }
}

private fun ScheduleDto.toModel(): Schedule = Schedule(id, name, startTime, endTime, active)

private fun Schedules.toModel(): Schedule = Schedule(id, name, start_time, end_time, active != 0L)
