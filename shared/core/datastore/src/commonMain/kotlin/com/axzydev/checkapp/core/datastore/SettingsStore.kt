package com.axzydev.checkapp.core.datastore

/**
 * Superficie pública del slice `core:datastore`.
 *
 * Almacén clave-valor multiplataforma (DataStore/NSUserDefaults) para estado
 * pequeño y no relacional: sesión, versión de app, catálogos de sync y flags.
 */
interface SettingsStore {
    suspend fun getString(key: String): String?
    suspend fun putString(key: String, value: String?)
    suspend fun getLong(key: String): Long?
    suspend fun putLong(key: String, value: Long?)
    suspend fun getBoolean(key: String): Boolean?
    suspend fun putBoolean(key: String, value: Boolean?)
    suspend fun remove(key: String)
    suspend fun clear()
}

/** Claves conocidas. Centralizadas para evitar strings mágicos. */
object SettingsKeys {
    const val LAST_SYNC_TIMESTAMP = "last_sync_timestamp"
    const val SYNC_CATALOGS = "sync_catalogs"
    const val CURRENT_APP_VERSION = "current_app_version"
    const val BYPASS_VERSION_CHECK = "bypass_version_check"
    const val AUTH_TOKEN = "auth_token"
    const val PENDING_PANIC_ALERTS = "pending_panic_alerts"
}
