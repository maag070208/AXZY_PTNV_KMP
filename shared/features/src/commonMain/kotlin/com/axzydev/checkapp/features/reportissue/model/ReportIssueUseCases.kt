package com.axzydev.checkapp.features.reportissue.model

import com.axzydev.checkapp.entities.incident.data.IncidentRepository
import com.axzydev.checkapp.entities.incident.model.Incident
import com.axzydev.checkapp.entities.incident.model.IncidentDraft
import com.axzydev.checkapp.entities.incidentcategory.data.IncidentCategoryRepository
import com.axzydev.checkapp.entities.incidentcategory.model.IncidentCategory
import com.axzydev.checkapp.entities.maintenance.data.MaintenanceRepository
import com.axzydev.checkapp.entities.maintenance.model.Maintenance
import com.axzydev.checkapp.entities.maintenance.model.MaintenanceDraft

/** Catálogo de categorías (INCIDENT/MAINTENANCE/DISCIPLINE). */
class ListIssueCategoriesUseCase(private val repository: IncidentCategoryRepository) {
    suspend operator fun invoke(type: String): List<IncidentCategory> = repository.byType(type)
}

/** Reporta una incidencia/falla local-first; `core:sync` la sube después. */
class ReportIncidentUseCase(private val repository: IncidentRepository) {
    suspend operator fun invoke(userId: String, draft: IncidentDraft): Incident = repository.report(userId, draft)
}

class ReportMaintenanceUseCase(private val repository: MaintenanceRepository) {
    suspend operator fun invoke(userId: String, draft: MaintenanceDraft): Maintenance = repository.report(userId, draft)
}
