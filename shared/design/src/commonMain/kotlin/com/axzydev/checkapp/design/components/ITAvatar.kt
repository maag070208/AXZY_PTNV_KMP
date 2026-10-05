package com.axzydev.checkapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzyType
import com.axzydev.checkapp.design.theme.Tone
import com.axzydev.checkapp.design.theme.palette

/**
 * Avatar cuadrado con la inicial, y punto de estado superpuesto.
 *
 * Es la pieza que más identifica las listas de la app React Native: el cuadro
 * de color con la inicial en el verde de marca y una bolita verde o roja
 * asomando por la esquina inferior derecha.
 *
 * El punto lleva un borde del color de la superficie para que se despegue del
 * avatar; sin ese borde los dos verdes se funden y no se ve el estado.
 *
 * @param size el lado del cuadro. En las listas son 48 dp, como en la app
 *   original.
 */
@Composable
fun ITAvatar(
    initial: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    status: Tone? = null,
    surface: Color = AxzyColors.surface,
) {
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .background(AxzyColors.surfaceVariant, AxzyShape.lg),
            contentAlignment = Alignment.Center,
        ) {
            ITText(
                text = initial.take(1).uppercase(),
                color = AxzyColors.primary,
                style = AxzyType.avatarInitial,
            )
        }

        status?.let { tone ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(14.dp)
                    .background(surface, AxzyShape.pill)
                    .padding(2.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(tone.palette.solid, AxzyShape.pill),
                )
            }
        }
    }
}
