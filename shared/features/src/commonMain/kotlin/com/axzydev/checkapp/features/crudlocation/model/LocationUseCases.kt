package com.axzydev.checkapp.features.crudlocation.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.location.data.LocationRepository
import com.axzydev.checkapp.entities.location.model.Location
import com.axzydev.checkapp.entities.location.model.LocationDraft

class ListLocationsUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(): List<Location> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> repository.activeLocations()
    }
}

class CreateLocationUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(draft: LocationDraft): ApiResult<Location> = repository.create(draft)
}

class UpdateLocationUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(id: String, draft: LocationDraft): ApiResult<Location> = repository.update(id, draft)
}

class DeleteLocationUseCase(private val repository: LocationRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
