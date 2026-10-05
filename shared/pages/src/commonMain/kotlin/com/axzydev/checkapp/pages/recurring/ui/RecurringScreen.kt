package com.axzydev.checkapp.pages.recurring.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITConfirmDialog
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITFooterDivider
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSearchField
import com.axzydev.checkapp.design.components.ITStatCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.recurring.viewmodel.RecurringAction
import com.axzydev.checkapp.pages.recurring.viewmodel.RecurringUiState

/**
 * Listado de rutas recurrentes.
 *
 * Cada tarjeta muestra cuántos puntos de control tiene la ruta: es el dato que
 * dice si una ruta está completa o a medio armar, y evita tener que entrar a
 * editarla para comprobarlo.
 */
@Composable
fun RecurringScreen(
    state: RecurringUiState,
    onAction: (RecurringAction) -> Unit,
    onNewRoute: () -> Unit,
    onEditRoute: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(RecurringAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin rutas",
            description = "Todavía no hay recorridos programados.",
            actionLabel = "Nueva ruta",
            onAction = onNewRoute,
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ninguna ruta coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(RecurringAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Rutas recurrentes",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} rutas",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Nueva",
                onClick = onNewRoute,
                tone = Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.items.isNotEmpty()) {
            val empty = state.items.count { it.pointCount == 0 }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Rutas",
                    value = state.items.size.toString(),
                    icon = ITIcons.Repeat,
                    tone = Tone.Brand,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Sin puntos",
                    value = empty.toString(),
                    icon = ITIcons.Place,
                    tone = if (empty > 0) Tone.Warning else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(RecurringAction.Search(it)) },
            placeholder = "Buscar ruta…",
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

        state.visibleItems.forEach { route ->
            ITListItem(
                title = route.title,
                subtitle = if (route.pointCount == 1) "1 punto" else "${route.pointCount} puntos",
                avatarInitial = route.title,
                avatarStatus = Tone.Brand,
                badge = {
                    ITBadge(
                        text = "${route.pointCount} pts",
                        tone = if (route.pointCount == 0) Tone.Warning else Tone.Neutral,
                    )
                },
                onClick = { onEditRoute(route.id) },
                footer = {
                    ITCardFooter {
                        ITFooterAction(
                            label = "Editar",
                            onClick = { onEditRoute(route.id) },
                        )
                        ITFooterDivider()
                        ITFooterAction(
                            label = "Eliminar",
                            onClick = { onAction(RecurringAction.RequestDelete(route.id)) },
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
            onClick = { onAction(RecurringAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar ruta",
            message = "¿Seguro que quieres eliminar «${state.pendingDeleteTitle.orEmpty()}»? " +
                "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(RecurringAction.ConfirmDelete) },
            onDismiss = { onAction(RecurringAction.CancelDelete) },
        )
    }
}
