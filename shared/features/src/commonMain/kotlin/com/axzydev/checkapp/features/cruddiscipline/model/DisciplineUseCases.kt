package com.axzydev.checkapp.features.cruddiscipline.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.guarddiscipline.data.GuardDisciplineRepository
import com.axzydev.checkapp.entities.guarddiscipline.model.GuardDiscipline

class ListDisciplinesUseCase(private val repository: GuardDisciplineRepository) {
    suspend operator fun invoke(): List<GuardDiscipline> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> emptyList()
    }
}

class ResolveDisciplineUseCase(private val repository: GuardDisciplineRepository) {
    suspend operator fun invoke(id: String): ApiResult<GuardDiscipline> = repository.resolve(id)
}
