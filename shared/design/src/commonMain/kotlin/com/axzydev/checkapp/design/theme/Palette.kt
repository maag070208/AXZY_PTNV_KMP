package com.axzydev.checkapp.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta de CheckApp.
 *
 * El color de marca es `#12A36F`, el mismo del logo y de la WEB: antes había
 * tres verdes distintos repartidos entre `theme.ts` de la app React Native
 * (`#46A545`), el tema KMP (`#065911`) y el logo.
 *
 * Las escalas están **ancladas**: `brand500` es exactamente el color de marca y
 * el resto se genera mezclando hacia blanco (50-400) o hacia negro (600-900),
 * así que la identidad no se distorsiona al aclarar u oscurecer.
 *
 * Regla: en pantallas se usan los **alias semánticos** de `AxzyColors`, no los
 * valores de escala directamente. Igual que en la WEB, un color crudo en una
 * pantalla es imposible de revisar cuando cambie el tema.
 */
internal object Palette {
    // ── Marca ──
    val brand50 = Color(0xFFECF8F3)
    val brand100 = Color(0xFFD4EEE5)
    val brand200 = Color(0xFFA5DCC8)
    val brand300 = Color(0xFF71C8A9)
    val brand400 = Color(0xFF3DB489)
    val brand500 = Color(0xFF12A36F)
    val brand600 = Color(0xFF0F895D)
    val brand700 = Color(0xFF0C6C49)
    val brand800 = Color(0xFF094E35)
    val brand900 = Color(0xFF053121)

    // ── Neutros (slate, alineados con la WEB) ──
    val slate50 = Color(0xFFF8FAFC)
    val slate100 = Color(0xFFF1F5F9)
    val slate200 = Color(0xFFE2E8F0)
    val slate300 = Color(0xFFCBD5E1)
    val slate400 = Color(0xFF94A3B8)
    val slate500 = Color(0xFF64748B)
    val slate600 = Color(0xFF475569)
    val slate700 = Color(0xFF334155)
    val slate800 = Color(0xFF1E293B)
    val slate900 = Color(0xFF0F172A)
    val slate950 = Color(0xFF020617)

    // ── Semánticos ──
    val success50 = Color(0xFFECFDF5)
    val success200 = Color(0xFFA7F3D0)
    val success500 = Color(0xFF10B981)
    val success600 = Color(0xFF059669)
    val success700 = Color(0xFF047857)

    val danger50 = Color(0xFFFEF2F2)
    val danger200 = Color(0xFFFECDD3)
    val danger500 = Color(0xFFEF4444)
    val danger600 = Color(0xFFDC2626)
    val danger700 = Color(0xFFB91C1C)

    val warning50 = Color(0xFFFFFBEB)
    val warning200 = Color(0xFFFDE68A)
    val warning500 = Color(0xFFF59E0B)
    val warning600 = Color(0xFFD97706)
    val warning700 = Color(0xFFB45309)

    val info50 = Color(0xFFEFF6FF)
    val info200 = Color(0xFFBFDBFE)
    val info500 = Color(0xFF3B82F6)
    val info600 = Color(0xFF2563EB)
    val info700 = Color(0xFF1D4ED8)

    val purple50 = Color(0xFFF5F3FF)
    val purple200 = Color(0xFFDDD6FE)
    val purple500 = Color(0xFF8B5CF6)
    val purple700 = Color(0xFF6D28D9)

    // ── Base ──
    val white = Color(0xFFFFFFFF)
    val black = Color(0xFF000000)
    val transparent = Color(0x00000000)
}

/**
 * Tonos semánticos disponibles para badges, indicadores y estados.
 *
 * Es un `enum` y no una cadena para que el compilador impida inventarse un
 * tono que no existe — el fallo clásico de pintar un badge con un color que no
 * está en la paleta.
 */
enum class Tone {
    Brand,
    Success,
    Danger,
    Warning,
    Info,
    Accent,
    Neutral,
}

/**
 * Colores que resuelve cualquier estado visual: fondo suave, borde del tono,
 * texto sobre el fondo, relleno sólido y texto sobre el relleno.
 *
 * El `border` es lo que hace que una tarjeta teñida se vea "premium" (fondo +
 * borde del mismo tono) en vez de un bloque de color pegado al fondo.
 */
data class ToneColors(
    val soft: Color,
    val border: Color,
    val onSoft: Color,
    val solid: Color,
    val onSolid: Color,
)

val Tone.palette: ToneColors
    get() = when (this) {
        Tone.Brand -> ToneColors(Palette.brand50, Palette.brand200, Palette.brand700, Palette.brand600, Palette.white)
        Tone.Success -> ToneColors(Palette.success50, Palette.success200, Palette.success700, Palette.success600, Palette.white)
        Tone.Danger -> ToneColors(Palette.danger50, Palette.danger200, Palette.danger700, Palette.danger600, Palette.white)
        Tone.Warning -> ToneColors(Palette.warning50, Palette.warning200, Palette.warning700, Palette.warning600, Palette.white)
        Tone.Info -> ToneColors(Palette.info50, Palette.info200, Palette.info700, Palette.info600, Palette.white)
        Tone.Accent -> ToneColors(Palette.purple50, Palette.purple200, Palette.purple700, Palette.purple500, Palette.white)
        Tone.Neutral -> ToneColors(Palette.slate100, Palette.slate200, Palette.slate700, Palette.slate600, Palette.white)
    }

/**
 * Alias estables del tema. Se mantienen los nombres que ya usaba el código
 * (`primary`, `error`, `emerald`…) para no romper las pantallas existentes,
 * pero ahora apuntan a la paleta de marca.
 */
object AxzyColors {
    val primary = Palette.brand500
    val primaryDark = Palette.brand600
    val primaryContainer = Palette.brand50
    val onPrimaryContainer = Palette.brand900

    val secondary = Palette.slate600
    val background = Palette.slate50
    val surface = Palette.white
    val surfaceVariant = Palette.slate100
    val onSurface = Palette.slate900
    val onSurfaceVariant = Palette.slate500
    val outline = Palette.slate300
    val outlineVariant = Palette.slate200

    val error = Palette.danger600
    val errorContainer = Palette.danger50
    val success = Palette.success600
    val successContainer = Palette.success50
    val warning = Palette.warning600
    val warningContainer = Palette.warning50
    val info = Palette.info600
    val infoContainer = Palette.info50

    /** Se conservan por compatibilidad con pantallas ya escritas. */
    val emerald = Palette.brand500
    val red = Palette.danger500
    val amber = Palette.warning500

    /** Negro de los bloques destacados (escáner, marca). */
    val ink = Color(0xFF0B0B0C)
    val inkSoft = Color(0xFF1A1A1A)

    val slate100 = Palette.slate100
    val slate200 = Palette.slate200
    val slate400 = Palette.slate400
    val slate500 = Palette.slate500
    val slate700 = Palette.slate700
    val slate800 = Palette.slate800
}
