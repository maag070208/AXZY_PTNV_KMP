package com.axzydev.checkapp.pages.clients.ui

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
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITAvatar
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITFooterDivider
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITConfirmDialog
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSearchField
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForStatus
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.clients.viewmodel.ClientItemUi
import com.axzydev.checkapp.pages.clients.viewmodel.ClientsAction
import com.axzydev.checkapp.pages.clients.viewmodel.ClientsUiState

/**
 * Listado de clientes.
 *
 * Antes era un único `Column` con scroll que mezclaba título, lista, formulario
 * de alta, formulario de edición, cinco botones sueltos y el diálogo. El título
 * se desplazaba con el contenido, cada cliente era una fila de texto con dos
 * `TextButton`, y "Actualizar"/"Volver" ocupaban el ancho completo al final como
 * si fueran las acciones principales de la pantalla.
 *
 * Ahora: barra fija, buscador, clientes como tarjetas con insignia de estado, y
 * la edición en diálogo para no competir con la lista.
 */
@Composable
fun ClientsScreen(
    state: ClientsUiState,
    onAction: (ClientsAction) -> Unit,
    onOpenClient: (clientId: String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(ClientsAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin clientes",
            description = "Todavía no hay clientes registrados.",
            actionLabel = "Nuevo cliente",
            onAction = { onAction(ClientsAction.ToggleForm) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ningún cliente coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(ClientsAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Clientes",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} registrados",
        state = screenState,
        modifier = modifier,
        onBack = null,
        actions = {
            ITButton(
                label = if (state.showForm) "Cancelar" else "Nuevo",
                onClick = { onAction(ClientsAction.ToggleForm) },
                outlined = state.showForm,
                tone = if (state.showForm) Tone.Neutral else Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.showForm && state.editingId == null) {
            ClientFormCard(state = state, onAction = onAction)
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(ClientsAction.Search(it)) },
            placeholder = "Buscar por nombre, RFC o contacto…",
            enabled = state.items.isNotEmpty(),
        )

        if (state.error != null && state.items.isNotEmpty()) {
            ITText(text = state.error, color = AxzyColors.error, style = AxzyType.labelSmall)
        }

        state.visibleItems.forEach { client ->
            ClientRow(
                client = client,
                deleting = state.deleting,
                onOpen = { onOpenClient(client.id) },
                onEdit = { onAction(ClientsAction.StartEdit(client.id)) },
                onDelete = { onAction(ClientsAction.RequestDelete(client.id)) },
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Actualizar",
            onClick = { onAction(ClientsAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    if (state.editingId != null) {
        ClientEditDialog(state = state, onAction = onAction)
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar cliente",
            message = "¿Seguro que quieres eliminar «${state.pendingDeleteName.orEmpty()}»? " +
                "Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(ClientsAction.ConfirmDelete) },
            onDismiss = { onAction(ClientsAction.CancelDelete) },
        )
    }
}

/**
 * Fila de cliente, con el patrón de tarjeta de la app React Native: avatar con
 * punto de estado, título, metadatos, insignia a la derecha y pie de acciones
 * separado por una línea.
 */
@Composable
private fun ClientRow(
    client: ClientItemUi,
    deleting: Boolean,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ITListItem(
        title = client.name,
        subtitle = client.contactName?.takeIf { it.isNotBlank() },
        meta = listOfNotNull(
            client.contactPhone?.takeIf { it.isNotBlank() },
            client.rfc?.takeIf { it.isNotBlank() },
        ).joinToString(" · ").ifBlank { client.address?.takeIf { it.isNotBlank() } },
        avatarInitial = client.name,
        avatarStatus = if (client.active) Tone.Success else Tone.Danger,
        onClick = onOpen,
        reference = client.address?.takeIf { it.isNotBlank() },
        badge = {
            ITBadge(
                text = if (client.active) "Activo" else "Inactivo",
                tone = if (client.active) Tone.Success else Tone.Danger,
                dot = true,
            )
        },
        footer = {
            ITCardFooter {
                ITFooterAction(label = "Detalle", onClick = onOpen)
                ITFooterDivider()
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

/** Formulario de alta, en tarjeta propia para que no compita con la lista. */
@Composable
private fun ClientFormCard(
    state: ClientsUiState,
    onAction: (ClientsAction) -> Unit,
) {
    ITCard(modifier = Modifier.fillMaxWidth()) {
        ITText(text = "Nuevo cliente", color = AxzyColors.onSurface, style = AxzyType.cardTitle)
        Spacer(Modifier.height(AxzySpacing.md))

        FormField("Nombre *", state.name) { onAction(ClientsAction.Name(it)) }
        FormField("Dirección", state.address) { onAction(ClientsAction.Address(it)) }
        FormField("RFC", state.rfc) { onAction(ClientsAction.Rfc(it)) }
        FormField("Contacto", state.contactName) { onAction(ClientsAction.ContactName(it)) }
        FormField("Teléfono", state.contactPhone) { onAction(ClientsAction.ContactPhone(it)) }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITButton(
            label = "Crear cliente",
            onClick = { onAction(ClientsAction.Create) },
            loading = state.creating,
            enabled = !state.creating && state.name.isNotBlank(),
        )
    }
}

/** Edición en diálogo: no obliga a desplazarse hasta el final de la lista. */
@Composable
private fun ClientEditDialog(
    state: ClientsUiState,
    onAction: (ClientsAction) -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onAction(ClientsAction.CancelEdit) },
        containerColor = AxzyColors.surface,
        shape = AxzyShape.lg,
        title = {
            ITText(text = "Editar cliente", color = AxzyColors.onSurface, style = AxzyType.cardTitle)
        },
        text = {
            Column {
                FormField("Nombre *", state.editName) { onAction(ClientsAction.EditName(it)) }
                FormField("Dirección", state.editAddress) { onAction(ClientsAction.EditAddress(it)) }
                FormField("RFC", state.editRfc) { onAction(ClientsAction.EditRfc(it)) }
                FormField("Contacto", state.editContactName) { onAction(ClientsAction.EditContactName(it)) }
                FormField("Teléfono", state.editContactPhone) { onAction(ClientsAction.EditContactPhone(it)) }
            }
        },
        confirmButton = {
            ITButton(
                label = "Guardar",
                onClick = { onAction(ClientsAction.SaveEdit) },
                loading = state.saving,
                enabled = !state.saving && state.editName.isNotBlank(),
                compact = true,
            )
        },
        dismissButton = {
            ITButton(
                label = "Cancelar",
                onClick = { onAction(ClientsAction.CancelEdit) },
                outlined = true,
                tone = Tone.Neutral,
                enabled = !state.saving,
                compact = true,
            )
        },
    )
}

/** Campo de texto del sistema. */
@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
) {
    ITTextField(
        label = label,
        value = value,
        onValueChange = onChange,
        modifier = Modifier.padding(bottom = AxzySpacing.sm),
    )
}
