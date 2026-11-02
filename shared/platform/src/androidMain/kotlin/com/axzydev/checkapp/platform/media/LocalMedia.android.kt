package com.axzydev.checkapp.platform.media

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val MAX_EDGE = 1440

/** Gris neutro para el marcador; el color de la casa vive en `:shared:design`. */
private val FALLBACK = Color(0xFF1F2937)

@Composable
actual fun LocalImage(uri: String, modifier: Modifier) {
    val context = LocalContext.current

    // Fuera del hilo principal: una foto de 12 Mpx decodificada en el hilo de UI
    // congela la pantalla justo cuando el guardia acaba de disparar.
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) { decode(context, uri) }
    }

    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Marcador neutro: `:shared:platform` no depende del sistema de diseño,
            // así que aquí no hay iconos de la casa. El contenido de verdad del
            // carrusel lo pone la pantalla, que sí los tiene.
            Box(modifier = Modifier.fillMaxSize().background(FALLBACK))
        }
    }
}

@Composable
actual fun LocalVideoFrame(uri: String, modifier: Modifier) {
    val context = LocalContext.current

    val frame by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            val retriever = android.media.MediaMetadataRetriever()
            try {
                if (uri.startsWith("content://")) {
                    retriever.setDataSource(context, Uri.parse(uri))
                } else {
                    retriever.setDataSource(uri.removePrefix("file://"))
                }
                retriever.getFrameAtTime(0)
            } catch (error: Throwable) {
                null
            } finally {
                runCatching { retriever.release() }
            }
        }
    }

    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        val current = frame
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(modifier = Modifier.fillMaxSize().background(FALLBACK))
        }
    }
}

@Composable
actual fun rememberMediaOpener(): (String) -> Unit {
    val context = LocalContext.current
    return { uri ->
        runCatching {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(Uri.parse(uri), "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        }
    }
}

private fun decode(context: android.content.Context, uri: String): Bitmap? {
    // Con `content://` el archivo no tiene ruta: se abre el stream. Los dos caminos
    // pasan por el mismo muestreo para no cargar 12 Mpx en memoria.
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    runCatching {
        if (uri.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
        } else {
            BitmapFactory.decodeFile(uri.removePrefix("file://"), bounds)
        }
    }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight)
    }

    return runCatching {
        if (uri.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        } else {
            BitmapFactory.decodeFile(uri.removePrefix("file://"), options)
        }
    }.getOrNull()
}

private fun sampleSize(width: Int, height: Int): Int {
    var sample = 1
    while (width / sample > MAX_EDGE || height / sample > MAX_EDGE) sample *= 2
    return sample
}
