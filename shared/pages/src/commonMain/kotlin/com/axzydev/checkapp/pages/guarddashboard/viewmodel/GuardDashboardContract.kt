package com.axzydev.checkapp.pages.guarddashboard.viewmodel

data class PointUi(
    val locationId: String,
    val name: String,
    val order: Int,
    val taskCount: Int,
    val verified: Boolean,
)

data class RouteUi(
    val id: String,
    val title: String,
    val pointCount: Int,
)

data class RoundHistoryUi(
    val id: String,
    val startTime: Long,
    val endTime: Long?,
    val status: String,
)

data class GuardDashboardUiState(
    val loading: Boolean = true,
    val userName: String = "",
    val routes: List<RouteUi> = emptyList(),
    val activeRoundId: String? = null,
    val activeRouteId: String? = null,
    val points: List<PointUi> = emptyList(),
    val history: List<RoundHistoryUi> = emptyList(),
    val panic: PanicUi? = null,
    val error: String? = null,
) {
    val hasActiveRound: Boolean get() = activeRoundId != null
}

/** Estado del overlay de pánico. */
data class PanicUi(val title: String, val message: String)

sealed interface GuardDashboardAction {
    data object Refresh : GuardDashboardAction
    data class StartRoute(val routeId: String) : GuardDashboardAction
    data object EndRound : GuardDashboardAction
    data object TriggerPanic : GuardDashboardAction
    data object DismissPanic : GuardDashboardAction
}

sealed interface GuardDashboardEffect {
    data class Error(val message: String) : GuardDashboardEffect
    data object RoundFinished : GuardDashboardEffect
}
