package com.axzydev.checkapp.pages.checkreport.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITChecklist
import com.axzydev.checkapp.design.components.ITChecklistItem
import com.axzydev.checkapp.design.components.ITChecklistScore
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportAction
import com.axzydev.checkapp.pages.checkreport.viewmodel.CheckReportUiState

/**
 * Reporte de un punto de control.
 *
 * La ubicación va en el subtítulo de la barra: es el contexto de todo lo que se
 * marca abajo. Las tareas son una lista de verificación con marcador, y la
 * evidencia se adjunta y se puede quitar antes de enviar.
 */
@Composable
fun CheckReportScreen(
    state: CheckReportUiState,
    onAction: (CheckReportAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    supportsVideo: Boolean = true,
    onCapturePhoto: () -> Unit = {},
    onCaptureVideo: () -> Unit = {},
) {
    val screenState = when {
        state.loading -> ScreenState.Loading
        state.error != null && state.tasks.isEmpty() -> ScreenState.Error(message = state.error)
        else -> ScreenState.Ready
    }

    ITScreenScaffold(
        title = "Punto de control",
        subtitle = state.locationName,
        state = screenState,
        modifier = modifier,
        onBack = onBack,
    ) {
        if (state.tasks.isNotEmpty()) {
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITChecklistScore(total = state.tasks.size, ok = state.tasks.count { it.done })
                Spacer(Modifier.height(AxzySpacing.md))
                ITChecklist(
                    items = state.tasks.map { ITChecklistItem(it.id, it.description, it.done) },
                    onToggle = { id, ok -> onAction(CheckReportAction.ToggleTask(id)) },
                )
            }
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITTextField(
                label = "Observaciones",
                value = state.notes,
                onValueChange = { onAction(CheckReportAction.Notes(it)) },
                singleLine = false,
                enabled = !state.submitting,
            )
        }

        ITCard(modifier = Modifier.fillMaxWidth()) {
            ITText(
                text = "Evidencia",
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.sectionLabel,
            )
            Spacer(Modifier.height(AxzySpacing.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                ITButton(
                    label = "Añadir foto",
                    onClick = onCapturePhoto,
                    outlined = true,
                    tone = Tone.Info,
                    compact = true,
                    modifier = Modifier.weight(1f),
                    fullWidth = false,
                )
                if (supportsVideo) {
                    ITButton(
                        label = "Añadir vídeo",
                        onClick = onCaptureVideo,
                        outlined = true,
                        tone = Tone.Info,
                        compact = true,
                        modifier = Modifier.weight(1f),
                        fullWidth = false,
                    )
                }
            }

            if (state.media.isNotEmpty()) {
                Spacer(Modifier.height(AxzySpacing.md))
                state.media.forEach { media ->
                    ITListItem(
                        title = if (media.isVideo) "Vídeo" else "Foto",
                        subtitle = media.uri,
                        avatarInitial = if (media.isVideo) "V" else "F",
                        avatarStatus = Tone.Info,
                        footer = {
                            ITCardFooter {
                                ITFooterAction(
                                    label = "Quitar",
                                    onClick = { onAction(CheckReportAction.RemoveMedia(media.id)) },
                                    tone = Tone.Danger,
                                )
                            }
                        },
                    )
                }
            }
        }

        state.error?.let {
            ITCard(modifier = Modifier.fillMaxWidth()) {
                ITText(text = it, color = AxzyColors.error, style = AxzyType.cardBody)
            }
        }

        ITButton(
            label = "Enviar reporte",
            onClick = { onAction(CheckReportAction.Submit) },
            loading = state.submitting,
            enabled = !state.submitting,
        )
    }
}
