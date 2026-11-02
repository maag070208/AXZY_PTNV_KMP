package com.axzydev.checkapp.pages.getstarted.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.axzydev.checkapp.design.components.BrandWordmark
import com.axzydev.checkapp.design.components.ITButton
import com.axzydev.checkapp.design.components.ITCard
import com.axzydev.checkapp.design.components.ITText
import com.axzydev.checkapp.design.components.ITWelcomeArt
import com.axzydev.checkapp.design.theme.AxzyColors
import com.axzydev.checkapp.design.theme.AxzyGradients
import com.axzydev.checkapp.design.theme.AxzyMotion
import com.axzydev.checkapp.design.theme.AxzyShape
import com.axzydev.checkapp.design.theme.AxzySpacing
import com.axzydev.checkapp.design.theme.AxzyType

/**
 * Bienvenida (Get Started).
 *
 * Misma gramática que el login: **campo de marca** con degradado arriba y **hoja**
 * blanca pegada abajo. Así las dos pantallas de entrada se leen como la misma app.
 *
 * Sobre el degradado va una ilustración **animada** ([ITWelcomeArt]): la tarjeta
 * de un reporte con su línea de ruta, y los checkpoints encendiéndose por turno —
 * el recorrido de una ronda. Antes eran las mismas formas dibujadas con `Box`,
 * quietas; el objeto del producto es el mismo, ahora cuenta lo que hace.
 */
@Composable
fun GetStartedScreen(
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    returningUserName: String = "",
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current

        // Misma entrada que el login: la hoja sube y aparece. Con `graphicsLayer`
        // la métrica no cambia mientras anima.
        var entered by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { entered = true }
        val entrance by animateFloatAsState(
            targetValue = if (entered) 1f else 0f,
            animationSpec = tween(durationMillis = AxzyMotion.normal),
            label = "getStartedSheetEntrance",
        )
        val entranceOffset = with(density) { SHEET_ENTER_OFFSET.toPx() }

        // La rampa se ancla a una altura concreta porque el verde se pinta a
        // pantalla completa y la hoja tapa el resto: debajo de la hoja siempre
        // tiene que haber verde profundo, nunca un corte.
        val ramp = remember(maxWidth, maxHeight) {
            with(density) {
                AxzyGradients.authRamp(
                    endX = maxWidth.toPx(),
                    endY = (maxHeight * GRADIENT_RAMP_RATIO).toPx(),
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().background(ramp))

        // Se calcula aquí y no dentro del `Column`: allí el receptor implícito es
        // `ColumnScope` y `maxHeight` deja de estar en alcance.
        val sheetMinHeight = maxHeight * SHEET_MIN_RATIO

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .windowInsetsPadding(WindowInsets.statusBars),
                contentAlignment = Alignment.Center,
            ) {
                ITWelcomeArt(width = ART_WIDTH)
            }

            GetStartedSheet(
                onStart = onStart,
                returningUserName = returningUserName,
                minHeight = sheetMinHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = entrance
                        translationY = (1f - entrance) * entranceOffset
                    },
            )
        }
    }
}

/** Hoja blanca con la marca, el mensaje y el botón. */
@Composable
private fun GetStartedSheet(
    onStart: () -> Unit,
    minHeight: Dp,
    modifier: Modifier = Modifier,
    returningUserName: String = "",
) {
    // Con sesión viva la pantalla saluda por el nombre y el botón dice a dónde va
    // ("Continuar" entra al inicio). Sin sesión, presenta la app y lleva al login.
    val returning = returningUserName.isNotBlank()

    ITCard(
        modifier = modifier,
        shape = AxzyShape.sheet,
        contentPadding = false,
    ) {
        // La hoja tiene alto mínimo para que la marca no quede apretada contra el
        // borde: antes medía lo que medía su contenido y el bloque blanco se veía
        // como una franja. El contenido se centra en ese alto, así que el aire se
        // reparte arriba y abajo. Con `heightIn` y no con `fillMaxHeight` porque el
        // alto del padre es el de la pantalla: rellenarlo se comería el verde.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight)
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
                    ),
                )
                .padding(horizontal = AxzySpacing.xxl, vertical = AxzySpacing.xxl),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // El lockup manda en la hoja: es lo único que identifica la app
                // antes de que el usuario lea una línea.
                BrandWordmark(height = WORDMARK_HEIGHT)

                Spacer(Modifier.height(AxzySpacing.xxl))

                ITText(
                    text = if (returning) {
                        "Hola de nuevo, $returningUserName"
                    } else {
                        "Todo tu turno, bajo control"
                    },
                    color = AxzyColors.onSurface,
                    style = AxzyType.screenTitle,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(AxzySpacing.sm))

                ITText(
                    text = if (returning) {
                        "Tu sesión sigue abierta. Continúa donde la dejaste."
                    } else {
                        "Rondas, novedades y evidencias desde el celular, incluso sin señal."
                    },
                    color = AxzyColors.onSurfaceVariant,
                    style = AxzyType.cardBody,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(AxzySpacing.xxxl))

                ITButton(
                    label = if (returning) "Continuar" else "Comenzar",
                    onClick = onStart,
                )
            }
        }
    }
}

/** Dónde termina la rampa del degradado; de ahí para abajo, verde profundo. */
private const val GRADIENT_RAMP_RATIO = 0.62f

/** Cuánto sube la hoja en la animación de entrada. */
private val SHEET_ENTER_OFFSET = 24.dp

/** Ancho de la ilustración animada. */
private val ART_WIDTH = 330.dp

/**
 * Alto mínimo de la hoja blanca, como fracción de la pantalla.
 *
 * La hoja mide lo que mide su contenido, y con el logo a 72 dp el bloque blanco se
 * quedaba en un tercio de la pantalla: se leía como una franja, no como la
 * superficie principal de la bienvenida.
 */
private const val SHEET_MIN_RATIO = 0.44f

/** Alto del lockup en la hoja. */
private const val WORDMARK_HEIGHT = 60
