package com.axzydev.checkapp.entities.session.model

import kotlinx.serialization.Serializable

/** Payload del JWT emitido por el backend (`IAuthToken` en RN). */
@Serializable
data class AuthTokenPayload(
    val id: String,
    val name: String = "",
    val lastName: String? = null,
    val username: String = "",
    val clientId: String? = null,
    val role: String = "",
    val active: Boolean = true,
    val iat: Long = 0,
    val exp: Long = 0,
)
