package com.axzydev.checkapp.core.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SyncColumnsTest {

    @Test
    fun naming_roundtrip_and_overrides() {
        assertEquals("createdAt", SyncColumns.apiName("created_at"))
        assertEquals("created_at", SyncColumns.localName("createdAt"))
        assertEquals("order", SyncColumns.apiName("sort_order"))
        assertEquals("sort_order", SyncColumns.localName("order"))
        assertEquals("recurringConfigurationId", SyncColumns.apiName("recurring_configuration_id"))
        assertEquals("kardex", SyncColumns.apiName("kardex"))
    }

    @Test
    fun type_detection_is_table_aware() {
        assertEquals(SyncValueType.BOOL, SyncColumns.typeOf("clients", "active", "INTEGER"))
        assertEquals(SyncValueType.BOOL, SyncColumns.typeOf("users", "is_logged_in", "INTEGER"))
        assertEquals(SyncValueType.TIMESTAMP, SyncColumns.typeOf("rounds", "start_time", "INTEGER"))
        assertEquals(SyncValueType.TIMESTAMP, SyncColumns.typeOf("kardex", "timestamp", "INTEGER"))
        assertEquals(SyncValueType.TEXT, SyncColumns.typeOf("schedules", "start_time", "TEXT"))
        assertEquals(SyncValueType.JSON, SyncColumns.typeOf("incidents", "media", "TEXT"))
        assertEquals(SyncValueType.REAL, SyncColumns.typeOf("kardex", "latitude", "REAL"))
        assertEquals(SyncValueType.TEXT, SyncColumns.typeOf("shift_handovers", "shift_date", "TEXT"))
    }

    @Test
    fun mappings_reset_and_writable() {
        assertEquals("incident", SyncMappings.apiModelFor("incidents"))
        assertEquals("incidents", SyncMappings.localFor("incident"))
        assertTrue(SyncMappings.resetLocalTables.contains("clients"))
        assertFalse(SyncMappings.resetLocalTables.contains("incidents"))
        assertTrue(DEVICE_WRITABLE_TABLES.contains("rounds"))
        assertFalse(DEVICE_WRITABLE_TABLES.contains("clients"))
    }
}
