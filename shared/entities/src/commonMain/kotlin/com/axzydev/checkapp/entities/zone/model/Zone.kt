package com.axzydev.checkapp.entities.zone.model

/** Zona operacional de un cliente. */
data class Zone(
    val id: String,
    val clientId: String,
    val name: String,
    val active: Boolean,
)

data class ZoneDraft(
    val name: String,
    val clientId: String,
)
