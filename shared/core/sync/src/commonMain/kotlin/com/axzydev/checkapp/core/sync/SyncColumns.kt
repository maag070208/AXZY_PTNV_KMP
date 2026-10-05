package com.axzydev.checkapp.core.sync

/** Tipo lógico de una columna local para la conversión API ↔ SQLite. */
enum class SyncValueType { TEXT, INT, REAL, BOOL, TIMESTAMP, JSON }

/**
 * Reglas de nomenclatura y tipos (paridad con el sync de RN, Anexo B):
 * - `camelCase` (API) ↔ `snake_case` (local).
 * - Fechas absolutas: ISO 8601 (API) ↔ epoch millis (local).
 * - `HH:mm` (`schedules.start_time/end_time`) se conservan como texto.
 * - Booleanos: `Boolean` (API) ↔ `INTEGER` 0/1 (local).
 * - Columnas JSON (`media`, `checklist`, `elements`, `items`): objeto (API) ↔ texto (local).
 */
object SyncColumns {

    private val BOOLEAN_COLUMNS = setOf(
        "active", "is_logged_in", "is_occupied", "req_photo",
        "completed", "reported_to_admin", "compliant",
    )

    private val JSON_COLUMNS = setOf("media", "checklist", "elements", "items")

    private val NAME_OVERRIDES = mapOf("sort_order" to "order")

    /** Columnas de texto `HH:mm` por tabla (no son fecha). */
    private val CLOCK_TEXT_COLUMNS = setOf("schedules.start_time", "schedules.end_time")

    fun snakeToCamel(value: String): String =
        value.replace(Regex("_([a-z])")) { m -> m.groupValues[1].uppercase() }

    fun camelToSnake(value: String): String =
        value.replace(Regex("[A-Z]")) { m -> "_" + m.value.lowercase() }

    /** Nombre de columna local → nombre de campo API. */
    fun apiName(local: String): String = NAME_OVERRIDES[local] ?: snakeToCamel(local)

    /** Nombre de campo API → nombre de columna local. */
    fun localName(api: String): String =
        NAME_OVERRIDES.entries.firstOrNull { it.value == api }?.key ?: camelToSnake(api)

    fun isTimestamp(table: String, local: String): Boolean {
        if ("$table.$local" in CLOCK_TEXT_COLUMNS) return false
        return local.endsWith("_at") || local == "timestamp" || local.endsWith("_time")
    }

    fun typeOf(table: String, local: String, sqlType: String): SyncValueType {
        val type = sqlType.uppercase()
        return when {
            local in JSON_COLUMNS -> SyncValueType.JSON
            local in BOOLEAN_COLUMNS -> SyncValueType.BOOL
            isTimestamp(table, local) -> SyncValueType.TIMESTAMP
            type.contains("INT") -> SyncValueType.INT
            type.contains("REAL") || type.contains("FLOA") || type.contains("DOUB") -> SyncValueType.REAL
            else -> SyncValueType.TEXT
        }
    }
}
