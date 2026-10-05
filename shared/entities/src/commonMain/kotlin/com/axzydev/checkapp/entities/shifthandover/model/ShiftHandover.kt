package com.axzydev.checkapp.entities.shifthandover.model

import com.axzydev.checkapp.entities.uniformcheck.model.ChecklistAnswer
import kotlinx.serialization.Serializable

@Serializable
data class HandoverElement(
    val guardId: String,
    val entryTime: String,
    val observations: String? = null,
)

data class ShiftHandover(
    val id: String,
    val clientId: String,
    val scheduleId: String,
    val shiftDate: String,
    val credentialsCount: Int?,
    val tarjetonesCount: Int?,
    val novedades: String?,
    val checklist: List<ChecklistAnswer>,
    val reportedToAdmin: Boolean,
    val createdAt: Long,
)

data class ShiftHandoverDraft(
    val clientId: String,
    val scheduleId: String,
    val shiftDate: String,
    val credentialsCount: Int?,
    val tarjetonesCount: Int?,
    val novedades: String?,
    val checklist: List<ChecklistAnswer>,
    val elements: List<HandoverElement>,
    val reportedToAdmin: Boolean,
    val createdById: String,
)
