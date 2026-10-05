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
                label = if (state.showForm) "Cancelar" else "Nueva",
                onClick = { onAction(ZonesAction.ToggleForm) },
                outlined = state.showForm,
                tone = if (state.showForm) Tone.Neutral else Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.showForm) {
            ZoneFormCard(
                name = state.name,
                creating = state.creating,
                onName = { onAction(ZonesAction.Name(it)) },
                onSubmit = { onAction(ZonesAction.Create) },
            )
        }

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

    if (state.editingId != null) {
        AlertDialog(
            onDismissRequest = { onAction(ZonesAction.CancelEdit) },
            containerColor = AxzyColors.surface,
            shape = AxzyShape.lg,
            title = {
                ITText(text = "Editar zona", color = AxzyColors.onSurface, style = AxzyType.cardTitle)
            },
            text = {
                ITTextField(
                    label = "Nombre *",
                    value = state.editName,
                    onValueChange = { onAction(ZonesAction.EditName(it)) },
                )
            },
            confirmButton = {
                ITButton(
                    label = "Guardar",
                    onClick = { onAction(ZonesAction.SaveEdit) },
                    loading = state.saving,
                    enabled = !state.saving && state.editName.isNotBlank(),
                    compact = true,
                )
            },
            dismissButton = {
                ITButton(
                    label = "Cancelar",
                    onClick = { onAction(ZonesAction.CancelEdit) },
                    outlined = true,
                    tone = Tone.Neutral,
                    enabled = !state.saving,
                    compact = true,
                )
            },
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

/** Formulario de alta. */
@Composable
private fun ZoneFormCard(
    name: String,
    creating: Boolean,
    onName: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    ITCard(modifier = Modifier.fillMaxWidth()) {
        ITText(text = "Nueva zona", color = AxzyColors.onSurface, style = AxzyType.cardTitle)
        Spacer(Modifier.height(AxzySpacing.md))

        ITTextField(label = "Nombre *", value = name, onValueChange = onName)

        Spacer(Modifier.height(AxzySpacing.lg))
        ITButton(
            label = "Crear zona",
            onClick = onSubmit,
            loading = creating,
            enabled = name.isNotBlank() && !creating,
        )
    }
}
