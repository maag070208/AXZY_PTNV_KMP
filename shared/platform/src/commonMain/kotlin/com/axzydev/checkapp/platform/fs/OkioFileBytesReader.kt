package com.axzydev.checkapp.platform.fs

import com.axzydev.checkapp.core.sync.FileBytesReader
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Lee los bytes de un archivo local multiplataforma con Okio.
 * Acepta URIs `file://...` o rutas absolutas.
 */
class OkioFileBytesReader : FileBytesReader {
    override suspend fun read(uri: String): ByteArray? = runCatching {
        val path = uri.removePrefix("file://").toPath()
        FileSystem.SYSTEM.read(path) { readByteArray() }
    }.getOrNull()
}
