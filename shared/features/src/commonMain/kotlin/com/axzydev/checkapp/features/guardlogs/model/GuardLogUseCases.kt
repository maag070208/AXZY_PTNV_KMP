package com.axzydev.checkapp.features.guardlogs.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.guardlog.data.GuardLogRepository
import com.axzydev.checkapp.entities.guardlog.model.GuardLog

class ListGuardLogsUseCase(private val repository: GuardLogRepository) {
    suspend operator fun invoke(page: Int = 1, limit: Int = 15): List<GuardLog> =
        when (val result = repository.datatable(page, limit)) {
            is ApiResult.Success -> result.data
            is ApiResult.Failure -> emptyList()
        }
}
