package com.axzydev.checkapp.core.common.result

import kotlinx.serialization.json.JsonElement

/**
 * Resultado de una operación contra la API o la base local.
 *
 * Réplica del `TResult` de la app React Native, pero tipado y sin `any`:
 * - [Success]   → operación correcta con datos.
 * - [Failure]   → error de negocio o de red ([Failure.networkError] = true cuando
 *                 no hubo respuesta o el servidor devolvió 5xx; en ese caso la
 *                 capa offline debe continuar en SQLite).
 */
sealed interface ApiResult<out T> {
    data class Success<T>(
        val data: T,
        val messages: List<String> = emptyList(),
    ) : ApiResult<T>

    data class Failure(
        val messages: List<String>,
        val networkError: Boolean = false,
        /** Código HTTP cuando el servidor respondió (`null` si no hubo respuesta). */
        val statusCode: Int? = null,
        /**
         * Cuerpo de `data` del sobre de error, sin tipar.
         *
         * Hace falta porque algunos errores traen información accionable que no
         * encaja en el tipo de la operación: el `POST /sync` responde `400` con
         * `data.rejected[]` (registros que el servidor no pudo aplicar y el por
         * qué). Sin esto el cliente perdía el detalle y la cola offline quedaba
         * atascada con un mensaje genérico.
         */
        val data: JsonElement? = null,
    ) : ApiResult<Nothing> {
        val firstMessage: String get() = messages.firstOrNull() ?: "Error desconocido"
    }
}

val ApiResult<*>.isSuccess: Boolean
    get() = this is ApiResult.Success

fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success)?.data

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data), messages)
    is ApiResult.Failure -> this
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onFailure(action: (ApiResult.Failure) -> Unit): ApiResult<T> {
    if (this is ApiResult.Failure) action(this)
    return this
}
