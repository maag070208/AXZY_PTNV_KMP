package com.axzydev.checkapp.design.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzyShadow
import com.axzydev.checkapp.design.theme.AxzySpacing

/**
 * Superficie base de la app: tarjeta blanca, borde sutil y un halo suave.
 *
 * El aspecto "premium terso" viene de dos cosas juntas: un borde de 1 px
 * (`slate200`) que define la tarjeta y un **halo grande y muy tenue** teñido de
 * slate —no una sombra gris marcada—. Antes sólo tenía elevación de 1 dp, así
 * que las tarjetas se veían planas sobre el fondo.
 *
 * Sigue siendo el contenedor de todo lo que agrupa información (métricas,
 * secciones de formulario, filas de lista).
 *
 * Con `onClick` la tarjeta entera es pulsable (y anuncia su rol de botón a
 * accesibilidad); sin él es sólo una superficie.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ITCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Boolean = true,
    shape: Shape = AxzyShape.xl,
    elevated: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    val inner = if (contentPadding) Modifier.padding(AxzySpacing.lg) else Modifier
    val border = BorderStroke(1.dp, AxzyColors.outlineVariant)

    val surface = if (elevated) {
        modifier.shadow(
            elevation = AxzyShadow.card,
            shape = shape,
            clip = false,
            ambientColor = AxzyShadow.color,
            spotColor = AxzyShadow.color,
        )
    } else {
        modifier
    }

    val common: @Composable () -> Unit = {
        Column(modifier = inner, content = content)
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = surface,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = AxzyColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = border,
            content = { common() },
        )
    } else {
        Card(
            modifier = surface,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = AxzyColors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = border,
            content = { common() },
        )
    }
}
