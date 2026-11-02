package com.axzydev.checkapp.platform.media

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Pinta una imagen **local** (`file://` o `content://`).
 *
 * Todavía no hay cargador de imágenes en el proyecto —y para una evidencia recién
 * tomada no hace falta uno con caché ni red—, así que esto lee el archivo y lo
 * dibuja. Con una URL del servidor muestra el marcador: la evidencia ya está arriba,
 * y ampliarla ahí es otra conversación.
 */
@Composable
expect fun LocalImage(uri: String, modifier: Modifier)

/**
 * Pinta el **primer fotograma** de un vídeo local.
 *
 * Una tarjeta de vídeo sin imagen es una caja gris indistinguible de un archivo
 * perdido; con el fotograma se ve qué se grabó sin abrir nada.
 */
@Composable
expect fun LocalVideoFrame(uri: String, modifier: Modifier)

/** Abre un medio con el reproductor del sistema. */
@Composable
expect fun rememberMediaOpener(): (String) -> Unit
