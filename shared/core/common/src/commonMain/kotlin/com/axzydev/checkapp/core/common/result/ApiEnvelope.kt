package com.axzydev.checkapp.core.common.result

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Envoltura estándar AXZY que devuelve el backend (`TResult`):
 * `{ success, data, messages }`.
 *
 * Se deserializa con `data` genérico y luego se mapea a [ApiResult] en
 * `core:network`.
 */
@Serializable
data class ApiEnvelope<T>(
    val success: Boolean = false,
    val data: T? = null,
    val messages: List<String> = emptyList(),
    @SerialName("stack") val stack: String? = null,
)
