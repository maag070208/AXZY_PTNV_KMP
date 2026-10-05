package com.axzydev.checkapp.design.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Diálogo de confirmación.
 *
 * Antes cada pantalla montaba su propio `AlertDialog` con `TextButton`s, así que
 * el botón destructivo no siempre se veía rojo ni decía lo mismo. Aquí el tono
 * decide el color y el texto de acción es obligatorio (`"Eliminar"`, no `"OK"`),
 * para que el usuario lea qué va a pasar en vez de confirmar a ciegas.
 *
 * @param tone `Danger` para acciones destructivas: pinta el botón de confirmar.
 * @param loading se usa durante la mutación para bloquear el doble toque.
 */
@Composable
fun ITConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    tone: Tone = Tone.Danger,
    loading: Boolean = false,
    dismissLabel: String = "Cancelar",
) {
    val colors = tone.palette

    AlertDialog(
        onDismissRequest = { if (!loading) onDismiss() },
        properties = DialogProperties(dismissOnClickOutside = !loading),
        containerColor = AxzyColors.surface,
        shape = AxzyShape.lg,
        title = {
            ITText(
                text = title,
                color = AxzyColors.onSurface,
                style = AxzyType.cardTitle,
            )
        },
        text = {
            ITText(
                text = message,
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.cardBody,
            )
        },
        // Los botones van sueltos, sin envolverlos en un `Row(fillMaxWidth)`:
        // `AlertDialog` ya los coloca él mismo en fila y alineados a la derecha.
        // Envolverlos los apilaba en vertical y desalineados (se vio en el
        // emulador), porque cada slot pasaba a ocupar el ancho completo.
        confirmButton = {
            ITButton(
                label = confirmLabel,
                onClick = onConfirm,
                loading = loading,
                enabled = !loading,
                tone = tone,
                compact = true,
            )
        },
        dismissButton = {
            ITButton(
                label = dismissLabel,
                onClick = onDismiss,
                outlined = true,
                enabled = !loading,
                compact = true,
                tone = Tone.Neutral,
            )
        },
    )
}
