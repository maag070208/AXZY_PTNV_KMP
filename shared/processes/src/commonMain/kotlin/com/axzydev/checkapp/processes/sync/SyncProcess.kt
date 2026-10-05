package com.axzydev.checkapp.processes.sync

import com.axzydev.checkapp.core.connectivity.ConnectivityObserver
import com.axzydev.checkapp.core.sync.SyncEngine
import com.axzydev.checkapp.core.sync.SyncStep
import com.axzydev.checkapp.core.sync.syncInBackground

/**
 * Flujo de sincronización. Orquesta el motor de sync y su disparo en segundo
 * plano al recuperar la conexión. Es unit-testable sin UI.
 */
class SyncProcess(
    private val engine: SyncEngine,
    private val connectivity: ConnectivityObserver,
) {
    suspend fun run(onStep: (SyncStep) -> Unit = {}): Result<Unit> =
        runCatching { engine.sync(onStep) }

    /** Sin red devuelve `false`; nunca lanza. */
    suspend fun runInBackgroundIfConnected(): Boolean =
        engine.syncInBackground(connectivity)
}
