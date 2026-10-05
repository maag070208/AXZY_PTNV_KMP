package com.axzydev.checkapp.entities.incidentcategory.model

/** Categoría unificada; `type` distingue el dominio. */
data class IncidentCategory(
    val id: String,
    val name: String,
    val value: String,
    val type: String,
    val color: String?,
    val icon: String?,
) {
    companion object {
        const val TYPE_INCIDENT = "INCIDENT"
        const val TYPE_MAINTENANCE = "MAINTENANCE"
        const val TYPE_DISCIPLINE = "DISCIPLINE"
    }
}
