package com.axzydev.checkapp.processes.roundexecution

import com.axzydev.checkapp.core.connectivity.ConnectivityObserver
import com.axzydev.checkapp.core.sync.SyncEngine
import com.axzydev.checkapp.core.sync.syncInBackground
import com.axzydev.checkapp.entities.kardex.data.KardexRepository
import com.axzydev.checkapp.entities.round.data.RoundRepository
import com.axzydev.checkapp.entities.round.model.Round

/**
 * Flujo de ejecución de ronda: iniciar → marcar puntos → finalizar → sincronizar.
 *
 * Orquesta las entidades y dispara el sync en segundo plano tras cada mutación.
 * Es unit-testable sin UI.
 */
class RoundExecutionProcess(
    private val rounds: RoundRepository,
    private val kardex: KardexRepository,
    private val sync: SyncEngine,
    private val connectivity: ConnectivityObserver,
) {
    suspend fun startRound(
        guardId: String,
        clientId: String?,
        recurringConfigurationId: String?,
    ): Round {
        val round = rounds.start(guardId, clientId, recurringConfigurationId)
        sync.syncInBackground(connectivity)
        return round
    }

    suspend fun registerPoint(
        userId: String,
        locationId: String,
        notes: String?,
        media: List<String>,
        latitude: Double?,
        longitude: Double?,
        assignmentId: String?,
    ) {
        kardex.register(userId, locationId, notes, media, latitude, longitude, assignmentId)
        sync.syncInBackground(connectivity)
    }

    suspend fun finishRound(roundId: String): Round? {
        val round = rounds.finish(roundId)
        sync.syncInBackground(connectivity)
        return round
    }
}
