package com.axzydev.checkapp.core.common.result

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
