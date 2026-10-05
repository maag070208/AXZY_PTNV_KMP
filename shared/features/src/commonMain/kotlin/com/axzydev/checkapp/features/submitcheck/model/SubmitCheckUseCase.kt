package com.axzydev.checkapp.features.submitcheck.model

import com.axzydev.checkapp.entities.kardex.data.KardexRepository
import com.axzydev.checkapp.entities.kardex.model.KardexEntry

/** Registra una marcación de punto (offline-first). */
class SubmitCheckUseCase(private val kardex: KardexRepository) {

    suspend operator fun invoke(
        userId: String,
        locationId: String,
        notes: String?,
        media: List<String>,
        latitude: Double?,
        longitude: Double?,
        assignmentId: String?,
    ): KardexEntry = kardex.register(
        userId = userId,
        locationId = locationId,
        notes = notes,
        media = media,
        latitude = latitude,
        longitude = longitude,
        assignmentId = assignmentId,
    )
}
