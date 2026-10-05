package com.axzydev.checkapp.pages.reportissue.viewmodel

import com.axzydev.checkapp.entities.incidentcategory.model.IncidentCategory

/** Tipo de reporte del guardia; determina el catálogo de categorías. */
enum class ReportIssueKind(val categoryType: String, val label: String) {
    INCIDENT(IncidentCategory.TYPE_INCIDENT, "Reportar incidencia"),
    MAINTENANCE(IncidentCategory.TYPE_MAINTENANCE, "Reportar falla"),
}

data class CategoryOption(val id: String, val name: String)

data class ReportMedia(val id: String, val uri: String, val isVideo: Boolean)

data class ReportIssueUiState(
    val kind: ReportIssueKind = ReportIssueKind.INCIDENT,
    val loading: Boolean = true,
    val categories: List<CategoryOption> = emptyList(),
    val selectedCategoryId: String? = null,
    val title: String = "",
    val description: String = "",
    val media: List<ReportMedia> = emptyList(),
    val saving: Boolean = false,
    val error: String? = null,
)

sealed interface ReportIssueAction {
    data class Title(val value: String) : ReportIssueAction
    data class Description(val value: String) : ReportIssueAction
    data class SelectCategory(val id: String) : ReportIssueAction
    data class AddMedia(val uri: String, val isVideo: Boolean) : ReportIssueAction
    data class RemoveMedia(val id: String) : ReportIssueAction
    data class MediaError(val message: String) : ReportIssueAction
    data object Submit : ReportIssueAction
}

sealed interface ReportIssueEffect {
    data object Saved : ReportIssueEffect
    data class Error(val message: String) : ReportIssueEffect
}
