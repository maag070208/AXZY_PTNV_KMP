package com.axzydev.checkapp.entities.incident.model

data class Incident(
    val id: String,
    val title: String,
    val guardName: String?,
    val categoryName: String?,
    val typeName: String?,
    val description: String?,
    val status: String,
    val createdAt: Long,
    val mediaCount: Int,
)

data class IncidentDraft(
    val title: String,
    val description: String? = null,
    val categoryId: String? = null,
    val typeId: String? = null,
    val clientId: String? = null,
    val media: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
