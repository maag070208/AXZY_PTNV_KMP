package com.axzydev.checkapp.entities.recurringroute.model

data class RecurringTask(
    val id: String,
    val description: String,
    val reqPhoto: Boolean,
)

data class RecurringPoint(
    val id: String,
    val locationId: String,
    val locationName: String?,
    val order: Int,
    val tasks: List<RecurringTask>,
)

data class RecurringRoute(
    val id: String,
    val title: String,
    val clientId: String?,
    val active: Boolean,
    val points: List<RecurringPoint>,
    val guardIds: List<String> = emptyList(),
)

// --- Borradores para alta/edición (REST, el servidor es la fuente de verdad) ---

data class RecurringTaskDraft(
    val description: String,
    val reqPhoto: Boolean = false,
)

data class RecurringPointDraft(
    val locationId: String,
    val tasks: List<RecurringTaskDraft> = emptyList(),
)

data class RecurringRouteDraft(
    val title: String,
    val clientId: String?,
    val points: List<RecurringPointDraft>,
    val guardIds: List<String> = emptyList(),
    val active: Boolean = true,
)
