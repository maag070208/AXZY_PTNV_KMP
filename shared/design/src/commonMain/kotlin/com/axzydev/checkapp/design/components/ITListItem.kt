package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Pie de tarjeta con acciones separadas por divisores verticales.
 *
 * Es el patrón de la app React Native: la tarjeta lleva una fila al final,
 * separada por una línea fina, con acciones de igual ancho ("QR | Editar |
 * Eliminar"). Se distingue del cuerpo porque cada acción ocupa su tercio, así
 * que se pueden pulsar sin apuntar.
 */
@Composable
fun ITCardFooter(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(AxzyColors.outlineVariant),
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = AxzySpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * Acción dentro del pie de una tarjeta.
 *
 * Es una extensión de `RowScope` **a propósito**: cada acción tiene que ocupar el
 * mismo ancho (`weight(1f)`) para que se repartan la fila. Sin el peso, la primera
 * acción se queda con todo el espacio y las siguientes desaparecen — que es
 * exactamente lo que pasó en Puntos: sólo se veía "Editar".
 */
@Composable
fun RowScope.ITFooterAction(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Brand,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
) {
    val color = if (enabled) tone.palette.solid else AxzyColors.onSurfaceVariant

    ITTouchableOpacity(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.weight(1f),
        scaleTo = 0.94f,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = AxzySpacing.sm),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(modifier = Modifier.padding(end = AxzySpacing.xs)) { icon() }
            }
            ITText(
                text = label,
                color = color,
                style = AxzyType.footerAction,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Separador vertical entre acciones del pie. */
@Composable
fun ITFooterDivider(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(1.dp)
            .height(20.dp)
            .background(AxzyColors.outlineVariant),
    )
}

/**
 * Tarjeta de elemento de lista.
 *
 * Réplica del `ITCard` que envuelve cada fila en la app React Native, con su
 * composición: avatar con punto de estado, título, línea de metadatos, insignia
 * a la derecha, y un pie de acciones opcional separado por una línea.
 *
 * Se expone como componente propio y no como receta para que las ~15 pantallas
 * de listado salgan iguales sin depender de que cada una recuerde los márgenes.
 */
@Composable
fun ITListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    meta: String? = null,
    badge: (@Composable () -> Unit)? = null,
    avatarInitial: String? = null,
    avatarStatus: Tone? = null,
    /**
     * Icono de la fila, **sin recuadro ni inicial**.
     *
     * Para listas de accesos (menú, ajustes, "Historial / Notificaciones / Mi
     * perfil"): una letra que repite la palabra de al lado no informa de nada, y
     * el recuadro tintado le da a la fila el peso visual de un dato cuando en
     * realidad sólo es un enlace.
     */
    leadingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    reference: String? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    ITCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        contentPadding = false,
    ) {
        Column(modifier = Modifier.padding(AxzySpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
            ) {
                avatarInitial?.let { ITAvatar(initial = it, status = avatarStatus) }

                leadingIcon?.let { icon ->
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center,
                    ) { icon() }
                }

                Column(modifier = Modifier.weight(1f)) {
                    ITText(
                        text = title,
                        color = AxzyColors.onSurface,
                        style = AxzyType.itemTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (subtitle != null) {
                        ITText(
                            text = subtitle,
                            color = AxzyColors.onSurfaceVariant,
                            style = AxzyType.itemMeta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (meta != null) {
                        ITText(
                            text = meta,
                            color = AxzyColors.slate400,
                            style = AxzyType.itemMeta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                badge?.invoke()
            }

            // Franja de referencia: fondo gris muy claro, como en la app original.
            if (reference != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AxzySpacing.sm)
                        .background(AxzyColors.surfaceVariant, AxzyShape.sm)
                        .border(1.dp, AxzyColors.outlineVariant, AxzyShape.sm)
                        .padding(AxzySpacing.sm),
                ) {
                    ITText(
                        text = reference,
                        color = AxzyColors.slate500,
                        style = AxzyType.itemMeta,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }

        footer?.let {
            Box(modifier = Modifier.padding(horizontal = AxzySpacing.lg, vertical = 0.dp)) {
                it()
            }
            Box(modifier = Modifier.height(AxzySpacing.md))
        }
    }
}
