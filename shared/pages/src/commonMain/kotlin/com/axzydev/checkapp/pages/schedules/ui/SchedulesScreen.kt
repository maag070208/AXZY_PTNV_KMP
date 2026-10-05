package com.axzydev.checkapp.pages.schedules.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
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
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.schedules.viewmodel.ScheduleItemUi
import com.axzydev.checkapp.pages.schedules.viewmodel.SchedulesAction
import com.axzydev.checkapp.pages.schedules.viewmodel.SchedulesUiState

/**
 * Listado de horarios.
 *
 * Misma plantilla que Clientes y Puntos. Se añade la **duración** en la tarjeta,
 * que no viene en el contrato pero se calcula de las horas: un turno de 21:00 a
 * 05:00 son ocho horas, y el usuario necesita saberlo sin restar.
 */
@Composable
fun SchedulesScreen(
    state: SchedulesUiState,
    onAction: (SchedulesAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(SchedulesAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin horarios",
            description = "Todavía no hay turnos definidos.",
            actionLabel = "Nuevo horario",
            onAction = { onAction(SchedulesAction.ToggleForm) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ningún horario coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(SchedulesAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Horarios",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} turnos",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Nuevo",
                onClick = { onAction(SchedulesAction.ToggleForm) },
                tone = Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.items.isNotEmpty()) {
            val overnight = state.items.count { isOvernight(it.startTime, it.endTime) }
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Turnos",
                    value = state.items.size.toString(),
                    icon = ITIcons.Calendar,
                    tone = Tone.Brand,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Cruzan medianoche",
                    value = overnight.toString(),
                    icon = ITIcons.Repeat,
                    tone = if (overnight > 0) Tone.Info else Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(SchedulesAction.Search(it)) },
            placeholder = "Buscar por nombre u hora…",
            leadingIcon = { SearchGlyph() },
            enabled = state.items.isNotEmpty(),
        )

        if (state.error != null && state.items.isNotEmpty()) {
            ITText(text = state.error, color = AxzyColors.error, style = AxzyType.labelSmall)
        }

        state.visibleItems.forEach { schedule ->
            ScheduleRow(
                schedule = schedule,
                deleting = state.deleting,
                onEdit = { onAction(SchedulesAction.StartEdit(schedule.id)) },
                onDelete = { onAction(SchedulesAction.RequestDelete(schedule.id)) },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(SchedulesAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    // Alta como pantalla modal.
    ITFormDialog(
        isOpen = state.showForm && state.editingId == null,
        title = "Nuevo horario",
        onDismiss = { onAction(SchedulesAction.ToggleForm) },
        onConfirm = { onAction(SchedulesAction.Create) },
        confirmLabel = "Crear horario",
        confirmEnabled = state.name.isNotBlank() && !state.creating,
        confirmLoading = state.creating,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.name,
            onValueChange = { onAction(SchedulesAction.Name(it)) },
            enabled = !state.creating,
        )
        ITTextField(
            label = "Hora de inicio",
            value = state.startTime,
            onValueChange = { onAction(SchedulesAction.StartTime(it)) },
            placeholder = "HH:mm",
            numeric = true,
            imeAction = ImeAction.Next,
            enabled = !state.creating,
        )
        ITTextField(
            label = "Hora de fin",
            value = state.endTime,
            onValueChange = { onAction(SchedulesAction.EndTime(it)) },
            placeholder = "HH:mm",
            numeric = true,
            imeAction = ImeAction.Done,
            enabled = !state.creating,
        )
    }

    // Edición como pantalla modal.
    ITFormDialog(
        isOpen = state.editingId != null,
        title = "Editar horario",
        onDismiss = { onAction(SchedulesAction.CancelEdit) },
        onConfirm = { onAction(SchedulesAction.SaveEdit) },
        confirmLabel = "Guardar",
        confirmEnabled = state.editName.isNotBlank() && !state.saving,
        confirmLoading = state.saving,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.editName,
            onValueChange = { onAction(SchedulesAction.EditName(it)) },
        )
        ITTextField(
            label = "Hora de inicio",
            value = state.editStart,
            onValueChange = { onAction(SchedulesAction.EditStart(it)) },
            placeholder = "HH:mm",
            numeric = true,
        )
        ITTextField(
            label = "Hora de fin",
            value = state.editEnd,
            onValueChange = { onAction(SchedulesAction.EditEnd(it)) },
            placeholder = "HH:mm",
            numeric = true,
        )
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar horario",
            message = "¿Seguro que quieres eliminar «${state.pendingDeleteName.orEmpty()}»? " +
                "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(SchedulesAction.ConfirmDelete) },
            onDismiss = { onAction(SchedulesAction.CancelDelete) },
        )
    }
}

/**
 * Fila de horario.
 *
 * El turno se muestra como "21:00 → 05:00" con la duración calculada al lado: es
 * lo que el administrador comprueba de un vistazo para saber si cubre la jornada.
 */
@Composable
private fun ScheduleRow(
    schedule: ScheduleItemUi,
    deleting: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val duration = shiftDuration(schedule.startTime, schedule.endTime)

    ITListItem(
        title = schedule.name,
        subtitle = "${schedule.startTime} → ${schedule.endTime}",
        avatarInitial = schedule.name,
        avatarStatus = Tone.Warning,
        badge = duration?.let { { ITBadge(text = it, tone = Tone.Neutral) } },
        onClick = onEdit,
        footer = {
            ITCardFooter {
                ITFooterAction(label = "Editar", onClick = onEdit)
                ITFooterDivider()
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

/** ¿El turno cruza la medianoche? (p. ej. 21:00 → 05:00). */
private fun isOvernight(start: String, end: String): Boolean {
    val s = parseTime(start) ?: return false
    val e = parseTime(end) ?: return false
    return e <= s
}

/**
 * Duración de un turno, en formato "8h 0m".
 *
 * Devuelve `null` cuando las horas aún no tienen formato válido, en lugar de un
 * cero engañoso: mientras el usuario escribe "2" en el campo no hay nada que
 * calcular.
 *
 * Si la hora de fin es menor que la de inicio el turno **cruza la medianoche**
 * (21:00 → 05:00 son ocho horas, no negativas), que en vigilancia es lo normal.
 */
private fun shiftDuration(start: String, end: String): String? {
    val startMinutes = parseTime(start) ?: return null
    val endMinutes = parseTime(end) ?: return null

    val raw = endMinutes - startMinutes
    val total = if (raw < 0) raw + MINUTES_PER_DAY else raw
    if (total == 0) return null

    return "${total / 60}h ${total % 60}m"
}

/** "21:00" → 1260. `null` si no es una hora válida. */
private fun parseTime(value: String): Int? {
    val parts = value.trim().split(":")
    if (parts.size != 2) return null
    val hours = parts[0].toIntOrNull() ?: return null
    val minutes = parts[1].toIntOrNull() ?: return null
    if (hours !in 0..23 || minutes !in 0..59) return null
    return hours * 60 + minutes
}

private const val MINUTES_PER_DAY = 24 * 60

/** Lupa del buscador. */
@Composable
private fun SearchGlyph() {
    Icon(
        imageVector = ITIcons.Search,
        contentDescription = null,
        tint = AxzyColors.slate400,
        modifier = Modifier.size(20.dp),
    )
}
