package com.axzydev.checkapp.features.syncdatabase.model

import com.axzydev.checkapp.core.sync.SyncEngine
import com.axzydev.checkapp.core.sync.SyncStep

/**
 * Interacción "sincronizar base local": ejecuta pull + cola de medios + push.
 */
class SyncDatabaseUseCase(private val engine: SyncEngine) {

    suspend fun run(onStep: (SyncStep) -> Unit = {}): Result<Unit> =
        runCatching { engine.sync(onStep) }

    suspend fun hasUnsyncedLocalChanges(): Boolean = engine.hasUnsyncedLocalChanges()

    suspend fun hasPendingServerChanges(): Boolean = engine.hasPendingServerChanges()
}
