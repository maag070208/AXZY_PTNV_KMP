package com.axzydev.checkapp.platform.media

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.fillMaxSize

/**
 * Pendiente en iOS: leer el archivo con `UIImage` y convertirlo a `ImageBitmap`.
 * Hasta entonces se muestra el marcador, no un hueco roto.
 */
@Composable
actual fun LocalImage(uri: String, modifier: Modifier) {
    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1F2937)))
    }
}

@Composable
actual fun rememberMediaOpener(): (String) -> Unit = { _ -> }

/** Pendiente en iOS: `AVAssetImageGenerator`. */
@Composable
actual fun LocalVideoFrame(uri: String, modifier: Modifier) {
    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1F2937)))
    }
}
