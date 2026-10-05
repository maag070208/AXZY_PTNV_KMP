package com.axzydev.checkapp.entities.assignment.model

data class Assignment(
    val id: String,
    val guardId: String,
    val guardName: String?,
    val locationId: String,
    val locationName: String?,
    val status: String,
    val assignedBy: String,
    val notes: String?,
)

data class AssignmentDraft(
    val guardId: String,
    val locationId: String,
    val assignedBy: String,
    val notes: String? = null,
)

data class AssignmentTaskDraft(
    val description: String,
    val reqPhoto: Boolean = false,
)
