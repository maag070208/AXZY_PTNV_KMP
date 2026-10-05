package com.axzydev.checkapp.features.notifications.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.notification.data.NotificationRepository
import com.axzydev.checkapp.entities.notification.model.NotificationDraft
import com.axzydev.checkapp.entities.notification.model.NotificationFeed
import com.axzydev.checkapp.entities.schedulednotification.data.ScheduledNotificationRepository
import com.axzydev.checkapp.entities.schedulednotification.model.ScheduledNotification
import com.axzydev.checkapp.entities.schedulednotification.model.ScheduledNotificationDraft

class ListNotificationsUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(unreadOnly: Boolean = false): ApiResult<NotificationFeed> = repository.my(unreadOnly)
}

class MarkNotificationReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.markRead(id)
}

class MarkAllNotificationsReadUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(): ApiResult<Unit> = repository.markAllRead()
}

/** Envía una notificación ahora (Ably + FCM). */
class SendNotificationUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(draft: NotificationDraft): ApiResult<Unit> = repository.send(draft)
}

// --- Notificaciones programadas ---

class ListScheduledNotificationsUseCase(private val repository: ScheduledNotificationRepository) {
    suspend operator fun invoke(
        page: Int = 1,
        limit: Int = 20,
        search: String? = null,
        status: String? = null,
    ): ApiResult<List<ScheduledNotification>> = repository.datatable(page, limit, search, status)
}

class CreateScheduledNotificationUseCase(private val repository: ScheduledNotificationRepository) {
    suspend operator fun invoke(draft: ScheduledNotificationDraft): ApiResult<ScheduledNotification> =
        repository.create(draft)
}

class ToggleScheduledNotificationUseCase(private val repository: ScheduledNotificationRepository) {
    suspend operator fun invoke(id: String, active: Boolean): ApiResult<ScheduledNotification> =
        repository.updateActive(id, active)
}

class DeleteScheduledNotificationUseCase(private val repository: ScheduledNotificationRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
