package com.axzydev.checkapp.features.roundcontrol.model

import com.axzydev.checkapp.entities.round.data.RoundRepository
import com.axzydev.checkapp.entities.round.model.Round

/** Inicia una ronda local (offline-first). */
class StartRoundUseCase(private val rounds: RoundRepository) {
    suspend operator fun invoke(
        guardId: String,
        clientId: String?,
        recurringConfigurationId: String?,
    ): Round = rounds.start(guardId, clientId, recurringConfigurationId)
}

/** Finaliza una ronda local. */
class EndRoundUseCase(private val rounds: RoundRepository) {
    suspend operator fun invoke(roundId: String): Round? = rounds.finish(roundId)
}
