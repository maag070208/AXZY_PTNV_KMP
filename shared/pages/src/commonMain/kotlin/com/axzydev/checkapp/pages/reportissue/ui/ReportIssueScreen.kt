package com.axzydev.checkapp.pages.reportissue.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
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
import com.axzydev.checkapp.design.components.ITBadge
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITCardFooter
import com.axzydev.checkapp.design.components.ITFooterAction
import com.axzydev.checkapp.design.components.ITListItem
import com.axzydev.checkapp.design.components.ITScreenScaffold
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITTextField
import com.axzydev.checkapp.design.components.ScreenState
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.pages.reportissue.viewmodel.CategoryOption
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueAction
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueKind
import com.axzydev.checkapp.pages.reportissue.viewmodel.ReportIssueUiState

/**
 * Reportar una incidencia o una falla de mantenimiento.
 *
 * El **tipo** se muestra como insignia arriba en lugar de poder cambiarse aquí:
 * se elige al entrar desde el inicio ("Alertas" o "Mantenimiento") y cambiarlo a
 * mitad del formulario sólo sirve para equivocarse de categoría.
 */
@Composable
fun ReportIssueScreen(
    state: ReportIssueUiState,
    onAction: (ReportIssueAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    supportsVideo: Boolean = true,
    onCapturePhoto: () -> Unit = {},
    onCaptureVideo: () -> Unit = {},
) {
    ITScreenScaffold(
        title = state.kind.label,
        state = ScreenState.Ready,
        modifier = modifier,
        onBack = onBack,
        actions = {
            ITBadge(
                text = if (state.kind == ReportIssueKind.INCIDENT) "Incidencia" else "Falla",
                tone = if (state.kind == ReportIssueKind.INCIDENT) Tone.Danger else Tone.Warning,
                dot = true,
            )
        },
    ) {
        ITCard(modifier = Modifier.fillMaxWidth()) {
            CategoryDropdown(
                categories = state.categories,
                selectedId = state.selectedCategoryId,
                onSelect = { onAction(ReportIssueAction.SelectCategory(it)) },
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Título *",
                value = state.title,
                onValueChange = { onAction(ReportIssueAction.Title(it)) },
                enabled = !state.saving,
            )
            Spacer(Modifier.height(AxzySpacing.sm))
            ITTextField(
                label = "Descripción",
                value = state.description,
                onValueChange = { onAction(ReportIssueAction.Description(it)) },
                singleLine = false,
                enabled = !state.saving,
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
                                    onClick = { onAction(ReportIssueAction.RemoveMedia(media.id)) },
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
            onClick = { onAction(ReportIssueAction.Submit) },
            loading = state.saving,
            enabled = !state.saving &&
                state.title.isNotBlank() &&
                state.selectedCategoryId != null,
        )
    }
}

/** Desplegable de categoría. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<CategoryOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.firstOrNull { it.id == selectedId }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.name ?: "Selecciona categoría",
            onValueChange = {},
            readOnly = true,
            label = { ITText(text = "Categoría *", style = AxzyType.labelSmall) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            shape = AxzyShape.lg,
            textStyle = AxzyType.body,
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AxzyColors.surfaceVariant,
                unfocusedContainerColor = AxzyColors.surfaceVariant,
                focusedBorderColor = AxzyColors.primary,
                unfocusedBorderColor = AxzyColors.outlineVariant,
            ),
            enabled = categories.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
        )

        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            categories.forEach { option ->
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
                    onClick = {
                        onSelect(option.id)
                        expanded = false
                    },
                )
            }
        }
    }
}
