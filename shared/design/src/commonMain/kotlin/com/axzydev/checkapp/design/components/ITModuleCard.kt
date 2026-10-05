package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzyShadow
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Tarjeta de módulo del inicio.
 *
 * Tarjeta blanca con **borde** y halo suave (antes era blanco puro sin borde y
 * se veía "pegada" al fondo), icono en tile del tono y etiqueta centrada. Si hay
 * algo pendiente, una insignia en la esquina con el color del módulo.
 *
 * El detalle que hace que funcione: el fondo del icono es el color del módulo al
 * tono suave, no un gris. Así una rejilla de doce tarjetas se distingue de un
 * vistazo por color sin que ninguna grite.
 */
@Composable
fun ITModuleCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Brand,
    badge: Int? = null,
) {
    val colors = tone.palette
    val count = badge ?: 0

    ITTouchableOpacity(
        onClick = onClick,
        modifier = modifier.height(108.dp),
        scaleTo = 0.96f,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(108.dp)
                .shadow(
                    elevation = AxzyShadow.card,
                    shape = AxzyShape.xl,
                    clip = false,
                    ambientColor = AxzyShadow.color,
                    spotColor = AxzyShadow.color,
                )
                .background(AxzyColors.surface, AxzyShape.xl)
                .border(1.dp, AxzyColors.outlineVariant, AxzyShape.xl),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AxzySpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(colors.soft, AxzyShape.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.solid,
                        modifier = Modifier.size(22.dp),
                    )
                }

                Box(modifier = Modifier.height(AxzySpacing.sm))

                ITText(
                    text = label,
                    color = AxzyColors.slate700,
                    style = AxzyType.moduleLabel,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (count > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(AxzySpacing.sm)
                        .background(colors.solid, AxzyShape.pill),
                    contentAlignment = Alignment.Center,
                ) {
                    ITText(
                        text = if (count > 99) "+99" else count.toString(),
                        color = AxzyColors.surface,
                        style = AxzyType.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                    )
                }
            }
        }
    }
}

/**
 * Fila de la rejilla. Se separa del componente de tarjeta para que la pantalla
 * decida qué va en cada hueco sin conocer los márgenes.
 */
@Composable
fun ITModuleRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        content()
    }
}

/**
 * Tile de estadística del inicio.
 *
 * Fondo del tono **con su borde**, icono arriba a la derecha, cifra grande y
 * etiqueta, alineados a la izquierda. Es el mismo lenguaje que las tarjetas de
 * KPI de la WEB: fondo teñido + borde del tono + número protagonista.
 */
@Composable
fun ITStatTile(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tone: Tone = Tone.Brand,
) {
    val colors = tone.palette

    Column(
        modifier = modifier
            .background(colors.soft, AxzyShape.lg)
            .border(1.dp, colors.border, AxzyShape.lg)
            .padding(AxzySpacing.md),
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.xs),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ITText(
                text = label,
                color = AxzyColors.onSurfaceVariant,
                style = AxzyType.statLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.solid,
                modifier = Modifier.size(18.dp),
            )
        }

        ITText(
            text = value,
            color = colors.onSoft,
            style = AxzyType.kpiValue,
            maxLines = 1,
        )
    }
}
