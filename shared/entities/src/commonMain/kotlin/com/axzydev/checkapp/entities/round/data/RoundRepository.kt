package com.axzydev.checkapp.entities.round.data

import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.uuid.randomUuid
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Rounds
import com.axzydev.checkapp.entities.round.model.Round
import com.axzydev.checkapp.entities.round.model.RoundStatus

interface RoundRepository {
    /** Crea una ronda local (`_status = created`); el sync la sube después. */
    suspend fun start(guardId: String, clientId: String?, recurringConfigurationId: String?): Round

    /** Finaliza la ronda localmente (`_status = updated`). */
    /**
     * Cierra la ronda.
     *
     * @param endedAt cuándo terminó **de verdad**. Sin él se usa "ahora", que es lo
     *   correcto cuando el guardia la finaliza; al cerrar una ronda abandonada,
     *   "ahora" sería el día en que la app se dio cuenta y quedaría con una duración
     *   de días.
     */
    suspend fun finish(roundId: String, endedAt: Long? = null): Round?

    suspend fun inProgressByGuard(guardId: String): Round?
    suspend fun all(): List<Round>
    suspend fun findById(id: String): Round?
}

class DefaultRoundRepository(
    private val database: AxzyCheckDatabase,
    private val timeProvider: TimeProvider,
) : RoundRepository {

    override suspend fun start(
        guardId: String,
        clientId: String?,
        recurringConfigurationId: String?,
    ): Round {
        val now = timeProvider.nowEpochMillis()
        val id = randomUuid()
        database.roundQueries.insertRound(
            id = id,
            guard_id = guardId,
            client_id = clientId,
            start_time = now,
            end_time = null,
            status = RoundStatus.IN_PROGRESS.name,
            recurring_configuration_id = recurringConfigurationId,
            created_at = now,
            updated_at = now,
            _status = "created",
        )
        return Round(
            id = id,
            guardId = guardId,
            clientId = clientId,
            startTime = now,
            endTime = null,
            status = RoundStatus.IN_PROGRESS,
            recurringConfigurationId = recurringConfigurationId,
        )
    }

    override suspend fun finish(roundId: String, endedAt: Long?): Round? {
        val now = timeProvider.nowEpochMillis()
        database.roundQueries.finishRound(end_time = endedAt ?: now, updated_at = now, id = roundId)
        return findById(roundId)
    }

    override suspend fun inProgressByGuard(guardId: String): Round? =
        database.roundQueries.selectInProgressByGuard(guardId).executeAsOneOrNull()?.toModel()

    override suspend fun all(): List<Round> =
        database.roundQueries.selectAllRounds().executeAsList().map { it.toModel() }

    override suspend fun findById(id: String): Round? =
        database.roundQueries.selectRoundById(id).executeAsOneOrNull()?.toModel()
}

internal fun Rounds.toModel(): Round = Round(
    id = id,
    guardId = guard_id,
    clientId = client_id,
    startTime = start_time,
    endTime = end_time,
    status = RoundStatus.from(status),
    recurringConfigurationId = recurring_configuration_id,
)
