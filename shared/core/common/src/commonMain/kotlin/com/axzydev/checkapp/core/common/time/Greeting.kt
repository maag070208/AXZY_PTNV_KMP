package com.axzydev.checkapp.core.common.time

import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Saludo según la hora local.
 *
 * Está aquí y no en cada pantalla porque el inicio del guardia y el del
 * administrador tienen que saludar igual: el del guardia decía "Buenos días" a
 * las seis de la tarde mientras el del administrador ya lo calculaba bien.
 *
 * Se usa la zona del dispositivo y no [DEFAULT_TIMEZONE]: si el teléfono está en
 * otra zona, el saludo tiene que ser el suyo, no el de la operación.
 */
fun greetingAt(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String = when (Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).hour) {
    in 5..11 -> "Buenos días"
    in 12..18 -> "Buenas tardes"
    // Madrugada incluida: en una app de vigilancia, a las 3 de la mañana se
    // saluda con "buenas noches", no con "buenos días".
    else -> "Buenas noches"
}

/** El saludo de ahora mismo. */
fun currentGreeting(): String = greetingAt(Clock.System.now().toEpochMilliseconds())
