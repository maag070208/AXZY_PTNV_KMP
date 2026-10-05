package com.axzydev.checkapp.core.common.format

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * Formato de fechas de la app.
 *
 * Incidencias, mantenimiento, quejas, recorridos y notificaciones muestran una
 * marca de tiempo, y cada una la formateaba a su manera. Aquí se centraliza para
 * que "hace 5 min" signifique lo mismo en las cinco.
 */
object DateFormat {

    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    /**
     * Fecha relativa para lo reciente y absoluta para lo viejo.
     *
     * Un listado de incidencias se lee por urgencia, así que "hace 20 min" es más
     * útil que una fecha completa para lo del día. Pasada una semana la hora ya no
     * aporta y se muestra sólo la fecha.
     */
    fun relative(epochMillis: Long, now: Long = Clock.System.now().toEpochMilliseconds()): String {
        val diff = now - epochMillis
        return when {
            diff < 0 -> shortDate(epochMillis)
            diff < MINUTE -> "Ahora mismo"
            diff < HOUR -> "Hace ${diff / MINUTE} min"
            diff < DAY -> "Hace ${diff / HOUR} h"
            diff < 7 * DAY -> "Hace ${diff / DAY} d"
            else -> shortDate(epochMillis)
        }
    }

    /** "01/10/2026 08:14" */
    fun dateTime(epochMillis: Long): String {
        val dt = local(epochMillis)
        return "${two(dt.dayOfMonth)}/${two(dt.monthNumber)}/${dt.year} " +
            "${two(dt.hour)}:${two(dt.minute)}"
    }

    /** "01/10/2026" */
    fun shortDate(epochMillis: Long): String {
        val dt = local(epochMillis)
        return "${two(dt.dayOfMonth)}/${two(dt.monthNumber)}/${dt.year}"
    }

    private fun local(epochMillis: Long) =
        Instant.fromEpochMilliseconds(epochMillis)
            .toLocalDateTime(TimeZone.currentSystemDefault())

    private fun two(value: Int): String = if (value < 10) "0$value" else value.toString()
}
