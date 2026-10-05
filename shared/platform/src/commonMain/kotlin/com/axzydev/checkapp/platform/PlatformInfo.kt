package com.axzydev.checkapp.platform

/**
 * Superficie pública del slice `platform`.
 *
 * Aquí viven las implementaciones `expect/actual` y las clases de plataforma
 * (cámara, QR, GPS, notificaciones, Ably, pánico, multimedia, archivos).
 * En la Fase 0 sólo se expone información de plataforma; el resto se añade
 * por fase sin tocar las capas superiores (dependen de interfaces de `core`).
 */
interface PlatformInfo {
    val name: String
}
