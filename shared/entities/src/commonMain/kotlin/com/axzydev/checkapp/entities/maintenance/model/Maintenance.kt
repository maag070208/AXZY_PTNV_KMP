package com.axzydev.checkapp.entities.maintenance.model

data class Maintenance(
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

data class MaintenanceDraft(
    val title: String,
    val description: String? = null,
    val categoryId: String? = null,
    val typeId: String? = null,
    val clientId: String? = null,
    val media: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
)
