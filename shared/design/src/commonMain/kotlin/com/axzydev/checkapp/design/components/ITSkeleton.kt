package com.axzydev.checkapp.design.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
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
 * Esqueleto de una fila de lista: misma tarjeta que `ITListItem` (borde +
 * superficie), avatar, dos líneas de texto y un hueco para el badge. Al tener
 * la forma real, el salto al contenido es mínimo.
 */
@Composable
fun ITSkeletonRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AxzyColors.surface, AxzyShape.xl)
            .border(1.dp, AxzyColors.outlineVariant, AxzyShape.xl)
            .padding(AxzySpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
    ) {
        ITShimmer(modifier = Modifier.size(44.dp), height = 44.dp, shape = AxzyShape.lg)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
        ) {
            ITShimmer(modifier = Modifier.fillMaxWidth(0.55f))
            ITShimmer(modifier = Modifier.fillMaxWidth(0.35f), height = 11.dp)
        }
        ITShimmer(modifier = Modifier.width(48.dp), height = 20.dp, shape = AxzyShape.pill)
    }
}

/**
 * Esqueleto completo de una lista. `rows` controla cuántas filas fantasma se
 * dibujan: tres o cuatro es suficiente para transmitir "esto se está llenando".
 */
@Composable
fun ITSkeletonList(
    modifier: Modifier = Modifier,
    rows: Int = 4,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.cardGap),
    ) {
        repeat(rows) { ITSkeletonRow() }
    }
}

/** Esqueleto de una tarjeta de KPI. */
@Composable
private fun StatSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(AxzyColors.surface, AxzyShape.xl)
            .border(1.dp, AxzyColors.outlineVariant, AxzyShape.xl)
            .padding(AxzySpacing.md),
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        ITShimmer(modifier = Modifier.fillMaxWidth(0.7f), height = 10.dp)
        ITShimmer(modifier = Modifier.width(48.dp), height = 24.dp)
    }
}

/** Esqueleto de las tarjetas de KPI del inicio. */
@Composable
fun ITSkeletonStats(
    modifier: Modifier = Modifier,
    count: Int = 3,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.cardGap),
    ) {
        repeat(count) {
            StatSkeleton(modifier = Modifier.weight(1f))
        }
    }
}

/**
 * Esqueleto del arranque de la app: barra, KPIs y lista. Se usa mientras se
 * restaura la sesión, en lugar de un spinner centrado que no anticipa nada.
 */
@Composable
fun ITSkeletonAppShell(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = AxzyColors.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = AxzySpacing.screenH, vertical = AxzySpacing.screenV),
            verticalArrangement = Arrangement.spacedBy(AxzySpacing.lg),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
            ) {
                ITShimmer(modifier = Modifier.size(40.dp), height = 40.dp, shape = AxzyShape.sm)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
                ) {
                    ITShimmer(modifier = Modifier.fillMaxWidth(0.4f), height = 12.dp)
                    ITShimmer(modifier = Modifier.fillMaxWidth(0.6f), height = 20.dp)
                }
            }

            ITSkeletonStats(count = 3)

            Spacer(Modifier.height(AxzySpacing.xs))

            ITSkeletonList(rows = 5)
        }
    }
}
