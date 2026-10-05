package com.axzydev.checkapp.entities.schedulednotification.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.common.time.isoToEpochMillis
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.core.network.DataTableParams
import com.axzydev.checkapp.core.network.DatatableDto
import com.axzydev.checkapp.entities.schedulednotification.model.ScheduledNotification
import com.axzydev.checkapp.entities.schedulednotification.model.ScheduledNotificationDraft
import kotlinx.serialization.Serializable

@Serializable
private data class TargetUserDto(val name: String = "", val lastName: String? = null)

@Serializable
private data class ScheduledNotificationDto(
    val id: String,
    val title: String? = null,
    val message: String,
    val type: String = "info",
    val channel: String = "global",
    val persistent: Boolean = false,
    val frequency: String? = null,
    val timeOfDay: String? = null,
    val daysOfWeek: String? = null,
    val scheduledAt: String? = null,
    val active: Boolean = true,
    val sendCount: Int = 0,
    val lastSentAt: String? = null,
    val nextSendAt: String? = null,
    val targetUser: TargetUserDto? = null,
)

@Serializable
private data class ScheduledNotificationRequest(
    val message: String,
    val title: String? = null,
    val type: String = "info",
    val channel: String = "global",
    val persistent: Boolean = false,
    val frequency: String = "ONCE",
    val timeOfDay: String? = null,
    val daysOfWeek: String? = null,
    val scheduledAt: String? = null,
    val userId: String? = null,
    val active: Boolean = true,
)

@Serializable
private data class UpdateActiveRequest(val active: Boolean)

interface ScheduledNotificationRepository {
    suspend fun datatable(page: Int, limit: Int, search: String? = null, status: String? = null): ApiResult<List<ScheduledNotification>>
    suspend fun create(draft: ScheduledNotificationDraft): ApiResult<ScheduledNotification>
    suspend fun updateActive(id: String, active: Boolean): ApiResult<ScheduledNotification>
    suspend fun delete(id: String): ApiResult<Unit>
}

class DefaultScheduledNotificationRepository(private val api: ApiClient) : ScheduledNotificationRepository {

    override suspend fun datatable(
        page: Int,
        limit: Int,
        search: String?,
        status: String?,
    ): ApiResult<List<ScheduledNotification>> {
        val filters = buildMap {
            search?.takeIf { it.isNotBlank() }?.let { put("search", it) }
            status?.takeIf { it.isNotBlank() }?.let { put("status", it) }
        }
        return api.post<DatatableDto<ScheduledNotificationDto>>(
            "/scheduled-notifications/datatable",
            DataTableParams(page = page, limit = limit, filters = filters),
        ).map { result -> result.rows.map { it.toModel() } }
    }

    override suspend fun create(draft: ScheduledNotificationDraft): ApiResult<ScheduledNotification> =
        api.post<ScheduledNotificationDto>("/scheduled-notifications", draft.toRequest()).map { it.toModel() }

    override suspend fun updateActive(id: String, active: Boolean): ApiResult<ScheduledNotification> =
        api.put<ScheduledNotificationDto>("/scheduled-notifications/$id", UpdateActiveRequest(active)).map { it.toModel() }

    override suspend fun delete(id: String): ApiResult<Unit> =
        when (val result = api.delete<Boolean>("/scheduled-notifications/$id")) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
            is ApiResult.Failure -> result
        }
}

private fun ScheduledNotificationDto.toModel(): ScheduledNotification = ScheduledNotification(
    id = id,
    title = title,
    message = message,
    type = type,
    channel = channel,
    persistent = persistent,
    frequency = frequency ?: "ONCE",
    timeOfDay = timeOfDay,
    daysOfWeek = daysOfWeek,
    scheduledAt = scheduledAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() },
    active = active,
    sendCount = sendCount,
    lastSentAt = lastSentAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() },
    nextSendAt = nextSendAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() },
    targetUserName = targetUser?.let { listOfNotNull(it.name, it.lastName).joinToString(" ").trim() },
)

private fun ScheduledNotificationDraft.toRequest(): ScheduledNotificationRequest = ScheduledNotificationRequest(
    message = message,
    title = title,
    type = type,
    channel = channel,
    persistent = persistent,
    frequency = frequency,
    timeOfDay = timeOfDay,
    daysOfWeek = daysOfWeek,
    scheduledAt = scheduledAt,
    userId = userId,
    active = active,
)
