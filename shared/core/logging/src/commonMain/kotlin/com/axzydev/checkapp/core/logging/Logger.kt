package com.axzydev.checkapp.core.logging

/**
 * Superficie pública del slice `core:logging`.
 */
interface Logger {
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warn(tag: String, message: String, error: Throwable? = null)
    fun error(tag: String, message: String, error: Throwable? = null)
}

/**
 * Logger por defecto multiplataforma. En fases posteriores se reemplaza por
 * implementaciones de plataforma (Logcat / os_log) sin tocar los llamadores.
 */
object DefaultLogger : Logger {
    override fun debug(tag: String, message: String) = println("[D][$tag] $message")
    override fun info(tag: String, message: String) = println("[I][$tag] $message")
    override fun warn(tag: String, message: String, error: Throwable?) =
        println("[W][$tag] $message${error?.let { " :: ${it.message}" } ?: ""}")

    override fun error(tag: String, message: String, error: Throwable?) =
        println("[E][$tag] $message${error?.let { " :: ${it.message}" } ?: ""}")
}
