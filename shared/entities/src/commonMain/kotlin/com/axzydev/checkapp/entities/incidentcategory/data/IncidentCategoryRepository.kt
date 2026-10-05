package com.axzydev.checkapp.entities.incidentcategory.data

import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.entities.incidentcategory.model.IncidentCategory

interface IncidentCategoryRepository {
    suspend fun byType(type: String): List<IncidentCategory>
    suspend fun all(): List<IncidentCategory>
}

class DefaultIncidentCategoryRepository(private val database: AxzyCheckDatabase) : IncidentCategoryRepository {

    override suspend fun byType(type: String): List<IncidentCategory> =
        database.incidentCategoryQueries.selectCategoriesByType(type).executeAsList().map { it.toModel() }

    override suspend fun all(): List<IncidentCategory> =
        database.incidentCategoryQueries.selectAllCategories().executeAsList().map { it.toModel() }

    private fun com.axzydev.checkapp.core.database.Incident_categories.toModel() = IncidentCategory(
        id = id,
        name = name,
        value = value_,
        type = type,
        color = color,
        icon = icon,
    )
}
