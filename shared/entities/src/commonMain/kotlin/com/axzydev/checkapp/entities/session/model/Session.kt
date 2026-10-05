package com.axzydev.checkapp.entities.session.model

import com.axzydev.checkapp.core.common.security.decodeJwtPayload
import kotlinx.serialization.json.Json

/** Sesión activa del usuario. */
data class Session(
    val token: String,
    val userId: String,
    val fullName: String,
    val username: String,
    val clientId: String?,
    val role: UserRole,
    /** Instante de expiración en segundos epoch (claim `exp`). */
    val expiresAtEpochSeconds: Long,
) {
    fun isExpired(nowEpochSeconds: Long): Boolean = expiresAtEpochSeconds <= nowEpochSeconds

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        /** Construye una sesión a partir del token JWT. `null` si el token es inválido o el rol es desconocido. */
        fun fromToken(token: String): Session? {
            val payload = decodeJwtPayload(token) ?: return null
            val decoded = runCatching { json.decodeFromString(AuthTokenPayload.serializer(), payload) }.getOrNull() ?: return null
            val role = UserRole.from(decoded.role) ?: return null
            val fullName = listOfNotNull(decoded.name, decoded.lastName)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { decoded.username }
            return Session(
                token = token,
                userId = decoded.id,
                fullName = fullName,
                username = decoded.username,
                clientId = decoded.clientId,
                role = role,
                expiresAtEpochSeconds = decoded.exp,
            )
        }
    }
}
