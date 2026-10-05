package com.axzydev.checkapp.core.datastore

import platform.Foundation.NSUserDefaults

/**
 * Implementación iOS basada en **NSUserDefaults**. Es el equivalente nativo de
 * DataStore Preferences para estado clave-valor pequeño.
 */
class IosSettingsStore(
    private val defaults: NSUserDefaults = NSUserDefaults.standardUserDefaults,
) : SettingsStore {

    override suspend fun getString(key: String): String? = defaults.stringForKey(key)

    override suspend fun putString(key: String, value: String?) {
        if (value == null) defaults.removeObjectForKey(key)
        else defaults.setObject(value, key)
    }

    override suspend fun getLong(key: String): Long? =
        if (defaults.objectForKey(key) == null) null else defaults.integerForKey(key)

    override suspend fun putLong(key: String, value: Long?) {
        if (value == null) defaults.removeObjectForKey(key)
        else defaults.setInteger(value, key)
    }

    override suspend fun getBoolean(key: String): Boolean? =
        if (defaults.objectForKey(key) == null) null else defaults.boolForKey(key)

    override suspend fun putBoolean(key: String, value: Boolean?) {
        if (value == null) defaults.removeObjectForKey(key)
        else defaults.setBool(value, key)
    }

    override suspend fun remove(key: String) {
        defaults.removeObjectForKey(key)
    }

    override suspend fun clear() {
        val dict = defaults.dictionaryRepresentation()
        dict.keys.forEach { key -> defaults.removeObjectForKey(key as String) }
    }
}
