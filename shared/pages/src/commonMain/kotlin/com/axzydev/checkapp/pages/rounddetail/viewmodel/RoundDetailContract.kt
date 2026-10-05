package com.axzydev.checkapp.pages.rounddetail.viewmodel

data class TimelineItemUi(
    val label: String,
    val timestamp: Long,
    val detail: String?,
)

data class RoundDetailUiState(
    val loading: Boolean = true,
    val status: String = "",
    val startTime: Long = 0,
    val endTime: Long? = null,
    val timeline: List<TimelineItemUi> = emptyList(),
    val error: String? = null,
)

sealed interface RoundDetailAction {
    data object Refresh : RoundDetailAction
}
