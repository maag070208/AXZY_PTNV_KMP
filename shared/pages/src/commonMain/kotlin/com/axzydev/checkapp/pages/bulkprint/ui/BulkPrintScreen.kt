package com.axzydev.checkapp.pages.bulkprint.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.bulkprint.viewmodel.BulkPrintAction
import com.axzydev.checkapp.pages.bulkprint.viewmodel.BulkPrintUiState
import com.axzydev.checkapp.pages.bulkprint.viewmodel.Option

/**
 * Impresión masiva de códigos QR.
 *
 * El cliente se elige primero y la lista de puntos se filtra a sus puntos: es el
 * único filtro que importa aquí, porque se imprimen los QRs **de un cliente** para
 * pegarlos en sus instalaciones.
 *
 * La selección múltiple se hace tocando la tarjeta, con una palomita de estado:
 * no hay casillas porque en la selección masiva lo importante es poder tocar
 * rápido y ver de un vistazo qué quedó marcado.
 */
@Composable
fun BulkPrintScreen(
    state: BulkPrintUiState,
    onAction: (BulkPrintAction) -> Unit,
    onShareSheet: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.locations.isEmpty() -> ScreenState.Loading

        state.error != null && state.locations.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(BulkPrintAction.Refresh) },
        )

        state.locations.isEmpty() -> ScreenState.Empty(
            title = "Sin puntos",
            description = "No hay puntos de control para imprimir.",
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Imprimir QRs",
        subtitle = "${state.selectedIds.size} seleccionados",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        ClientDropdown(
            clients = state.clients,
            selectedId = state.selectedClientId,
            onSelect = { onAction(BulkPrintAction.SelectClient(it)) },
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            ITButton(
                label = "Seleccionar todo",
                onClick = { onAction(BulkPrintAction.SelectAll) },
                outlined = true,
                tone = Tone.Neutral,
                compact = true,
                modifier = Modifier.weight(1f),
                fullWidth = false,
            )
            Spacer(Modifier.size(AxzySpacing.sm))
            ITButton(
                label = "Limpiar",
                onClick = { onAction(BulkPrintAction.Clear) },
                outlined = true,
                tone = Tone.Neutral,
                compact = true,
                enabled = state.selectedIds.isNotEmpty(),
                modifier = Modifier.weight(1f),
                fullWidth = false,
            )
        }

        state.visibleLocations.forEach { location ->
            val selected = location.id in state.selectedIds
            ITListItem(
                title = location.name,
                subtitle = location.clientName ?: "Sin cliente",
                avatarInitial = location.name,
                avatarStatus = if (selected) Tone.Success else Tone.Neutral,
                badge = if (selected) {
                    { ITBadge(text = "Seleccionado", tone = Tone.Success, dot = true) }
                } else {
                    null
                },
                onClick = { onAction(BulkPrintAction.Toggle(location.id)) },
            )
        }

        Spacer(Modifier.height(AxzySpacing.lg))

        if (state.sheet.isNotEmpty()) {
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITText(
                    text = "Hoja generada: ${state.sheet.size} códigos",
                    color = AxzyColors.onSurfaceVariant,
                    style = AxzyType.cardBody,
                )
                Spacer(Modifier.height(AxzySpacing.md))
                ITButton(label = "Compartir hoja", onClick = onShareSheet)
                Spacer(Modifier.height(AxzySpacing.sm))
                ITButton(
                    label = "Descartar hoja",
                    onClick = { onAction(BulkPrintAction.ClearSheet) },
                    outlined = true,
                    tone = Tone.Neutral,
                )
            }
        } else {
            ITButton(
                label = "Generar hoja de QRs",
                onClick = { onAction(BulkPrintAction.Generate) },
                enabled = state.selectedIds.isNotEmpty(),
            )
        }
    }
}

/** Desplegable de cliente, con opción de "todos". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDropdown(
    clients: List<Option>,
    selectedId: String?,
    onSelect: (String?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = clients.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.label ?: "Todos los clientes",
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = "Cliente", style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            enabled = clients.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = {
                    ITText(
                        text = "Todos los clientes",
                        color = AxzyColors.onSurface,
                        style = AxzyType.body,
                    )
                },
                onClick = { onSelect(null); expanded = false },
            )
            clients.forEach { option ->
                DropdownMenuItem(
                    text = {
                        ITText(
                            text = option.label,
                            color = AxzyColors.onSurface,
                            style = AxzyType.body,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = { onSelect(option.id); expanded = false },
                )
            }
        }
    }
}
