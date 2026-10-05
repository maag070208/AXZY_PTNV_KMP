package com.axzydev.checkapp.core.common.security

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Decodifica el payload (segunda sección) de un JWT sin validar la firma.
 * Devuelve `null` si el token no tiene la forma esperada.
 */
@OptIn(ExperimentalEncodingApi::class)
fun decodeJwtPayload(token: String): String? {
    val parts = token.split('.')
    if (parts.size < 2) return null
    val payload = parts[1]
    val padded = payload + "=".repeat((4 - payload.length % 4) % 4)
    return runCatching { Base64.UrlSafe.decode(padded).decodeToString() }.getOrNull()
}
