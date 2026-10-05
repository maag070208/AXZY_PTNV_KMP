package com.axzydev.checkapp.core.sync

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlPreparedStatement
import com.axzydev.checkapp.core.common.time.epochMillisToIso
import com.axzydev.checkapp.core.common.time.isoToEpochMillis
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/**
 * Implementación del store de sync sobre el `SqlDriver` de SQLDelight usando
 * SQL crudo. El motor de sync es genérico respecto al esquema, por lo que no
 * usa consultas tipadas por tabla.
 */
class SqliteSyncLocalStore(private val driver: SqlDriver) : SyncLocalStore {

    private data class ColumnMeta(
        val local: String,
        val api: String,
        val type: SyncValueType,
        val index: Int,
    )

    private val metaCache = mutableMapOf<String, List<ColumnMeta>>()

    private fun columns(table: String): List<ColumnMeta> = metaCache.getOrPut(table) {
        driver.executeQuery(
            identifier = null,
            sql = "PRAGMA table_info($table)",
            mapper = { cursor ->
                val cols = mutableListOf<ColumnMeta>()
                var index = 0
                while (cursor.next().value) {
                    val name = cursor.getString(1) ?: ""
                    val sqlType = cursor.getString(2) ?: ""
                    cols += ColumnMeta(
                        local = name,
                        api = SyncColumns.apiName(name),
                        type = SyncColumns.typeOf(table, name, sqlType),
                        index = index,
                    )
                    index++
                }
                QueryResult.Value(cols)
            },
            parameters = 0,
            binders = null,
        ).value
    }

    override suspend fun readPending(table: String): List<PendingRecord> {
        val cols = columns(table)
        val statusIndex = cols.firstOrNull { it.local == "_status" }?.index ?: return emptyList()
        val dataCols = cols.filter { it.local != "_status" }

        return driver.executeQuery(
            identifier = null,
            sql = "SELECT * FROM $table WHERE _status != 'synced'",
            mapper = { cursor ->
                val rows = mutableListOf<PendingRecord>()
                while (cursor.next().value) {
                    val obj = buildJsonObject {
                        dataCols.forEach { column -> put(column.api, readValue(cursor, column)) }
                    }
                    val id = (obj["id"] as? JsonPrimitive)?.contentOrNull.orEmpty()
                    val status = cursor.getString(statusIndex) ?: "created"
                    rows += PendingRecord(id = id, status = status, data = obj)
                }
                QueryResult.Value(rows)
            },
            parameters = 0,
            binders = null,
        ).value
    }

    override suspend fun applyChanges(apiModel: String, changeSet: SyncChangeSetDto) {
        val table = SyncMappings.localFor(apiModel) ?: return
        val dataCols = columns(table).filter { it.local != "_status" }
        val columnList = (dataCols.map { it.local } + "_status").joinToString(", ")
        val placeholders = (1..dataCols.size + 1).joinToString(", ") { "?" }
        val insertSql = "INSERT OR REPLACE INTO $table ($columnList) VALUES ($placeholders)"

        (changeSet.created + changeSet.updated).forEach { record ->
            val id = (record["id"] as? JsonPrimitive)?.contentOrNull
            // Regla de merge: un registro local sucio gana sobre el remoto.
            if (id != null && localStatus(table, id) !in setOf(null, "synced")) return@forEach

            driver.execute(
                identifier = null,
                sql = insertSql,
                parameters = dataCols.size + 1,
                binders = {
                    dataCols.forEachIndexed { position, column ->
                        bind(this, position + 1, record[column.api], column.type)
                    }
                    bindString(dataCols.size + 1, "synced")
                },
            )
        }

        changeSet.deleted.forEach { id -> deleteNow(table, id) }
    }

    override suspend fun updateMediaJson(table: String, id: String, mediaJson: String) {
        driver.execute(
            identifier = null,
            sql = "UPDATE $table SET media = ? WHERE id = ?",
            parameters = 2,
            binders = {
                bindString(1, mediaJson)
                bindString(2, id)
            },
        )
    }

