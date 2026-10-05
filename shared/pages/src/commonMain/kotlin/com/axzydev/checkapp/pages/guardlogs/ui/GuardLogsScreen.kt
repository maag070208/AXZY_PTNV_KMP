package com.axzydev.checkapp.pages.guardlogs.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.core.common.format.DateFormat
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
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
import com.axzydev.checkapp.pages.guardlogs.viewmodel.GuardLogItemUi
import com.axzydev.checkapp.pages.guardlogs.viewmodel.GuardLogsAction
import com.axzydev.checkapp.pages.guardlogs.viewmodel.GuardLogsUiState

/**
 * Prenómina: entradas y salidas del personal.
 *
 * El turno **abierto** se marca con una insignia de aviso, no con color de fondo:
 * un guardia que entró y no ha salido es lo que el administrador viene a buscar
 * aquí, y tiene que saltar a la vista sin teñir toda la fila.
 *
 * La duración se muestra calculada porque restar horas mentalmente es justo lo que
 * esta pantalla viene a evitar.
 */
@Composable
fun GuardLogsScreen(
    state: GuardLogsUiState,
    onAction: (GuardLogsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(GuardLogsAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin registros",
            description = "Todavía no hay entradas ni salidas de personal.",
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Nada coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(GuardLogsAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Prenómina",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} turnos",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        if (state.items.isNotEmpty()) {
            val open = state.items.count { it.logoutAt == null }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Abiertos",
                    value = open.toString(),
                    icon = ITIcons.Walk,
                    tone = if (open > 0) Tone.Warning else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Cerrados",
                    value = (state.items.size - open).toString(),
                    icon = ITIcons.ShieldCheck,
                    tone = Tone.Success,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(GuardLogsAction.Search(it)) },
            placeholder = "Buscar guardia…",
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

        state.visibleItems.forEach { log -> GuardLogRow(log) }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(GuardLogsAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }
}

/** Fila de turno: quién, cuándo entró, cuándo salió y cuánto duró. */
@Composable
private fun GuardLogRow(log: GuardLogItemUi) {
    val open = log.logoutAt == null

    ITListItem(
        title = log.guardName,
        subtitle = "Entrada ${DateFormat.dateTime(log.loginAt)}" +
            (log.logoutAt?.let { " · Salida ${DateFormat.dateTime(it)}" } ?: ""),
        avatarInitial = log.guardName,
        avatarStatus = if (open) Tone.Warning else Tone.Success,
        badge = if (open) {
            { ITBadge(text = "Abierto", tone = Tone.Warning, dot = true) }
        } else {
            log.durationMinutes?.let { minutes ->
                { ITBadge(text = formatDuration(minutes), tone = Tone.Neutral) }
            }
        },
    )
}

/** "480" → "8h 0m". */
private fun formatDuration(minutes: Long): String = "${minutes / 60}h ${minutes % 60}m"
