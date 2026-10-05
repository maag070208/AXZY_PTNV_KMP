package com.axzydev.checkapp.pages.rounddetail.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.core.common.format.DateFormat
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForStatus
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.rounddetail.viewmodel.RoundDetailAction
import com.axzydev.checkapp.pages.rounddetail.viewmodel.RoundDetailUiState

/**
 * Detalle de un recorrido, en línea de tiempo.
 *
 * La línea se dibuja con un punto y una guía vertical: sin ella, una lista de
 * horas es un bloque de texto y se pierde la secuencia, que es justo lo que se
 * viene a ver.
 */
@Composable
fun RoundDetailScreen(
    state: RoundDetailUiState,
    onAction: (RoundDetailAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading -> ScreenState.Loading
        state.error != null -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(RoundDetailAction.Refresh) },
        )
        state.timeline.isEmpty() -> ScreenState.Empty(
            title = "Sin registros",
            description = "Este recorrido todavía no tiene puntos escaneados.",
        )
        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Detalle del recorrido",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITBadge(text = state.status, tone = toneForStatus(state.status), dot = true)
        },
    ) {
        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITText(
                text = "Línea de tiempo",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.sectionLabel,
            )
            Spacer(Modifier.height(AxzySpacing.md))

            state.timeline.forEachIndexed { index, item ->
                TimelineRow(
                    label = item.label,
                    time = DateFormat.dateTime(item.timestamp),
                    detail = item.detail,
                    isLast = index == state.timeline.lastIndex,
                )
            }
        }
    }
}

/** Un hito de la línea de tiempo, con su guía vertical. */
@Composable
private fun TimelineRow(
    label: String,
    time: String,
    detail: String?,
    isLast: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.width(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(AxzyColors.primary, AxzyShape.pill),
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(44.dp)
                        .background(AxzyColors.outlineVariant),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = AxzySpacing.sm, bottom = if (isLast) 0.dp else AxzySpacing.md),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ITText(
                text = label,
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle,
            )
            ITText(
                text = time,
                color = AxzyColors.slate400,
                style = AxzyType.labelSmall,
            )
            detail?.let {
                ITText(
                    text = it,
                    color = AxzyColors.onSurfaceVariant,
                    style = AxzyType.itemMeta,
                )
            }
        }
    }
}
