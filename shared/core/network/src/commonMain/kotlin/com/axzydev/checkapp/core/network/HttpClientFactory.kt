package com.axzydev.checkapp.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
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
