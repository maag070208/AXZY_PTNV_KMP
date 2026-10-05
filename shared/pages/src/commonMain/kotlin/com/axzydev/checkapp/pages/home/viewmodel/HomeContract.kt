package com.axzydev.checkapp.pages.home.viewmodel

import com.axzydev.checkapp.entities.dashboard.model.DashboardStats

/**
 * Estado del inicio.
 *
 * `stats` arranca en ceros y `loadingStats` distingue "todavía no cargó" de "cargó
 * y de verdad hay cero". Sin esa distinción, un fallo de red y un día tranquilo se
 * verían igual, que es justo lo que hace desconfiar de un panel.
 */
data class HomeUiState(
    val stats: DashboardStats = DashboardStats.Empty,
    val loadingStats: Boolean = true,
    val statsError: String? = null,
)

sealed interface HomeAction {
    data object RefreshStats : HomeAction
}
