package com.axzydev.checkapp.pages.assignments.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITConfirmDialog
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITFooterDivider
import com.axzydev.checkapp.design.components.ITFormDialog
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSearchField
import com.axzydev.checkapp.design.components.ITStatCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForStatus
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentItemUi
import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentStatuses
import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentsAction
import com.axzydev.checkapp.pages.assignments.viewmodel.AssignmentsUiState
import com.axzydev.checkapp.pages.assignments.viewmodel.Option

/**
 * Asignaciones de tareas.
 *
 * Es el único listado con **alta y ciclo de estado a la vez**: una asignación se
 * crea eligiendo guardia y punto, y luego avanza por estados (pendiente →
 * comprobando → en revisión → revisada, o anomalía).
 *
 * El botón del pie avanza al estado siguiente y **desaparece en el estado final**
 * (`REVIEWED`): dejar un botón que no hace nada es peor que no tenerlo. `ANOMALY`
 * tampoco avanza porque es una parada, no un paso.
 */
@Composable
fun AssignmentsScreen(
    state: AssignmentsUiState,
    onAction: (AssignmentsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(AssignmentsAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin asignaciones",
            description = "Todavía no hay tareas asignadas a guardias.",
            actionLabel = "Nueva asignación",
            onAction = { onAction(AssignmentsAction.ToggleForm) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Nada coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(AssignmentsAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Asignaciones",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} tareas",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Nueva",
                onClick = { onAction(AssignmentsAction.ToggleForm) },
                tone = Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.items.isNotEmpty()) {
            val pending = state.items.count { it.status.equals("PENDING", ignoreCase = true) }
            val done = state.items.count { it.status.equals("REVIEWED", ignoreCase = true) }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Pendientes",
                    value = pending.toString(),
                    icon = ITIcons.Layers,
                    tone = if (pending > 0) Tone.Warning else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Revisadas",
                    value = done.toString(),
                    icon = ITIcons.ShieldCheck,
                    tone = Tone.Success,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(AssignmentsAction.Search(it)) },
            placeholder = "Buscar por guardia o punto…",
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

        state.visibleItems.forEach { assignment ->
            AssignmentRow(
                assignment = assignment,
                updating = state.statusUpdatingId == assignment.id,
                deleting = state.deleting,
                onAdvance = {
                    onAction(AssignmentsAction.AdvanceStatus(assignment.id, assignment.status))
                },
                onDelete = { onAction(AssignmentsAction.RequestDelete(assignment.id)) },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(AssignmentsAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    // Alta como pantalla modal.
    ITFormDialog(
        isOpen = state.showForm,
        title = "Nueva asignación",
        onDismiss = { onAction(AssignmentsAction.ToggleForm) },
        onConfirm = { onAction(AssignmentsAction.Create) },
        confirmLabel = "Crear asignación",
        confirmEnabled = state.selectedGuardId != null && state.selectedLocationId != null && !state.creating,
        confirmLoading = state.creating,
    ) {
        OptionDropdown(
            label = "Guardia",
            emptyLabel = "Sin guardia",
            options = state.guards,
            selectedId = state.selectedGuardId,
            onSelect = { onAction(AssignmentsAction.SelectGuard(it)) },
        )
        OptionDropdown(
            label = "Punto de control",
            emptyLabel = "Sin punto",
            options = state.locations,
            selectedId = state.selectedLocationId,
            onSelect = { onAction(AssignmentsAction.SelectLocation(it)) },
        )
        ITTextField(
            label = "Notas",
            value = state.notes,
            onValueChange = { onAction(AssignmentsAction.Notes(it)) },
        )
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar asignación",
            message = "¿Seguro que quieres eliminar la asignación de " +
                "«${state.pendingDeleteName.orEmpty()}»? Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(AssignmentsAction.ConfirmDelete) },
            onDismiss = { onAction(AssignmentsAction.CancelDelete) },
        )
    }
}

/** Fila de asignación, con la acción de avance sólo si el estado lo permite. */
@Composable
private fun AssignmentRow(
    assignment: AssignmentItemUi,
    updating: Boolean,
    deleting: Boolean,
    onAdvance: () -> Unit,
    onDelete: () -> Unit,
) {
    val tone = toneForStatus(assignment.status)
    val canAdvance = nextStatus(assignment.status) != null

    ITListItem(
        title = assignment.guardName ?: "Guardia sin asignar",
        subtitle = assignment.locationName ?: "Sin punto de control",
        avatarInitial = assignment.guardName ?: "?",
        avatarStatus = tone,
        badge = { ITBadge(text = assignment.status, tone = tone, dot = true) },
        footer = {
            ITCardFooter {
                if (canAdvance) {
                    ITFooterAction(
                        label = "Avanzar",
                        onClick = onAdvance,
                        enabled = !updating,
                    )
                    ITFooterDivider()
                }
                ITFooterAction(
                    label = "Eliminar",
                    onClick = onDelete,
                    tone = Tone.Danger,
                    enabled = !deleting,
                )
            }
        },
    )
}

/** Desplegable genérico, para no repetir el de cliente en cada módulo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptionDropdown(
    label: String,
    emptyLabel: String,
    options: List<Option>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selected?.label ?: emptyLabel,
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = label, style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            enabled = options.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
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
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

/**
 * Estado siguiente en el ciclo, o `null` si no hay a dónde avanzar.
 *
 * Se calcula sobre `AssignmentStatuses` para no duplicar la lista: si mañana el
 * backend añade un estado, basta con tocar la constante del contrato.
 */
private fun nextStatus(current: String): String? {
    val index = AssignmentStatuses.indexOfFirst { it.equals(current, ignoreCase = true) }
    if (index < 0 || index >= AssignmentStatuses.lastIndex) return null
    return AssignmentStatuses[index + 1]
}
