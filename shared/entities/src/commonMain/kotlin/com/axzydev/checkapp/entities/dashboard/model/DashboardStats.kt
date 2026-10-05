package com.axzydev.checkapp.entities.dashboard.model

/**
 * Cifras del inicio.
 *
 * Salen de `GET /home/stats`. El backend devuelve además la lista completa de
 * rondas activas; aquí sólo se conserva el conteo porque el inicio pinta un
 * número, y traerse la lista entera para contarla sería trabajo desperdiciado.
 */
data class DashboardStats(
    val activeRounds: Int = 0,
    val pendingIncidents: Int = 0,
    val pendingMaintenance: Int = 0,
) {
    companion object {
        /** Estado inicial: todo a cero, antes de la primera carga. */
        val Empty = DashboardStats()
    }
}
