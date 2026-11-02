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

/**
 * Sub-tipo de una categoría.
 *
 * Ojo con los dos nombres: `value` es el que se pinta —"Basura Acumulada"— y `name`
 * es el código interno con el que se capturó —"BASURA"—. Enseñar `name` deja al
 * guardia leyendo identificadores.
 */
data class IncidentType(
    val id: String,
    val categoryId: String,
    val name: String,
    val value: String,
)
