package com.axzydev.checkapp.core.sync

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * El store de sync contra SQLite de verdad.
 *
 * Los tests del motor usan un store falso, así que el mapeo de columnas
 * (`PRAGMA table_info` → campo de la API) no estaba cubierto por nada. Esta clase
 * existe porque esa parte sí se rompe: en el emulador, con una novedad pendiente,
 * el sync falló con `Index 1 out of bounds for length 1` antes de llegar a subir.
 */
class SqliteSyncLocalStoreTest {

    private class Fixture {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val store: SqliteSyncLocalStore

        init {
            AxzyCheckDatabase.Schema.create(driver).value
            store = SqliteSyncLocalStore(driver)
        }
    }

    /** La misma fila que crea la app al reportar una novedad sin conexión. */
    private fun Fixture.insertIncident(id: String, title: String, media: String?) {
        driver.execute(
            identifier = null,
            sql = """
                INSERT INTO incidents
                    (id, guard_id, title, category_id, type_id, description, media,
                     latitude, longitude, status, client_id, resolved_at, resolved_by_id,
                     created_at, updated_at, _status)
                VALUES (?, ?, ?, NULL, NULL, NULL, ?, NULL, NULL, 'OPEN', NULL, NULL, NULL,
                        1759800000000, 1759800000000, 'created')
            """.trimIndent(),
            parameters = 3,
            binders = {
                bindString(0, id)
                bindString(1, GUARD_ID)
                bindString(2, title)
                bindString(3, media)
            },
        )
    }

    @Test
    fun empty_table_yields_no_pending_records() = runTest {
        val fixture = Fixture()

        assertEquals(emptyList(), fixture.store.readPending("incidents"))
    }

    @Test
    fun pending_incident_is_mapped_to_api_field_names() = runTest {
        val fixture = Fixture()
        fixture.insertIncident(id = "inc-1", title = "Portón abierto", media = "[]")

        val pending = fixture.store.readPending("incidents")

        assertEquals(1, pending.size)
        val record = pending.single()
        assertEquals("inc-1", record.id)
        assertEquals("created", record.status)

        // snake_case local → camelCase de la API: si esto se rompe, el servidor
        // recibe campos que no reconoce y rechaza el lote entero.
        assertEquals(GUARD_ID, record.data["guardId"]?.jsonPrimitive?.contentOrNull)
        assertEquals("Portón abierto", record.data["title"]?.jsonPrimitive?.contentOrNull)
        // Los timestamps viajan en ISO, no en epoch millis.
        assertNotNull(record.data["createdAt"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun nullable_columns_are_sent_as_null_and_do_not_break_the_mapping() = runTest {
        val fixture = Fixture()
        // Sin media ni categoría: es lo que pasa cuando el guardia escribe la
        // novedad sin adjuntar foto. Antes reventaba aquí.
        fixture.insertIncident(id = "inc-2", title = "", media = null)

        val pending = fixture.store.readPending("incidents")

        assertEquals(1, pending.size)
        val record = pending.single()
        assertEquals("", record.data["title"]?.jsonPrimitive?.contentOrNull)
        assertEquals("null", record.data["media"].toString())
        assertEquals("null", record.data["categoryId"].toString())
    }

    /**
     * El caso que rompió el sync en el emulador.
     *
     * `applyChanges` consulta el estado local de cada registro antes de escribirlo
     * (la regla de merge: lo local sucio gana). Esa consulta tiene **un** parámetro
     * y se bindeaba en el índice 1, así que el primer registro del pull lanzaba
     * `Index 1 out of bounds for length 1` y no se sincronizaba nada.
     */
    @Test
    fun pulled_records_are_applied_as_already_synced() = runTest {
        val fixture = Fixture()

        fixture.store.applyChanges(
            apiModel = "incident",
            changeSet = SyncChangeSetDto(
                created = listOf(
                    buildJsonObject {
                        put("id", "remote-1")
                        put("guardId", GUARD_ID)
                        put("title", "Novedad del servidor")
                        put("status", "OPEN")
                        // El servidor siempre manda los timestamps en ISO; la tabla
                        // los guarda en epoch millis.
                        put("createdAt", ISO_CREATED_AT)
                        put("updatedAt", ISO_CREATED_AT)
                    },
                ),
            ),
        )

        val rows = fixture.readIncidents()
        assertEquals(1, rows.size)
        assertEquals("remote-1", rows.single().id)
        assertEquals("Novedad del servidor", rows.single().title)
        // Lo que baja del servidor ya está sincronizado: si no, el push lo
        // devolvería y entraríamos en un bucle.
        assertEquals("synced", rows.single().status)
        // ISO → epoch millis, no el string tal cual.
        assertEquals(EPOCH_CREATED_AT, rows.single().createdAt)
    }

    @Test
    fun a_locally_dirty_record_is_not_overwritten_by_the_pull() = runTest {
        val fixture = Fixture()
        fixture.insertIncident(id = "inc-3", title = "Lo mío", media = null)

        fixture.store.applyChanges(
            apiModel = "incident",
            changeSet = SyncChangeSetDto(
                created = listOf(
                    buildJsonObject {
                        put("id", "inc-3")
                        put("guardId", GUARD_ID)
                        put("title", "Lo del servidor")
                        put("status", "OPEN")
                        put("createdAt", ISO_CREATED_AT)
                        put("updatedAt", ISO_CREATED_AT)
                    },
                ),
            ),
        )

        val row = fixture.readIncidents().single()
        assertEquals("Lo mío", row.title)
        assertEquals("created", row.status)
    }

    @Test
    fun deleted_ids_are_removed_and_mark_synced_updates_the_status() = runTest {
        val fixture = Fixture()
        fixture.insertIncident(id = "inc-4", title = "Se borra", media = null)

        fixture.store.markSynced("incidents", listOf("inc-4"))
        assertEquals("synced", fixture.readIncidents().single().status)

        fixture.store.applyChanges(
            apiModel = "incident",
            changeSet = SyncChangeSetDto(deleted = listOf("inc-4")),
        )
        assertEquals(emptyList(), fixture.readIncidents())
    }

    private data class IncidentRow(
        val id: String,
        val title: String,
        val status: String,
        val createdAt: Long,
    )

    private fun Fixture.readIncidents(): List<IncidentRow> {
        val rows = mutableListOf<IncidentRow>()
        driver.executeQuery(
            identifier = null,
            sql = "SELECT id, title, _status, created_at FROM incidents",
            mapper = { cursor ->
                while (cursor.next().value) {
                    rows += IncidentRow(
                        id = cursor.getString(0).orEmpty(),
                        title = cursor.getString(1).orEmpty(),
                        status = cursor.getString(2).orEmpty(),
                        createdAt = cursor.getLong(3) ?: 0L,
                    )
                }
                QueryResult.Value(Unit)
            },
            parameters = 0,
            binders = null,
        )
        return rows
    }

    private companion object {
        const val GUARD_ID = "70d8296e-dcfd-4a8c-a3fb-957600484e61"

        /** 2026-10-06T21:05:00Z en epoch millis. */
        const val EPOCH_CREATED_AT = 1791320700000L
        const val ISO_CREATED_AT = "2026-10-06T21:05:00Z"
    }
}
