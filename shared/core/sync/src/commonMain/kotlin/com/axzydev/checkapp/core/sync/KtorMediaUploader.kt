package com.axzydev.checkapp.core.sync

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

/**
 * Subida de evidencias a `POST /uploads` (multipart) con reintentos.
 * Reemplaza el archivo local por la URL pública, de forma reanudable.
 */
class KtorMediaUploader(
    private val client: HttpClient,
    private val baseUrl: String,
    private val fileReader: FileBytesReader,
) : MediaUploader {

    override suspend fun upload(
        uri: String,
        type: MediaType,
        location: String,
        roundId: String?,
    ): UploadResult {
        val bytes = fileReader.read(uri)
            ?: return UploadResult(success = false, error = "No se pudo leer el archivo local")

        var attempt = 0
        val maxAttempts = 3
        while (attempt < maxAttempts) {
            attempt++
            val result = runCatching { performUpload(bytes, uri, type, location, roundId) }
                .getOrElse { UploadResult(false, error = it.message ?: "Error de red", networkError = true) }
            if (result.success || !result.networkError) return result
        }
        return UploadResult(false, error = "No se pudo subir la evidencia tras $maxAttempts intentos", networkError = true)
    }

    private suspend fun performUpload(
        bytes: ByteArray,
        uri: String,
        type: MediaType,
        location: String,
        roundId: String?,
    ): UploadResult {
        val response = client.post("$baseUrl/uploads") {
            setBody(
                MultiPartFormDataContent(
                    formData {
                        append("location", location)
                        if (roundId != null) append("roundId", roundId)
                        append(
                            "file",
                            bytes,
                            Headers.build {
                                append(HttpHeaders.ContentType, mimeTypeOf(uri, type))
                                append(HttpHeaders.ContentDisposition, "filename=\"${fileNameOf(uri)}\"")
                            },
                        )
                    },
                ),
            )
        }

        if (response.status.value >= 500) {
            return UploadResult(false, error = "Servidor no disponible (${response.status.value})", networkError = true)
        }

        val dto = response.body<UploadResponseDto>()
        val url = dto.url ?: dto.data?.url
        return if (dto.success && url != null) {
            UploadResult(success = true, url = url)
        } else {
            UploadResult(false, error = "Respuesta inválida del servidor")
        }
    }

    private fun fileNameOf(uri: String): String =
        uri.substringAfterLast('/').ifBlank { if (typeIsVideo(uri)) "video.mp4" else "image.jpg" }

    private fun typeIsVideo(uri: String): Boolean = uri.lowercase().let {
        it.endsWith(".mp4") || it.endsWith(".mov") || it.endsWith(".3gp")
    }

    private fun mimeTypeOf(uri: String, type: MediaType): String {
        val lower = uri.lowercase()
        return when {
            lower.endsWith(".mov") -> "video/quicktime"
            lower.endsWith(".3gp") -> "video/3gpp"
            lower.endsWith(".png") -> "image/png"
            type == MediaType.VIDEO -> "video/mp4"
            else -> "image/jpeg"
        }
    }
}
