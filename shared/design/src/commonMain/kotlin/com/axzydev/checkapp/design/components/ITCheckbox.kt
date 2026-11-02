package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Casilla con etiqueta, para opciones binarias dentro de un formulario
 * (hoy: "Recordarme" en el login).
 *
 * Detalles que no son obvios:
 * - **Toda la fila es pulsable** y mide 48 dp de alto como mínimo. Una casilla de
 *   20 dp suelta es un objetivo táctil imposible en el móvil.
 * - Los descendientes se fusionan en un solo nodo de accesibilidad, así el lector
 *   de pantalla anuncia "Recordarme, casilla, activada" y no dos elementos.
 * - Deshabilitado baja la opacidad del conjunto, no sólo la del cuadro.
 */
@Composable
fun ITCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val brand = Tone.Brand.palette
    val alpha = if (enabled) 1f else DISABLED_ALPHA

    Row(
        modifier = modifier
            .heightIn(min = MIN_TOUCH_TARGET)
            .semantics(mergeDescendants = true) { }
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = onCheckedChange,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(BOX_SIZE)
                .background(
                    color = if (checked) brand.solid.copy(alpha = alpha) else Color.Transparent,
                    shape = AxzyShape.xs,
                )
                .border(
                    width = BORDER_WIDTH,
                    color = if (checked) {
                        brand.solid.copy(alpha = alpha)
                    } else {
                        AxzyColors.outline.copy(alpha = alpha)
                    },
                    shape = AxzyShape.xs,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (checked) {
                Icon(
                    imageVector = ITIcons.Check,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = alpha),
                    modifier = Modifier.size(TICK_SIZE),
                )
            }
        }

        ITText(
            text = label,
            color = AxzyColors.onSurfaceVariant.copy(alpha = alpha),
            style = AxzyType.cardBody,
        )
    }
}

private val BOX_SIZE = 20.dp
private val TICK_SIZE = 14.dp
private val BORDER_WIDTH = 1.5.dp
private val MIN_TOUCH_TARGET = 48.dp
private const val DISABLED_ALPHA = 0.38f
