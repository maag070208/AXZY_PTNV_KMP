package com.axzydev.checkapp.platform.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Vista de escaneo de códigos QR con la cámara del dispositivo.
 *
 * Invoca [onQrCode] con el primer código legible de cada frame. La cámara emite el
 * mismo código en frames consecutivos, por lo que **el consumidor debe deduplicar**
 * (p. ej. desactivando la cámara al recibir el primer valor).
 *
 * Implementaciones: CameraX + ML Kit (Android) y AVFoundation (iOS).
 *
 * Pertenece a la capa `platform`; las páginas que la usan deben depender de
 * `:shared:platform`. No contiene lógica de negocio.
 */
@Composable
expect fun QrScannerView(
    onQrCode: (String) -> Unit,
    modifier: Modifier,
)
