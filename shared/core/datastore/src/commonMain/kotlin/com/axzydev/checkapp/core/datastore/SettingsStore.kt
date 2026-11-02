package com.axzydev.checkapp.core.datastore

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
    const val THEME_MODE = "theme_mode"
}

/** Modo de color de la app. */
enum class ThemeMode {
    /** El del teléfono. Es el de fábrica: la app no impone su gusto. */
    SYSTEM,
    LIGHT,
    DARK;

    companion object {
        fun from(value: String?): ThemeMode = entries.firstOrNull { it.name == value } ?: SYSTEM
    }
}

/**
 * Preferencia de tema, en memoria **y** persistida.
 *
 * Expone un `StateFlow` porque el tema tiene que cambiar en el momento: leer el
 * almacén es `suspend`, y una lectura única al arrancar dejaría el ajuste sin efecto
 * hasta reiniciar la app.
 */
interface ThemePreference {
    val mode: StateFlow<ThemeMode>

    /** Lee lo guardado. Se llama una vez al arrancar. */
    suspend fun load()

    suspend fun set(mode: ThemeMode)
}

class DefaultThemePreference(private val store: SettingsStore) : ThemePreference {

    private val current = MutableStateFlow(ThemeMode.SYSTEM)
    override val mode: StateFlow<ThemeMode> = current.asStateFlow()

    override suspend fun load() {
        current.value = ThemeMode.from(store.getString(SettingsKeys.THEME_MODE))
    }

    override suspend fun set(mode: ThemeMode) {
        current.value = mode
        store.putString(SettingsKeys.THEME_MODE, mode.name)
    }
}
