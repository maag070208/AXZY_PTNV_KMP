package com.axzydev.checkapp.features.qr.model

/**
 * Payload de los QR de punto de control.
 *
 * Formato `{"name":"<nombre>","id":"<uuid>"}`, idéntico al de los QR impresos por la
 * app React Native; [com.axzydev.checkapp.features.scanqr.model.MatchLocationUseCase]
 * lo resuelve contra el catálogo local.
 */
object QrPayload {

    fun forLocation(name: String, id: String): String =
        """{"name":"${escape(name)}","id":"${escape(id)}"}"""

    private fun escape(value: String): String = buildString {
        value.forEach { c ->
            when (c) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(c)
            }
        }
    }
}
