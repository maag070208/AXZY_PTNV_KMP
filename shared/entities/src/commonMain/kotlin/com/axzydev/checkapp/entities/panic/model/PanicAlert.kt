package com.axzydev.checkapp.entities.panic.model

/** Datos de una alerta de pánico. El guardia se toma del token (backend). */
data class PanicAlertDraft(
    val source: String = "in_app",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Double? = null,
    val message: String? = null,
)

/** Resultado de disparar el pánico. */
sealed interface PanicResult {
    /** Enviada al servidor (y difundida por FCM/Ably). */
    data object Sent : PanicResult

    /** Sin conexión: quedó en la cola local para reenviar al reconectar. */
    data object Queued : PanicResult

    /** Error de negocio (no se pudo entregar ni encolar). */
    data class Failed(val message: String) : PanicResult
}
