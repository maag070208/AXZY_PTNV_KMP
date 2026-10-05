package com.axzydev.checkapp.platform.share

import androidx.compose.runtime.Composable

/** Envía contenido al menú de compartir del sistema (hoja nativa). */
interface Sharer {
    fun shareText(text: String, subject: String? = null)
}

@Composable
expect fun rememberSharer(): Sharer
