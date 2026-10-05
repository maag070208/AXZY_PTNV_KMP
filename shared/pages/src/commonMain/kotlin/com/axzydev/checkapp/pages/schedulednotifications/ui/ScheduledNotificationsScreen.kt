package com.axzydev.checkapp.pages.schedulednotifications.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.core.common.format.DateFormat
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITConfirmDialog
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITFooterDivider
import com.axzydev.checkapp.design.components.ITFormDialog
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSearchField
import com.axzydev.checkapp.design.components.ITStatCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForStatus
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.schedulednotifications.viewmodel.ScheduledAction
import com.axzydev.checkapp.pages.schedulednotifications.viewmodel.ScheduledItemUi
import com.axzydev.checkapp.pages.schedulednotifications.viewmodel.ScheduledUiState

/**
 * Avisos programados.
 *
 * Cada aviso lleva su **frecuencia** y, si aplica, la hora. El interruptor de
 * activo/inactivo va en el pie de la tarjeta, porque es la acción más habitual:
 * desactivar un aviso sin borrarlo para no perder su configuración.
 */
@Composable
fun ScheduledNotificationsScreen(
    state: ScheduledUiState,
    onAction: (ScheduledAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(ScheduledAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin avisos programados",
            description = "Todavía no hay notificaciones recurrentes.",
            actionLabel = "Nuevo aviso",
            onAction = { onAction(ScheduledAction.ToggleForm) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Nada coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(ScheduledAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Avisos programados",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} avisos",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Nuevo",
                onClick = { onAction(ScheduledAction.ToggleForm) },
                tone = Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.items.isNotEmpty()) {
            val active = state.items.count { it.active }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Activos",
                    value = active.toString(),
                    icon = ITIcons.Bell,
                    tone = if (active > 0) Tone.Brand else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Pausados",
                    value = (state.items.size - active).toString(),
                    icon = ITIcons.Close,
                    tone = Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(ScheduledAction.Search(it)) },
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

        state.visibleItems.forEach { item -> ScheduledRow(item = item, state = state, onAction = onAction) }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(ScheduledAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    // Alta como pantalla modal.
    ITFormDialog(
        isOpen = state.showForm,
        title = "Nuevo aviso programado",
        onDismiss = { onAction(ScheduledAction.ToggleForm) },
        onConfirm = { onAction(ScheduledAction.Create) },
        confirmLabel = "Crear aviso",
        confirmEnabled = state.message.isNotBlank() && !state.creating,
        confirmLoading = state.creating,
    ) {
        ITTextField(
            label = "Título",
            value = state.title,
            onValueChange = { onAction(ScheduledAction.Title(it)) },
        )
        ITTextField(
            label = "Mensaje *",
            value = state.message,
            onValueChange = { onAction(ScheduledAction.Message(it)) },
            singleLine = false,
        )

        ITText(text = "Frecuencia", color = AxzyColors.onSurfaceVariant, style = AxzyType.labelSmall)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            listOf("ONCE" to "Una vez", "DAILY" to "Diario", "WEEKLY" to "Semanal").forEach { (value, label) ->
                ITTouchableOpacity(
                    onClick = { onAction(ScheduledAction.Frequency(value)) },
                    modifier = Modifier.weight(1f),
                    scaleTo = 0.95f,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(44.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        ITBadge(
                            text = label,
                            tone = if (state.frequency == value) Tone.Brand else Tone.Neutral,
                            dot = state.frequency == value,
                        )
                    }
                }
            }
        }

        if (state.frequency != "ONCE") {
            ITTextField(
                label = "Hora del día (HH:mm)",
                value = state.timeOfDay,
                onValueChange = { onAction(ScheduledAction.TimeOfDay(it)) },
                placeholder = "09:00",
                numeric = true,
            )
        }
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar aviso",
            message = "¿Seguro que quieres eliminar «${state.pendingDeleteTitle.orEmpty()}»? " +
                "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(ScheduledAction.ConfirmDelete) },
            onDismiss = { onAction(ScheduledAction.CancelDelete) },
        )
    }
}

/** Fila de aviso programado. */
@Composable
private fun ScheduledRow(
    item: ScheduledItemUi,
    state: ScheduledUiState,
    onAction: (ScheduledAction) -> Unit,
) {
    ITListItem(
        title = item.title.orEmpty().ifBlank { "Aviso" },
        subtitle = item.message,
        meta = item.nextSendAt?.let { "Próximo: ${DateFormat.dateTime(it)}" },
        avatarInitial = item.title.orEmpty().ifBlank { item.message },
        avatarStatus = if (item.active) Tone.Success else Tone.Neutral,
        badge = {
            ITBadge(
                text = if (item.active) "Activo" else "Pausado",
                tone = toneForStatus(if (item.active) "ACTIVE" else "INACTIVE"),
                dot = item.active,
            )
        },
        footer = {
            ITCardFooter {
                ITFooterAction(
                    label = if (item.active) "Pausar" else "Activar",
                    onClick = { onAction(ScheduledAction.ToggleActive(item.id, !item.active)) },
                    enabled = state.togglingId != item.id,
                )
                ITFooterDivider()
                ITFooterAction(
                    label = "Eliminar",
                    onClick = { onAction(ScheduledAction.RequestDelete(item.id)) },
                    tone = Tone.Danger,
                    enabled = !state.deleting,
                )
            }
        },
    )
}


