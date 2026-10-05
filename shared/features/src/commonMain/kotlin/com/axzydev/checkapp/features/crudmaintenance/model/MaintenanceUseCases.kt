package com.axzydev.checkapp.features.crudmaintenance.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.maintenance.data.MaintenanceRepository
import com.axzydev.checkapp.entities.maintenance.model.Maintenance

class ListMaintenancesUseCase(private val repository: MaintenanceRepository) {
    suspend operator fun invoke(): List<Maintenance> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> repository.localAll()
    }
}

class ResolveMaintenanceUseCase(private val repository: MaintenanceRepository) {
    suspend operator fun invoke(id: String): ApiResult<Maintenance> = repository.resolve(id)
}

class DeleteMaintenanceUseCase(private val repository: MaintenanceRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
