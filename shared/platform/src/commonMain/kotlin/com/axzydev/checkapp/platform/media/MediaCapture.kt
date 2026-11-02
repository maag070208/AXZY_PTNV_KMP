package com.axzydev.checkapp.platform.media

import androidx.compose.runtime.Composable

/** Resultado de una captura de evidencia (foto o video). */
sealed interface CaptureResult {
    /** Ruta local del archivo recién capturado, lista para la cola de medios. */
    data class Captured(val uri: String, val isVideo: Boolean) : CaptureResult
    data object Cancelled : CaptureResult
    data class Failed(val message: String) : CaptureResult
}

/**
 * Lanzador de captura de evidencias con la cámara nativa.
 *
 * Devuelve la ruta local del archivo; `core:sync` se encarga de subirlo a
 * `/uploads` y reemplazar la `file://` por la URL pública (Anexo B).
 * Implementado con el contrato de captura del sistema en Android e
 * `UIImagePickerController` en iOS.
 */
interface MediaCaptureLauncher {
    val supportsVideo: Boolean
    fun capturePhoto(onResult: (CaptureResult) -> Unit)
    fun captureVideo(onResult: (CaptureResult) -> Unit)
}

/** Recuerda un [MediaCaptureLauncher] ligado al ciclo de vida de la composición. */
@Composable
expect fun rememberMediaCapture(): MediaCaptureLauncher

/**
 * Dibuja la cámara **de la app** mientras hay una foto pendiente.
 *
 * Capturar con el contrato del sistema abre la aplicación de cámara del teléfono, con
 * su zoom, su volteo de cámara y sus ajustes: demasiado para lo único que se hace
 * aquí, que es encuadrar un punto y disparar. Esto pinta sólo el encuadre y el
 * disparador.
 *
 * Se coloca **una vez**, en la pantalla que captura, encima del contenido. Con la
 * cámara del sistema esto no hacía falta porque era otra actividad la que aparecía.
 */
@Composable
expect fun MediaCaptureHost(launcher: MediaCaptureLauncher)
