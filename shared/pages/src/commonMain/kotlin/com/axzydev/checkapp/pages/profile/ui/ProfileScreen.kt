package com.axzydev.checkapp.pages.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITAvatar
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.profile.viewmodel.ProfileAction
import com.axzydev.checkapp.pages.profile.viewmodel.ProfileUiState

/**
 * Perfil del usuario: datos personales y cambio de contraseña.
 *
 * Van en **dos tarjetas separadas** y con botones independientes. Antes eran un
 * único bloque con dos botones seguidos, y era fácil pulsar "Guardar" pensando que
 * también cambiaba la contraseña. Cada tarjeta guarda lo suyo.
 */
@Composable
fun ProfileScreen(
    state: ProfileUiState,
    onAction: (ProfileAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenState = if (state.loading) ScreenState.Loading else ScreenState.Ready

    ITScreenScaffold(
        title = "Mi perfil",
        subtitle = "@${state.username}",
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        // Los mensajes de resultado se muestran arriba y en tarjeta: en línea se
        // pierden entre los campos.
        state.message?.let { message ->
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITText(text = message, color = AxzyColors.success, style = AxzyType.cardBody)
            }
        }
        state.error?.let { error ->
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITText(text = error, color = AxzyColors.error, style = AxzyType.cardBody)
            }
        }

        // Cabecera: identidad del usuario.
        ITCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.lg),
            ) {
                ITAvatar(initial = state.name, size = 64.dp, status = Tone.Brand)
                Column(modifier = Modifier.weight(1f)) {
                    ITText(
                        text = state.name.ifBlank { "Usuario" },
                        color = AxzyColors.onSurface,
                        style = AxzyType.screenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    ITText(
                        text = "@${state.username}",
                        color = AxzyColors.onSurfaceVariant,
                        style = AxzyType.cardBody,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITText(
                text = "Datos personales",
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle,
            )
            Spacer(Modifier.height(AxzySpacing.md))

            ITTextField(
                label = "Nombre *",
                value = state.name,
                onValueChange = { onAction(ProfileAction.Name(it)) },
                enabled = !state.savingProfile,
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Apellidos",
                value = state.lastName,
                onValueChange = { onAction(ProfileAction.LastName(it)) },
                enabled = !state.savingProfile,
            )

            Spacer(Modifier.height(AxzySpacing.lg))
            ITButton(
                label = "Guardar cambios",
                onClick = { onAction(ProfileAction.SaveProfile) },
                loading = state.savingProfile,
                enabled = !state.savingProfile && !state.changingPassword && state.name.isNotBlank(),
            )
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITText(
                text = "Cambiar contraseña",
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle,
            )
            ITText(
                text = "Necesitas la contraseña actual para cambiarla.",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.itemMeta,
            )
            Spacer(Modifier.height(AxzySpacing.md))

            ITTextField(
                label = "Contraseña actual",
                value = state.oldPassword,
                onValueChange = { onAction(ProfileAction.OldPassword(it)) },
                isPassword = true,
                enabled = !state.changingPassword,
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Nueva contraseña",
                value = state.newPassword,
                onValueChange = { onAction(ProfileAction.NewPassword(it)) },
                isPassword = true,
                enabled = !state.changingPassword,
                error = if (state.newPassword.isNotEmpty() && state.newPassword.length < MIN_PASSWORD) {
                    "Mínimo $MIN_PASSWORD caracteres"
                } else {
                    null
                },
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Confirmar nueva contraseña",
                value = state.confirmPassword,
                onValueChange = { onAction(ProfileAction.ConfirmPassword(it)) },
                isPassword = true,
                enabled = !state.changingPassword,
                // Se avisa sólo cuando ya se puede comparar: mientras se escribe la
                // confirmación, marcar error es prematuro.
                error = if (state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword) {
                    "Las contraseñas no coinciden"
                } else {
                    null
                },
            )

            Spacer(Modifier.height(AxzySpacing.lg))
            ITButton(
                label = "Cambiar contraseña",
                onClick = { onAction(ProfileAction.ChangePassword) },
                loading = state.changingPassword,
                enabled = !state.changingPassword &&
                    !state.savingProfile &&
                    state.oldPassword.isNotBlank() &&
                    state.newPassword.length >= MIN_PASSWORD &&
                    state.newPassword == state.confirmPassword,
            )
        }

        Spacer(Modifier.height(AxzySpacing.sm))
        ITBadge(text = "v1.0.27 · AXZY Digital Systems", tone = Tone.Neutral)
    }
}

private const val MIN_PASSWORD = 6
