package com.axzydev.checkapp.features.crudzone.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.zone.data.ZoneRepository
import com.axzydev.checkapp.entities.zone.model.Zone
import com.axzydev.checkapp.entities.zone.model.ZoneDraft

class ListZonesUseCase(private val repository: ZoneRepository) {
    suspend operator fun invoke(clientId: String): List<Zone> {
        return when (val result = repository.remoteAll()) {
            is ApiResult.Success -> result.data.filter { it.clientId == clientId }
            is ApiResult.Failure -> repository.localByClient(clientId)
        }
    }
}

class CreateZoneUseCase(private val repository: ZoneRepository) {
    suspend operator fun invoke(draft: ZoneDraft): ApiResult<Zone> = repository.create(draft)
}

class UpdateZoneUseCase(private val repository: ZoneRepository) {
    suspend operator fun invoke(id: String, name: String): ApiResult<Zone> = repository.update(id, name)
}

class DeleteZoneUseCase(private val repository: ZoneRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
