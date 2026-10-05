package com.axzydev.checkapp.design.qr

import qrcode.raw.ErrorCorrectionLevel
import qrcode.raw.QRCodeProcessor

/**
 * Matriz de módulos de un código QR (`true` = módulo oscuro).
 * Codificación con corrección de error media (suficiente para QR impresos).
 */
object QrMatrix {

    fun of(content: String): List<List<Boolean>> {
        if (content.isBlank()) return emptyList()
        val raw = runCatching {
            QRCodeProcessor(content, ErrorCorrectionLevel.MEDIUM).encode()
        }.getOrNull() ?: return emptyList()
        return raw.map { row -> row.map { square -> square.dark } }
    }
}
