package com.axzydev.checkapp.design.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing

/**
 * Bloque gris con pulso, para construir esqueletos de carga.
 *
 * Regla de la app (heredada del design system de React Native): **en la carga
 * inicial nunca se usa un spinner centrado**, sino un esqueleto con la forma
 * del contenido. El spinner queda para bloqueos de mutación (guardar, borrar).
 * Así la pantalla no salta de "vacío" a "lleno": cambia de forma gris a
 * contenido.
 */
@Composable
fun ITShimmer(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
    shape: RoundedCornerShape = AxzyShape.xs,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmerAlpha",
    )

    Box(
        modifier = modifier
            .height(height)
            .alpha(alpha)
            .background(AxzyColors.surfaceVariant, shape),
    )
}

/**
 * Esqueleto de una fila de lista: avatar, dos líneas de texto y un hueco para
 * las acciones. Tiene la forma real de una fila para que el salto al contenido
 * sea mínimo.
 */
@Composable
fun ITSkeletonRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AxzySpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
    ) {
        ITShimmer(modifier = Modifier.size(40.dp), height = 40.dp, shape = AxzyShape.sm)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            ITShimmer(modifier = Modifier.fillMaxWidth(0.55f))
            ITShimmer(modifier = Modifier.fillMaxWidth(0.35f), height = 11.dp)
        }
        ITShimmer(modifier = Modifier.width(40.dp), height = 20.dp, shape = AxzyShape.pill)
    }
}

/**
 * Esqueleto completo de una lista. `rows` controla cuántas filas fantasma se
 * dibujan: tres o cuatro es suficiente para transmitir "esto se está llenando"
 * sin que la pantalla parezca rota.
 */
@Composable
fun ITSkeletonList(
    modifier: Modifier = Modifier,
    rows: Int = 4,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(rows) { ITSkeletonRow() }
    }
}

/**
 * Esqueleto de las tarjetas de KPI del inicio.
 */
@Composable
fun ITSkeletonStats(
    modifier: Modifier = Modifier,
    count: Int = 4,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.cardGap),
    ) {
        repeat(count) {
            ITCard(modifier = Modifier.weight(1f)) {
                ITShimmer(modifier = Modifier.fillMaxWidth(0.6f), height = 10.dp)
                Spacer(Modifier.height(AxzySpacing.sm))
                ITShimmer(modifier = Modifier.width(48.dp), height = 24.dp)
            }
        }
    }
}
