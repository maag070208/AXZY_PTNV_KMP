package com.axzydev.checkapp.entities.client.model

/** Organización donde se presta el servicio de seguridad. */
data class Client(
    val id: String,
    val name: String,
    val address: String?,
    val rfc: String?,
    val contactName: String?,
    val contactPhone: String?,
    val active: Boolean,
)

/** Datos para crear/editar un cliente. */
data class ClientDraft(
    val name: String,
    val address: String? = null,
    val rfc: String? = null,
    val contactName: String? = null,
    val contactPhone: String? = null,
    val appUsername: String? = null,
    val appPassword: String? = null,
)
