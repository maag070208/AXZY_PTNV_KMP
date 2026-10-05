package com.axzydev.checkapp.entities.location.model

/** Punto de control georreferenciado. */
data class Location(
    val id: String,
    val name: String,
    val clientId: String?,
    val zoneId: String?,
    val reference: String?,
    val active: Boolean,
)

/** Datos para crear una ubicación/punto de control. */
data class LocationDraft(
    val name: String,
    val clientId: String,
    val zoneId: String? = null,
    val reference: String? = null,
    val aisle: String? = null,
    val spot: String? = null,
    val number: String? = null,
)
