package com.axzydev.checkapp.platform.location

import androidx.compose.runtime.Composable

/** Posición del aparato, con la precisión que reporta el sistema. */
data class Coordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float? = null,
)

/**
 * Posición del aparato.
 *
 * Devuelve `null` en vez de lanzar cuando no se puede: sin permiso, sin GPS activo o
 * sin última posición conocida. Un punto de control se puede marcar igual —la
 * evidencia y las tareas no dependen de la ubicación— así que la falta de posición es
 * un dato más, no un error que deba romper el flujo del guardia.
 */
interface LocationProvider {
    suspend fun current(): Coordinates?
}

/** Recuerda un [LocationProvider] ligado al ciclo de vida de la composición. */
@Composable
expect fun rememberLocationProvider(): LocationProvider
