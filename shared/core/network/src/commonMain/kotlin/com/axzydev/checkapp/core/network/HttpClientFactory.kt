package com.axzydev.checkapp.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** Construye el `HttpClient` compartido con JSON, timeouts y headers base. */
fun createHttpClient(
    config: ApiConfig,
    tokenProvider: AuthTokenProvider,
    versionProvider: AppVersionProvider,
): HttpClient = HttpClient(createHttpEngine()) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
                encodeDefaults = true
            },
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = config.timeoutMillis
        connectTimeoutMillis = config.timeoutMillis
        socketTimeoutMillis = config.timeoutMillis
    }

    install(Logging) {
        level = LogLevel.INFO
        // Sin esto el plugin no imprime **nada** en Android: su logger por defecto
        // va por SLF4J, que en la app no tiene provider ("No SLF4J providers were
        // found") y se queda en no-op. El resultado era que un fallo de red se veía
        // como "Sin conexión" sin una sola línea en el logcat para diagnosticarlo.
        // `println` sí llega: en Android sale por System.out → logcat.
        logger = object : Logger {
            override fun log(message: String) {
                println("HTTP $message")
            }
        }
    }

    defaultRequest {
        contentType(ContentType.Application.Json)
        headers.append(HttpHeaders.Accept, "application/json")
        tokenProvider.token()?.let { headers.append(HttpHeaders.Authorization, "Bearer $it") }
        headers.append("X-App-Version", versionProvider.appVersion())
        if (versionProvider.bypassVersionCheck()) {
            headers.append("X-Bypass-Version-Check", "true")
        }
    }
}
