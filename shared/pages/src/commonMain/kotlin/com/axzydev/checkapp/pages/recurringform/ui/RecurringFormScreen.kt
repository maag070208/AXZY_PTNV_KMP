package com.axzydev.checkapp.pages.recurringform.ui

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.ITSectionTitle
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.pages.recurringform.viewmodel.ClientOption
import com.axzydev.checkapp.pages.recurringform.viewmodel.GuardOption
import com.axzydev.checkapp.pages.recurringform.viewmodel.LocationOption
import com.axzydev.checkapp.pages.recurringform.viewmodel.PointUi
import com.axzydev.checkapp.pages.recurringform.viewmodel.RecurringFormAction
import com.axzydev.checkapp.pages.recurringform.viewmodel.RecurringFormStepTitles
import com.axzydev.checkapp.pages.recurringform.viewmodel.RecurringFormUiState
import com.axzydev.checkapp.pages.recurringform.viewmodel.TaskUi
import com.axzydev.checkapp.pages.recurringform.viewmodel.ZoneOption

/**
 * Alta/edición de una ruta recurrente, en cuatro pasos.
 *
 * El indicador de pasos va arriba y no se puede saltar: cada paso valida lo suyo
 * antes de avanzar, así que es imposible llegar al resumen con una ruta sin
 * puntos o sin guardias. Es un wizard y se nota.
 */
