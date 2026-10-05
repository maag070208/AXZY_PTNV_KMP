package com.axzydev.checkapp.entities.guarddiscipline.model

/** Incidencia disciplinaria de un guardia. */
data class GuardDiscipline(
    val id: String,
    val guardName: String?,
    val categoryName: String?,
    val typeName: String?,
    val description: String?,
    val status: String,
    val createdAt: Long,
)
