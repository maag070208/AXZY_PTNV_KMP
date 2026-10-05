package com.axzydev.checkapp.core.sync

import kotlinx.serialization.Serializable

/** Resultado de subida de un archivo de evidencia. */
@Serializable
data class UploadResult(
    val success: Boolean,
    val url: String? = null,
    val error: String? = null,
    val networkError: Boolean = false,
)

enum class MediaType { IMAGE, VIDEO }

/**
 * Cola de medios offline. Sube `file://` y reemplaza por la URL pública,
 * de forma reanudable y sin duplicados (Anexo B, paso 1 del push).
 */
interface MediaUploader {
    suspend fun upload(uri: String, type: MediaType, location: String, roundId: String?): UploadResult
}
