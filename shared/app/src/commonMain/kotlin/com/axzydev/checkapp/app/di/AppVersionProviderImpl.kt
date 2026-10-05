package com.axzydev.checkapp.app.di

import com.axzydev.checkapp.app.config.ApiConstants
import com.axzydev.checkapp.core.datastore.SettingsKeys
import com.axzydev.checkapp.core.datastore.SettingsStore
import com.axzydev.checkapp.core.network.AppVersionProvider

/**
 * Versión de app y flag de bypass para el header `X-App-Version`.
 * Se refresca al arrancar (la lectura de DataStore es suspend).
 */
class AppVersionProviderImpl(private val settings: SettingsStore) : AppVersionProvider {

    private var cachedVersion: String = ApiConstants.DEFAULT_APP_VERSION
    private var cachedBypass: Boolean = false

    suspend fun refresh() {
        cachedVersion = settings.getString(SettingsKeys.CURRENT_APP_VERSION)
            ?: ApiConstants.DEFAULT_APP_VERSION
        cachedBypass = settings.getBoolean(SettingsKeys.BYPASS_VERSION_CHECK) == true
    }

    override fun appVersion(): String = cachedVersion

    override fun bypassVersionCheck(): Boolean = cachedBypass
}
