package com.axzydev.checkapp.pages.sendnotification.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ITTouchableOpacity
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.sendnotification.viewmodel.SendNotificationAction
import com.axzydev.checkapp.pages.sendnotification.viewmodel.SendNotificationUiState

/**
 * Enviar un aviso al personal.
 *
 * El **tipo** se elige con pastillas y no con un desplegable: son cuatro opciones
 * fijas y verlas todas de un vistazo es más rápido que desplegar. El color de la
 * pastilla es el mismo que tendrá el aviso en la bandeja del guardia, así que
 * sirve de vista previa.
 */
@Composable
fun SendNotificationScreen(
    state: SendNotificationUiState,
    onAction: (SendNotificationAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ITScreenScaffold(
        title = "Enviar aviso",
        subtitle = "Notificación inmediata",
        state = ScreenState.Ready,
        modifier = modifier,
        onBack = onBack,
    ) {
        state.messageOk?.let {
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITText(text = it, color = AxzyColors.success, style = AxzyType.cardBody)
            }
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITTextField(
                label = "Título",
                value = state.title,
                onValueChange = { onAction(SendNotificationAction.Title(it)) },
                enabled = !state.sending,
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Mensaje *",
                value = state.message,
                onValueChange = { onAction(SendNotificationAction.Message(it)) },
                singleLine = false,
                enabled = !state.sending,
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Sólo para un usuario (opcional)",
                value = state.userId,
                onValueChange = { onAction(SendNotificationAction.UserId(it)) },
                enabled = !state.sending,
            )
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITText(
                text = "Tipo de aviso",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.sectionLabel,
            )
            Spacer(Modifier.height(AxzySpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                NotificationTypes.forEach { type ->
                    TypeChip(
                        label = type.label,
                        active = state.type.equals(type.value, ignoreCase = true),
                        tone = type.tone,
                        onClick = { onAction(SendNotificationAction.Type(type.value)) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            Spacer(Modifier.height(AxzySpacing.lg))

            ToggleRow(
                label = "Aviso persistente",
                description = "Se queda en la bandeja hasta que el guardia lo lea.",
                checked = state.persistent,
                onClick = { onAction(SendNotificationAction.TogglePersistent) },
            )

            state.error?.let {
                Spacer(Modifier.height(AxzySpacing.sm))
                ITText(text = it, color = AxzyColors.error, style = AxzyType.itemMeta)
            }

            Spacer(Modifier.height(AxzySpacing.lg))
            ITButton(
                label = "Enviar aviso",
                onClick = { onAction(SendNotificationAction.Send) },
                loading = state.sending,
                enabled = !state.sending && state.message.isNotBlank(),
            )
        }
    }
}

/** Una opción de tipo, con el color que tendrá el aviso. */
@Composable
private fun TypeChip(
    label: String,
    active: Boolean,
    tone: Tone,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ITTouchableOpacity(onClick = onClick, modifier = modifier, scaleTo = 0.95f) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            ITBadge(
                text = label,
                tone = if (active) tone else Tone.Neutral,
                dot = active,
            )
        }
    }
}

/** Interruptor con descripción. */
@Composable
private fun ToggleRow(
    label: String,
    description: String,
    checked: Boolean,
    onClick: () -> Unit,
) {
    ITTouchableOpacity(onClick = onClick, modifier = Modifier.fillMaxWidth(), scaleTo = 0.98f) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            androidx.compose.foundation.layout.Column(modifier = Modifier.weight(4f)) {
                ITText(text = label, color = AxzyColors.onSurface, style = AxzyType.cardBody)
                ITText(
                    text = description,
                    color = AxzyColors.onSurfaceVariant,
                    style = AxzyType.labelSmall,
                )
            }
            ITBadge(
                text = if (checked) "Sí" else "No",
                tone = if (checked) Tone.Brand else Tone.Neutral,
            )
        }
    }
}

/** Tipos de aviso y su color, alineados con la bandeja del guardia. */
private data class NotificationType(val value: String, val label: String, val tone: Tone)

private val NotificationTypes = listOf(
    NotificationType("info", "Info", Tone.Info),
    NotificationType("success", "Éxito", Tone.Success),
    NotificationType("warning", "Aviso", Tone.Warning),
    NotificationType("error", "Urgente", Tone.Danger),
)
