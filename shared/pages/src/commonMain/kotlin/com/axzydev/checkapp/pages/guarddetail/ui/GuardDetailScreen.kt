package com.axzydev.checkapp.pages.guarddetail.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITAvatar
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.guarddetail.viewmodel.GuardDetailAction
import com.axzydev.checkapp.pages.guarddetail.viewmodel.GuardDetailUiState

/**
 * Ficha de un guardia.
 *
 * El historial de asistencia y las asignaciones llegarán en fases posteriores; se
 * dice explícitamente en pantalla en lugar de dejar un hueco vacío, que se lee
 * como un error de carga.
 */
@Composable
fun GuardDetailScreen(
    state: GuardDetailUiState,
    onAction: (GuardDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading -> ScreenState.Loading
        state.error != null -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(GuardDetailAction.Refresh) },
        )
        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Ficha del guardia",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Actualizar",
                onClick = { onAction(GuardDetailAction.Refresh) },
                outlined = true,
                tone = Tone.Neutral,
                compact = true,
            )
        },
    ) {
        // Cabecera con avatar: la identidad del guardia primero.
        ITCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.lg),
            ) {
                ITAvatar(
                    initial = state.name,
                    size = 64.dp,
                    status = if (state.active) Tone.Success else Tone.Neutral,
                )
                Column(modifier = Modifier.weight(1f)) {
                    ITText(
                        text = state.name,
                        color = AxzyColors.onSurface,
                        style = AxzyType.screenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ITText(
                        text = "@${state.username}",
                        color = AxzyColors.onSurfaceVariant,
                        style = AxzyType.cardBody,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(AxzySpacing.sm))
                    ITBadge(
                        text = if (state.active) "Activo" else "Inactivo",
                        tone = if (state.active) Tone.Success else Tone.Neutral,
                        dot = true,
                    )
                }
            }
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                ITText(
                    text = "Próximamente",
                    color = AxzyColors.onSurfaceVariant,
                    style = AxzyType.sectionLabel,
                )
            }
            Spacer(Modifier.height(AxzySpacing.sm))
            ITText(
                text = "El historial de asistencia y las asignaciones de este guardia " +
                    "todavía no están disponibles en la app.",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.cardBody,
            )
        }
    }
}
