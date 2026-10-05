package com.axzydev.checkapp.pages.locations.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.axzydev.checkapp.pages.locations.viewmodel.LocationsAction
import com.axzydev.checkapp.pages.locations.viewmodel.LocationsUiState
import com.axzydev.checkapp.pages.locations.viewmodel.Option

/**
 * Listado de puntos de control.
 *
 * Sigue la plantilla de Clientes: barra fija, buscador, tarjeta por punto, alta y
 * edición en diálogo, y borrado con confirmación.
 *
 * Antes era un `Column` con scroll que mezclaba el título, la lista, los dos
 * formularios y cinco botones sueltos. El título se desplazaba y "Actualizar" y
 * "Volver" ocupaban el ancho completo al final, como si fueran lo principal.
 */
@Composable
fun LocationsScreen(
    state: LocationsUiState,
    onAction: (LocationsAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(LocationsAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin puntos de control",
            description = "Todavía no hay puntos registrados.",
            actionLabel = "Nuevo punto",
            onAction = { onAction(LocationsAction.ToggleForm) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ningún punto coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(LocationsAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Puntos de control",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} registrados",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Nuevo",
                onClick = { onAction(LocationsAction.ToggleForm) },
                tone = Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.items.isNotEmpty()) {
            val clients = state.items.mapNotNull { it.clientName }.distinct().size
            Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
                ITStatCard(
                    label = "Puntos",
                    value = state.items.size.toString(),
                    icon = ITIcons.Place,
                    tone = Tone.Success,
                    modifier = Modifier.weight(1f),
                )
                ITStatCard(
                    label = "Clientes",
                    value = clients.toString(),
                    icon = ITIcons.Business,
                    tone = Tone.Info,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(LocationsAction.Search(it)) },
            placeholder = "Buscar por nombre, referencia o cliente…",
            leadingIcon = { SearchGlyph() },
            enabled = state.items.isNotEmpty(),
        )

        if (state.error != null && state.items.isNotEmpty()) {
            ITText(text = state.error, color = AxzyColors.error, style = AxzyType.labelSmall)
        }

        state.visibleItems.forEach { location ->
            ITListItem(
                title = location.name,
                subtitle = location.clientName ?: "Sin cliente",
                avatarInitial = location.name,
                avatarStatus = Tone.Brand,
                reference = location.reference,
                onClick = { onAction(LocationsAction.StartEdit(location.id)) },
                footer = {
                    ITCardFooter {
                        ITFooterAction(
                            label = "Editar",
                            onClick = { onAction(LocationsAction.StartEdit(location.id)) },
                        )
                        ITFooterDivider()
                        ITFooterAction(
                            label = "Eliminar",
                            onClick = { onAction(LocationsAction.RequestDelete(location.id)) },
                            tone = Tone.Danger,
                            enabled = !state.deleting,
                        )
                    }
                },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(LocationsAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    // Alta como pantalla modal.
    ITFormDialog(
        isOpen = state.showForm && state.editingId == null,
        title = "Nuevo punto",
        onDismiss = { onAction(LocationsAction.ToggleForm) },
        onConfirm = { onAction(LocationsAction.Create) },
        confirmLabel = "Crear punto",
        confirmEnabled = state.name.isNotBlank() && !state.creating,
        confirmLoading = state.creating,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.name,
            onValueChange = { onAction(LocationsAction.Name(it)) },
            enabled = !state.creating,
        )
        ITTextField(
            label = "Referencia",
            value = state.reference,
            onValueChange = { onAction(LocationsAction.Reference(it)) },
            enabled = !state.creating,
        )
        ClientDropdown(
            clients = state.clients,
            selectedId = state.selectedClientId,
            onSelect = { onAction(LocationsAction.SelectClient(it)) },
        )
    }

    // Edición como pantalla modal.
    ITFormDialog(
        isOpen = state.editingId != null,
        title = "Editar punto",
        onDismiss = { onAction(LocationsAction.CancelEdit) },
        onConfirm = { onAction(LocationsAction.SaveEdit) },
        confirmLabel = "Guardar",
        confirmEnabled = state.editName.isNotBlank() && !state.saving,
        confirmLoading = state.saving,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.editName,
            onValueChange = { onAction(LocationsAction.EditName(it)) },
        )
        ITTextField(
            label = "Referencia",
            value = state.editReference,
            onValueChange = { onAction(LocationsAction.EditReference(it)) },
        )
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar punto",
            message = "¿Seguro que quieres eliminar «${state.pendingDeleteName.orEmpty()}»? " +
                "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(LocationsAction.ConfirmDelete) },
            onDismiss = { onAction(LocationsAction.CancelDelete) },
        )
    }
}

/**
 * Desplegable de cliente.
 *
 * Un punto pertenece a un cliente, así que en la práctica el campo es obligatorio
 * aunque el backend acepte vacío: sin cliente, el punto no entra en ninguna ruta.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDropdown(
    clients: List<Option>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = clients.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selected?.label ?: "Sin cliente",
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = "Cliente", style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

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
