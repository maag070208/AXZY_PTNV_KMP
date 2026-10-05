package com.axzydev.checkapp.features.guardlist.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.guard.data.GuardRepository
import com.axzydev.checkapp.entities.guard.model.Guard

class ListGuardsUseCase(private val repository: GuardRepository) {
    suspend operator fun invoke(): List<Guard> = repository.guards()
}

class GetGuardUseCase(private val repository: GuardRepository) {
    suspend operator fun invoke(id: String): Guard? = repository.findById(id)
}

class DeleteGuardUseCase(private val repository: GuardRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
