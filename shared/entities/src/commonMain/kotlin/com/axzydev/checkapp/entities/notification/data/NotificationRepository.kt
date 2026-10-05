package com.axzydev.checkapp.entities.notification.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.common.time.isoToEpochMillis
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.notification.model.Notification
import com.axzydev.checkapp.entities.notification.model.NotificationDraft
import com.axzydev.checkapp.entities.notification.model.NotificationFeed
import kotlinx.serialization.Serializable

@Serializable
private data class NotificationDto(
    val id: String,
    val title: String? = null,
    val message: String,
    val type: String = "info",
    val read: Boolean = false,
    val createdAt: String? = null,
)

@Serializable
private data class NotificationFeedDto(
    val notifications: List<NotificationDto> = emptyList(),
    val unreadCount: Int = 0,
)

@Serializable
private data class SendNotificationRequest(
    val message: String,
    val title: String? = null,
    val type: String = "info",
    val channel: String = "global",
    val userId: String? = null,
    val persistent: Boolean = false,
)

interface NotificationRepository {
    /** Bandeja del usuario en sesión (online). */
    suspend fun my(unreadOnly: Boolean): ApiResult<NotificationFeed>
    suspend fun markRead(id: String): ApiResult<Unit>
    suspend fun markAllRead(): ApiResult<Unit>
    /** Envía una notificación ahora (Ably + FCM en el backend). */
    suspend fun send(draft: NotificationDraft): ApiResult<Unit>
}

class DefaultNotificationRepository(private val api: ApiClient) : NotificationRepository {

    override suspend fun my(unreadOnly: Boolean): ApiResult<NotificationFeed> =
        api.get<NotificationFeedDto>(
            "/notifications/my",
            if (unreadOnly) mapOf("unreadOnly" to "true") else emptyMap(),
        ).map { dto ->
            NotificationFeed(
                notifications = dto.notifications.map { it.toModel() },
                unreadCount = dto.unreadCount,
            )
        }

    override suspend fun markRead(id: String): ApiResult<Unit> =
        asUnit(api.patch<Boolean>("/notifications/$id/read", null))

    override suspend fun markAllRead(): ApiResult<Unit> =
        asUnit(api.patch<Boolean>("/notifications/read-all", null))

    override suspend fun send(draft: NotificationDraft): ApiResult<Unit> =
        asUnit(
            api.post<Boolean>(
                "/notifications/send",
                SendNotificationRequest(
                    message = draft.message,
                    title = draft.title,
                    type = draft.type,
                    channel = draft.channel,
                    userId = draft.userId,
                    persistent = draft.persistent,
                ),
            ),
        )

    private fun asUnit(result: ApiResult<*>): ApiResult<Unit> = when (result) {
        is ApiResult.Success -> ApiResult.Success(Unit, result.messages)
        is ApiResult.Failure -> result
    }
}

private fun NotificationDto.toModel(): Notification = Notification(
    id = id,
    title = title,
    message = message,
    type = type,
    read = read,
    createdAt = createdAt?.let { runCatching { it.isoToEpochMillis() }.getOrNull() } ?: 0L,
)
