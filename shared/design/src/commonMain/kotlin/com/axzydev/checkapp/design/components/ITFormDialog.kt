package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone

/**
 * Formulario largo como **pantalla modal**.
 *
 * Regla del sistema: si el formulario no cabe cómodo en un `AlertDialog`, se
 * abre a pantalla completa. Un formulario de cuatro campos o más empujando la
 * lista hacia abajo obliga a hacer scroll para ver la lista y el formulario a
 * la vez, y el usuario pierde de vista dónde estaba.
 *
 * Trae cabecera con botón de cerrar, contenido con scroll y pie con Cancelar /
 * Guardar fijos.
 */
@Composable
fun ITFormDialog(
    isOpen: Boolean,
    title: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmLabel: String,
    modifier: Modifier = Modifier,
    cancelLabel: String = "Cancelar",
    confirmEnabled: Boolean = true,
    confirmLoading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!isOpen) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            color = AxzyColors.background,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Cabecera ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AxzySpacing.sm, vertical = AxzySpacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = ITIcons.Close,
                            contentDescription = "Cerrar",
                            tint = AxzyColors.onSurface,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    ITText(
                        text = title,
                        color = AxzyColors.onSurface,
                        style = AxzyType.screenTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                }

                Divider()

                // ── Contenido ──
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = AxzySpacing.screenH, vertical = AxzySpacing.screenV),
                    verticalArrangement = Arrangement.spacedBy(AxzySpacing.cardGap),
                    content = content,
                )

                Divider()

                // ── Pie ──
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AxzyColors.surface)
                        .padding(horizontal = AxzySpacing.screenH, vertical = AxzySpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ITButton(
                        label = cancelLabel,
                        onClick = onDismiss,
                        outlined = true,
                        tone = Tone.Neutral,
                        compact = true,
                        fullWidth = false,
                    )
                    ITButton(
                        label = confirmLabel,
                        onClick = onConfirm,
                        loading = confirmLoading,
                        enabled = confirmEnabled,
                        compact = true,
                        fullWidth = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AxzyColors.outlineVariant),
    )
}
