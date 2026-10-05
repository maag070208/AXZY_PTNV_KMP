package com.axzydev.checkapp.entities.uniformcheck.model

import kotlinx.serialization.Serializable

@Serializable
data class ChecklistAnswer(val key: String, val ok: Boolean)

data class UniformCheck(
    val id: String,
    val guardId: String,
    val shiftDate: String,
    val score: Int,
    val compliant: Boolean,
    val notes: String?,
    val createdAt: Long,
)

data class UniformCheckDraft(
    val guardId: String,
    val clientId: String?,
    val scheduleId: String?,
    val shiftDate: String,
    val evaluatedById: String,
    val items: List<ChecklistAnswer>,
    val notes: String?,
    val minCompliantScore: Int,
)
