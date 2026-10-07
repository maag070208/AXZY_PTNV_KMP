package com.axzydev.checkapp.core.common

import com.axzydev.checkapp.core.common.time.DEFAULT_TIMEZONE
import com.axzydev.checkapp.core.common.time.greetingAt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/**
 * El saludo se prueba con instantes fijos y una zona fija: el reloj del sistema
 * no se puede fijar desde un test, y el saludo es lo único que decide si la
 * pantalla dice "Buenos días" a las nueve de la noche.
 */
class GreetingTest {

    private val tijuana = TimeZone.of(DEFAULT_TIMEZONE)

    @Test
    fun manana() {
        // 14:00 UTC = 06:00 en Tijuana (UTC-8 en enero).
        assertEquals("Buenos días", greetingAt(utc("2026-01-15T14:00:00Z"), tijuana))
    }

    @Test
    fun tarde() {
        // 22:00 UTC = 14:00.
        assertEquals("Buenas tardes", greetingAt(utc("2026-01-15T22:00:00Z"), tijuana))
    }

    @Test
    fun noche() {
        // 05:00 UTC del día siguiente = 21:00.
        assertEquals("Buenas noches", greetingAt(utc("2026-01-16T05:00:00Z"), tijuana))
    }

    @Test
    fun medianocheSigueSiendoNoche() {
        // 08:00 UTC = 00:00: "Buenas noches" y no "Buenos días".
        assertEquals("Buenas noches", greetingAt(utc("2026-01-15T08:00:00Z"), tijuana))
    }

    private fun utc(iso: String): Long = Instant.parse(iso).toEpochMilliseconds()
}
