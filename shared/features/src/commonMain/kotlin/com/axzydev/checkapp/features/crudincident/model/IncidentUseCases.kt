package com.axzydev.checkapp.features.crudincident.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.incident.model.Incident
import com.axzydev.checkapp.entities.incident.data.IncidentRepository

class ListIncidentsUseCase(private val repository: IncidentRepository) {
    suspend operator fun invoke(): List<Incident> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> repository.localAll()
    }
}

class ResolveIncidentUseCase(private val repository: IncidentRepository) {
    suspend operator fun invoke(id: String): ApiResult<Incident> = repository.resolve(id)
}

class DeleteIncidentUseCase(private val repository: IncidentRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
