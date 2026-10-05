package com.axzydev.checkapp.features.scanqr.model

import com.axzydev.checkapp.entities.location.data.LocationRepository
import com.axzydev.checkapp.entities.location.model.Location
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive

data class ScanResult(val code: String, val location: Location?)

/**
 * Resuelve un código QR contra el catálogo local de puntos.
 * Acepta JSON `{ name, id }` (formato de los QR impresos) o el nombre directo.
 */
class MatchLocationUseCase(private val locations: LocationRepository) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    suspend operator fun invoke(code: String, clientId: String?): ScanResult {
        val trimmed = code.trim()
        val candidates = locations.activeLocations(clientId)

        val payload = runCatching { json.parseToJsonElement(trimmed) as? JsonObject }.getOrNull()
        val name = payload?.get("name")?.jsonPrimitive?.contentOrNull
        val id = payload?.get("id")?.jsonPrimitive?.contentOrNull

        val match = candidates.firstOrNull { location ->
            (id != null && location.id == id) ||
                (name != null && location.name.equals(name, ignoreCase = true)) ||
                location.name.equals(trimmed, ignoreCase = true)
        }
        return ScanResult(code = trimmed, location = match)
    }
}
