package com.axzydev.checkapp.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Botón estándar del design system.
 *
 * Dos cambios respecto a la versión anterior, que venían de ver las pantallas
 * en el emulador:
 *
 * 1. **`tone`** — antes sólo existía el verde de marca, así que "Eliminar" salía
 *    verde como "Guardar" y el color de peligro había que pasarlo a mano.
 * 2. **`fullWidth`** — antes el botón *siempre* ocupaba todo el ancho y medía
 *    52 dp. Por eso las pantallas viejas son columnas de barras gigantes: no era
 *    una decisión de diseño, era el único modo que existía.
 *
 * Se mantiene `fullWidth = true` por defecto para no romper las pantallas que ya
 * lo usan; las nuevas pasan `false` cuando el botón no es la acción principal.
 */
@Composable
fun ITButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    outlined: Boolean = false,
    tone: Tone = Tone.Brand,
    /** Botón bajo y ajustado al contenido, para diálogos y barras de acciones. */
    compact: Boolean = false,
    fullWidth: Boolean = !compact,
    /** Se pinta a la derecha de la etiqueta: una flecha, un ▶… */
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = tone.palette
    val height = if (compact) 36.dp else 52.dp
    val shape = if (compact) AxzyShape.sm else AxzyShape.button
    val padding = if (compact) {
        PaddingValues(horizontal = AxzySpacing.lg)
    } else {
        PaddingValues(horizontal = AxzySpacing.xl)
    }

    val base = modifier
        .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
        .height(height)

    // Sombra del color del tono: un botón de marca con halo verde se lee como
    // acción principal; el outlined se queda plano a propósito.
    val filled = base.shadow(
        elevation = 10.dp,
        shape = shape,
        clip = false,
        ambientColor = colors.solid.copy(alpha = 0.28f),
        spotColor = colors.solid.copy(alpha = 0.28f),
    )

    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.height(18.dp).width(18.dp),
                strokeWidth = 2.dp,
                color = if (outlined) colors.solid else AxzyColors.surface,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                ITText(
                    text = label,
                    color = if (outlined) colors.solid else AxzyColors.surface,
                    style = AxzyType.button,
                )
                trailing?.invoke()
            }
        }
    }

    if (outlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = base,
            enabled = enabled && !loading,
            shape = shape,
            contentPadding = padding,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.solid),
            content = { content() },
        )
    } else {
        Button(
            onClick = onClick,
            modifier = filled,
            enabled = enabled && !loading,
            shape = shape,
            contentPadding = padding,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.solid,
                contentColor = AxzyColors.surface,
            ),
            content = { content() },
        )
    }
}
