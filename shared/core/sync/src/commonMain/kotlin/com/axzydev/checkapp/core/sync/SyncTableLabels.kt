package com.axzydev.checkapp.core.sync

/**
 * Cómo se cuenta un rechazo del servidor.
 *
 * El servidor rechaza registros nombrando **modelos de la API** en singular
 * (`incident`, `shiftHandover`), no tablas locales (`incidents`,
 * `shift_handovers`). Esa traducción ya existe en [SyncMappings.displayNames] y
 * es la única lista de nombres: aquí no se duplica.
 *
 * Esto vivía antes en un mapa propio con las claves en plural, y el resultado en
 * el móvil era un rechazo que decía `incident` — el nombre técnico— en vez de
 * "Reportes de incidencias". Se vio en el emulador.
 */
fun syncTableLabel(apiModel: String): String = SyncMappings.displayNameFor(apiModel)

/** Acción del protocolo → lo que pasó, en pasado y en corto. */
fun syncActionLabel(action: String): String = when (action) {
    "create" -> "Nuevo"
    "update" -> "Editado"
    "delete" -> "Eliminado"
    else -> action
}

/**
 * Identificador corto para mostrar.
 *
 * Los ids son UUID; completos no caben en una fila y no le sirven a nadie en
 * campo. El recorte sólo es para pintar: para buscar el registro se usa el
 * completo, que sigue en el modelo.
 */
fun syncShortId(id: String): String = id.take(SHORT_ID_LENGTH)

private const val SHORT_ID_LENGTH = 8
