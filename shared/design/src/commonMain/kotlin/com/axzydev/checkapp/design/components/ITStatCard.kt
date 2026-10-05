package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
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
 * Tarjeta de indicador (KPI).
 *
 * Fondo del tono **con su borde**, icono arriba a la derecha, cifra grande y
 * etiqueta. Mismo lenguaje que las tarjetas de KPI de la WEB: el color comunica
 * de un vistazo si el número es bueno (verde), neutro o requiere atención
 * (rojo/ámbar), sin leer la etiqueta.
 */
@Composable
fun ITStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    tone: Tone = Tone.Neutral,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
) {
    val colors = tone.palette

    val surface: @Composable () -> Unit = {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = AxzyShadow.card,
                    shape = AxzyShape.xl,
                    clip = false,
                    ambientColor = AxzyShadow.color,
                    spotColor = AxzyShadow.color,
                )
                .background(colors.soft, AxzyShape.xl)
                .border(1.dp, colors.border, AxzyShape.xl)
                .padding(AxzySpacing.lg),
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
                    style = AxzyType.kpiLabel,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.solid,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            ITText(
                text = value,
                color = colors.onSoft,
                style = AxzyType.kpiValue,
                maxLines = 1,
            )

            if (hint != null) {
                ITText(
                    text = hint,
                    color = AxzyColors.onSurfaceVariant,
                    style = AxzyType.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    if (onClick != null) {
        ITTouchableOpacity(onClick = onClick, modifier = modifier) { surface() }
    } else {
        Box(modifier = modifier) { surface() }
    }
}
