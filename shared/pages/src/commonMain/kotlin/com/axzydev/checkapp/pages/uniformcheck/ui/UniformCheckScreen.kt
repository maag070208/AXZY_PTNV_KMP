package com.axzydev.checkapp.pages.uniformcheck.ui

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
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITChecklist
import com.axzydev.checkapp.design.components.ITChecklistItem
import com.axzydev.checkapp.design.components.ITChecklistScore
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.uniformcheck.viewmodel.Option
import com.axzydev.checkapp.pages.uniformcheck.viewmodel.UniformCheckAction
import com.axzydev.checkapp.pages.uniformcheck.viewmodel.UniformCheckUiState

/**
 * Revisión de uniforme en turno.
 *
 * El **marcador** va arriba y se recalcula al marcar: es lo que el supervisor mira
 * al terminar, y tenerlo que contar a mano al final es lo que hacía que se
 * rellenara de cualquier manera.
 *
 * Cuando falta el catálogo se dice explícitamente en lugar de mostrar una lista
 * vacía, porque un catálogo sin configurar no es lo mismo que "todo conforme".
 */
@Composable
fun UniformCheckScreen(
    state: UniformCheckUiState,
    onAction: (UniformCheckAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading -> ScreenState.Loading

        state.catalogMissing -> ScreenState.Empty(
            title = "Sin catálogo",
            description = "No hay puntos de revisión configurados. Pide a un " +
                "administrador que los configure antes de continuar.",
        )

        state.error != null && state.items.isEmpty() -> ScreenState.Error(message = state.error)

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Revisión de uniforme",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        if (state.items.isNotEmpty()) {
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITChecklistScore(
                    total = state.items.size,
                    ok = state.items.count { it.ok },
                )
            }
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            GuardDropdown(
                guards = state.guards,
                selectedId = state.selectedGuardId,
                onSelect = { onAction(UniformCheckAction.SelectGuard(it)) },
            )

            Spacer(Modifier.height(AxzySpacing.lg))

            ITChecklist(
                items = state.items.map { ITChecklistItem(it.key, it.label, it.ok) },
                onToggle = { key, ok -> onAction(UniformCheckAction.Toggle(key, ok)) },
            )

            Spacer(Modifier.height(AxzySpacing.lg))

            ITTextField(
                label = "Observaciones",
                value = state.notes,
                onValueChange = { onAction(UniformCheckAction.Notes(it)) },
                singleLine = false,
            )

            state.error?.let {
                Spacer(Modifier.height(AxzySpacing.sm))
                ITText(text = it, color = AxzyColors.error, style = AxzyType.itemMeta)
            }

            Spacer(Modifier.height(AxzySpacing.lg))
            ITButton(
                label = "Guardar revisión",
                onClick = { onAction(UniformCheckAction.Save) },
                loading = state.saving,
                enabled = !state.saving && state.selectedGuardId != null,
            )
        }
    }
}

/** Desplegable de guardia. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GuardDropdown(
    guards: List<Option>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = guards.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.label ?: "Selecciona guardia",
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = "Guardia", style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            enabled = guards.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            guards.forEach { option ->
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
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
