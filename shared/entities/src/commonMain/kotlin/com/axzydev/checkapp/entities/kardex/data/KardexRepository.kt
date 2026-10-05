package com.axzydev.checkapp.entities.kardex.data

import com.axzydev.checkapp.core.common.time.TimeProvider
import com.axzydev.checkapp.core.common.uuid.randomUuid
import com.axzydev.checkapp.core.database.AxzyCheckDatabase
import com.axzydev.checkapp.core.database.Kardex
import com.axzydev.checkapp.entities.kardex.model.KardexEntry
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

interface KardexRepository {
    /** Registra una marcación local (`_status = created`). */
    suspend fun register(
        userId: String,
        locationId: String,
        notes: String?,
        media: List<String>,
        latitude: Double?,
        longitude: Double?,
        assignmentId: String?,
    ): KardexEntry

    /** Actualiza notas/evidencias de una marcación (`_status = updated`). */
    suspend fun updateContent(id: String, notes: String?, media: List<String>): KardexEntry?

    suspend fun byUser(userId: String): List<KardexEntry>
    suspend fun between(startMillis: Long, endMillis: Long): List<KardexEntry>
    suspend fun findById(id: String): KardexEntry?
}

class DefaultKardexRepository(
    private val database: AxzyCheckDatabase,
    private val timeProvider: TimeProvider,
) : KardexRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun register(
        userId: String,
        locationId: String,
        notes: String?,
        media: List<String>,
        latitude: Double?,
        longitude: Double?,
        assignmentId: String?,
    ): KardexEntry {
        val now = timeProvider.nowEpochMillis()
        val id = randomUuid()
        database.kardexQueries.insertKardex(
            id = id,
            user_id = userId,
            location_id = locationId,
            timestamp = now,
            notes = notes,
            media = json.encodeToString(media),
            latitude = latitude,
            longitude = longitude,
            assignment_id = assignmentId,
            scan_type = "RECURRING",
            created_at = now,
            updated_at = now,
            _status = "created",
        )
        return KardexEntry(id, userId, locationId, now, notes, media, latitude, longitude, assignmentId, "RECURRING")
    }

    override suspend fun updateContent(id: String, notes: String?, media: List<String>): KardexEntry? {
        val now = timeProvider.nowEpochMillis()
        database.kardexQueries.updateKardexContent(
            notes = notes,
            media = json.encodeToString(media),
            updated_at = now,
            id = id,
        )
        return findById(id)
    }

    override suspend fun byUser(userId: String): List<KardexEntry> =
        database.kardexQueries.selectKardexByUser(userId).executeAsList().map { it.toModel() }

    override suspend fun between(startMillis: Long, endMillis: Long): List<KardexEntry> =
        database.kardexQueries.selectKardexBetween(startMillis, endMillis).executeAsList().map { it.toModel() }

    override suspend fun findById(id: String): KardexEntry? =
        database.kardexQueries.selectKardexById(id).executeAsOneOrNull()?.toModel()

    private fun Kardex.toModel(): KardexEntry = KardexEntry(
        id = id,
        userId = user_id,
        locationId = location_id,
        timestamp = timestamp,
        notes = notes,
        media = parseMedia(media),
        latitude = latitude,
        longitude = longitude,
        assignmentId = assignment_id,
        scanType = scan_type,
    )

    private fun parseMedia(raw: String?): List<String> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
    }
}
