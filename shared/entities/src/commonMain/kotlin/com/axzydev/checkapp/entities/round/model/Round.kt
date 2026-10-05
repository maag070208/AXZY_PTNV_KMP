package com.axzydev.checkapp.entities.round.model

enum class RoundStatus {
    IN_PROGRESS,
    COMPLETED,
    UNKNOWN;

    companion object {
        fun from(value: String): RoundStatus = when (value) {
            "IN_PROGRESS" -> IN_PROGRESS
            "COMPLETED" -> COMPLETED
            else -> UNKNOWN
        }
    }
}

/** Recorrido de vigilancia. */
data class Round(
    val id: String,
    val guardId: String,
    val clientId: String?,
    val startTime: Long,
    val endTime: Long?,
    val status: RoundStatus,
    val recurringConfigurationId: String?,
)
