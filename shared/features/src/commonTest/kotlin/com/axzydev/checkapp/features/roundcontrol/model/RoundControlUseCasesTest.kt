package com.axzydev.checkapp.features.roundcontrol.model

import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.entities.kardex.data.KardexRepository
import com.axzydev.checkapp.entities.kardex.model.KardexEntry
import com.axzydev.checkapp.entities.round.data.RoundRepository
import com.axzydev.checkapp.entities.round.model.Round
import com.axzydev.checkapp.entities.round.model.RoundStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

/**
 * Reglas de la ronda abandonada.
 *
 * Es la lógica que impedía que sincronizar metiera al guardia en una ruta que no
 * empezó hoy, y la que evita lo contrario —cerrar una ronda **viva**— que sería peor.
 */
class RoundControlUseCasesTest {

    private val now = 1_800_000_000_000L
    private val hour = 60L * 60L * 1000L

    @Test
    fun `una ronda recien empezada no esta abandonada`() {
        val round = round(startTime = now - hour)

        assertFalse(round.isAbandoned(now))
    }

    @Test
    fun `una ronda abierta mas de un dia si lo esta`() {
        // 25 h: más que cualquier turno, así que no hay riesgo de cerrar una viva.
        val round = round(startTime = now - 25 * hour)

        assertTrue(round.isAbandoned(now))
    }

    @Test
    fun `el limite exacto todavia cuenta como viva`() {
        val round = round(startTime = now - MAX_ROUND_AGE_MS)

        assertFalse(round.isAbandoned(now))
    }

    @Test
    fun `cierra la ronda abandonada en su ultima marcacion`() = runTest {
        val start = now - 30 * hour
        val lastMark = start + 3 * hour
        val rounds = FakeRounds(round(startTime = start))
        val kardex = FakeKardex(
            listOf(
                mark(at = start + hour),
                mark(at = lastMark),
                // Anterior a la ronda: no cuenta como actividad suya.
                mark(at = start - 5 * hour),
            ),
        )

        val closed = CloseAbandonedRoundUseCase(rounds, kardex, FixedTime(now))("guard-1")

        assertEquals(rounds.round?.id, closed?.id)
        // La ronda acabó cuando el guardia dejó de marcar, no hoy: si no, quedaría con
        // una duración de días en el historial.
        assertEquals(lastMark, rounds.finishedWith?.second)
    }

    @Test
    fun `sin marcaciones cierra en su inicio`() = runTest {
        val start = now - 30 * hour
        val rounds = FakeRounds(round(startTime = start))

        CloseAbandonedRoundUseCase(rounds, FakeKardex(emptyList()), FixedTime(now))("guard-1")

        assertEquals(start, rounds.finishedWith?.second)
    }

    @Test
    fun `no toca una ronda viva`() = runTest {
        val rounds = FakeRounds(round(startTime = now - hour))

        val closed = CloseAbandonedRoundUseCase(rounds, FakeKardex(emptyList()), FixedTime(now))("guard-1")

        assertNull(closed)
        assertNull(rounds.finishedWith)
    }

    @Test
    fun `una ronda abandonada no bloquea cerrar sesion`() = runTest {
        val rounds = FakeRounds(round(startTime = now - 30 * hour))

        // Bloquear la salida por una ronda de hace días deja al guardia sin salida y
        // sin explicación.
        assertFalse(HasActiveRoundUseCase(rounds, FixedTime(now))("guard-1"))
    }

    @Test
    fun `una ronda viva si bloquea cerrar sesion`() = runTest {
        val rounds = FakeRounds(round(startTime = now - hour))

        assertTrue(HasActiveRoundUseCase(rounds, FixedTime(now))("guard-1"))
    }

    private fun round(startTime: Long) = Round(
        id = "round-1",
        guardId = "guard-1",
        clientId = null,
        startTime = startTime,
        endTime = null,
        status = RoundStatus.IN_PROGRESS,
        recurringConfigurationId = null,
    )

    private fun mark(at: Long) = KardexEntry(
        id = "mark-$at",
        userId = "guard-1",
        locationId = "location-1",
        timestamp = at,
        notes = null,
        media = emptyList(),
        latitude = null,
        longitude = null,
        assignmentId = null,
        scanType = "RECURRING",
    )
}

private class FixedTime(private val now: Long) : TimeProvider {
    override fun nowEpochMillis(): Long = now
}

private class FakeRounds(val round: Round?) : RoundRepository {
    var finishedWith: Pair<String, Long?>? = null

    override suspend fun finish(roundId: String, endedAt: Long?): Round? {
        finishedWith = roundId to endedAt
        return round
    }

    override suspend fun inProgressByGuard(guardId: String): Round? = round

    override suspend fun start(
        guardId: String,
        clientId: String?,
        recurringConfigurationId: String?,
    ): Round = error("no se usa en estas reglas")

    override suspend fun all(): List<Round> = error("no se usa en estas reglas")

    override suspend fun findById(id: String): Round? = error("no se usa en estas reglas")
}

private class FakeKardex(private val entries: List<KardexEntry>) : KardexRepository {
    override suspend fun byUser(userId: String): List<KardexEntry> = entries

    override suspend fun register(
        userId: String,
        locationId: String,
        notes: String?,
        media: List<String>,
        latitude: Double?,
        longitude: Double?,
        assignmentId: String?,
    ): KardexEntry = error("no se usa en estas reglas")

    override suspend fun updateContent(id: String, notes: String?, media: List<String>): KardexEntry? =
        error("no se usa en estas reglas")

    override suspend fun between(startMillis: Long, endMillis: Long): List<KardexEntry> =
        error("no se usa en estas reglas")

    override suspend fun findById(id: String): KardexEntry? = error("no se usa en estas reglas")
}
