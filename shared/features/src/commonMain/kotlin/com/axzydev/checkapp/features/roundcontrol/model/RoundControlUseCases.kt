package com.axzydev.checkapp.features.roundcontrol.model

import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.entities.kardex.data.KardexRepository
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

/**
 * ¿Esta ronda quedó abandonada?
 *
 * Una ronda abierta **más de** un día ya no es la ronda de nadie: es la que quedó de
 * un turno anterior. Se usa el día y no el turno porque **cerrar una ronda viva es
 * mucho peor que dejar abierta una muerta**: con 24 h no hay turno que se alcance, y
 * el límite exacto se deja del lado de "viva".
 */
fun Round.isAbandoned(now: Long): Boolean = now - startTime > MAX_ROUND_AGE_MS

/** Un día. Cualquier turno cabe de sobra. */
const val MAX_ROUND_AGE_MS = 24L * 60L * 60L * 1000L

/**
 * Cierra la ronda que quedó abierta de un turno anterior.
 *
 * Mientras siga abierta, la app la trata como "en ruta": el guardia abre la app,
 * sincroniza —lo que hace es **bajarse su propia ronda vieja del servidor**— y
 * aparece dentro de una ruta que no empezó hoy. Parecía que sincronizar iniciaba la
 * ruta. Y no puede empezar otra sin quedarse con dos rondas abiertas a la vez.
 *
 * Termina en su **última marcación**: la ronda acabó cuando el guardia dejó de
 * marcar, no el día en que la app se dio cuenta. Sin marcaciones, en su inicio.
 *
 * @return la ronda cerrada, o `null` si no había nada que cerrar.
 */
class CloseAbandonedRoundUseCase(
    private val rounds: RoundRepository,
    private val kardex: KardexRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(guardId: String): Round? {
        val now = timeProvider.nowEpochMillis()
        val round = rounds.inProgressByGuard(guardId) ?: return null
        if (!round.isAbandoned(now)) return null

        val lastMark = kardex.byUser(guardId)
            .filter { it.timestamp >= round.startTime }
            .maxOfOrNull { it.timestamp }

        return rounds.finish(round.id, endedAt = lastMark ?: round.startTime)
    }
}

/**
 * ¿Tiene este guardia una ronda sin terminar **de verdad**?
 *
 * Sirve para no dejarlo cerrar sesión a media ruta. La ronda vive atada al turno y
 * al dispositivo: si se cierra sesión con una abierta, el puesto queda con una ronda
 * que no es de nadie —el siguiente guardia se la encuentra, y los puntos que falten
 * ya no los registra quien los recorrió—.
 *
 * Una ronda abandonada **no** cuenta: bloquear la salida por una ronda de hace días
 * deja al guardia sin salida y sin explicación.
 */
class HasActiveRoundUseCase(
    private val rounds: RoundRepository,
    private val timeProvider: TimeProvider,
) {
    suspend operator fun invoke(guardId: String): Boolean {
        val round = rounds.inProgressByGuard(guardId) ?: return false
        return !round.isAbandoned(timeProvider.nowEpochMillis())
    }
}
