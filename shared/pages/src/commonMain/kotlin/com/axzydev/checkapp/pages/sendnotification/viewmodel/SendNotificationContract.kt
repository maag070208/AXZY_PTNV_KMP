package com.axzydev.checkapp.pages.sendnotification.viewmodel

data class SendNotificationUiState(
    val title: String = "",
    val message: String = "",
    val type: String = "info",
    val persistent: Boolean = false,
    val userId: String = "",
    val sending: Boolean = false,
    val messageOk: String? = null,
    val error: String? = null,
)

sealed interface SendNotificationAction {
    data class Title(val value: String) : SendNotificationAction
    data class Message(val value: String) : SendNotificationAction
    data class Type(val value: String) : SendNotificationAction
    data object TogglePersistent : SendNotificationAction
    data class UserId(val value: String) : SendNotificationAction
    data object Send : SendNotificationAction
    data object Dismiss : SendNotificationAction
}
