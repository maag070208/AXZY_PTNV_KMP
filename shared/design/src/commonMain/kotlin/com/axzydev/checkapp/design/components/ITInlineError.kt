package com.axzydev.checkapp.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Banda de error **dentro** de una pantalla, sin ocupar la pantalla entera.
 *
 * Para el fallo de un campo o de una acción que el usuario puede corregir sin
 * salir: el error de un formulario no justifica un `EmptyState`, pero tampoco puede
 * ser un texto rojo suelto. Lleva icono además de color, porque el color solo no
 * comunica.
 *
 * Para el fallo que impide usar la pantalla está `ScreenState.Error`, que además
 * ofrece reintentar.
 */
@Composable
fun ITInlineError(
    message: String,
    modifier: Modifier = Modifier,
) {
    val colors = Tone.Danger.palette

    Surface(
        color = colors.soft,
        shape = AxzyShape.md,
        border = BorderStroke(1.dp, colors.border),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AxzySpacing.md,
                vertical = AxzySpacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            Icon(
                imageVector = ITIcons.Warning,
                contentDescription = null,
                tint = colors.onSoft,
                modifier = Modifier.size(18.dp),
            )
            ITText(
                text = message,
                color = colors.onSoft,
                style = AxzyType.cardBody,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
