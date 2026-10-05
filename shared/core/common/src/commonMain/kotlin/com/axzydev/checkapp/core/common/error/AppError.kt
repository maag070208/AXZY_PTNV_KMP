package com.axzydev.checkapp.core.common.error

/**
 * Error de aplicación. Equivalente al `AppError` del backend/RN:
 * mensaje legible + código HTTP opcional.
 */
class AppError(
    override val message: String,
    val code: Int? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

fun Throwable.toAppError(defaultMessage: String = "Error inesperado"): AppError =
    this as? AppError ?: AppError(message ?: defaultMessage, cause = this)
