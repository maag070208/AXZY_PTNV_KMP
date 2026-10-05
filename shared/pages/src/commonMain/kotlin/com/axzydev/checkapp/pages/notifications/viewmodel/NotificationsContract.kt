package com.axzydev.checkapp.pages.notifications.viewmodel

import com.axzydev.checkapp.core.common.list.QueryableListState

data class NotificationItemUi(
    val id: String,
    val title: String?,
    val message: String,
    val type: String,
    val read: Boolean,
    val createdAt: Long,
)

data class NotificationsUiState(
    val loading: Boolean = true,
    override val items: List<NotificationItemUi> = emptyList(),
    override val query: String = "",
    val unreadCount: Int = 0,
    val unreadOnly: Boolean = false,
    val markingId: String? = null,
    val markingAll: Boolean = false,
    val error: String? = null,
) : QueryableListState<NotificationItemUi> {
    /** Campos por los que busca el filtro de esta pantalla. */
    override val searchFields: List<(NotificationItemUi) -> String?>
        get() = listOf(
        { it.title },
        { it.message },
        )

    /** Además de la búsqueda, respeta el filtro de no leídas. */
    override val visibleItems: List<NotificationItemUi>
        get() = super.visibleItems.let { if (unreadOnly) it.filter { n -> !n.read } else it }
}

sealed interface NotificationsAction {
    data object Refresh : NotificationsAction
    data class Search(val value: String) : NotificationsAction
    data object ToggleUnreadOnly : NotificationsAction
    data class MarkRead(val id: String) : NotificationsAction
    data object MarkAllRead : NotificationsAction
}

sealed interface NotificationsEffect {
    data object MarkedRead : NotificationsEffect
    data object MarkedAllRead : NotificationsEffect
    data class Error(val message: String) : NotificationsEffect
}
