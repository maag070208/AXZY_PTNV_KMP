package com.axzydev.checkapp.features.panic.model

import com.axzydev.checkapp.entities.panic.data.PanicRepository
import com.axzydev.checkapp.entities.panic.model.PanicAlertDraft
import com.axzydev.checkapp.entities.panic.model.PanicResult

/** Dispara (o encola) una alerta de pánico. */
class TriggerPanicUseCase(private val repository: PanicRepository) {
    suspend operator fun invoke(draft: PanicAlertDraft): PanicResult = repository.trigger(draft)
}

/** Reintenta las alertas encoladas sin conexión. */
class FlushPanicQueueUseCase(private val repository: PanicRepository) {
    suspend operator fun invoke(): Int = repository.flushPending()
}
