package com.axzydev.checkapp.entities.guardlog.model

/** Registro de prenómina: entrada/salida de un guardia. */
data class GuardLog(
    val id: String,
    val guardName: String,
    val username: String,
    val loginAt: Long,
    val logoutAt: Long?,
) {
    val durationMinutes: Long?
        get() = logoutAt?.let { (it - loginAt) / 60_000 }
}
