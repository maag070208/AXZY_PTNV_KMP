package com.axzydev.checkapp.entities.incidentcategory.data

import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.entities.incidentcategory.model.IncidentCategory
import com.axzydev.checkapp.entities.incidentcategory.model.IncidentType

interface IncidentCategoryRepository {
    suspend fun byType(type: String): List<IncidentCategory>
    suspend fun all(): List<IncidentCategory>

    /** Sub-tipos de una categoría. */
    suspend fun typesByCategory(categoryId: String): List<IncidentType>

    /**
     * Sub-tipos de todas las categorías de un dominio, agrupados por `categoryId`.
     *
     * Una consulta en lugar de una por categoría: el catálogo tiene cinco categorías
     * y veinte sub-tipos, y traerlos de uno en uno son seis viajes a disco para
     * pintar una sola pantalla.
     */
    suspend fun typesByCategoryType(type: String): Map<String, List<IncidentType>>
}

class DefaultIncidentCategoryRepository(private val database: AxzyCheckDatabase) : IncidentCategoryRepository {

    override suspend fun byType(type: String): List<IncidentCategory> =
        database.incidentCategoryQueries.selectCategoriesByType(type).executeAsList().map { it.toModel() }

    override suspend fun all(): List<IncidentCategory> =
        database.incidentCategoryQueries.selectAllCategories().executeAsList().map { it.toModel() }

    override suspend fun typesByCategory(categoryId: String): List<IncidentType> =
        database.incidentCategoryQueries.selectTypesByCategory(categoryId).executeAsList().map { it.toModel() }

    override suspend fun typesByCategoryType(type: String): Map<String, List<IncidentType>> =
        database.incidentCategoryQueries.selectTypesByCategoryType(type).executeAsList()
            .map { it.toModel() }
            .groupBy { it.categoryId }

    private fun com.axzydev.checkapp.core.database.Incident_categories.toModel() = IncidentCategory(
        id = id,
        name = name,
        value = value_,
        type = type,
        color = color,
        icon = icon,
    )

    private fun com.axzydev.checkapp.core.database.Incident_types.toModel() = IncidentType(
        id = id,
        categoryId = category_id,
        name = name,
        value = value_,
    )
}
