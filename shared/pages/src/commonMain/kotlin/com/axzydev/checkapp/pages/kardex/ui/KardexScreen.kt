package com.axzydev.checkapp.pages.kardex.ui

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
import com.axzydev.checkapp.pages.kardex.viewmodel.KardexAction
import com.axzydev.checkapp.pages.kardex.viewmodel.KardexUiState

/**
 * Historial de rondas (kardex).
 *
 * Pantalla de **sólo lectura**: es el registro de lo que ya pasó, así que no hay
 * alta, edición ni borrado. Se busca y se ordena por lo más reciente, que es lo
 * único que se hace con un historial.
 */
@Composable
fun KardexScreen(
    state: KardexUiState,
    onAction: (KardexAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(KardexAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin registros",
            description = "Todavía no hay rondas registradas en el historial.",
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ningún registro coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(KardexAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Historial",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} registros",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        if (state.items.isNotEmpty()) {
            val withMedia = state.items.count { it.mediaCount > 0 }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Registros",
                    value = state.items.size.toString(),
                    icon = ITIcons.Route,
                    tone = Tone.Info,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Con evidencia",
                    value = withMedia.toString(),
                    icon = ITIcons.Eye,
                    tone = if (withMedia > 0) Tone.Brand else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(KardexAction.Search(it)) },
            placeholder = "Buscar por punto o nota…",
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

        state.visibleItems.forEach { entry ->
            ITListItem(
                title = entry.locationName,
                subtitle = DateFormat.relative(entry.timestamp),
                avatarInitial = entry.locationName,
                avatarStatus = Tone.Info,
                reference = entry.notes?.takeIf { it.isNotBlank() },
                badge = if (entry.mediaCount > 0) {
                    { ITBadge(text = "${entry.mediaCount} fotos", tone = Tone.Neutral) }
                } else {
                    null
                },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(KardexAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }
}
