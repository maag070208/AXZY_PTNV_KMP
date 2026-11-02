package com.axzydev.checkapp.platform.fs

import android.content.Context
import android.net.Uri
import com.axzydev.checkapp.core.sync.FileBytesReader
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Lee los bytes de una evidencia en Android.
 *
 * Entiende **las dos formas** de nombrar un archivo, y eso era un bug: el lector
 * anterior sólo abría rutas (`file://`), así que toda evidencia que llegara como
 * `content://` —los vídeos, que los graba la app de cámara del sistema, y cualquier
 * archivo elegido de la galería— fallaba al subir. Y como el sync aborta cuando una
 * subida falla, se llevaba por delante todo lo demás pendiente.
 */
class AndroidFileBytesReader(private val context: Context) : FileBytesReader {

    override suspend fun read(uri: String): ByteArray? = runCatching {
        if (uri.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(uri))?.use { it.readBytes() }
        } else {
            FileSystem.SYSTEM.read(uri.removePrefix("file://").toPath()) { readByteArray() }
        }
    }.getOrNull()
}
