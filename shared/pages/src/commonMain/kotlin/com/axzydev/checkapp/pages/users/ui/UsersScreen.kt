package com.axzydev.checkapp.pages.users.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.axzydev.checkapp.design.components.ITFormDialog
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITSearchField
import com.axzydev.checkapp.design.components.ITStatCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.components.toneForRole
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette
import com.axzydev.checkapp.pages.users.viewmodel.Option
import com.axzydev.checkapp.pages.users.viewmodel.UserItemUi
import com.axzydev.checkapp.pages.users.viewmodel.UsersAction
import com.axzydev.checkapp.pages.users.viewmodel.UsersUiState

/**
 * Listado de usuarios.
 *
 * Es el CRUD más completo: alta con **dos desplegables dependientes** (el rol
 * decide si tiene sentido asignar un cliente) y contraseña, más edición y borrado.
 *
 * El cliente sólo se pide para roles de cliente. Un administrador no pertenece a
 * ninguno, y un guardia sí: por eso el segundo desplegable aparece condicionado al
 * rol en lugar de estar siempre visible y vacío.
 */
@Composable
fun UsersScreen(
    state: UsersUiState,
    onAction: (UsersAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Los roles vienen de la tabla local (sync). Si aún no sincronizó, se
    // derivan de los usuarios ya cargados para no dejar el formulario bloqueado.
    val roleOptions = remember(state.roles, state.items) {
        if (state.roles.isNotEmpty()) {
            state.roles
        } else {
            state.items
                .mapNotNull { user -> user.roleId?.let { id -> Option(id, user.roleName ?: id) } }
                .distinctBy { it.id }
        }
    }

    // El formulario de alta es largo (rol con tiles + cliente condicionado), así
    // que se abre a pantalla completa y no empuja la lista.
    val createNeedsClient = requiresClient(state.selectedRoleId, roleOptions)
    val canCreate = state.name.isNotBlank() &&
        state.username.isNotBlank() &&
        state.password.length >= MIN_PASSWORD &&
        state.selectedRoleId != null &&
        (!createNeedsClient || state.selectedClientId != null)

    val screenState = when {
        state.loading && state.items.isEmpty() -> ScreenState.Loading

        state.error != null && state.items.isEmpty() -> ScreenState.Error(
            message = state.error,
            onRetry = { onAction(UsersAction.Refresh) },
        )

        state.items.isEmpty() -> ScreenState.Empty(
            title = "Sin usuarios",
            description = "Todavía no hay cuentas creadas.",
            actionLabel = "Nuevo usuario",
            onAction = { onAction(UsersAction.ToggleForm) },
        )

        state.visibleItems.isEmpty() -> ScreenState.Empty(
            title = "Sin resultados",
            description = "Ningún usuario coincide con «${state.query}».",
            actionLabel = "Limpiar búsqueda",
            onAction = { onAction(UsersAction.Search("")) },
        )

        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Usuarios",
        subtitle = if (state.items.isEmpty()) null else "${state.items.size} cuentas",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITButton(
                label = "Nuevo",
                onClick = { onAction(UsersAction.ToggleForm) },
                tone = Tone.Brand,
                compact = true,
            )
        },
    ) {
        if (state.items.isNotEmpty()) {
            UsersOverview(items = state.items)
        }

        ITSearchField(
            value = state.query,
            onValueChange = { onAction(UsersAction.Search(it)) },
            placeholder = "Buscar por nombre, usuario o rol…",
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

        state.visibleItems.forEach { user ->
            val fullName = listOfNotNull(user.name, user.lastName).joinToString(" ")

            ITListItem(
                title = fullName,
                subtitle = "@${user.username}",
                avatarInitial = user.name,
                avatarStatus = toneForRole(user.roleName),
                badge = user.roleName?.let { role ->
                    { ITBadge(text = role, tone = toneForRole(role)) }
                },
                onClick = { onAction(UsersAction.StartEdit(user.id)) },
                footer = {
                    ITCardFooter {
                        ITFooterAction(
                            label = "Editar",
                            onClick = { onAction(UsersAction.StartEdit(user.id)) },
                        )
                        ITFooterDivider()
                        ITFooterAction(
                            label = "Eliminar",
                            onClick = { onAction(UsersAction.RequestDelete(user.id)) },
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
            onClick = { onAction(UsersAction.Refresh) },
            outlined = true,
            tone = Tone.Neutral,
            compact = true,
            fullWidth = false,
        )
    }

    // Alta como pantalla modal (formulario largo).
    ITFormDialog(
        isOpen = state.showForm && state.editingId == null,
        title = "Nuevo usuario",
        onDismiss = { onAction(UsersAction.ToggleForm) },
        onConfirm = { onAction(UsersAction.Create) },
        confirmLabel = "Crear usuario",
        confirmEnabled = canCreate,
        confirmLoading = state.creating,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.name,
            onValueChange = { onAction(UsersAction.Name(it)) },
            enabled = !state.creating,
        )
        ITTextField(
            label = "Apellidos",
            value = state.lastName,
            onValueChange = { onAction(UsersAction.LastName(it)) },
            enabled = !state.creating,
        )
        ITTextField(
            label = "Usuario *",
            value = state.username,
            onValueChange = { onAction(UsersAction.Username(it)) },
            enabled = !state.creating,
        )
        ITTextField(
            label = "Contraseña *",
            value = state.password,
            onValueChange = { onAction(UsersAction.Password(it)) },
            isPassword = true,
            enabled = !state.creating,
            error = if (state.password.isNotEmpty() && state.password.length < MIN_PASSWORD) {
                "Mínimo $MIN_PASSWORD caracteres"
            } else {
                null
            },
        )

        ITText(text = "Rol *", color = AxzyColors.onSurfaceVariant, style = AxzyType.labelSmall)
        if (roleOptions.isEmpty()) {
            ITText(text = "Cargando roles…", color = AxzyColors.slate400, style = AxzyType.labelSmall)
        } else {
            RoleSelector(
                roles = roleOptions,
                selectedId = state.selectedRoleId,
                onSelect = { onAction(UsersAction.SelectRole(it)) },
            )
        }

        if (createNeedsClient) {
            OptionDropdown(
                label = "Cliente *",
                emptyLabel = "Sin cliente",
                options = state.clients,
                selectedId = state.selectedClientId,
                onSelect = { onAction(UsersAction.SelectClient(it)) },
            )
        }
    }

    // Edición, también como pantalla modal.
    ITFormDialog(
        isOpen = state.editingId != null,
        title = "Editar usuario",
        onDismiss = { onAction(UsersAction.CancelEdit) },
        onConfirm = { onAction(UsersAction.SaveEdit) },
        confirmLabel = "Guardar",
        confirmEnabled = state.editName.isNotBlank() && !state.saving,
        confirmLoading = state.saving,
    ) {
        ITTextField(
            label = "Nombre *",
            value = state.editName,
            onValueChange = { onAction(UsersAction.EditName(it)) },
        )
        ITTextField(
            label = "Apellidos",
            value = state.editLastName,
            onValueChange = { onAction(UsersAction.EditLastName(it)) },
        )

        ITText(text = "Rol", color = AxzyColors.onSurfaceVariant, style = AxzyType.labelSmall)
        if (roleOptions.isEmpty()) {
            ITText(text = "Cargando roles…", color = AxzyColors.slate400, style = AxzyType.labelSmall)
        } else {
            RoleSelector(
                roles = roleOptions,
                selectedId = state.editRoleId,
                onSelect = { onAction(UsersAction.EditRole(it)) },
            )
        }

        // La contraseña no se edita aquí: se cambia por un flujo aparte.
        if (requiresClient(state.editRoleId, roleOptions)) {
            OptionDropdown(
                label = "Cliente",
                emptyLabel = "Sin cliente",
                options = state.clients,
                selectedId = state.editClientId,
                onSelect = { onAction(UsersAction.EditClient(it)) },
            )
        }
    }

    if (state.pendingDeleteId != null) {
        ITConfirmDialog(
            title = "Eliminar usuario",
            message = "¿Seguro que quieres eliminar la cuenta de " +
                "«${state.pendingDeleteName.orEmpty()}»? Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            loading = state.deleting,
            onConfirm = { onAction(UsersAction.ConfirmDelete) },
            onDismiss = { onAction(UsersAction.CancelDelete) },
        )
    }
}


/**
 * Resumen del directorio: total de cuentas, número de roles y el desglose por
 * rol. Da contexto antes de la lista sin tener que contar a ojo.
 */
@Composable
private fun UsersOverview(items: List<UserItemUi>) {
    val byRole = items
        .groupBy { it.roleName?.takeIf { it.isNotBlank() } ?: "Sin rol" }
        .mapValues { it.value.size }

    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md)) {
            ITStatCard(
                label = "Cuentas",
                value = items.size.toString(),
                icon = ITIcons.Person,
                tone = Tone.Brand,
                modifier = Modifier.weight(1f),
            )
            ITStatCard(
                label = "Roles",
                value = byRole.size.toString(),
                icon = ITIcons.ShieldCheck,
                tone = Tone.Info,
                modifier = Modifier.weight(1f),
            )
        }

        byRole.entries.sortedByDescending { it.value }.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                row.forEach { (role, count) ->
                    Box(modifier = Modifier.weight(1f)) {
                        ITBadge(text = "$role · $count", tone = toneForRole(role))
                    }
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Selector de rol como tiles.
 *
 * Un desplegable esconde las opciones y obliga a dos toques; con cinco roles
 * fijos, verlos todos y elegir de un toque es más rápido y más claro. Cada tile
 * usa el color del rol, así que el rol elegido se reconoce por color.
 */
@Composable
private fun RoleSelector(
    roles: List<Option>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm)) {
        roles.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                row.forEach { option ->
                    RoleTile(
                        option = option,
                        selected = option.id == selectedId,
                        onClick = { onSelect(option.id) },
                        modifier = Modifier.weight(1f),
                    )
                }
                repeat(2 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun RoleTile(
    option: Option,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = toneForRole(option.label).palette
    ITTouchableOpacity(onClick = onClick, modifier = modifier, scaleTo = 0.97f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (selected) colors.solid else colors.soft, AxzyShape.lg)
                .border(1.dp, colors.border, AxzyShape.lg)
                .padding(horizontal = AxzySpacing.md, vertical = AxzySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            ITText(
                text = option.label,
                color = if (selected) colors.onSolid else colors.onSoft,
                style = AxzyType.moduleLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Desplegable genérico. */
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
 * ¿Este rol necesita cliente?
 *
 * Sólo los roles operativos pertenecen a un cliente; un administrador es de la
 * plataforma. Se decide por el **nombre** del rol porque el identificador cambia
 * entre entornos y el nombre no.
 */
private fun requiresClient(roleId: String?, roles: List<Option>): Boolean {
    val roleName = roles.firstOrNull { it.id == roleId }?.label?.uppercase() ?: return false
    return roleName !in PLATFORM_ROLES
}

/** Roles que no pertenecen a ningún cliente. */
private val PLATFORM_ROLES = setOf("ADMIN", "ADMINISTRADOR", "LIDER", "SUPERVISOR")

/** La contraseña mínima la valida también el backend; aquí se avisa antes. */
private const val MIN_PASSWORD = 6
