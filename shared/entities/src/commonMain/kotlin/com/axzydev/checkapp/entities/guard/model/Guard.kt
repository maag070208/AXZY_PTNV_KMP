package com.axzydev.checkapp.entities.guard.model

/** Guardia (usuario con rol GUARD). */
data class Guard(
    val id: String,
    val name: String,
    val lastName: String?,
    val username: String,
    val active: Boolean,
    val clientId: String?,
    val scheduleId: String?,
) {
    val fullName: String
        get() = listOfNotNull(name, lastName).filter { it.isNotBlank() }.joinToString(" ")
}
