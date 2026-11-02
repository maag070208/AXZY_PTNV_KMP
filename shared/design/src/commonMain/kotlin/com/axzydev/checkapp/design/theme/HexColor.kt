package com.axzydev.checkapp.design.theme

import androidx.compose.ui.graphics.Color

/**
 * Convierte el color de una categoría ("#EF4444") en `Color`.
 *
 * El §1 prohíbe el color crudo, y esto es la excepción que lo confirma: el color de
 * las categorías **es dato**, no decoración. Lo elige el administrador en el panel y
 * viaja con el catálogo —seguridad en rojo, servicios en azul—, así que la app no
 * puede decidirlo con un token. Es el mismo caso que un avatar con el color de su
 * área.
 *
 * Devuelve `null` si no es un hex de seis dígitos, y quien lo use decide el respaldo:
 * un color mal capturado no debe pintar un cuadrado transparente.
 */
fun parseHexColor(hex: String?): Color? {
    val clean = hex?.trim()?.removePrefix("#") ?: return null
    if (clean.length != 6) return null
    val rgb = clean.toLongOrNull(radix = 16) ?: return null
    return Color(0xFF000000L or rgb)
}

/**
 * El tono de la casa más parecido a un color de catálogo.
 *
 * Los tonos son la paleta con la que está construida la app —su forma de decir
 * "esto es peligro, esto es información"— y una categoría no debería inventarse un
 * sexto lenguaje de color. Se busca el más cercano en RGB en vez de mantener una
 * tabla a mano: así una categoría nueva creada desde el panel cae en el tono que le
 * toca sin que nadie actualice el código, y un color raro cae en el más parecido en
 * lugar de no pintarse.
 */
fun toneForColor(color: Color?): Tone {
    if (color == null) return Tone.Neutral

    return TONES.minByOrNull { tone -> squaredDistance(tone.palette.solid, color) } ?: Tone.Neutral
}

private fun squaredDistance(a: Color, b: Color): Float {
    val red = (a.red - b.red) * (a.red - b.red)
    val green = (a.green - b.green) * (a.green - b.green)
    val blue = (a.blue - b.blue) * (a.blue - b.blue)
    return red + green + blue
}

/** Los tonos que puede tomar una categoría. */
private val TONES = listOf(Tone.Brand, Tone.Info, Tone.Success, Tone.Warning, Tone.Danger, Tone.Neutral)
