package com.axzydev.checkapp.processes

/**
 * Capa FSD `processes`.
 *
 * Orquesta flujos multi-página y es unit-testable sin UI:
 * - `round-execution`: inicio → escaneo → check → cierre de ronda.
 * - `sync`: pull → cola de medios → push → catálogos.
 * - `guard-onboarding`: primer arranque del guardia.
 */
