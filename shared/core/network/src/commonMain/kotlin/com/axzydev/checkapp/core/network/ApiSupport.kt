package com.axzydev.checkapp.core.network

import com.axzydev.checkapp.core.common.time.TimeProvider
import io.ktor.client.engine.HttpClientEngine

/**
 * Superficie pública del slice `core:network`.
 *
 * Provee el cliente HTTP (Ktor) y un [ApiClient] que devuelve siempre
 * `ApiResult`, replicando el comportamiento del wrapper Axios de la app RN.
 */
interface AuthTokenProvider {
    fun token(): String?
}

interface AppVersionProvider {
    fun appVersion(): String
    fun bypassVersionCheck(): Boolean
}

/** Reloj compartido para health-checks/latencia si se requiere. */
class ServerConfig(
    val apiConfig: ApiConfig,
    val timeProvider: TimeProvider,
)

/** Configuración de red. */
data class ApiConfig(
    val baseUrl: String,
    val timeoutMillis: Long = 30_000,
    val defaultAppVersion: String = "1.0.0",
)

/** Motor HTTP por plataforma (OkHttp en Android, Darwin en iOS). */
expect fun createHttpEngine(): HttpClientEngine
