package com.axzydev.checkapp.pages.shifthandover.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITChecklist
import com.axzydev.checkapp.design.components.ITChecklistItem
import com.axzydev.checkapp.design.components.ITChecklistScore
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.shifthandover.viewmodel.Option
import com.axzydev.checkapp.pages.shifthandover.viewmodel.ShiftHandoverAction
import com.axzydev.checkapp.pages.shifthandover.viewmodel.ShiftHandoverUiState

/**
 * Entrega de turno.
 *
 * Se agrupa en dos tarjetas: los datos del turno (cliente, horario, credenciales,
 * tarjetones) y la lista de verificación con su marcador. El interruptor de
 * "reportado al administrador" va al final, porque es el cierre del proceso y no
 * un dato más.
 */
@Composable
fun ShiftHandoverScreen(
    state: ShiftHandoverUiState,
    onAction: (ShiftHandoverAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading -> ScreenState.Loading

        state.catalogMissing -> ScreenState.Empty(
            title = "Sin catálogo",
            description = "No hay puntos de entrega configurados. Pide a un " +
                "administrador que los configure antes de continuar.",
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Entrega de turno",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        state.savedCount.let { count ->
            if (count > 0) {
                ITCard(modifier = Modifier.fillMaxWidth()) {
                    ITBadge(text = "$count entregas guardadas", tone = Tone.Success, dot = true)
                }
            }
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            Dropdown(
                label = "Cliente",
                emptyLabel = "Selecciona cliente",
                options = state.clients,
                selectedId = state.selectedClientId,
                onSelect = { onAction(ShiftHandoverAction.SelectClient(it)) },
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            Dropdown(
                label = "Horario",
                emptyLabel = "Selecciona horario",
                options = state.schedules,
                selectedId = state.selectedScheduleId,
                onSelect = { onAction(ShiftHandoverAction.SelectSchedule(it)) },
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Credenciales",
                value = state.credentials,
                onValueChange = { onAction(ShiftHandoverAction.Credentials(it)) },
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Tarjetones",
                value = state.tarjetones,
                onValueChange = { onAction(ShiftHandoverAction.Tarjetones(it)) },
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Novedades",
                value = state.novedades,
                onValueChange = { onAction(ShiftHandoverAction.Novedades(it)) },
                singleLine = false,
            )
        }

        if (state.items.isNotEmpty()) {
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITChecklistScore(
                    total = state.items.size,
                    ok = state.items.count { it.ok },
                )
                Spacer(Modifier.height(AxzySpacing.md))
                ITChecklist(
                    items = state.items.map { ITChecklistItem(it.key, it.label, it.ok) },
                    onToggle = { key, ok -> onAction(ShiftHandoverAction.Toggle(key, ok)) },
                )
            }
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ReportedToggle(
                checked = state.reportedToAdmin,
                onClick = { onAction(ShiftHandoverAction.SetReported(!state.reportedToAdmin)) },
            )

            state.error?.let {
                Spacer(Modifier.height(AxzySpacing.sm))
                ITText(text = it, color = AxzyColors.error, style = AxzyType.itemMeta)
            }

            Spacer(Modifier.height(AxzySpacing.lg))
            ITButton(
                label = "Guardar entrega",
                onClick = { onAction(ShiftHandoverAction.Save) },
                loading = state.saving,
                enabled = !state.saving &&
                    state.selectedClientId != null &&
                    state.selectedScheduleId != null,
            )
        }
    }
}

/** Desplegable genérico de entrega de turno. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Dropdown(
    label: String,
    emptyLabel: String,
    options: List<Option>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.label ?: emptyLabel,
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = label, style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            enabled = options.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
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

/** Cierre del proceso: reportar al administrador. */
@Composable
private fun ReportedToggle(checked: Boolean, onClick: () -> Unit) {
    ITTouchableOpacity(onClick = onClick, modifier = Modifier.fillMaxWidth(), scaleTo = 0.98f) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(4f)) {
                ITText(text = "Reportado al administrador", color = AxzyColors.onSurface, style = AxzyType.cardBody)
                ITText(
                    text = "Confirma que la entrega quedó registrada.",
                    color = AxzyColors.onSurfaceVariant,
                    style = AxzyType.labelSmall,
                )
            }
            ITBadge(text = if (checked) "Sí" else "No", tone = if (checked) Tone.Brand else Tone.Neutral)
        }
    }
}
