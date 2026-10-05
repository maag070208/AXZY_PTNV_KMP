package com.axzydev.checkapp.features.dashboard.model

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.entities.dashboard.data.DashboardRepository
import com.axzydev.checkapp.entities.dashboard.model.DashboardStats

/**
 * Cifras del inicio.
 *
 * No hay respaldo local: si falla, el inicio se queda con los ceros anteriores en
 * lugar de mostrar datos viejos que podrían estar equivocados. Un contador de
 * "alertas pendientes" desactualizado es peor que no tenerlo.
 */
class GetDashboardStatsUseCase(private val repository: DashboardRepository) {
    suspend operator fun invoke(): ApiResult<DashboardStats> = repository.stats()
}
