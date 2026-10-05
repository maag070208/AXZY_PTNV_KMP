package com.axzydev.checkapp.features.crudrecurring.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.recurringroute.data.RecurringRouteRepository
import com.axzydev.checkapp.entities.recurringroute.model.RecurringRoute
import com.axzydev.checkapp.entities.recurringroute.model.RecurringRouteDraft

/** Lista rutas recurrentes: remoto si hay red; si falla, la copia local del sync. */
class ListRecurringUseCase(private val repository: RecurringRouteRepository) {
    suspend operator fun invoke(): List<RecurringRoute> = when (val result = repository.remoteAll()) {
        is ApiResult.Success -> result.data
        is ApiResult.Failure -> repository.activeRoutes(null)
    }
}

/** Detalle completo (puntos, tareas y guardias) para edición. */
class GetRecurringUseCase(private val repository: RecurringRouteRepository) {
    suspend operator fun invoke(id: String): ApiResult<RecurringRoute> = repository.remoteById(id)
}

class CreateRecurringUseCase(private val repository: RecurringRouteRepository) {
    suspend operator fun invoke(draft: RecurringRouteDraft): ApiResult<RecurringRoute> = repository.create(draft)
}

class UpdateRecurringUseCase(private val repository: RecurringRouteRepository) {
    suspend operator fun invoke(id: String, draft: RecurringRouteDraft): ApiResult<RecurringRoute> =
        repository.update(id, draft)
}

class DeleteRecurringUseCase(private val repository: RecurringRouteRepository) {
    suspend operator fun invoke(id: String): ApiResult<Unit> = repository.delete(id)
}
