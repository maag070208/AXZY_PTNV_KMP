package com.axzydev.checkapp.design.components

import androidx.compose.foundation.BorderStroke
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
 * Tres cambios respecto a la versión anterior, que venían de ver las pantallas
 * en el emulador:
 *
 * 1. **`tone`** — antes sólo existía el verde de marca, así que "Eliminar" salía
 *    verde como "Guardar" y el color de peligro había que pasarlo a mano.
 * 2. **`fullWidth`** — antes el botón *siempre* ocupaba todo el ancho y medía
 *    52 dp. Por eso las pantallas viejas son columnas de barras gigantes: no era
 *    una decisión de diseño, era el único modo que existía.
 * 3. **Estado deshabilitado** — la etiqueta se pintaba blanca pasara lo que
 *    pasara, así que un botón inactivo era texto blanco sobre el gris
 *    translúcido de Material, con la sombra del tono asomando por debajo. Ahora
 *    el inactivo es el **tono en su versión suave** (`soft` + borde del tono +
 *    `onSoft`), no un gris: se lee como una acción que todavía no está
 *    disponible, no como un botón roto.
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

    // Mientras carga, el botón se pinta como si estuviera activo aunque el
    // formulario lo deshabilite (`canSubmit` suele ser false con `loading`): el
    // spinner es la señal de que la acción está en marcha, y sobre el relleno
    // suave en vez del sólido se lee como "apagado" justo cuando trabaja.
    val solid = enabled || loading

    // Sombra del color del tono: un botón de marca con halo verde se lee como
    // acción principal; el outlined se queda plano a propósito. El botón
    // deshabilitado también va plano: un halo de marca debajo de un relleno gris
    // no aporta nada y, como el gris de Material es translúcido, se transparentaba
    // por dentro del botón (se veía un rectángulo fantasma).
    val filled = if (solid) {
        base.shadow(
            elevation = 10.dp,
            shape = shape,
            clip = false,
            ambientColor = colors.solid.copy(alpha = 0.28f),
            spotColor = colors.solid.copy(alpha = 0.28f),
        )
    } else {
        base
    }

    // La etiqueta la pinta `ITText` con un color explícito, así que el
    // `disabledContentColor` de Material nunca llega a aplicarse: hay que
    // resolverlo aquí.
    //
    // Deshabilitado no es "gris": es el mismo tono en su versión suave
    // (`soft` + `border` + `onSoft`), que se lee como "la acción está ahí, todavía
    // no disponible" en vez de como un botón roto. El outlined, que no tiene
    // relleno que lo sostenga, sí se apaga del todo.
    val labelColor = when {
        solid && outlined -> colors.solid
        solid -> AxzyColors.surface
        outlined -> AxzyColors.onSurfaceVariant
        else -> colors.onSoft
    }

    val content: @Composable () -> Unit = {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.height(18.dp).width(18.dp),
                strokeWidth = 2.dp,
                color = labelColor,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
            ) {
                ITText(
                    text = label,
                    color = labelColor,
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
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.solid,
                disabledContentColor = AxzyColors.onSurfaceVariant,
            ),
            content = { content() },
        )
    } else {
        Button(
            onClick = onClick,
            modifier = filled,
            enabled = enabled && !loading,
            shape = shape,
            contentPadding = padding,
            // Un borde fino del propio tono sostiene el relleno suave del estado
            // inactivo; sin él, el botón se lee como un manchón.
            border = if (solid) null else BorderStroke(1.dp, colors.border),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.solid,
                contentColor = AxzyColors.surface,
                // El relleno deshabilitado por defecto es `onSurface` al 12 %:
                // translúcido y agrisado, así que el botón parecía roto y dejaba
                // ver lo que hubiera detrás. El tono suave mantiene la jerarquía
                // (sigue siendo la acción principal) y contrasta 6:1.
                disabledContainerColor = if (solid) colors.solid else colors.soft,
                disabledContentColor = if (solid) AxzyColors.surface else colors.onSoft,
            ),
            content = { content() },
        )
    }
}
