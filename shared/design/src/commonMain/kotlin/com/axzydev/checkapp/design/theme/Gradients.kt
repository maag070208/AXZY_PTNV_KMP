package com.axzydev.checkapp.design.theme

import androidx.compose.ui.graphics.Brush

/**
 * Degradados de marca.
 *
 * Un degradado mal usado es ruido, así que sólo hay tres y cada uno tiene un
 * trabajo: dar peso a la cabecera de una pantalla, teñir una superficie de
 * estado, o rellenar un indicador. El resto de la app es color plano.
 */
object AxzyGradients {

    /** Cabecera de marca: de verde medio a verde profundo, en diagonal. */
    val hero = Brush.linearGradient(
        colors = listOf(Palette.brand500, Palette.brand700),
    )

    /**
     * Degradado de la cabecera del login.
     *
     * Va de un verde medio a uno profundo, con la diagonal marcada. Se calcula
     * sobre la escala de marca y no sobre un verde suelto, para que el degradado
     * y el logo sean el mismo color.
     */
    val brand = Brush.linearGradient(
        colors = listOf(Palette.brand400, Palette.brand600, Palette.brand800),
    )

    /** Cabecera más suave, para pantallas de detalle. */
    val heroSoft = Brush.linearGradient(
        colors = listOf(Palette.brand400, Palette.brand600),
    )

    /** Velo sutil sobre el fondo, para que la pantalla no sea un plano vacío. */
    val surfaceVeil = Brush.verticalGradient(
        colors = listOf(Palette.brand50, Palette.slate50),
    )

    /** Barra de progreso o acento. */
    val accent = Brush.horizontalGradient(
        colors = listOf(Palette.brand400, Palette.brand600),
    )
}

/**
 * Tokens de opacidad para capas.
 *
 * En vez de escribir `Color.White.copy(alpha = 0.14f)` por las pantallas (que es
 * como se acaba con cinco valores distintos de "blanco un poco transparente"),
 * se nombran aquí.
 */
object AxzyAlpha {
    /** Texto o icono sobre fondo de marca, atenuado. */
    const val onBrandMuted = 0.78f

    /** Separadores finos dentro de una cabecera de color. */
    const val onBrandDivider = 0.22f

    /** Relleno de una superficie translúcida sobre marca. */
    const val onBrandSurface = 0.16f

    /** Borde suave. */
    const val hairline = 0.6f
}
