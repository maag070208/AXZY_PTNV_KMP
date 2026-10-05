package com.axzydev.checkapp.pages.guards.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.axzydev.checkapp.pages.guards.viewmodel.GuardsAction
import com.axzydev.checkapp.pages.guards.viewmodel.GuardsUiState

/**
 * Listado de guardias.
 *
 * Plantilla de Clientes: barra fija, buscador, tarjeta por guardia y borrado con
 * confirmación. El guardia se identifica por nombre y usuario porque dos personas
 * pueden llamarse igual y el usuario de la app es único.
 */
@Composable
fun GuardsScreen(
    state: GuardsUiState,
    onAction: (GuardsAction) -> Unit,
    onOpenGuard: (guardId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(GuardsAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin guardias",
            description = "Todavía no hay personal operativo registrado.",
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ningún guardia coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(GuardsAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Guardias",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} registrados",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        if (state.items.isNotEmpty()) {
            ITStatCard(
                label = "Personal operativo",
                value = state.items.size.toString(),
                icon = ITIcons.ShieldCheck,
                tone = Tone.Success,
                hint = "Cuentas de guardia registradas",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(GuardsAction.Search(it)) },
            placeholder = "Buscar por nombre o usuario…",
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

        state.visibleItems.forEach { guard ->
            ITListItem(
                title = guard.name,
                subtitle = "@${guard.username}",
                avatarInitial = guard.name,
                avatarStatus = Tone.Success,
                onClick = { onOpenGuard(guard.id) },
                footer = {
                    ITCardFooter {
                        ITFooterAction(
                            label = "Ver ficha",
                            onClick = { onOpenGuard(guard.id) },
                        )
                        ITFooterDivider()
                        ITFooterAction(
                            label = "Eliminar",
                            onClick = { onAction(GuardsAction.RequestDelete(guard.id)) },
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
            onClick = { onAction(GuardsAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar guardia",
            message = "¿Seguro que quieres eliminar a «${state.pendingDeleteName.orEmpty()}»? " +
                "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(GuardsAction.ConfirmDelete) },
            onDismiss = { onAction(GuardsAction.CancelDelete) },
        )
    }
}
