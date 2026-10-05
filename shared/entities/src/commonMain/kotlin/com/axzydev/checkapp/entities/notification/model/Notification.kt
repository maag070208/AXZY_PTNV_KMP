package com.axzydev.checkapp.entities.notification.model

/** Notificación personal del usuario. */
data class Notification(
    val id: String,
    val title: String?,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: Long,
)

/** Bandeja del usuario: notificaciones + contador de no leídas. */
data class NotificationFeed(
    val notifications: List<Notification>,
    val unreadCount: Int,
)

/** Datos para enviar una notificación (ahora). */
data class NotificationDraft(
    val message: String,
    val title: String? = null,
    val type: String = "info",
    val channel: String = "global",
    val userId: String? = null,
    val persistent: Boolean = false,
)

/** Tipos válidos del backend. */
val NotificationTypes: List<String> = listOf("info", "success", "warning", "error")
