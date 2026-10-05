package com.axzydev.checkapp.pages.zones.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForStatus
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.zones.viewmodel.ZonesAction
import com.axzydev.checkapp.pages.zones.viewmodel.ZonesUiState

/**
 * Listado de zonas.
 *
 * Plantilla de Clientes. Las zonas son el nivel intermedio entre cliente y punto
 * de control, así que se marcan como activas o inactivas: una zona inactiva deja
 * de aparecer al crear rutas, y por eso el estado se muestra en la tarjeta.
 */
@Composable
fun ZonesScreen(
    state: ZonesUiState,
    onAction: (ZonesAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(ZonesAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin zonas",
            description = "Todavía no hay zonas definidas.",
            actionLabel = "Nueva zona",
            onAction = { onAction(ZonesAction.ToggleForm) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ninguna zona coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(ZonesAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Zonas",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} zonas",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Nueva",
                onClick = { onAction(ZonesAction.ToggleForm) },
                tone = Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.items.isNotEmpty()) {
            val active = state.items.count { it.active }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Activas",
                    value = active.toString(),
                    icon = ITIcons.Place,
                    tone = Tone.Success,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Inactivas",
                    value = (state.items.size - active).toString(),
                    icon = ITIcons.Layers,
                    tone = if (state.items.size - active > 0) Tone.Neutral else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(ZonesAction.Search(it)) },
            placeholder = "Buscar zona…",
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

        state.visibleItems.forEach { zone ->
            ITListItem(
                title = zone.name,
                avatarInitial = zone.name,
                avatarStatus = if (zone.active) Tone.Success else Tone.Neutral,
                badge = {
                    ITBadge(
                        text = if (zone.active) "Activa" else "Inactiva",
                        tone = toneForStatus(if (zone.active) "ACTIVE" else "INACTIVE"),
                        dot = true,
                    )
                },
                onClick = { onAction(ZonesAction.StartEdit(zone.id)) },
                footer = {
                    ITCardFooter {
                        ITFooterAction(
                            label = "Editar",
                            onClick = { onAction(ZonesAction.StartEdit(zone.id)) },
                        )
                        ITFooterDivider()
                        ITFooterAction(
                            label = "Eliminar",
                            onClick = { onAction(ZonesAction.RequestDelete(zone.id)) },
                            tone = Tone.Danger,
                            enabled = !state.deleting,
                        )
                    }
                },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(ZonesAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    // Alta como pantalla modal.
    ITFormDialog(
        isOpen = state.showForm && state.editingId == null,
        title = "Nueva zona",
        onDismiss = { onAction(ZonesAction.ToggleForm) },
        onConfirm = { onAction(ZonesAction.Create) },
        confirmLabel = "Crear zona",
        confirmEnabled = state.name.isNotBlank() && !state.creating,
        confirmLoading = state.creating,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.name,
            onValueChange = { onAction(ZonesAction.Name(it)) },
            enabled = !state.creating,
        )
    }

    // Edición como pantalla modal.
    ITFormDialog(
        isOpen = state.editingId != null,
        title = "Editar zona",
        onDismiss = { onAction(ZonesAction.CancelEdit) },
        onConfirm = { onAction(ZonesAction.SaveEdit) },
        confirmLabel = "Guardar",
        confirmEnabled = state.editName.isNotBlank() && !state.saving,
        confirmLoading = state.saving,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.editName,
            onValueChange = { onAction(ZonesAction.EditName(it)) },
        )
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar zona",
            message = "¿Seguro que quieres eliminar «${state.pendingDeleteName.orEmpty()}»? " +
                "Los puntos de control que tenga dentro se quedarán sin zona.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(ZonesAction.ConfirmDelete) },
            onDismiss = { onAction(ZonesAction.CancelDelete) },
        )
    }
}


