package com.axzydev.checkapp.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.axzySettings: DataStore<Preferences> by preferencesDataStore(name = "axzy_settings")

/**
 * Implementación Android basada en **DataStore Preferences**.
 * El `Context` lo inyecta Koin desde `androidApp`.
 */
class AndroidSettingsStore(private val context: Context) : SettingsStore {

    private val dataStore get() = context.axzySettings

    override suspend fun getString(key: String): String? =
        dataStore.data.first()[stringPreferencesKey(key)]

    override suspend fun putString(key: String, value: String?) {
        dataStore.edit { prefs ->
            val prefKey = stringPreferencesKey(key)
            if (value == null) prefs.remove(prefKey) else prefs[prefKey] = value
        }
    }

    override suspend fun getLong(key: String): Long? =
        dataStore.data.first()[longPreferencesKey(key)]

    override suspend fun putLong(key: String, value: Long?) {
        dataStore.edit { prefs ->
            val prefKey = longPreferencesKey(key)
            if (value == null) prefs.remove(prefKey) else prefs[prefKey] = value
        }
    }

    override suspend fun getBoolean(key: String): Boolean? =
        dataStore.data.first()[booleanPreferencesKey(key)]

    override suspend fun putBoolean(key: String, value: Boolean?) {
        dataStore.edit { prefs ->
            val prefKey = booleanPreferencesKey(key)
            if (value == null) prefs.remove(prefKey) else prefs[prefKey] = value
        }
    }

    override suspend fun remove(key: String) {
        dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(key))
            prefs.remove(longPreferencesKey(key))
            prefs.remove(booleanPreferencesKey(key))
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
    }
}
