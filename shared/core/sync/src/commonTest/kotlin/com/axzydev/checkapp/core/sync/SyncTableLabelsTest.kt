package com.axzydev.checkapp.core.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Cómo se cuenta un rechazo.
 *
 * Importa porque es lo único que ve el guardia cuando el servidor le rechaza un
 * registro: si dijera `shiftHandover` en vez de "Entregas de turno", el mensaje no
 * le dice qué corregir. El servidor manda **modelos de la API** en singular, así
 * que las claves que se prueban aquí son esas, no los nombres de tabla locales.
 */
class SyncTableLabelsTest {

    @Test
    fun every_pushable_table_has_a_name_the_user_recognises() {
        // Si mañana se añade una tabla a DEVICE_WRITABLE_TABLES, esto falla y
        // obliga a nombrarla en vez de dejar que el nombre técnico llegue a la
        // pantalla.
        val missing = DEVICE_WRITABLE_TABLES.mapNotNull { local ->
            val api = SyncMappings.apiModelFor(local) ?: return@mapNotNull local
            val label = syncTableLabel(api)
            if (label == api) "$local → $api" else null
        }

        assertEquals(emptyList(), missing, "Tablas sin nombre visible: $missing")
    }

    @Test
    fun unknown_model_falls_back_to_its_technical_name() {
        assertEquals("weirdTable", syncTableLabel("weirdTable"))
    }

    @Test
    fun rejection_table_is_translated_by_api_model_name_not_by_local_table() {
        // El servidor manda el singular: `incident`, no `incidents`.
        assertEquals("Reportes de incidencias", syncTableLabel("incident"))
        assertNotEquals("incident", syncTableLabel("incident"))
        // Y un nombre local no está en el mapa: así se detectó el error.
        assertEquals("incidents", syncTableLabel("incidents"))
    }

    @Test
    fun actions_are_shown_in_past_tense() {
        assertEquals("Nuevo", syncActionLabel("create"))
        assertEquals("Editado", syncActionLabel("update"))
        assertEquals("Eliminado", syncActionLabel("delete"))
    }

    @Test
    fun unknown_action_is_not_hidden() {
        assertEquals("merge", syncActionLabel("merge"))
    }

    @Test
    fun short_id_is_truncated_but_never_invented() {
        assertEquals("0192ab7c", syncShortId("0192ab7c-4f2e-7c31-9c1a-2b3d4e5f6a7b"))
        assertEquals("abc", syncShortId("abc"))
    }
}
