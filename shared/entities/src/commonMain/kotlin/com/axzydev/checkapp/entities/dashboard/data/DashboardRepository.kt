package com.axzydev.checkapp.entities.dashboard.data

import com.axzydev.checkapp.core.common.result.ApiResult
import com.axzydev.checkapp.core.common.result.map
import com.axzydev.checkapp.core.network.ApiClient
import com.axzydev.checkapp.entities.dashboard.model.DashboardStats
import kotlinx.serialization.Serializable

/**
 * Respuesta de `GET /home/stats`.
 *
 * Los nombres llevan el sufijo `Count` en el backend; se mapean a nombres sin él
 * para que el dominio no arrastre la convención del servidor.
 *
 * `activeRounds` viene como lista de rondas con guardia y cliente incluidos. No
 * se modela porque el inicio no la usa todavía; `ignoreUnknownKeys` la descarta.
 */
@Serializable
private data class DashboardStatsDto(
    val activeRoundsCount: Int = 0,
    val pendingIncidentsCount: Int = 0,
    val pendingMaintenanceCount: Int = 0,
)

interface DashboardRepository {
    /** Cifras del inicio. Requiere sesión; no hay versión offline. */
    suspend fun stats(): ApiResult<DashboardStats>
}

class DefaultDashboardRepository(
    private val api: ApiClient,
) : DashboardRepository {

    override suspend fun stats(): ApiResult<DashboardStats> =
        api.get<DashboardStatsDto>("/home/stats").map { dto ->
            DashboardStats(
                activeRounds = dto.activeRoundsCount,
                pendingIncidents = dto.pendingIncidentsCount,
                pendingMaintenance = dto.pendingMaintenanceCount,
            )
        }
}
