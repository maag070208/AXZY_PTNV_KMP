package com.axzydev.checkapp.entities.kardex.model

/** Marcación de un punto de control durante una ronda. */
data class KardexEntry(
    val id: String,
    val userId: String,
    val locationId: String,
    val timestamp: Long,
    val notes: String?,
    val media: List<String>,
    val latitude: Double?,
    val longitude: Double?,
    val assignmentId: String?,
    val scanType: String,
)
