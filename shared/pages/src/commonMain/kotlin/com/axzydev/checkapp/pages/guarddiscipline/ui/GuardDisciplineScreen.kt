package com.axzydev.checkapp.pages.guarddiscipline.ui

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
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSearchField
import com.axzydev.checkapp.design.components.ITStatCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForStatus
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.guarddiscipline.viewmodel.DisciplineItemUi
import com.axzydev.checkapp.pages.guarddiscipline.viewmodel.GuardDisciplineAction
import com.axzydev.checkapp.pages.guarddiscipline.viewmodel.GuardDisciplineUiState

/**
 * Listado de quejas y faltas del personal.
 *
 * Misma plantilla que el resto de listados, con la **insignia de estado** en la
 * tarjeta: es lo que permite ver de un vistazo qué está sin atender sin abrir
 * cada registro.
 *
 * El orden de los datos en la tarjeta es el de urgencia: primero qué pasó, luego
 * quién y cuándo.
 */
@Composable
fun GuardDisciplineScreen(
    state: GuardDisciplineUiState,
    onAction: (GuardDisciplineAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(GuardDisciplineAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin quejas",
            description = "No hay faltas ni quejas registradas.",
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Nada coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(GuardDisciplineAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Quejas de guardias",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} registros",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        if (state.items.isNotEmpty()) {
            val pending = state.items.count { it.status.equals("PENDING", ignoreCase = true) }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Pendientes",
                    value = pending.toString(),
                    icon = ITIcons.ErrorOutline,
                    tone = if (pending > 0) Tone.Accent else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Resueltas",
                    value = (state.items.size - pending).toString(),
                    icon = ITIcons.ShieldCheck,
                    tone = Tone.Success,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(GuardDisciplineAction.Search(it)) },
            placeholder = "Buscar guardia o motivo…",
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

        state.visibleItems.forEach { item ->
            ITListItem(
                title = item.guardName ?: "Guardia sin asignar",
                subtitle = listOfNotNull(item.categoryName, item.typeName).joinToString(" · ")
                    .ifBlank { "Sin asignar" },
                meta = DateFormat.relative(item.createdAt),
                avatarInitial = item.guardName ?: "?",
                avatarStatus = toneForStatus(item.status),
                badge = {
                    ITBadge(text = item.status, tone = toneForStatus(item.status), dot = true)
                },
                footer = {
                    ITCardFooter {
                        ITFooterAction(
                            label = "Resolver",
                            onClick = { onAction(GuardDisciplineAction.Resolve(item.id)) },
                            enabled = state.resolvingId != item.id,
                        )
                        
                    }
                },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(GuardDisciplineAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    
}
