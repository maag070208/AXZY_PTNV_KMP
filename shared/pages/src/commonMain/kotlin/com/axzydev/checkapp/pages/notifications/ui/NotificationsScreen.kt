package com.axzydev.checkapp.pages.notifications.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.core.common.format.DateFormat
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSearchField
import com.axzydev.checkapp.design.components.ITStatCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.pages.notifications.viewmodel.NotificationsAction
import com.axzydev.checkapp.pages.notifications.viewmodel.NotificationsUiState
import com.axzydev.checkapp.pages.notifications.viewmodel.NotificationItemUi

/**
 * Bandeja de notificaciones.
 *
 * Tiene dos filtros que se combinan: la **búsqueda** de texto y el interruptor de
 * "sólo no leídas". Por eso la lista visible no sale sólo de la fundación común,
 * sino que el contrato la sobrescribe para aplicar los dos.
 *
 * Las no leídas se marcan con un punto de color y el texto en negrita, no con un
 * fondo distinto: así se distingue el estado sin que la lista parezca tener filas
 * seleccionadas.
 */
@Composable
fun NotificationsScreen(
    state: NotificationsUiState,
    onAction: (NotificationsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(NotificationsAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin notificaciones",
            description = "No tienes avisos por ahora.",
        )

        state.visibleItems.isEmpty() && state.unreadOnly -> ScreenState.Empty(
            title = "Todo leído",
            description = "No quedan notificaciones sin leer.",
            actionLabel = "Ver todas",
            onAction = { onAction(NotificationsAction.ToggleUnreadOnly) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ninguna notificación coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(NotificationsAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Notificaciones",
        subtitle = if (state.unreadCount > 0) "${state.unreadCount} sin leer" else "Todo leído",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        if (state.items.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Sin leer",
                    value = state.unreadCount.toString(),
                    icon = ITIcons.Bell,
                    tone = if (state.unreadCount > 0) Tone.Brand else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Total",
                    value = state.items.size.toString(),
                    icon = ITIcons.Layers,
                    tone = Tone.Info,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UnreadFilterChip(
                active = state.unreadOnly,
                unreadCount = state.unreadCount,
                onClick = { onAction(NotificationsAction.ToggleUnreadOnly) },
            )
            Spacer(Modifier.weight(1f))
            if (state.unreadCount > 0) {
                ITButton(
                    label = "Marcar todo leído",
                    onClick = { onAction(NotificationsAction.MarkAllRead) },
                    outlined = true,
                    tone = Tone.Neutral,
                    loading = state.markingAll,
                    enabled = !state.markingAll,
                    compact = true,
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(NotificationsAction.Search(it)) },
            placeholder = "Buscar aviso…",
            leadingIcon = {
                Icon(
                    imageVector = ITIcons.Search,
                    contentDescription = null,
                    tint = AxzyColors.slate400,
                    modifier = Modifier.size(20.dp),
                )
            },
            enabled = state.items.isNotEmpty(),
        )

        if (state.error != null && state.items.isNotEmpty()) {
            ITText(text = state.error, color = AxzyColors.error, style = AxzyType.labelSmall)
        }

        state.visibleItems.forEach { notification ->
            NotificationRow(
                notification = notification,
                marking = state.markingId == notification.id,
                onMarkRead = { onAction(NotificationsAction.MarkRead(notification.id)) },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(NotificationsAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }
}

/**
 * Fila de notificación.
 *
 * El icono cambia según el tipo de aviso (alerta, aviso, información), que es lo
 * que permite recorrer la bandeja de un vistazo sin leer cada mensaje.
 */
@Composable
private fun NotificationRow(
    notification: NotificationItemUi,
    marking: Boolean,
    onMarkRead: () -> Unit,
) {
    val tone = toneForType(notification.type)

    ITListItem(
        title = notification.title.orEmpty().ifBlank { "Aviso" },
        subtitle = notification.message,
        meta = DateFormat.relative(notification.createdAt),
        avatarInitial = notification.title.orEmpty().ifBlank { notification.message },
        avatarStatus = if (notification.read) Tone.Neutral else tone,
        badge = if (notification.read) null else {
            { ITBadge(text = "Nuevo", tone = tone, dot = true) }
        },
        footer = if (notification.read) null else {
            {
                ITCardFooter {
                    ITFooterAction(
                        label = "Marcar como leída",
                        onClick = onMarkRead,
                        enabled = !marking,
                    )
                }
            }
        },
    )
}

/** Interruptor de "sólo no leídas", con el contador dentro. */
@Composable
private fun UnreadFilterChip(
    active: Boolean,
    unreadCount: Int,
    onClick: () -> Unit,
) {
    val colors = Tone.Brand.palette

    ITTouchableOpacity(onClick = onClick, scaleTo = 0.96f) {
        Box(
            modifier = Modifier
                .background(
                    color = if (active) colors.solid else AxzyColors.surfaceVariant,
                    shape = AxzyShape.pill,
                )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = AxzySpacing.md, vertical = AxzySpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                ITText(
                    text = if (active) "Sólo sin leer" else "Todas",
                    color = if (active) AxzyColors.surface else AxzyColors.onSurfaceVariant,
                    style = AxzyType.button,
                )
                if (unreadCount > 0) {
                    ITBadge(
                        text = unreadCount.toString(),
                        tone = if (active) Tone.Neutral else Tone.Brand,
                    )
                }
            }
        }
    }
}

/** Tipo de aviso → tono. */
private fun toneForType(type: String): Tone = when (type.uppercase()) {
    "ALERT", "ERROR", "PANIC" -> Tone.Danger
    "WARNING" -> Tone.Warning
    "SUCCESS" -> Tone.Success
    else -> Tone.Info
}
