package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyAlpha
import com.axzydev.checkapp.design.theme.AxzyGradients
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType

/**
 * Cabecera de marca con degradado.
 *
 * Es la firma visual de la app: aparece en el login y en el inicio, y es lo que
 * hace que una pantalla se reconozca como CheckApp antes de leer nada.
 *
 * Respeta el área segura superior (`statusBars`), así que el texto nunca queda
 * debajo del reloj — el bug que tenían las pantallas antiguas, donde el título
 * se dibujaba bajo la barra de estado.
 *
 * Se le puede pasar `content` para añadir debajo lo que necesite cada pantalla
 * (un saludo, unas cifras) sin salir del bloque de color.
 */
@Composable
fun ITBrandHero(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    compact: Boolean = false,
    brush: Brush = AxzyGradients.hero,
    onBack: (() -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(brush)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(
                start = AxzySpacing.xl,
                end = AxzySpacing.xl,
                top = if (compact) AxzySpacing.lg else AxzySpacing.xxl,
                bottom = if (compact) AxzySpacing.lg else AxzySpacing.xxl,
            ),
        verticalArrangement = Arrangement.spacedBy(AxzySpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.md),
        ) {
            BrandMark(
                size = if (compact) 32 else 44,
                background = Color.White.copy(alpha = AxzyAlpha.onBrandSurface),
                contentColor = Color.White,
            )

            Column(modifier = Modifier.weight(1f)) {
                ITText(
                    text = title,
                    color = Color.White,
                    style = if (compact) AxzyType.cardTitle else AxzyType.screenTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    ITText(
                        text = subtitle,
                        color = Color.White.copy(alpha = AxzyAlpha.onBrandMuted),
                        style = AxzyType.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            actions?.invoke()
        }

        content?.let {
            Spacer(Modifier.height(AxzySpacing.xs))
            it()
        }
    }
}

/**
 * Métrica sobre fondo de marca, para las cabeceras con cifras.
 *
 * Va en blanco translúcido en vez de en tarjeta opaca: sobre el degradado una
 * tarjeta blanca corta el color y se ve pegada.
 */
@Composable
fun ITHeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = Color.White,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = AxzyAlpha.onBrandSurface), AxzyShape.sm)
            .padding(horizontal = AxzySpacing.md, vertical = AxzySpacing.sm),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AxzySpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(accent, AxzyShape.pill),
            )
            ITText(
                text = label.uppercase(),
                color = Color.White.copy(alpha = AxzyAlpha.onBrandMuted),
                style = AxzyType.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ITText(
            text = value,
            color = Color.White,
            style = AxzyType.kpiValue,
            maxLines = 1,
        )
    }
}

/** Alto reservado para no repetir números sueltos en las pantallas. */
object HeroSizes {
    val compact: Dp = 64.dp
    val regular: Dp = 132.dp
}
