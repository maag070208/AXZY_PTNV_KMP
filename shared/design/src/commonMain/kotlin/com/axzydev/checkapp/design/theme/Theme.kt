package com.axzydev.checkapp.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf

/** Esquema de color de CheckApp en **claro**. */
private val AxzyLightColorScheme = lightColorScheme(
    primary = AxzyColors.primary,
    onPrimary = Palette.white,
    primaryContainer = AxzyColors.primaryContainer,
    onPrimaryContainer = AxzyColors.onPrimaryContainer,

    secondary = AxzyColors.secondary,
    onSecondary = Palette.white,

    background = AxzyColors.background,
    onBackground = AxzyColors.onSurface,
    surface = AxzyColors.surface,
    onSurface = AxzyColors.onSurface,
    surfaceVariant = AxzyColors.surfaceVariant,
    onSurfaceVariant = AxzyColors.onSurfaceVariant,

    outline = AxzyColors.outline,
    outlineVariant = AxzyColors.outlineVariant,

    error = AxzyColors.error,
    onError = Palette.white,
    errorContainer = AxzyColors.errorContainer,
    onErrorContainer = Palette.danger700,
)

/** Esquema de color de CheckApp en **oscuro**. */
private val AxzyDarkColorScheme = darkColorScheme(
    primary = Palette.brand400,
    onPrimary = Palette.slate950,
    primaryContainer = Palette.brand900,
    onPrimaryContainer = Palette.brand100,

    secondary = Palette.slate400,
    onSecondary = Palette.slate950,

    background = Palette.slate950,
    onBackground = Palette.slate50,
    surface = Palette.slate900,
    onSurface = Palette.slate50,
    surfaceVariant = Palette.slate800,
    onSurfaceVariant = Palette.slate400,

    outline = Palette.slate600,
    outlineVariant = Palette.slate800,

    error = Palette.danger500,
    onError = Palette.slate950,
    errorContainer = Palette.danger900,
    onErrorContainer = Palette.danger300,
)

private val AxzyShapes = Shapes(
    extraSmall = AxzyShape.xs,
    small = AxzyShape.sm,
    medium = AxzyShape.md,
    large = AxzyShape.lg,
    extraLarge = AxzyShape.xl,
)

/**
 * Tono semántico activo.
 *
 * Permite que un componente sepa si está dentro de una zona "peligro" (por
 * ejemplo un diálogo de borrado) sin que haya que pasarle el color a mano a
 * cada hijo.
 */
val LocalTone = staticCompositionLocalOf { Tone.Brand }

/** Acceso cómodo al trío de colores de un tono desde cualquier composable. */
@Composable
fun toneColors(tone: Tone = LocalTone.current): ToneColors = tone.palette

/**
 * Tema raíz. Envuelve toda la app.
 *
 * `darkTheme` activa el modo oscuro; `AxzyColors` y `Tone.palette` son getters
 * que consultan ese estado, así que **todas las pantallas cambian solas** sin
 * tener que reescribirlas. Se añaden tipografía y formas al `MaterialTheme`
 * para que los componentes de Material3 que no hemos reemplazado salgan con la
 * escala correcta.
 */
@Composable
fun AxzyTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    SideEffect { applyDarkMode(darkTheme) }

    CompositionLocalProvider(LocalTone provides Tone.Brand) {
        MaterialTheme(
            colorScheme = if (darkTheme) AxzyDarkColorScheme else AxzyLightColorScheme,
            typography = AxzyTypography,
            shapes = AxzyShapes,
            content = content,
        )
    }
}
