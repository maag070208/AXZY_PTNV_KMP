package com.axzydev.checkapp.core.network

import com.axzydev.checkapp.core.common.result.ApiEnvelope
import com.axzydev.checkapp.core.common.result.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

@PublishedApi
internal fun HttpRequestBuilder.applyQuery(query: Map<String, Any?>) {
    query.forEach { (key, value) -> if (value != null) parameter(key, value) }
}

/**
 * Cliente HTTP tipado que siempre devuelve [ApiResult].
 *
 * Réplica del wrapper `get/post/put/patch/remove` de RN, incluyendo la marca
 * `networkError` (sin respuesta o 5xx) que habilita el fallback offline.
 * La URL se compone explícitamente con [baseUrl] (p. ej. `.../api/v1` + `/users/login`).
 */
class ApiClient(
    @PublishedApi internal val client: HttpClient,
    @PublishedApi internal val baseUrl: String,
) {

    suspend inline fun <reified T> get(
        path: String,
        query: Map<String, Any?> = emptyMap(),
    ): ApiResult<T> = request { client.get(baseUrl + path) { applyQuery(query) } }

    suspend inline fun <reified T> post(
        path: String,
        body: Any? = null,
    ): ApiResult<T> = request { client.post(baseUrl + path) { if (body != null) setBody(body) } }

    suspend inline fun <reified T> put(
        path: String,
        body: Any? = null,
    ): ApiResult<T> = request { client.put(baseUrl + path) { if (body != null) setBody(body) } }

    suspend inline fun <reified T> patch(
        path: String,
        body: Any? = null,
    ): ApiResult<T> = request { client.patch(baseUrl + path) { if (body != null) setBody(body) } }

    suspend inline fun <reified T> delete(
        path: String,
    ): ApiResult<T> = request { client.delete(baseUrl + path) }

    @PublishedApi
    internal suspend inline fun <reified T> request(block: () -> HttpResponse): ApiResult<T> {
        val response = try {
            block()
        } catch (t: Throwable) {
            return ApiResult.Failure(listOf(t.message ?: "Error de conexión"), networkError = true)
        }

        if (response.status.value >= 500) {
            return ApiResult.Failure(
                listOf("Servidor no disponible (${response.status.value})"),
                networkError = true,
            )
        }

        val envelope = try {
            response.body<ApiEnvelope<T>>()
        } catch (t: Throwable) {
            return ApiResult.Failure(listOf(t.message ?: "Respuesta inválida"))
        }

        return if (response.status.isSuccess() && envelope.success) {
            @Suppress("UNCHECKED_CAST")
            ApiResult.Success(envelope.data ?: (Unit as T), envelope.messages)
        } else {
            ApiResult.Failure(envelope.messages.ifEmpty { listOf("Error ${response.status.value}") })
        }
    }
}