@Composable
fun RecurringFormScreen(
    state: RecurringFormUiState,
    onAction: (RecurringFormAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = if (state.loading) ScreenState.Loading else ScreenState.Ready

    ITScreenScaffold(
        title = if (state.isEditing) "Editar ruta" else "Nueva ruta",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        StepIndicator(current = state.step, titles = RecurringFormStepTitles)

        Spacer(Modifier.height(AxzySpacing.lg))

        when (state.step) {
            0 -> InfoStep(state, onAction)
            1 -> RouteStep(state, onAction)
            2 -> AssignmentStep(state, onAction)
            else -> SummaryStep(state)
        }

        state.error?.let {
            Spacer(Modifier.height(AxzySpacing.sm))
            ITText(text = it, color = AxzyColors.error, style = AxzyType.itemMeta)
        }

        Spacer(Modifier.height(AxzySpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            if (state.step > 0) {
                ITButton(
                    label = "Atrás",
                    onClick = { onAction(RecurringFormAction.Back) },
                    outlined = true,
                    tone = Tone.Neutral,
                    modifier = Modifier.weight(1f),
                )
            }
            val isLast = state.step == RecurringFormStepTitles.lastIndex
            ITButton(
                label = if (isLast) "Guardar ruta" else "Siguiente",
                onClick = {
                    if (isLast) onAction(RecurringFormAction.Submit)
                    else onAction(RecurringFormAction.Next)
                },
                loading = state.saving,
                enabled = !state.saving && canAdvance(state),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/** El botón "Siguiente" se habilita cuando el paso actual está completo. */
private fun canAdvance(state: RecurringFormUiState): Boolean = when (state.step) {
    0 -> state.title.isNotBlank() && state.selectedClientId != null
    1 -> state.points.isNotEmpty()
    2 -> state.selectedGuardIds.isNotEmpty()
    else -> true
}

/** Indicador de los cuatro pasos. */
@Composable
private fun StepIndicator(current: Int, titles: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        titles.forEachIndexed { index, title ->
            val tone = if (index == current) Tone.Brand else if (index < current) Tone.Success else Tone.Neutral
            val colors = tone.palette
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(if (index == current) colors.solid else colors.soft, AxzyShape.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    if (index < current) {
                        Icon(
                            imageVector = ITIcons.ShieldCheck,
                            contentDescription = "Paso completado",
                            tint = colors.solid,
                            modifier = Modifier.size(16.dp),
                        )
                    } else {
                        ITText(
                            text = "${index + 1}",
                            color = if (index == current) colors.onSolid else colors.onSoft,
                            style = AxzyType.labelSmall,
                        )
                    }
                }
                ITText(
                    text = title,
                    color = if (index == current) colors.onSoft else AxzyColors.onSurfaceVariant,
                    style = AxzyType.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** Paso 1: título y cliente. */
@Composable
private fun InfoStep(state: RecurringFormUiState, onAction: (RecurringFormAction) -> Unit) {
    ITCard(modifier = Modifier.fillMaxWidth()) {
        ITTextField(
            label = "Nombre de la ruta *",
            value = state.title,
            onValueChange = { onAction(RecurringFormAction.Title(it)) },
        )
        Spacer(Modifier.height(AxzySpacing.md))
        ClientDropdown(
            clients = state.clients,
            selectedId = state.selectedClientId,
            onSelect = { onAction(RecurringFormAction.SelectClient(it)) },
        )
    }
}

/** Paso 2: elegir puntos y añadir tareas. */
@Composable
private fun RouteStep(state: RecurringFormUiState, onAction: (RecurringFormAction) -> Unit) {
    Column {
        ITCard(modifier = Modifier.fillMaxWidth()) {
            ZoneDropdown(
                zones = state.zones,
                selectedId = state.selectedZoneId,
                onSelect = { onAction(RecurringFormAction.SelectZone(it)) },
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            LocationDropdown(
                locations = state.availableLocations,
                onSelect = { onAction(RecurringFormAction.AddLocation(it)) },
                onAddAll = { onAction(RecurringFormAction.AddAllFromZone) },
                canAddAll = state.selectedZoneId != null && state.availableLocations.isNotEmpty(),
            )
        }

        state.points.forEachIndexed { index, point ->
            PointCard(
                index = index,
                point = point,
                onRemove = { onAction(RecurringFormAction.RemovePoint(index)) },
                onAddTask = { onAction(RecurringFormAction.AddTask(index)) },
                onTaskDescription = { taskIndex, value ->
                    onAction(RecurringFormAction.TaskDescription(index, taskIndex, value))
                },
                onTogglePhoto = { taskIndex -> onAction(RecurringFormAction.ToggleTaskPhoto(index, taskIndex)) },
                onRemoveTask = { taskIndex -> onAction(RecurringFormAction.RemoveTask(index, taskIndex)) },
            )
        }
    }
}

/** Paso 3: guardias asignados. */
@Composable
private fun AssignmentStep(state: RecurringFormUiState, onAction: (RecurringFormAction) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            ITButton(
                label = "Todos",
                onClick = { onAction(RecurringFormAction.SelectAllGuards) },
                outlined = true,
                tone = Tone.Neutral,
                compact = true,
                modifier = Modifier.weight(1f),
                fullWidth = false,
            )
            Spacer(Modifier.size(AxzySpacing.sm))
            ITButton(
                label = "Ninguno",
                onClick = { onAction(RecurringFormAction.ClearGuards) },
                outlined = true,
                tone = Tone.Neutral,
                compact = true,
                enabled = state.selectedGuardIds.isNotEmpty(),
                modifier = Modifier.weight(1f),
                fullWidth = false,
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))

        state.availableGuards.forEach { guard ->
            val selected = guard.id in state.selectedGuardIds
            ITListItem(
                title = guard.name,
                subtitle = if (selected) "Asignado" else "Toca para asignar",
                avatarInitial = guard.name,
                avatarStatus = if (selected) Tone.Success else Tone.Neutral,
                badge = if (selected) {
                    { ITBadge(text = "En la ruta", tone = Tone.Success, dot = true) }
                } else {
                    null
                },
                onClick = { onAction(RecurringFormAction.ToggleGuard(guard.id)) },
            )
        }
    }
}

/** Paso 4: resumen de lo que se va a guardar. */
@Composable
private fun SummaryStep(state: RecurringFormUiState) {
    Column {
        ITCard(modifier = Modifier.fillMaxWidth()) {
            SummaryRow("Cliente", state.clientName ?: "—")
            SummaryRow("Puntos", state.points.size.toString())
            SummaryRow("Guardias", state.selectedGuards.size.toString())
        }

        Spacer(Modifier.height(AxzySpacing.sm))

        state.points.forEach { point ->
            ITListItem(
                title = point.locationName,
                subtitle = if (point.tasks.isEmpty()) "Sin tareas" else "${point.tasks.size} tareas",
                avatarInitial = point.locationName,
                avatarStatus = Tone.Brand,
            )
        }
    }
}

/** Fila de resumen. */
@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = AxzySpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        ITText(text = label, color = AxzyColors.onSurfaceVariant, style = AxzyType.cardBody)
        ITText(text = value, color = AxzyColors.onSurface, style = AxzyType.cardTitle)
    }
}

/** Tarjeta de un punto con sus tareas. */
@Composable
private fun PointCard(
    index: Int,
    point: PointUi,
    onRemove: () -> Unit,
    onAddTask: () -> Unit,
    onTaskDescription: (Int, String) -> Unit,
    onTogglePhoto: (Int) -> Unit,
    onRemoveTask: (Int) -> Unit,
) {
    ITCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            ITText(
                text = "${index + 1}. ${point.locationName}",
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle,
                modifier = Modifier.weight(1f),
            )
            ITTouchableOpacity(onClick = onRemove, scaleTo = 0.94f) {
                ITText(
                    text = "Quitar",
                    color = AxzyColors.error,
                    style = AxzyType.button,
                    modifier = Modifier.padding(horizontal = AxzySpacing.sm),
                )
            }
        }

        point.tasks.forEachIndexed { taskIndex, task ->
            Spacer(Modifier.height(AxzySpacing.sm))
            TaskRow(
                task = task,
                onDescription = { onTaskDescription(taskIndex, it) },
                onTogglePhoto = { onTogglePhoto(taskIndex) },
                onRemove = { onRemoveTask(taskIndex) },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(label = "Añadir tarea", onClick = onAddTask, outlined = true, tone = Tone.Neutral, compact = true)
    }
}

/** Fila de tarea dentro de un punto. */
@Composable
private fun TaskRow(
    task: TaskUi,
    onDescription: (String) -> Unit,
    onTogglePhoto: () -> Unit,
    onRemove: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ITTextField(
            label = "Tarea",
            value = task.description,
            onValueChange = onDescription,
        )
        Spacer(Modifier.height(AxzySpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ITBadge(
                text = if (task.reqPhoto) "Requiere foto" else "Sin foto",
                tone = if (task.reqPhoto) Tone.Info else Tone.Neutral,
            )
            ITButton(
                label = if (task.reqPhoto) "Quitar foto" else "Pedir foto",
                onClick = onTogglePhoto,
                outlined = true,
                tone = Tone.Neutral,
                compact = true,
                fullWidth = false,
            )
        }
    }
}

/** Desplegable de cliente. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDropdown(clients: List<ClientOption>, selectedId: String?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = clients.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.name ?: "Selecciona cliente",
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = "Cliente *", style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            clients.forEach { option ->
                DropdownMenuItem(
                    text = {
                        ITText(
                            text = option.name,
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

/** Desplegable de zona (opcional). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZoneDropdown(zones: List<ZoneOption>, selectedId: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selected = zones.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.name ?: "Todas las zonas",
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = "Zona (opcional)", style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { ITText(text = "Todas las zonas", color = AxzyColors.onSurface, style = AxzyType.body) },
                onClick = { onSelect(null); expanded = false },
            )
            zones.forEach { option ->
                DropdownMenuItem(
                    text = {
                        ITText(
                            text = option.name,
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

/** Selector de punto, con "añadir toda la zona". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDropdown(
    locations: List<LocationOption>,
    onSelect: (String) -> Unit,
    onAddAll: () -> Unit,
    canAddAll: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = "Añadir punto…",
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = "Puntos de control", style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            enabled = locations.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().menuAnchor(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (canAddAll) {
                DropdownMenuItem(
                    text = {
                        ITText(
                            text = "Añadir todos los de la zona",
                            color = AxzyColors.primary,
                            style = AxzyType.body,
                        )
                    },
                    onClick = { onAddAll(); expanded = false },
                )
            }
            locations.forEach { option ->
                DropdownMenuItem(
                    text = {
                        ITText(
                            text = option.name,
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
