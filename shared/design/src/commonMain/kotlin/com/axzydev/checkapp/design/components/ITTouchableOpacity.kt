package com.axzydev.checkapp.design.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role

/**
 * Contenedor interactivo con respuesta al pulsar.
 *
 * Portado del `ITTouchableOpacity` de la app React Native, que encoge la vista
 * a **0.97 con un muelle** al presionar. Es el detalle que hace que tocar una
 * tarjeta se sienta físico en lugar de instantáneo; sin él, una lista de
 * tarjetas idénticas se siente muerta al pulsarla.
 *
 * Se usa un muelle y no una transición lineal porque el rebote al soltar es lo
 * que da la sensación de material.
 *
 * `role` es obligatorio a propósito: un contenedor pulsable que no declara si es
 * botón, pestaña o casilla deja la pantalla muda para un lector de pantalla.
 */
@Composable
fun ITTouchableOpacity(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: Role = Role.Button,
    scaleTo: Float = 0.97f,
    content: @Composable BoxScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) scaleTo else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "pressScale",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled,
                role = role,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        content = content,
    )
}
