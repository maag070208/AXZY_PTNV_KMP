package com.axzydev.checkapp.core.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Superficie pública del slice `core:connectivity`.
 *
 * El motor de sincronización consulta [ConnectivityObserver.isConnected] y
 * escucha [ConnectivityObserver.status] para disparar el sync al reconectar.
 * La implementación real (ConnectivityManager / NWPathMonitor) vive en
 * `:shared:platform`.
 */
interface ConnectivityObserver {
    val status: Flow<Boolean>
    suspend fun isConnected(): Boolean
}

/** Implementación simple para pruebas y previews. */
class FixedConnectivity(connected: Boolean = true) : ConnectivityObserver {
    private val state = MutableStateFlow(connected)
    override val status: Flow<Boolean> = state
    override suspend fun isConnected(): Boolean = state.value
}
