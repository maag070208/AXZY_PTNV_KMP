package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing

/**
 * Botón de menú (tres barras).
 *
 * Se dibuja con `Box` en vez de usar un icono porque el proyecto no tiene la
 * dependencia de iconos y estamos construyendo en modo offline: añadirla
 * habría obligado a descargar un artefacto nuevo.
 */
@Composable
fun MenuButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ITTouchableOpacity(
        onClick = onClick,
        modifier = modifier,
        role = Role.Button,
    ) {
        Column(
            modifier = Modifier.padding(AxzySpacing.sm),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            repeat(3) {
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier
                        .size(width = 18.dp, height = 2.dp)
                        .background(AxzyColors.onSurface, AxzyShape.pill),
                )
            }
        }
    }
}
