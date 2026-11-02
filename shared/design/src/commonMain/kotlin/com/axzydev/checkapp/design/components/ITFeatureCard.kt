package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.icons.ITIcons
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Bloque destacado oscuro.
 *
 * Es el elemento con más peso visual de la app: en la pantalla del guardia es el
 * escáner, y en el login es la marca. Negro, radio 32 y contenido centrado.
 *
 * La app React Native lo usa para reservar el sitio de la acción principal —
 * "aquí va a pasar lo importante"— y por eso funciona mejor que un degradado de
 * color: no compite con el verde de los botones.
 */
@Composable
fun ITFeatureCard(
    modifier: Modifier = Modifier,
    height: Dp? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (height != null) Modifier.height(height) else Modifier)
            .background(AxzyColors.ink, AxzyShape.heroCard),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Círculo claro con un contenido dentro, para poner sobre un bloque oscuro.
 *
 * En la app original el escáner es un círculo blanco de 72 dp sobre el negro; es
 * lo que hace legible el icono sin subir el brillo del bloque entero.
 */
@Composable
fun ITFeatureBadge(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    background: Color = Color.White,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, AxzyShape.pill),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Tarjeta de acción secundaria.
 *
 * Par de acciones rápidas ("Incidencia", "Mantenimiento") que en la app original
 * van lado a lado bajo el botón principal. El fondo es el tono suave y el texto
 * el tono sólido, así que se distinguen sin llenar la pantalla de color saturado.
 *
 * @param selected elegida: **no** se rellena de color, se marca con borde grueso del
 *   tono y una palomita. Un bloque saturado al lado de otros elementos de color
 *   saturan la pantalla —y el rojo de una categoría junto al verde de un estado
 *   vibra—; el borde y la palomita dicen lo mismo sin gritar.
 */
@Composable
fun ITActionTile(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Neutral,
    selected: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
) {
    val colors = tone.palette

    ITTouchableOpacity(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        scaleTo = 0.96f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(colors.soft, AxzyShape.actionTile)
                // Un borde del propio tono: sin él, el relleno suave se lee como
                // un rectángulo descolorido en vez de como una acción.
                .border(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) colors.solid else colors.border,
                    shape = AxzyShape.actionTile,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(modifier = Modifier.padding(end = AxzySpacing.sm)) { icon() }
            }
            ITText(
                text = label,
                color = colors.onSoft,
                style = AxzyType.button,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (selected) {
                Icon(
                    imageVector = ITIcons.Check,
                    contentDescription = null,
                    tint = colors.solid,
                    modifier = Modifier
                        .padding(start = AxzySpacing.xs)
                        .size(16.dp),
                )
            }
        }
    }
}

/**
 * Encabezado de sección, con acción opcional a la derecha.
 *
 * Mayúsculas y `letterSpacing` amplio, portado del `sectionTitle` original. Es
 * lo que separa bloques en una pantalla con muchas tarjetas sin necesidad de
 * líneas ni cajas.
 */
@Composable
fun ITSectionTitle(
    text: String,
    modifier: Modifier = Modifier,
    /** Marca a la izquierda del texto: el número de paso, un estado, un punto. */
    leading: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(AxzySpacing.sm))
        }
        ITText(
            text = text.uppercase(),
            color = AxzyColors.slate400,
            style = AxzyType.sectionLabel,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.weight(1f))
        action?.invoke()
    }
}

/** Fila con fondo tenue y esquinas redondeadas, para listas dentro de tarjetas. */
@Composable
fun ITTintedRow(
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Brand,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = tone.palette
    val base = modifier
        .fillMaxWidth()
        .background(colors.soft, AxzyShape.md)

    Box(
        modifier = if (onClick != null) {
            base.height(64.dp)
        } else {
            base
        },
        contentAlignment = Alignment.CenterStart,
    ) {
        if (onClick != null) {
            ITTouchableOpacity(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(horizontal = AxzySpacing.lg)) { content() }
            }
        } else {
            Box(modifier = Modifier.padding(horizontal = AxzySpacing.lg)) { content() }
        }
    }
}
