package com.axzydev.checkapp.core.common.time

import kotlin.time.Instant

/** Zona operativa del sistema (misma que la API). */
const val DEFAULT_TIMEZONE = "America/Tijuana"

/** Formato de fecha de negocio. */
const val DATE_FORMAT = "yyyy-MM-dd"

/** Formato de hora de negocio. */
const val TIME_FORMAT = "HH:mm"

/** Reloj inyectable para facilitar pruebas deterministas. */
interface TimeProvider {
    fun nowEpochMillis(): Long
}

object SystemTimeProvider : TimeProvider {
    override fun nowEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
}

/** Epoch millis → ISO 8601 (lo que espera la API en los push). */
fun Long.epochMillisToIso(): String = Instant.fromEpochMilliseconds(this).toString()

/** ISO 8601 → epoch millis (lo que guardamos localmente en SQLite). */
fun String.isoToEpochMillis(): Long = Instant.parse(this).toEpochMilliseconds()

/** Epoch millis → texto `yyyy-MM-dd HH:mm` (UTC) para mostrar en UI. */
fun Long.epochMillisToDateTimeText(): String =
    Instant.fromEpochMilliseconds(this).toString()
        .replace('T', ' ')
        .substringBefore('.')
        .take(16)

/** True si el string tiene forma de hora `HH:mm` o `HH:mm:ss` (no es fecha). */
private val HHMM_REGEX = Regex("^\\d{2}:\\d{2}(:\\d{2})?$")
fun String.looksLikeClockTime(): Boolean = HHMM_REGEX.matches(this)