    override suspend fun markSynced(table: String, ids: List<String>) {
        ids.forEach { id ->
            driver.execute(
                identifier = null,
                sql = "UPDATE $table SET _status = 'synced' WHERE id = ?",
                parameters = 1,
                binders = { bindString(1, id) },
            )
        }
    }

    override suspend fun hardDelete(table: String, id: String) = deleteNow(table, id)

    override suspend fun countUnsynced(): Int = DEVICE_WRITABLE_TABLES.sumOf { table ->
        driver.executeQuery(
            identifier = null,
            sql = "SELECT count(*) FROM $table WHERE _status != 'synced'",
            mapper = { cursor ->
                QueryResult.Value(if (cursor.next().value) cursor.getLong(0)?.toInt() ?: 0 else 0)
            },
            parameters = 0,
            binders = null,
        ).value
    }

    override suspend fun emptyTables(tables: List<String>): List<String> = tables.filter { table ->
        driver.executeQuery(
            identifier = null,
            sql = "SELECT count(*) FROM $table",
            mapper = { cursor ->
                QueryResult.Value(if (cursor.next().value) (cursor.getLong(0) ?: 0L) == 0L else true)
            },
            parameters = 0,
            binders = null,
        ).value
    }

    override suspend fun clearAll() {
        AxzyCheckDatabase.allTableNames().forEach { table ->
            driver.execute(
                identifier = null,
                sql = "DELETE FROM $table",
                parameters = 0,
                binders = null,
            )
        }
    }

    private fun localStatus(table: String, id: String): String? =
        driver.executeQuery(
            identifier = null,
            sql = "SELECT _status FROM $table WHERE id = ?",
            mapper = { cursor -> QueryResult.Value(if (cursor.next().value) cursor.getString(0) else null) },
            parameters = 1,
            binders = { bindString(1, id) },
        ).value

    private fun deleteNow(table: String, id: String) {
        driver.execute(
            identifier = null,
            sql = "DELETE FROM $table WHERE id = ?",
            parameters = 1,
            binders = { bindString(1, id) },
        )
    }

    private fun readValue(cursor: SqlCursor, column: ColumnMeta): JsonElement = when (column.type) {
        SyncValueType.TEXT -> cursor.getString(column.index)?.let { JsonPrimitive(it) } ?: JsonNull
        SyncValueType.INT -> cursor.getLong(column.index)?.let { JsonPrimitive(it) } ?: JsonNull
        SyncValueType.REAL -> cursor.getDouble(column.index)?.let { JsonPrimitive(it) } ?: JsonNull
        SyncValueType.BOOL -> cursor.getLong(column.index)?.let { JsonPrimitive(it != 0L) } ?: JsonNull
        SyncValueType.TIMESTAMP -> cursor.getLong(column.index)?.let { JsonPrimitive(it.epochMillisToIso()) } ?: JsonNull
        SyncValueType.JSON -> cursor.getString(column.index)
            ?.let { raw -> runCatching { Json.parseToJsonElement(raw) }.getOrNull() }
            ?: JsonNull
    }

    private fun bind(
        statement: SqlPreparedStatement,
        index: Int,
        element: JsonElement?,
        type: SyncValueType,
    ) {
        val primitive = element as? JsonPrimitive
        when (type) {
            SyncValueType.TEXT -> statement.bindString(index, primitive?.contentOrNull)
            SyncValueType.INT -> statement.bindLong(index, primitive?.longOrNull)
            SyncValueType.REAL -> statement.bindDouble(index, primitive?.doubleOrNull)
            SyncValueType.BOOL -> statement.bindLong(
                index,
                primitive?.booleanOrNull?.let { if (it) 1L else 0L },
            )
            SyncValueType.TIMESTAMP -> statement.bindLong(
                index,
                primitive?.contentOrNull?.let { raw -> runCatching { raw.isoToEpochMillis() }.getOrNull() },
            )
            SyncValueType.JSON -> statement.bindString(
                index,
                if (element == null || element is JsonNull) null else element.toString(),
            )
        }
    }
}
