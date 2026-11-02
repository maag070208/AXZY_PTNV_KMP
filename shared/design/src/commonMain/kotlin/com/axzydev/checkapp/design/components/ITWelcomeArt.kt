package com.axzydev.checkapp.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.generated.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.Resource
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

/**
 * Ilustración de la bienvenida: la tarjeta del reporte con su línea de ruta.
 *
 * Es una animación de Lottie y no formas dibujadas con `Box` porque los
 * checkpoints se encienden por turno —el recorrido de una ruta— y eso en Compose
 * habría sido una coreografía de `animate*AsState` encadenados, más código y peor
 * de ajustar que el JSON. El JSON lo genera
 * `scripts/generate_welcome_lottie.py`: **no se edita a mano**, se regenera.
 *
 * No trae fondo: son blancos translúcidos que se componen sobre el degradado de
 * marca, así que sirve igual en la bienvenida que en cualquier superficie verde.
 */
@OptIn(ExperimentalCompottieApi::class)
@Composable
fun ITWelcomeArt(
    modifier: Modifier = Modifier,
    width: Dp = 260.dp,
    /**
     * Repeticiones. Por defecto en bucle, que es lo que quiere una pantalla de
     * presentación. En el login se pasa `1` a propósito: allí el usuario está
     * escribiendo y un bucle permanente distrae (y gasta batería) para no aportar
     * nada nuevo después de la primera pasada.
     */
    iterations: Int = Compottie.IterateForever,
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Resource(
            path = WELCOME_FILE,
            reader = { path -> Res.readBytes(path) },
        ),
    )

    val painter = rememberLottiePainter(
        composition = composition,
        iterations = iterations,
    )

    Image(
        painter = painter,
        // Decorativa: no hay nada que anunciar, sólo que se vea.
        contentDescription = null,
        modifier = modifier.size(width = width, height = width * ART_RATIO),
    )
}

private const val WELCOME_FILE = "files/checkapp-welcome.json"

/** Proporción del lienzo de la animación (360x258). */
private const val ART_RATIO = 258f / 360f
