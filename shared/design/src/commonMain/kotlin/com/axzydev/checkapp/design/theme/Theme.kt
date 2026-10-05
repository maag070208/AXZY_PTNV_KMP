package com.axzydev.checkapp.design.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Esquema de color de CheckApp. **Sólo claro por ahora** (decisión del usuario);
 * los tokens están centralizados para que añadir oscuro después no obligue a
 * tocar las pantallas.
 */
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
 * Se añaden tipografía y formas al `MaterialTheme` para que los componentes de
 * Material3 que no hemos reemplazado (TextField, Card…) salgan con la escala
 * correcta en vez de con los valores por defecto.
 */
@Composable
fun AxzyTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTone provides Tone.Brand) {
        MaterialTheme(
            colorScheme = AxzyLightColorScheme,
            typography = AxzyTypography,
            shapes = AxzyShapes,
            content = content,
        )
    }
}
