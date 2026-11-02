package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing

/**
 * Las tres barras del menú, sin superficie ni gesto.
 *
 * Se dibujan con `Box` en vez de usar un icono porque el proyecto no tiene la
 * dependencia de iconos y estamos construyendo en modo offline: añadirla habría
 * obligado a descargar un artefacto nuevo.
 *
 * Va aparte de [MenuButton] porque sobre el degradado de marca las barras tienen
 * que ser blancas y el gesto lo pone el contenedor del hero, no ellas.
 */
@Composable
fun MenuBars(
    modifier: Modifier = Modifier,
    tint: Color = AxzyColors.onSurface,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .size(width = 18.dp, height = 2.dp)
                    .background(tint, AxzyShape.pill),
            )
        }
    }
}

/**
 * Botón de menú (tres barras) sobre una superficie de la app.
 *
 * Para la cabecera de marca se usa [ITHeroIconButton] con [MenuBars] en blanco.
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
        Box(modifier = Modifier.padding(AxzySpacing.sm)) {
            MenuBars()
        }
    }
}
