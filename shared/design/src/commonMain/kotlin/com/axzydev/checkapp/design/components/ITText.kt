package com.axzydev.checkapp.design.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

/**
 * Texto estándar del design system.
 *
 * Todo texto de la app pasa por aquí: es lo que garantiza que la tipografía sea
 * la misma en las 31 pantallas y que un cambio de escala no obligue a buscarla
 * por todo el código.
 *
 * `maxLines` + `overflow` existen porque sin ellos un nombre largo de cliente o
 * de ubicación desborda la tarjeta en vez de cortarse con puntos suspensivos.
 */
@Composable
fun ITText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    textAlign: TextAlign? = null,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
    )
}
