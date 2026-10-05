package com.axzydev.checkapp.core.datastore

import kotlinx.serialization.Serializable

/** Item de checklist de un catálogo sincronizado. */
@Serializable
data class SyncChecklistItem(val key: String, val label: String, val group: String)

/** Catálogo de revisión de uniforme. */
@Serializable
data class UniformCatalog(val items: List<SyncChecklistItem>, val minCompliantScore: Int)

/**
 * Catálogos descargados en la última sincronización. Se persisten en
 * `SettingsStore` (clave `sync_catalogs`) y los consumen las entidades.
 */
@Serializable
data class SyncCatalogs(
    val shiftHandover: List<SyncChecklistItem> = emptyList(),
    val uniform: UniformCatalog = UniformCatalog(emptyList(), 80),
)
