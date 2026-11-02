package com.axzydev.checkapp.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.generated.resources.Res
import com.axzydev.checkapp.design.theme.Palette
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.ExperimentalCompottieApi
import io.github.alexzhirkevich.compottie.LottieClipSpec
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
// `Resource` es una extensión sobre el companion de `LottieCompositionSpec`, no
// un miembro: hay que importarla aparte o no resuelve.
import io.github.alexzhirkevich.compottie.Resource
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

/**
 * Pantalla de arranque: el `checkapp-loader.json` que ya usa la WEB.
 *
 * Se reproduce sobre **verde plano** ([Palette.brand500]) y no sobre el degradado
 * de marca, por un motivo concreto: la animación trae su propia capa de fondo de
 * ese mismo verde, así que sobre un degradado se vería el recuadro de la
 * animación recortado contra el fondo más oscuro de abajo. Sobre verde plano, el
 * fondo de la animación desaparece y sólo se ve el QR con el pin.
 *
 * El JSON vive en `composeResources/files/` y no en `drawable/`: no es una imagen,
 * es un documento que se lee en tiempo de ejecución.
 */
@OptIn(ExperimentalCompottieApi::class)
@Composable
fun ITSplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Palette.brand500),
        contentAlignment = Alignment.Center,
    ) {
        val composition by rememberLottieComposition(
            LottieCompositionSpec.Resource(
                path = LOTTIE_FILE,
                reader = { path -> Res.readBytes(path) },
            ),
        )

        val painter = rememberLottiePainter(
            composition = composition,
            // Una sola pasada y **cortada antes del fundido** del final.
            //
            // El JSON es un *loader* de la WEB: se arma (frames 0-80), aguanta y se
            // desvanece (185-200) para volver a empezar. Reproducido en bucle aquí,
            // el logo desaparecía solo a los 3 s y, con el splash en 1,4 s, lo que
            // se veía era el logo a medio armar y yéndose: justo lo contrario de un
            // momento de marca. Con `iterations = 1` y el corte en el frame 182, se
            // arma, se queda montado y espera ahí hasta que la app está lista.
            iterations = 1,
            clipSpec = LottieClipSpec.Frame(min = 0, max = LAST_BUILD_FRAME),
        )

        Image(
            painter = painter,
            // Decorativo: la app no necesita que el lector de pantalla anuncie
            // "animación" al arrancar, sólo que llegue a la pantalla siguiente.
            contentDescription = null,
            modifier = Modifier.size(LOTTIE_SIZE),
        )
    }
}

private const val LOTTIE_FILE = "files/checkapp-loader.json"

/**
 * Último frame que se reproduce.
 *
 * El logo queda completo sobre el frame 80; a partir del 185 las capas se desvanecen
 * para que el bucle no dé un salto. Se corta en el 182: todo lo que hay entre el 80
 * y el 182 es el logo montado, que es lo que se quiere dejar en pantalla.
 */
private const val LAST_BUILD_FRAME = 182

/** Tamaño del loader: el mismo peso visual que el logo del login. */
private val LOTTIE_SIZE = 180.dp
