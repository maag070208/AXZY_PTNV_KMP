package com.axzydev.checkapp.core.common.uuid

import kotlin.random.Random

private const val HEX = "0123456789abcdef"

/**
 * Genera un UUID v4 puro en Kotlin (sin código de plataforma).
 *
 * Se usa para crear ids locales de registros offline antes de sincronizar
 * (mismo rol que `generateUUID()` en la app RN).
 */
fun randomUuid(random: Random = Random.Default): String {
    val bytes = ByteArray(16) { random.nextInt(0, 256).toByte() }
    // version 4
    bytes[6] = ((bytes[6].toInt() and 0x0F) or 0x40).toByte()
    // variant 10
    bytes[8] = ((bytes[8].toInt() and 0x3F) or 0x80).toByte()

    val sb = StringBuilder(36)
    for (i in bytes.indices) {
        if (i == 4 || i == 6 || i == 8 || i == 10) sb.append('-')
        val v = bytes[i].toInt() and 0xFF
        sb.append(HEX[v ushr 4]).append(HEX[v and 0x0F])
    }
    return sb.toString()
}
