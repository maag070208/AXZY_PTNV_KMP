package com.axzydev.checkapp.entities.schedulednotification.model

/** Notificación programada (recurrente o de una sola vez). */
data class ScheduledNotification(
    val id: String,
    val title: String?,
    val message: String,
    val type: String,
    val channel: String,
    val persistent: Boolean,
    val frequency: String,
    val timeOfDay: String?,
    val daysOfWeek: String?,
    val scheduledAt: Long?,
    val active: Boolean,
    val sendCount: Int,
    val lastSentAt: Long?,
    val nextSendAt: Long?,
    val targetUserName: String?,
)

data class ScheduledNotificationDraft(
    val message: String,
    val title: String? = null,
    val type: String = "info",
    val channel: String = "global",
    val persistent: Boolean = false,
    val frequency: String = "ONCE",
    val timeOfDay: String? = null,
    val daysOfWeek: String? = null,
    /** ISO 8601 para programaciones de una sola vez. */
    val scheduledAt: String? = null,
    val userId: String? = null,
    val active: Boolean = true,
)

val ScheduledFrequencies: List<String> =
    listOf("ONCE", "DAILY", "EVERY_2_DAYS", "WEEKLY", "EVERY_2_WEEKS", "MONTHLY")
