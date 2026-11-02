package com.axzydev.checkapp.core.network

import com.axzydev.checkapp.core.common.result.ApiEnvelope
import com.axzydev.checkapp.core.common.result.ApiResult
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.serializer

@PublishedApi
internal fun HttpRequestBuilder.applyQuery(query: Map<String, Any?>) {
    query.forEach { (key, value) -> if (value != null) parameter(key, value) }
}

/**
 * Traduce un fallo de transporte a un mensaje que se pueda leer en pantalla.
 *
 * Ktor propaga la excepción de la plataforma tal cual, y en iOS su `message` es
 * el volcado entero de `NSURLErrorDomain` — quince líneas de diccionario nativo
 * con la URL dentro. Eso llegaba literalmente al formulario de login, debajo del
 * campo de contraseña. El detalle técnico se descarta a propósito: al usuario le
 * sirve saber *qué puede hacer*, y para diagnosticar están los logs del cliente.
 */
@PublishedApi
internal fun transportErrorMessage(t: Throwable): String = when (t) {
    is HttpRequestTimeoutException ->
        "El servidor tardó demasiado en responder. Intenta de nuevo."

    else ->
        "Sin conexión con el servidor. Revisa tu red e intenta de nuevo."
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
    @PublishedApi internal val json: Json = Json { ignoreUnknownKeys = true },
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
            return ApiResult.Failure(listOf(transportErrorMessage(t)), networkError = true)
        }

        val status = response.status.value

        if (status >= 500) {
            return ApiResult.Failure(
                listOf("Servidor no disponible ($status)"),
                networkError = true,
                statusCode = status,
            )
        }

        // El cuerpo se lee SIN tipar a propósito. En una respuesta de error, `data`
        // puede no encajar con el tipo de la operación (el `400` del push de sync
        // trae `rejected[]`); deserializarlo directamente a T provocaba perder los
        // mensajes reales del servidor y reportar "Respuesta inválida".
        val raw: JsonElement = try {
            response.body<JsonElement>()
        } catch (t: Throwable) {
            return ApiResult.Failure(listOf("Respuesta inválida del servidor"), statusCode = status)
        }

        val envelope: ApiEnvelope<JsonElement> = try {
            json.decodeFromJsonElement(ApiEnvelope.serializer(JsonElement.serializer()), raw)
        } catch (t: Throwable) {
            return ApiResult.Failure(listOf("Respuesta inválida del servidor"), statusCode = status)
        }

        if (!response.status.isSuccess() || !envelope.success) {
            return ApiResult.Failure(
                envelope.messages.ifEmpty { listOf("Error $status") },
                statusCode = status,
                data = envelope.data,
            )
        }

        // Éxito: recién aquí se convierte `data` al tipo esperado.
        val data: T = try {
            val element = envelope.data
            if (element == null || element is JsonNull) Unit as T
            else json.decodeFromJsonElement(serializer<T>(), element)
        } catch (t: Throwable) {
            return ApiResult.Failure(listOf("Respuesta inválida del servidor"), statusCode = status)
        }

        return ApiResult.Success(data, envelope.messages)
    }
}
