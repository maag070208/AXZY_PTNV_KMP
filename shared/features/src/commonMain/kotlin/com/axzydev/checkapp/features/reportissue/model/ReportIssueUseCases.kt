package com.axzydev.checkapp.features.reportissue.model

import com.axzydev.checkapp.entities.incident.data.IncidentRepository
import com.axzydev.checkapp.entities.incident.model.Incident
import com.axzydev.checkapp.entities.incident.model.IncidentDraft
import com.axzydev.checkapp.entities.incidentcategory.data.IncidentCategoryRepository
import com.axzydev.checkapp.entities.maintenance.data.MaintenanceRepository
import com.axzydev.checkapp.entities.maintenance.model.Maintenance
import com.axzydev.checkapp.entities.maintenance.model.MaintenanceDraft

/**
 * Categoría con sus sub-tipos, listos para pintar.
 *
 * El sub-tipo se queda con el **nombre legible** (`value` en el catálogo): el código
 * interno —"FUGA_AGUA"— no se enseña nunca. La categoría arrastra su color y su
 * icono, que también son catálogo.
 */
data class IssueCategory(
    val id: String,
    val name: String,
    val color: String?,
    val icon: String?,
    val types: List<IssueType>,
)

data class IssueType(val id: String, val name: String)

/**
 * Catálogo de un dominio (INCIDENT/MAINTENANCE/DISCIPLINE), con los sub-tipos ya
 * agrupados bajo su categoría.
 *
 * Devuelve las dos cosas juntas porque la pantalla necesita las dos a la vez: pedir
 * las categorías en una llamada y los tipos en otra obliga a mantener dos estados
 * de carga sincronizados, y basta con que uno falle para pintar una pantalla a
 * medias.
 */
class ListIssueCategoriesUseCase(private val repository: IncidentCategoryRepository) {
    suspend operator fun invoke(type: String): List<IssueCategory> {
        val typesByCategory = repository.typesByCategoryType(type)

        return repository.byType(type).map { category ->
            IssueCategory(
                id = category.id,
                name = category.name,
                color = category.color,
                icon = category.icon,
                types = typesByCategory[category.id].orEmpty().map { IssueType(it.id, it.value) },
            )
        }
    }
}

/** Reporta una incidencia/falla local-first; `core:sync` la sube después. */
class ReportIncidentUseCase(private val repository: IncidentRepository) {
    suspend operator fun invoke(userId: String, draft: IncidentDraft): Incident = repository.report(userId, draft)
}

class ReportMaintenanceUseCase(private val repository: MaintenanceRepository) {
    suspend operator fun invoke(userId: String, draft: MaintenanceDraft): Maintenance = repository.report(userId, draft)
}
