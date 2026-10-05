package com.axzydev.checkapp.entities.synccatalog.data

import com.axzydev.checkapp.core.datastore.SettingsKeys
import com.axzydev.checkapp.core.datastore.SettingsStore
import com.axzydev.checkapp.core.datastore.SyncCatalogs
import kotlinx.serialization.json.Json

/** Catálogos descargados en la última sincronización (checklist de entrega de turno y uniforme). */
interface SyncCatalogRepository {
    suspend fun catalogs(): SyncCatalogs?
}

class DefaultSyncCatalogRepository(private val settings: SettingsStore) : SyncCatalogRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun catalogs(): SyncCatalogs? {
        val raw = settings.getString(SettingsKeys.SYNC_CATALOGS) ?: return null
        return runCatching { json.decodeFromString(SyncCatalogs.serializer(), raw) }.getOrNull()
    }
}
